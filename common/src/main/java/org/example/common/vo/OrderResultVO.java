package org.example.common.vo;

import lombok.Data;

@Data
public class OrderResultVO {
    private Long orderNo;
    private Integer status;
    private String msg;
    private Long orderId;
    private String failReason;
}
