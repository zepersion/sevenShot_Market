package org.example.goodsservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAMQPtest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Test
    public void contextLoads() {
        //队列名称
        String queueName = "test";
        //消息
        String message = "Hello World";
        //发送消息
        rabbitTemplate.convertAndSend(queueName, message);


    }
}
