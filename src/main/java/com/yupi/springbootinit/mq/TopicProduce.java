package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class TopicProduce {

    // 定义消息队列名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            Scanner scan = new Scanner(System.in);
            while (scan.hasNext()) {

                String userInput = scan.nextLine();
                // 消息与路由键用空格隔开
                String[] s = userInput.split(" ");

                if (s.length < 1) {
                    System.out.println("请输入message和routingKey");
                    continue;
                }
                String message = s[0];
                String routingKey = s[1];
                // 发送消息
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "' " + "路由键是: " + routingKey);
            }
        }
        //..
    }
}