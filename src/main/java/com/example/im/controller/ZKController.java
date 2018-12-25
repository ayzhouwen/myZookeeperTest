package com.example.im.controller;

import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
import com.example.im.service.IAppUserService;
import com.example.im.zook.IStringCallback;
import com.example.im.zook.Zookeeper_Constructor_Usage_Simple;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Controller
@RequestMapping("/zookeeper")
public class ZKController {

    private static final Logger log = LoggerFactory.getLogger(ZKController.class);
    @Autowired
    private AppVule appVule;


    //基础连接
    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult connect(@RequestParam Map map){
        CountDownLatch connectedSemaphore =new CountDownLatch(1);
        ZooKeeper zooKeeper=null;
        try {
             zooKeeper =new ZooKeeper(appVule.zkUrl,5000,new Zookeeper_Constructor_Usage_Simple(connectedSemaphore));
            log.info(String.valueOf(zooKeeper.getState()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

            connectedSemaphore.await();
           zooKeeper.close(); //不关闭 tcp连接不会关掉
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ApiResult.success(1);
    }

//复用 sessionId和sessionPasswd来创建一个zookeeper对象实例,复用连接会造成通知事件一直被触发
    @RequestMapping(value = "/connect2", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult connect2(@RequestParam Map map) throws IOException, InterruptedException {
        CountDownLatch connectedSemaphore =new CountDownLatch(1);
        ZooKeeper zooKeeper=null;

            zooKeeper =new ZooKeeper(appVule.zkUrl,5000,new Zookeeper_Constructor_Usage_Simple(connectedSemaphore));
            connectedSemaphore.await();
            long sessionId =zooKeeper.getSessionId();
            byte[] passwd=zooKeeper.getSessionPasswd();
        //zooKeeper.close();
                //错误的id和密码进行连接
           // zooKeeper = new ZooKeeper(appVule.zkUrl,5000,new Zookeeper_Constructor_Usage_Simple(connectedSemaphore),1L,"test".getBytes());
            //正确的id和密码进行连接
            connectedSemaphore =new CountDownLatch(1);
            zooKeeper = new ZooKeeper(appVule.zkUrl,5000,new Zookeeper_Constructor_Usage_Simple(connectedSemaphore),sessionId,passwd);
            connectedSemaphore.await();


            log.info("执行完毕:"+String.valueOf(zooKeeper.getState()));


            Thread.sleep(5000);
        zooKeeper.close();


        return ApiResult.success(1);
    }


    public ZooKeeper  getZk() throws IOException, InterruptedException {
        CountDownLatch connectedSemaphore =new CountDownLatch(1);
        ZooKeeper zooKeeper=null;

        zooKeeper =new ZooKeeper(appVule.zkUrl,5000,new Zookeeper_Constructor_Usage_Simple(connectedSemaphore));
        connectedSemaphore.await();
        return zooKeeper;
    }
    //创建节点
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult create(@RequestParam Map map) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper=getZk();
        String string="";
        for (int i=0;i<20;i++){
            string+=appVule.robotNames+",";
        }


        zooKeeper.create("/zw/mynode",string.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        log.info("增加完成:"+string.getBytes().length);
        return ApiResult.success(1);
    }

    //异步创建接口

    @RequestMapping(value = "/create2", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult create2(@RequestParam Map map) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper=getZk();
        String string="";
        zooKeeper.create("/zw/mynode",string.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,new IStringCallback(),"大哈哈");
        log.info("增加完成:");
        return ApiResult.success(1);
    }





    //删除节点

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult delete(@RequestParam Map map) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper=getZk();
        zooKeeper.delete("/zw/mynode",0);
        log.info("删除成功:");
        return ApiResult.success(1);
    }


    //获取子节点

    @RequestMapping(value = "/getChildren", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult getChildren(@RequestParam Map map) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper=getZk();
        List<String> list= zooKeeper.getChildren("/zw",false);
        log.info("读取成功:"+list);
        return ApiResult.success(1);
    }



}
