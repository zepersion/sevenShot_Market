package org.example.common.vo.OrderVO.OrderDetailVO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderGoodsVO {
private Long goodsId;
private String title;
private String imageUrl;
private BigDecimal price;
private Integer quantity;
}
