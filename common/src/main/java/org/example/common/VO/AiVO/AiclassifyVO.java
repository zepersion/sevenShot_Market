package org.example.common.VO.AiVO;

import lombok.Data;

import java.util.List;
@Data
public class AiclassifyVO {
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private Double confidence;//可信度
}
