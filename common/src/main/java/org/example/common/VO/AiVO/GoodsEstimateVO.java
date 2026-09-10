package org.example.common.VO.AiVO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsEstimateVO {
    private BigDecimal suggestedPrice;
   private AipriveRangeVO suggestedRange;
   private String estimateBasis;
   private String saleTips;

}
