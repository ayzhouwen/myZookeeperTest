package com.example.im.zook;

import com.example.im.controller.ZKController;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;


//watch需要反复注册
public class Zookeeper_GetData_Sync {
    private static final Logger log = LoggerFactory.getLogger(ZKController.class);
    private  String url;
    public Zookeeper_GetData_Sync(String url){
        this.url=url;
    }

    private ZooKeeper zk;
    private CountDownLatch connectedSemaphore=new CountDownLatch(1);
    private Stat stat=new Stat();

    public  void  init() throws IOException, InterruptedException, KeeperException {
        String path="/lj";

        zk=new ZooKeeper(url, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

                log.info("监听:"+String.valueOf(event));
                if (Event.KeeperState.SyncConnected==event.getState()){
                    if (Event.EventType.None==event.getType()&&null==event.getPath()){
                        connectedSemaphore.countDown();
                    }else  if (event.getType()==Event.EventType.NodeDataChanged){
                        try {
                            log.info("子节点数据变动:"+new String(zk.getData(event.getPath(),true,stat)));
                            log.info("监听stat:Czxid "+stat.getCzxid()+",stat:Mzxid "+stat.getMzxid()+",stat:Version "+stat.getVersion());
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
        Thread.sleep(2000);
        zk.create(path+"/c1","呵呵哒".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        Thread.sleep(2000);
        zk.create(path+"/c2","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);

        Thread.sleep(2000);

        zk.create(path+"/c3","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);


        Thread.sleep(2000);

        zk.create(path+"/c4","咕吉吉".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);

        //开始获取数据
        log.info(new String(zk.getData(path,true,stat)));
        log.info("stat:Czxid"+stat.getCzxid()+",stat:Mzxid"+stat.getMzxid()+",stat:Version"+stat.getVersion());


        zk.setData(path,"123".getBytes(),-1);


    }

}
