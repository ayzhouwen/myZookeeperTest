package com.example.im.controller;

import cn.hutool.core.thread.ThreadUtil;
import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
import com.example.im.zook.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Controller
@RequestMapping("/zkDebug")
public class ZkDebugController {


    private static final Logger log = LoggerFactory.getLogger(ZkDebugController.class);
    @Autowired
    private AppVule appVule;





        //启动zkserver
        @RequestMapping(value = "/debug", method = RequestMethod.POST)
        @ResponseBody
        public ApiResult debug(@RequestParam Map map){
            try {
                ThreadUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            QuorumPeerMain.main(new String[]{ResourceUtils.getURL("classpath:zk/zoo.cfg").getFile()});
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return ApiResult.success(1);
        }



    }




