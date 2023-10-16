package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class deadQueueConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 频道绑定交换机
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");
        // 创建死信队列
        String queueName = "laoban_dlx_queue";
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "laoban");
        // 创建死信队列
        String queueName2 = "waibao_dlx_queue";
        channel.queueDeclare(queueName2, false, false, false, null);
        channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        // 处理死信队列中消息
        DeliverCallback laobanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [laoban] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback waibaoDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [waibao] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, laobanDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, waibaoDeliverCallback, consumerTag -> {
        });
    }
}