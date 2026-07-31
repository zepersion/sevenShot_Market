package org.example.goodsservice.service;

import org.example.common.dto.GoodsCategoryDTO;
import org.example.common.vo.GoodsCategoryVO;
import org.example.common.vo.PageVO;

import java.util.List;

public interface GoodsCategoryService {

    List<GoodsCategoryVO> categoresList(GoodsCategoryDTO dto);
}
