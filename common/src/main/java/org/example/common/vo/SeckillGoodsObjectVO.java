package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillGoodsObjectVO {
    private Long goodsId;
    private String title;
    private String coverImage;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Long soldCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
