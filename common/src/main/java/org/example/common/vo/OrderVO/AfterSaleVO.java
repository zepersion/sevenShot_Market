package org.example.common.vo.OrderVO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AfterSaleVO {

    private Long id;

    private String orderNo;

    private Long orderId;

    private Integer type;           // 1仅退款 2退货退款

    private String typeName;        // 类型描述

    private String reason;

    private String description;

    private List<String> images;

    private BigDecimal refundAmount;

    private Integer status;         // 0待处理 1卖家同意 2卖家拒绝 3买家撤销 4已完成

    private String statusName;

    private String sellerReply;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
