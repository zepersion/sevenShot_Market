package org.example.common.VO.AiVO;

import lombok.Data;

import java.util.List;
@Data
public class AiGoodsTextVO {
    private String title;
    private String description;
    private List<String> tags;
}
