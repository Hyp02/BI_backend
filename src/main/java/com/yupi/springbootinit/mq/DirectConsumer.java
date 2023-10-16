package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;
import org.apache.commons.io.filefilter.FalseFileFilter;

public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 频道绑定交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 定义队列名称
        String queueName = "xiaohan_queue";
        // 创建队列
        channel.queueDeclare(queueName, false, false, false, null);
        // 绑定队列 指定路由键(绑定到对应的队列)
        channel.queueBind(queueName, EXCHANGE_NAME, "xiaohan");

        // 定义第二个队列名称
        String queueName2 = "xiaopi_queue";
        // 创建第二个队列
        channel.queueDeclare(queueName2, false, false, false, null);
        // 绑定第二个队列到频道，指定路由键
        channel.queueBind(queueName2, EXCHANGE_NAME, "xiaopi");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 任务处理
        DeliverCallback xioahanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaohan] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback xiaopiDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaopi] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, xioahanDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, xiaopiDeliverCallback, consumerTag -> {
        });
    }
}