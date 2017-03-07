package com.gavin.socket;

import com.gavin.component.RedisClient;
import com.gavin.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * User: Gavin
 * E-mail: GavinChangCN@163.com
 * Desc:
 * Date: 2017-03-06
 * Time: 19:19
 */
@ServerEndpoint(value = "/websocket")
@Component
public class MyWebSocket {

    @Autowired
    private RedisClient redisClient;

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            sendMessage("有新连接加入！当前在线人数为" + getOnlineCount());
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        System.out.println("来自客户端的消息:" + message);
        pushTask2Jedis(message);
        String popMessage = popTask4Jedis();
        if (StringUtil.isEmpty(popMessage)) {
            return;
        }
        //群发消息
        for (MyWebSocket item : webSocketSet) {
            try {
                item.sendMessage(popMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pushTask2Jedis(String message) throws Exception {
        // 模拟生成一个任务
        UUID taskid = UUID.randomUUID();
        //将任务插入任务队列：task-queue
        redisClient.lpush("task-queue", "message -> " + message + "；" + taskid.toString());
        System.out.println("插入了一个新的任务： " + "message -> " + message + "；" + taskid);
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
            System.out.println(taskid + "处理失败，被弹回任务队列");
            redisClient.rpoplpush("tmp-queue", "task-queue");
            return null;
        } else {// 模拟成功的情况
            // 将本次任务从暂存队列"tmp-queue"中清除
            System.out.println(taskid + "处理成功，被清除");
            return redisClient.rpop("tmp-queue");
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        for (MyWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
}