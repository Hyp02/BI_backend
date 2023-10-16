package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

import java.util.HashMap;

public class DlxDirectConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 频道绑定交换机
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        // 指定死信队列参数
        HashMap<String, Object> args = new HashMap<>();
        // 绑定死信交换机
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        // 指定路有键
        args.put("x-dead-letter-routing-key","laoban");

        // 创建第一个死信队列
        String queueName = "xiaohan1_queue";
        // 创建队列，绑定指定死信队列参数
        channel.queueDeclare(queueName, false, false, false, args);
        channel.queueBind(queueName, WORK_EXCHANGE_NAME, "xiaohan1");
        // 死信队列参数
        HashMap<String, Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key","waibao");

        // 创建第二个死信队列
        String queueName2 = "xiaopi1_queue";
        channel.queueDeclare(queueName2, false, false, false, args2);
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "xiaopi1");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 任务处理
        DeliverCallback xioahanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息，模拟出错，发送到死信队列
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
            System.out.println(" [xiaohan1] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback xiaopiDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);

            System.out.println(" [xiaopi1] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // 注意，要模拒绝消息一定要将autoAck自动确认关闭
        channel.basicConsume(queueName, false, xioahanDeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, xiaopiDeliverCallback, consumerTag -> {
        });


    }
}