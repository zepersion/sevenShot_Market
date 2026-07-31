package org.example.aiservice.mq;

import jakarta.annotation.Resource;
import org.example.aiservice.FeignClient.GoodsFeignClient;
import org.example.common.Message.GoodsAuditTaskMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

public class Aiconsumer {
    @Resource
    private GoodsFeignClient goodsFeignClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ai.audit.queue",durable = "true"),
            exchange =@Exchange(name = "ai.direct",type = ExchangeTypes.DIRECT),
            key = {"goods.audit"}
    ))
    public void auditGoods(GoodsAuditTaskMessage message) {
    //TODO ai审核后面再写

    }


}
