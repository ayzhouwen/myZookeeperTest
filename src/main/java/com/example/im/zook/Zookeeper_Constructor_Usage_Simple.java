package com.example.im.zook;

import com.example.im.common.util.Constants.AppVule;
import com.example.im.controller.AppUserController;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

public class Zookeeper_Constructor_Usage_Simple implements Watcher {
    private static final Logger log = LoggerFactory.getLogger(Zookeeper_Constructor_Usage_Simple.class);
    @Autowired
    private AppVule appVule;

    private
    CountDownLatch countDownLatch;

    public  Zookeeper_Constructor_Usage_Simple(CountDownLatch countDownLatch){
        this.countDownLatch=countDownLatch;
    };
    @Override
    public void process(WatchedEvent event) {
        log.info("收到监听事件: "+event);

        if (event.getState()==Event.KeeperState.SyncConnected){
           countDownLatch.countDown();
        }
    }
}
