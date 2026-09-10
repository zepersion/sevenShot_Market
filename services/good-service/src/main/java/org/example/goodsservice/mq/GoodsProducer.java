package org.example.goodsservice.mq;

import org.example.common.Message.AllGoodsMsg.GoodsPublishMQMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsProducer
{
@Autowired
    private RabbitTemplate rabbitTemplate;
public void goods_publish(GoodsPublishMQMessage msg){
        String exchangeName = "goods.direct";
        String routingKey = "goods.audit";
        rabbitTemplate.convertAndSend(exchangeName, routingKey, msg);
}



}
