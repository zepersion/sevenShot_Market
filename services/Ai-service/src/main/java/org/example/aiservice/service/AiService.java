package org.example.aiservice.service;

import org.example.common.dto.aiDTO.AiGoodsEstimateDTO;
import org.example.common.dto.aiDTO.AiGoodsTextDTO;
import org.example.common.vo.AiVO.AiGoodsTextVO;
import org.example.common.vo.AiVO.AiclassifyVO;
import org.example.common.vo.AiVO.GoodsEstimateVO;

public interface AiService {

    AiGoodsTextVO copywriting(AiGoodsTextDTO dto);

    GoodsEstimateVO estimate(AiGoodsEstimateDTO dto);

    AiclassifyVO classify(String title, String description);

    String recommend(String text);
}
