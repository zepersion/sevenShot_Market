package org.example.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Integer buyerId;

    private Integer sellerId;

    private Integer goodsId;

    private String goodsTitle;

    private String goodsImage;

    private BigDecimal goodsPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private Integer isSeckill;

    private Integer status;

    private Integer payStatus;

    private LocalDateTime payTime;

    private LocalDateTime deliverTime;

    private LocalDateTime receiveTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private Integer buyerWay;

    private String address;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
