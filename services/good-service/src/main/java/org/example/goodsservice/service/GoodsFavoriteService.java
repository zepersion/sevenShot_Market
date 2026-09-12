package org.example.goodsservice.service;

import org.example.common.dto.AllGoodsDTO.GoodsFavoriteDTO;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.vo.PageVO;

public interface GoodsFavoriteService {
    void isFavorite(GoodsFavoriteDTO goodFavoriteDTO);

    PageVO<GoodsVO> goodsFavoriteToList(GoodsFavoriteDTO dto, Integer page, Integer size);
}
