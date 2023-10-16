package com.yupi.springbootinit.bizMq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Han
 * @data 2023/10/16
 * @apiNode
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routing, String message) {
        rabbitTemplate.convertAndSend(exchange, routing, message);

    }
}
