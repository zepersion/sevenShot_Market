package org.example.common.DTO.aiDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiGoodsTextDTO {
    @NotNull(message = "商品分类不能为空")
private  Long categoryId;//商品分类
    @NotNull(message="关键词不能为空")
    private String keywords;//关键词/简介
    @NotNull(message = "原价不能为空")
    private BigDecimal OrinaPriceprice;//原价
    @NotNull(message = "售价不能为空")
    private BigDecimal sellingPrice;//售价
    @NotNull(message = "成色不能为空")
    private  Integer degree;//成色
    private  Integer style;//文案风格
}
