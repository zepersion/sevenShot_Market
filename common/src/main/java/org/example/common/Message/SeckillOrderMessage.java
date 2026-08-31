package org.example.common.Message;

import lombok.Data;

@Data
public class SeckillOrderMessage {
    private Long goodsId;//商品ID（路径参数）
    private Integer quantity;//数量，默认1
    private Integer tradeWay;//交易方式：1面交 2快递
    private String address;//收获地址
    private  String message;//留言
    private Long userId;//当前用户id


}
