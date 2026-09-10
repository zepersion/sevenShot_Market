package org.example.goodsservice.service;

import org.example.common.DTO.AllGoodsDTO.GoodsCategoryDTO;
import GoodsCategoryVO;

import java.util.List;

public interface GoodsCategoryService {

    List<org.example.common.VO.GoodsAllVO.GoodsCategoryVO> categoresList(GoodsCategoryDTO dto);
}
