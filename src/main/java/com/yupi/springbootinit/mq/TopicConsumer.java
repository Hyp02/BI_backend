package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

    // 定义消息队列名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 创建队列1
        String queueName = "qianduan_queue";
        // 创建消息队列
        channel.queueDeclare(queueName, false, false, false, null);
        // 绑定队列 指定路由键

        channel.queueBind(queueName, EXCHANGE_NAME, "#.前端.#");
        // 创建队列2
        String queueName2 = "houduan_queue";
        channel.queueDeclare(queueName2, false, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");

        // 创建队列3
        String queueName3 = "chanpin_queue";
        channel.queueDeclare(queueName3, false, false, false, null);
        channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 任务处理
        DeliverCallback qianduanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [前端a] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback houduanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [后端b：] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback chanpinDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [产品c：] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, qianduanDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, houduanDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName3, true, chanpinDeliverCallback, consumerTag -> {
        });
    }
}