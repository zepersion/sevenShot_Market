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



    // seckill-order-exchange 和 seckill-order-queue 由 Consumer 的 @QueueBinding 声明
    // 这里只声明延迟交换机（@QueueBinding 不支持 x-delayed-message 类型）


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

    // ==================== 订单完成异步处理 ====================

    @Bean
    public DirectExchange orderCompleteExchange() {
        return new DirectExchange("order.complete.exchange", true, false);
    }

    @Bean
    public Queue creditQueue() {
        return QueueBuilder.durable("order.credit.queue").build();
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable("order.notify.queue").build();
    }

    @Bean
    public Queue goodsSoldQueue() {
        return QueueBuilder.durable("order.goods.sold.queue").build();
    }

    @Bean
    public Binding creditBinding() {
        return BindingBuilder.bind(creditQueue())
                .to(orderCompleteExchange()).with("credit");
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue())
                .to(orderCompleteExchange()).with("notify");
    }

    @Bean
    public Binding goodsSoldBinding() {
        return BindingBuilder.bind(goodsSoldQueue())
                .to(orderCompleteExchange()).with("goods.sold");
    }

    // ==================== JSON 消息转换器 ====================

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
