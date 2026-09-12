package org.example.common.dto.AllGoodsDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsDTO {

    private Long id;

    private Long sellerId;

    private String title;

    private String description;

    private Long categoryId;

    private BigDecimal originalPrice;

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

    private Integer hotScore;

    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer isDonation;

    private String tags;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
