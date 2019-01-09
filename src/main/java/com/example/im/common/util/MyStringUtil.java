package com.example.im.common.util;

public class MyStringUtil {
    //模拟大数据
  static   public StringBuilder getLongStr(){
        StringBuilder sb=new StringBuilder();
        //sb.append("开始");
        for (int i=0;i<1024;i++){
            sb.append('a');
        }

        //sb.append("结束");
        return sb;
    }
}
