package org.example.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AfterSaleApplyDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "售后类型不能为空")
    private Integer type;           // 1仅退款 2退货退款

    @NotBlank(message = "售后原因不能为空")
    private String reason;

    @NotBlank(message = "详细描述不能为空")
    private String description;

    private List<String> images;    // 凭证图片URL列表，可选

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;
}
