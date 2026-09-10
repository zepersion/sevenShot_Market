package org.example.goodsservice.service;

import org.example.common.DTO.AllGoodsDTO.GoodsFavoriteDTO;
import org.example.common.VO.GoodsAllVO.GoodsVO;
import org.example.common.VO.PageVO;

public interface GoodsFavoriteService {
    void isFavorite(GoodsFavoriteDTO goodFavoriteDTO);

    PageVO<GoodsVO> goodsFavoriteToList(GoodsFavoriteDTO dto, Integer page, Integer size);
}
