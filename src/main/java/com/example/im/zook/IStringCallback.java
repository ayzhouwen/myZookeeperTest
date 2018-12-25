package com.example.im.zook;

import org.apache.zookeeper.AsyncCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IStringCallback implements AsyncCallback.StringCallback {
    private static final Logger log = LoggerFactory.getLogger(IStringCallback.class);
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        log.info("创建结果:["+rc+", "+path+", "+ctx+", real path name: "+name);
    }
}
