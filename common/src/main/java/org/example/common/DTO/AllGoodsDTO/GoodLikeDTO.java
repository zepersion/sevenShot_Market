package org.example.common.DTO.AllGoodsDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodLikeDTO {
    private Integer id;

    private Integer userId;

    private Integer goodsId;

    private LocalDateTime createTime;
}
