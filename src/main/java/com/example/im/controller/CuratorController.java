package com.example.im.controller;

import com.example.im.common.util.ApiResult;
import com.example.im.common.util.Constants.AppVule;
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

        return ApiResult.success(1);
    }

    //创建连续节点

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult create(@RequestParam Map map){

        return ApiResult.success(1);
    }



    //节点监听,
    @RequestMapping(value = "/monitor", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult monitor(@RequestParam Map map) throws InterruptedException {


        return ApiResult.success(1);
    }


}
