package org.example.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RewardPublishDTO {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "赏金不能为空")
    private BigDecimal reward;

    private Long categoryId;

    private String location;

    private LocalDateTime deadline;

    private String tags;
}
