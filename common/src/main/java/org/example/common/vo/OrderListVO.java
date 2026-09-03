package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListVO {
    private Long orderId;
    private String orderNo;
    private Integer status;
    private String statusName;
    private Integer isSeckill;
    private String goodsTitle;
    private String goodsImage;
    private BigDecimal totalPrice;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
