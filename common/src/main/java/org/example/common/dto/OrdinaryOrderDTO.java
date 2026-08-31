package org.example.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrdinaryOrderDTO {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "收货地址不能为空")
    private String address;

    @NotNull(message = "联系电话不能为空")
    private String contactPhone;

    private String remark;

    private Integer tradeMethod;
}
