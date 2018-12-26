package com.example.im.controller;

import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path,"init呵呵哒".getBytes());
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

}
