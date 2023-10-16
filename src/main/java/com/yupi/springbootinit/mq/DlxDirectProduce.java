package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class DlxDirectProduce {

    // 定义队列名称
    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 频道绑定交换机，为direct
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

            // 老板给员工队列发送任务
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
                channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "' " + "路由键是: " + routingKey);
            }
        }

    }
}
