package org.example.common.dto.AllGoodsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    @NotNull(message = "售价不能为空")
    private BigDecimal sellingPrice;

    private String coverImage;

    private String images;

    private Integer degree;

    private Integer stock;

    private Integer isSeckill;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private LocalDateTime seckillStart;

    private LocalDateTime seckillEnd;

    private Integer isDonation;

    private String tags;
}
