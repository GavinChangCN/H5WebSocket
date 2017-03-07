package com.gavin;

import com.gavin.task.TaskCustomer;
import com.gavin.task.TaskProducer;
import com.gavin.util.DateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

/**
 * User: Gavin
 * E-mail: GavinChangCN@163.com
 * Desc:
 * Date: 2017-03-07
 * Time: 14:08
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = H5WebSocketApplication.class)
public class JedisTests {

    @Autowired
    JedisPool jedisPool;

    private int mStartTime;

    @Before
    public void beforeJunitTest() {
        mStartTime = DateUtil.getInt();
        System.out.println("========================= 单元测试开始时间：" + DateUtil.getInt() + " ==========================");
    }

    @After
    public void afterJunitTest() {
        int endTime = DateUtil.getInt();
        System.out.println("========================= 单元测试开始时间：" + endTime + " ==========================");
        System.out.println("*********************** 累计用时：" + (endTime - mStartTime) + "s ************************");
    }

    @Test
    public void testJedisPool() {
        String uuid = UUID.randomUUID().toString();
        System.out.println(("jedisPool uuid : " + uuid));
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(uuid, 1000, "huajun.zhang");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJedisTask() throws InterruptedException {
        // 启动一个生产者线程，模拟任务的产生
        new Thread(new TaskProducer()).start();
        Thread.sleep(15000);
        //启动一个线程者线程，模拟任务的处理
        new Thread(new TaskCustomer()).start();
        //主线程休眠
        Thread.sleep(Long.MAX_VALUE);
    }
}
