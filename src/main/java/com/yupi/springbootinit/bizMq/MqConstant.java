package com.yupi.springbootinit.bizMq;

/**
 * @author Han
 * @data 2023/10/16
 * @apiNode
 */
public interface MqConstant {

    /**
     * bi项目消息队列
     */
    String BI_QUEUE_NAME = "bi_queue";
    /**
     * bi项目队列交换机
     */
    String BI_EXCHANGE_NAME = "bi_exchange";
    /**
     * bi项目消息队列路由键
     */
    String BI_ROUTING_KEY = "bi_routing_key";



}
