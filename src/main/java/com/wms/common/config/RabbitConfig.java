package com.wms.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑：领域事件 Topic 交换机 + 持久化队列。
 * 路由键 wms.domain.* 按事件类型（如 inbound.putaway / outbound.shipped）区分。
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "wms.domain.exchange";
    public static final String QUEUE = "wms.domain.events";
    public static final String ROUTING_PREFIX = "wms.domain.";
    public static final String ROUTING_ALL = "wms.domain.#";

    @Bean
    public TopicExchange domainExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue domainQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding domainBinding() {
        return BindingBuilder.bind(domainQueue()).to(domainExchange()).with(ROUTING_ALL);
    }
}
