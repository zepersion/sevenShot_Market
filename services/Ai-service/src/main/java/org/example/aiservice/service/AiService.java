package org.example.aiservice.service;

import org.example.common.DTO.aiDTO.AiGoodsEstimateDTO;
import org.example.common.DTO.aiDTO.AiGoodsTextDTO;
import org.example.common.VO.AiVO.AiGoodsTextVO;
import org.example.common.VO.AiVO.AiclassifyVO;
import org.example.common.VO.AiVO.GoodsEstimateVO;

public interface AiService {

    AiGoodsTextVO copywriting(AiGoodsTextDTO dto);

    GoodsEstimateVO estimate(AiGoodsEstimateDTO dto);

    AiclassifyVO classify(String title, String description);

    String recommend(String text);
}
