package org.example.common.DTO.aiDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiGoodsEstimateDTO {

    /**
     * 分类ID 必填
     */
    @NotNull
    private Long categoryId;

    /**
     * 品牌 非必填
     */
    private String brand;

    /**
     * 型号 非必填
     */
    private String model;

    /**
     * 原价 必填
     */
    @NotNull
    private BigDecimal originalPrice;

    /**
     * 成色 必填
     */
    @NotNull
    private Integer degree;

    /**
     * 购买时间 非必填
     */
    private String purchaseDate;

    /**
     * 补充描述 非必填
     */
    private String description;

}
