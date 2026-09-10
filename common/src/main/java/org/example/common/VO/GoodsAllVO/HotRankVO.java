package org.example.common.VO.GoodsAllVO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotRankVO {
    private Long id;
    private String goodsName;
    private String goodsPic;
    private BigDecimal price;
    private Double hotScore; //来源于Redis，不是数据库
    private Integer rankNum;
}
