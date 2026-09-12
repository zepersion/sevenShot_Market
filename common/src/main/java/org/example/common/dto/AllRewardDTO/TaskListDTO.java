package org.example.common.dto.AllRewardDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskListDTO {

    private String keyWord;

    private Long taskId;

    private BigDecimal minReward;

    private BigDecimal maxReward;

    private Integer categoryId;

    private Integer sortType;

    private Integer page;

    private Integer size;
}
