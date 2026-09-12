package org.example.common.vo.OrderVO.OrderDetailVO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetailOrderVO {
    private Long orderId;
    private String orderNo;
    private String status;
    private String statusName;
    private Integer isSeckill;
    private Integer tradeway;
    private String buyerMsg;
    private OrderGoodsVO goodsVO;
    private SellerVO sellerVO;
    private BuyerVO buyerVO;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime;
    private LocalDateTime receiveTime;
    private LocalDateTime finishTime;
}
