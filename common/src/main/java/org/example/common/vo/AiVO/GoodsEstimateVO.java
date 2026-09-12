package org.example.common.vo.AiVO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsEstimateVO {
    private BigDecimal suggestedPrice;
   private AipriveRangeVO suggestedRange;
   private String estimateBasis;
   private String saleTips;

}
