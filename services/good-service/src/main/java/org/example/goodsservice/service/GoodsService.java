package org.example.goodsservice.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.dto.GoodsDTO;
import org.example.common.dto.GoodsListDTO;
import org.example.common.dto.GoodsPublishDTO;
import org.example.common.dto.HotRankDTO;
import org.example.common.vo.GoodsVO;
import org.example.common.vo.HotRankVO;
import org.example.common.vo.PageVO;

import java.util.List;

public interface GoodsService {
    GoodsVO publish(@Valid GoodsPublishDTO dto);


    PageVO<GoodsVO> goodsToList(GoodsListDTO dto, Integer page, Integer size);

    GoodsVO goodsDetail(GoodsDTO dto, Long id);

    PageVO<GoodsVO> myList(Integer page, Integer size);

    void offSaleGoods(@NotNull Long id);

   List<HotRankVO> getHotRank(HotRankDTO dto);
}
