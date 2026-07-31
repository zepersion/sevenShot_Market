package org.example.aiservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jdk.jfr.Category;

@Configuration
public class Mqconfig {
    /*TODO 创建交换机*/
    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange("ai.audit.exchange")
                .durable(true)
                .build();
    }
    @Bean
    public Queue queue() {
        return new Queue("ai.audit.queue");
    }
}
