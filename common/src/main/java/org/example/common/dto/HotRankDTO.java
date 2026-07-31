package org.example.common.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class HotRankDTO {
    Integer top;
    Long categoryId;
}
