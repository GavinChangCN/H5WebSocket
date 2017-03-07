package com.gavin.task;


import com.gavin.component.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.UUID;

/**
 * 模拟一个生产者
 * <p>Title: TaskProducer</p>
 * <p>Description: </p>
 * <p>Company: </p>
 *
 * @author 夏 杰
 * @date 2015年12月11日 下午4:26:48
 * @vesion 1.0
 */
public class TaskProducer implements Runnable {

    @Autowired
    private RedisClient redisClient;

    public void run() {
        Random random = new Random();
        while (true) {
            try {
                Thread.sleep(random.nextInt(600) + 600);
                // 模拟生成一个任务
                UUID taskid = UUID.randomUUID();
                //将任务插入任务队列：task-queue
                redisClient.lpush("task-queue", taskid.toString());
                System.out.println("插入了一个新的任务： " + taskid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}