package org.example.goodsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
public class Mqconfig {
    //审核任务
    // direct交换机
    @Bean
    public DirectExchange directExchange() {

        return ExchangeBuilder.directExchange("goods.direct")
                .durable(true).build();
    }

    //
    @Bean
    public org.springframework.amqp.core.Queue directQueue() {
        return QueueBuilder.durable("goods.audit.queue").build();
    }

/*    @Bean
    public Binding directBinding(DirectExchange directExchange, Queue directQueue) {
        return BindingBuilder.bind(directQueue).
                to(directExchange).
                with("goods.audit");

    }*/

    //广播给用户用fanout交换机
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder.fanoutExchange("product.onsale.fanout").build();
    }

    @Bean
    public org.springframework.amqp.core.Queue fanoutQueue1() {
        return QueueBuilder.durable("search.queue").build();
    }

    @Bean
    public org.springframework.amqp.core.Queue fanoutQueue2() {
        return QueueBuilder.durable("push.queue").build();
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    //点赞列表
    @Bean
    public DirectExchange goodsLikeExchange(){
        return ExchangeBuilder.directExchange("goods.like.exchange").build();
    }
    @Bean
    public Queue goodsLikeQueue(){
        return QueueBuilder.durable("goods.like.queue").build();
    }
    //收藏
    @Bean
    public DirectExchange goodsFavoriteExchange(){
        return ExchangeBuilder.directExchange("goods.favorite.exchange").build();
    }
    @Bean
    public Queue goodsFavoriteQueue(){
        return QueueBuilder.durable("goods.favorite.queue").build();
    }
}

