package org.example.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RewardPublishDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "赏金不能为空")
    private BigDecimal rewardAmount;

    private Integer categoryId;

    private String coverImage;

    private List<String> images;

    private LocalDateTime deadline;
}
