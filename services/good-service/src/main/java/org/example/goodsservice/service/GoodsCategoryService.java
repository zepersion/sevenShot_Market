package org.example.goodsservice.service;

import org.example.common.dto.AllGoodsDTO.GoodsCategoryDTO;
import org.example.common.vo.GoodsAllVO.GoodsCategoryVO;


import java.util.List;

public interface GoodsCategoryService {

    List<GoodsCategoryVO> categoresList(GoodsCategoryDTO dto);
}
