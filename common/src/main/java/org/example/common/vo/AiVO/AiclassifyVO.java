package org.example.common.vo.AiVO;

import lombok.Data;

import java.util.List;
@Data
public class AiclassifyVO {
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private Double confidence;//可信度
}
