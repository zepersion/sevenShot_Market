package org.example.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodLikeDTO {
    private Integer id;

    private Integer userId;

    private Integer goodsId;

    private LocalDateTime createTime;
}
