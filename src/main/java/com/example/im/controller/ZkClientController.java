package com.example.im.controller;

import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
import com.example.im.zook.*;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Controller
@RequestMapping("/zkClient")
public class ZkClientController {

    private static final Logger log = LoggerFactory.getLogger(ZkClientController.class);
    @Autowired
    private AppVule appVule;


    //基础连接
    @RequestMapping(value = "/connect", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult connect(@RequestParam Map map){
        ZkClient zkClient=new ZkClient(appVule.zkUrl,5000);
        log.info("zkClient连上了");
        return ApiResult.success(1);
    }

    //创建连续节点

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult create(@RequestParam Map map){
        ZkClient zkClient=new ZkClient(appVule.zkUrl,5000);
        zkClient.createPersistent("/root/zw1/mysql",true);
        log.info("节点创建好了");
        return ApiResult.success(1);
    }



    //节点监听,
    @RequestMapping(value = "/monitor", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult monitor(@RequestParam Map map) throws InterruptedException {
        String path="/root2";
        ZkClient zkClient=new ZkClient(appVule.zkUrl,5000);
        zkClient.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                log.info(parentPath+" 子节点改变,当前节点列表:"+currentChilds);
            }
        });

        zkClient.createPersistent(path);
        Thread.sleep(1000);
        log.info(String.valueOf(zkClient.getChildren(path)));
        Thread.sleep(1000);
        zkClient.createPersistent(path+"/c1");
        Thread.sleep(1000);
        zkClient.delete(path+"/c1");
        Thread.sleep(1000);
        zkClient.delete(path);
        zkClient.createPersistent(path+"/c2",true);
        zkClient.writeData(path+"/c2","哈哈哈");
        zkClient.close();

        return ApiResult.success(1);
    }


}
