package org.example.common.dto.AllGoodsDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsFavoriteDTO {
    private Integer id;

    private Integer userId;

    private Integer goodsId;

    private LocalDateTime createTime;

    private String goodsName;      //商品名称

    private String coverImg;       //封面图

    private BigDecimal price;      //价格

    private Integer status;        //商品状态 1上架 0下架


}
