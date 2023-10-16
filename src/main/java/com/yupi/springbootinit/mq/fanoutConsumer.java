package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * fanout交换机消费者代码
 */
public class fanoutConsumer {
  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    // 创建频道
    Channel channel = connection.createChannel();
    // 声明交换机
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    // 定义队列名称
    String queueName = "xiaoli_queue";
    String queueName2 = "xiaowang_queue";
    // 创建队列
    channel.queueDeclare(queueName, false, false, false, null);
    channel.queueDeclare(queueName2, false, false, false, null);
    // 绑定队列
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    channel.queueBind(queueName2, EXCHANGE_NAME, "");
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 任务处理
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [小王] Received '" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [小李] Received '" + message + "'");
    };
    // 消息确认
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}