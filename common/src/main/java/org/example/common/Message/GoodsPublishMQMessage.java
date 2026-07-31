package org.example.common.Message;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsPublishMQMessage {
    private Long userId;

    // 前端传过来的全部参数
    private String title;
    private String description;
    private Long categoryId;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private String coverImage;
    private List<String> images;
    private Integer degree;
    private Integer stock;
    private List<String> tags;
    private Integer isDonation;
}
