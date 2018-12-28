package com.example.im.controller;

import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Controller
@RequestMapping("/curator")
public class CuratorController {

    private static final Logger log = LoggerFactory.getLogger(CuratorController.class);
    @Autowired
    private AppVule appVule;


    //基础连接
    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult connect(@RequestParam Map map){
        RetryPolicy retryPolicy= new ExponentialBackoffRetry(1000,3);
        CuratorFramework client=CuratorFrameworkFactory.newClient(appVule.zkUrl,5000,3000,retryPolicy);
        client.start();
        return ApiResult.success(1);
    }

    //创建连续节点

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult create(@RequestParam Map map) throws Exception {
        String path="/root3/p1";
        CuratorFramework client=CuratorFrameworkFactory.builder()
                 .connectString(appVule.zkUrl)
                 .sessionTimeoutMs(5000)
                 .retryPolicy(new ExponentialBackoffRetry(1000,3))
                 .build();

        client.start();
        for (int i=0;i<50;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.create().creatingParentsIfNeeded()
                                .withMode(CreateMode.PERSISTENT)
                                .forPath(path,"init呵呵哒".getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        log.info("创建成功");
        return ApiResult.success(1);
    }



    //节点监听,
    @RequestMapping(value = "/monitor", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult monitor(@RequestParam Map map) throws Exception {
        String path="/root3/p2";
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path,"init呵呵哒".getBytes());
        final NodeCache cache= new NodeCache( client,path,false);
        cache.start(true);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                log.info("节点的数据更新,新的数据:"+new String(cache.getCurrentData().getData()));
            }
        });

        client.setData().forPath(path,"嘿嘿哦".getBytes());
        Thread.sleep(2000);
        //删除了节点也会触发 nodeChanged
        client.delete().deletingChildrenIfNeeded().forPath(path);

        Thread.sleep(15000);
        client.close();
        return ApiResult.success(1);
    }



    //子节点监听

    @RequestMapping(value = "/monitor2", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult monitor2(@RequestParam Map map) throws Exception {
        String path="/root3";
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        final PathChildrenCache cache= new PathChildrenCache( client,path,false);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()){
                    case  CHILD_ADDED:
                        log.info("子节点增加,"+event.getData().getPath());
                        break;
                    case CHILD_UPDATED:
                        log.info("子节点更新,"+event.getData().getPath());
                        break;
                    case  CHILD_REMOVED:
                        log.info("子节点删除,"+event.getData().getPath());
                        break;
                }
            }
        });

        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path,"init呵呵哒".getBytes());
        Thread.sleep(2000);


        client.create().withMode(CreateMode.PERSISTENT).forPath(path+"/g1");
        //删除了节点也会触发 nodeChanged
        Thread.sleep(2000);
        client.delete().forPath(path+"/g1");

        Thread.sleep(20000);

        client.close();
        return ApiResult.success(1);
    }

    //选举

    @RequestMapping(value = "/master", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult master(@RequestParam Map map) throws Exception {
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        LeaderSelector selector=new LeaderSelector(client, "/master_path", new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                log.info("成为Master角色");
                Thread.sleep(3000);
                log.info("完成Master操作,释放Master权利");
            }
        });

        selector.autoRequeue(); //如果注释掉这句那么takeLeadership方法不会重复执行
        selector.start();
        //Thread.sleep(Integer.MAX_VALUE);
        return ApiResult.success(1);
    }


    //分布式锁

    @RequestMapping(value = "/lock", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult lock(@RequestParam Map map) throws Exception {
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

         String lock_path="/mylock";
        final InterProcessMutex lock=new InterProcessMutex(client,lock_path);
        final CountDownLatch down=new CountDownLatch(1);

        for (int i=0;i<100;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        down.await();
                        lock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss|SSS");
                    String orderNo=sdf.format(new Date());
                    log.info("生成的订单号是 : "+orderNo);
                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //Thread.sleep(5000);

        down.countDown();

        return ApiResult.success(1);
    }


    //分布式计数器,add函数不保证新增成功,坑
    @RequestMapping(value = "/atomicInt", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult atomicInt(@RequestParam Map map) throws Exception {
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();
        String path="/atomic";
        DistributedAtomicInteger atomicInteger=new DistributedAtomicInteger(client,path,new RetryNTimes(3,1000));
        int n=100;
         CountDownLatch down=new CountDownLatch(n);
        for (int i=0;i<n;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        boolean flag=false;
                        AtomicValue<Integer> rc=null;
                        while (!flag){

                            rc=atomicInteger.add(1);
                            flag=rc.succeeded();
                            log.info("计数新增结果:"+rc.succeeded());
                        }

                        down.countDown();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
        down.await();
        log.info("计数器最终结果:Result: "+atomicInteger.get().preValue());


        return ApiResult.success(1);
    }


    //分布式Barrier,需要主线程手动删除Barrier不够智能

    static   DistributedBarrier barrier;
    @RequestMapping(value = "/barrier", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult barrier(@RequestParam Map map) throws Exception {
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        String path="/barrier";

        for (int i=0;i<100;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CuratorFramework client=CuratorFrameworkFactory.builder()
                            .connectString(appVule.zkUrl)
                            .retryPolicy(new ExponentialBackoffRetry(1000,3))
                            .build();
                    client.start();

                    barrier=new DistributedBarrier(client,path);
                    log.info("barrier设置");
                    try {
                        barrier.setBarrier();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ;
                    try {
                        barrier.waitOnBarrier();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    log.info("启动...");
                }
            }).start();
        }

        Thread.sleep(1000*30);
        barrier.removeBarrier();
        return ApiResult.success(1);
    }



    //分布式Barrier,自动删除Barrier,无需手动设置和全局变量

    @RequestMapping(value = "/auto_barrier", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult auto_barrier(@RequestParam Map map) throws Exception {
        CuratorFramework client=CuratorFrameworkFactory.builder()
                .connectString(appVule.zkUrl)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();

        client.start();

        String path="/barrier2";
        int n=100;
        for (int i=0;i<n;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CuratorFramework client=CuratorFrameworkFactory.builder()
                            .connectString(appVule.zkUrl)
                            .retryPolicy(new ExponentialBackoffRetry(1000,3))
                            .build();
                    client.start();

                    DistributedDoubleBarrier  auto_barrier=new DistributedDoubleBarrier(client,path,n);
                    try {
                        Thread.sleep(Math.round(Math.random()*3000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("进入barrier");
                    try {
                        auto_barrier.enter();
                        log.info("启动...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ;
                    try {
                        Thread.sleep(Math.round(Math.random()*3000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        auto_barrier.leave();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread.sleep(1000*30);
        return ApiResult.success(1);
    }

}
