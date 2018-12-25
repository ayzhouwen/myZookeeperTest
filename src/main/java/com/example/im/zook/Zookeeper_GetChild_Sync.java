package com.example.im.zook;

import com.example.im.controller.ZKController;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;


//watch需要反复注册
public class Zookeeper_GetChild_Sync {
    private static final Logger log = LoggerFactory.getLogger(ZKController.class);
    private  String url;
    public Zookeeper_GetChild_Sync(String url){
        this.url=url;
    }

    private ZooKeeper zk;
    private CountDownLatch connectedSemaphore=new CountDownLatch(1);

    public  void  init() throws IOException, InterruptedException, KeeperException {
        String path="/lj";

        zk=new ZooKeeper(url, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

                log.info("监听:"+String.valueOf(event));
                if (Event.KeeperState.SyncConnected==event.getState()){
                    if (Event.EventType.None==event.getType()&&null==event.getPath()){
                        connectedSemaphore.countDown();
                    }else  if (event.getType()==Event.EventType.NodeChildrenChanged){
                        try {
                            log.info("子节点变动:"+zk.getChildren(event.getPath(),true));
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        connectedSemaphore.await();
        zk.create(path,"".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        Thread.sleep(3000);
        zk.create(path+"/c1","呵呵哒".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        List<String> childrenList=zk.getChildren(path,true);
        log.info(String.valueOf(childrenList));
        Thread.sleep(3000);
        zk.create(path+"/c2","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);

        Thread.sleep(3000);

        zk.create(path+"/c3","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);


        Thread.sleep(3000);

        zk.create(path+"/c4","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);


    }

}
