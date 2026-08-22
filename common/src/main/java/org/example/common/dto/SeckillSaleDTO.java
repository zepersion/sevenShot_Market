package org.example.common.dto;

import lombok.Data;

@Data
public class SeckillSaleDTO {
    private Integer quantity;

    private Integer tradeWay;

    private String address;

    private String msg;
}
