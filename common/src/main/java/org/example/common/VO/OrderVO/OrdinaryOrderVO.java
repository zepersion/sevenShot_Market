package org.example.common.VO.OrderVO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrdinaryOrderVO {

    private String orderNo;

    private Integer status;

    private BigDecimal amount;

    private Long expireTime;
}
