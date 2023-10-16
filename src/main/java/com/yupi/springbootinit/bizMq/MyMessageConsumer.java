package com.yupi.springbootinit.bizMq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Han
 * @data 2023/10/16
 * @apiNode
 */
@Component
@Slf4j
public class MyMessageConsumer {
    @SneakyThrows
    // 监听code_queue这个队列中的消息，
    @RabbitListener(queues = "code_queue",ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG)  long deliverTag){

        log.info("接收到消息= {}",message);
        channel.basicAck(deliverTag,false);

    }

}
