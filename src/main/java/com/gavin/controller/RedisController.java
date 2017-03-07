package com.gavin.controller;

import com.gavin.component.RedisClient;
import com.gavin.model.ResponseModal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: Gavin
 * E-mail: GavinChangCN@163.com
 * Desc:
 * Date: 2017-03-07
 * Time: 17:34
 */
@RestController
public class RedisController {
    protected static final String TAG = "RedisController";

    @Autowired
    private RedisClient redisClient;

    @RequestMapping("/redis/set")
    public ResponseModal redisSet(@RequestParam("value")String value) throws Exception {
        redisClient.set("name", value);
        return new ResponseModal(200, true, "success", null);
    }

    @RequestMapping("/redis/get")
    public ResponseModal redisGet() throws Exception {
        String name = redisClient.get("name");
        return new ResponseModal(200, true,"success",name);
    }
}
