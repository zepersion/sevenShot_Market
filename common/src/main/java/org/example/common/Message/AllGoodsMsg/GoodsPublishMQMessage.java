package org.example.common.Message.AllGoodsMsg;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsPublishMQMessage {
    private Long id;
    // 前端传过来的全部参数
    private String title;
    private String description;

}
