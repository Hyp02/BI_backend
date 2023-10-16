package com.yupi.springbootinit.bizMq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 初始化项目队列
 * @author Han
 * @data 2023/10/16
 * @apiNode
 */
public class MqInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // 频道绑定交换机
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 定义队列名称
            String queueName = "code_queue";
            // 创建队列
            channel.queueDeclare(queueName, false, false, false, null);
            // 绑定队列 指定路由键(绑定到对应的队列)
            channel.queueBind(queueName, EXCHANGE_NAME, "my_routing_key");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
