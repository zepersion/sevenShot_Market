package org.example.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("after_sale")
public class AfterSale {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long orderId;

    private Long buyerId;

    private Long sellerId;

    private Long goodsId;

    private Integer type;           // 1仅退款 2退货退款

    private String reason;          // 申请原因

    private String description;     // 详细描述

    private String images;          // 凭证图片URL，逗号分隔

    private Integer status;         // 0待处理 1卖家同意 2卖家拒绝 3买家撤销 4已完成

    private BigDecimal refundAmount;

    private String sellerReply;     // 卖家回复

    private String aiJudgment;      // AI判定意见

    private Long handlerId;         // 平台处理人ID

    private String handlerResult;   // 平台处理结果

    private LocalDateTime createTime;

    private LocalDateTime handleTime;   // 处理时间

    private LocalDateTime finishTime;   // 完成时间

    private LocalDateTime updateTime;
}
