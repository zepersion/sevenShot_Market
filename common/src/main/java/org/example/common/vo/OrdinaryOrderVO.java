package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrdinaryOrderVO {

    private String orderNo;

    private Integer status;

    private BigDecimal amount;

    private Long expireTime;
}
