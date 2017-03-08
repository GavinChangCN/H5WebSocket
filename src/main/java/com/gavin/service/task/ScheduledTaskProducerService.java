package com.gavin.service.task;

import com.gavin.component.RedisClient;
import com.gavin.socket.MyWebSocket;
import com.gavin.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * User: Gavin
 * E-mail: GavinChangCN@163.com
 * Desc:
 * Date: 2017-03-08
 * Time: 14:36
 */
@Service
public class ScheduledTaskProducerService {
    protected static final String TAG = "ScheduledTaskProducerService";

    private static final Logger LOG = LogManager
            .getLogger(ScheduledTaskProducerService.class);

    @Autowired
    private RedisClient redisClient;

    @Bean
    public MyWebSocket webSocket() {
        return new MyWebSocket();
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 3000)
    public void reportCurrentTime() throws Exception {
        System.out.println(" 每隔三秒执行一次：" + dateFormat.format(new Date()));
        pushTask2Jedis(" 每隔五秒执行一次：" + dateFormat.format(new Date()));
    }

    @Scheduled(fixedRate = 5000)
    public void popRedisData() throws Exception {
        System.out.println(" 每隔五秒执行一次：" + dateFormat.format(new Date()));
        String popMessage = popTask4Jedis();
        if (StringUtil.isEmpty(popMessage)) {
            return;
        }
        System.out.println(popMessage);
        webSocket().sendInfo(popMessage);
    }

    @Scheduled(cron = "0 08 15 ? * *")
    public void fixTimeExecution() throws Exception {
        System.out.println(String.format(" 在指定时间 " + dateFormat.format(new Date()) + " 执行"));
        String popMessage = popTask4Jedis();
        if (StringUtil.isEmpty(popMessage)) {
            return;
        }
        System.out.println(popMessage);
    }

    private void pushTask2Jedis(String message) throws Exception {
        // 模拟生成一个任务
        UUID taskid = UUID.randomUUID();
        //将任务插入任务队列：task-queue
        redisClient.lpush("task-queue", "message -> " + message + "；" + taskid.toString());
        LOG.debug("插入了一个新的任务： " + "message -> " + message + "；" + taskid);
    }

    private String popTask4Jedis() throws Exception {
        Random random = new Random();
        //从任务队列"task-queue"中获取一个任务，并将该任务放入暂存队列"tmp-queue"
        String taskid = redisClient.rpoplpush("task-queue", "tmp-queue");
        // 处理任务----纯属业务逻辑，模拟一下：睡觉
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //模拟成功和失败的偶然现象
        if (random.nextInt(13) % 7 == 0) {// 模拟失败的情况,概率为2/13
            //将本次处理失败的任务从暂存队列"tmp-queue"中，弹回任务队列"task-queue"
            LOG.error(taskid + "处理失败，被弹回任务队列");
            redisClient.rpoplpush("tmp-queue", "task-queue");
            return null;
        } else {// 模拟成功的情况
            // 将本次任务从暂存队列"tmp-queue"中清除
            LOG.info(taskid + "处理成功，被清除");
            return redisClient.rpop("tmp-queue");
        }
    }
}
