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
public class BiProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(MqConstant.BI_EXCHANGE_NAME, MqConstant.BI_ROUTING_KEY, message);

    }
}
