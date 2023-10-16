package com.yupi.springbootinit.mq;

/**
 * @author Han
 * @data 2023/10/13
 * @apiNode
 */
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 单生产者 1v1
 */
public class ttlProducer {

    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            Scanner scan = new Scanner(System.in);
            while (scan.hasNext()) {

                String message= scan.nextLine();
                // 发送消息
                channel.basicPublish(QUEUE_NAME, "", null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "' ");
            }
        }
    }
}
