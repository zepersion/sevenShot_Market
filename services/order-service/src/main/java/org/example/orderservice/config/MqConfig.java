package org.example.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MqConfig {

    // ==================== 秒杀异步下单（普通交换机） ====================

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable("seckill-order-queue").build();
    }

    @Bean
    public DirectExchange seckillOrderCreateExchange() {
        return ExchangeBuilder.directExchange("seckill-order-exchange").durable(true).build();
    }

    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillOrderCreateExchange())
                .with("seckill.order");
    }

    // ==================== 秒杀延迟关单（延迟交换机） ====================

    @Bean
    public CustomExchange seckillOrderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("seckill-delay-exchange", "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue seckillOrderDelayQueue() {
        return QueueBuilder.durable("seckill-order-delay-queue").build();
    }

    @Bean
    public Binding seckillDelayBinding() {
        return BindingBuilder.bind(seckillOrderDelayQueue())
                .to(seckillOrderDelayExchange())
                .with("seckill.delay")
                .noargs();
    }

    // ==================== 普通订单延迟关单（延迟交换机） ====================

    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("order-delay-exchange", "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable("order-delay-queue").build();
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with("order.delay")
                .noargs();
    }

    // ==================== JSON 消息转换器 ====================

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
