package org.example.goodsservice.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.dto.*;
import org.example.common.vo.*;

import java.util.List;

public interface GoodsService {
    GoodsVO publish(@Valid GoodsPublishDTO dto);


    PageVO<GoodsVO> goodsToList(GoodsListDTO dto, Integer page, Integer size);

    GoodsVO goodsDetail( Long id) throws InterruptedException;

    PageVO<GoodsVO> myList(Integer page, Integer size);

    void offSaleGoods(@NotNull Long id);

   List<HotRankVO> getHotRank(HotRankDTO dto);

    SeckillGoodsDataVO seckillList();

   void seckill(Long goodsId, SeckillSaleDTO dto);

    void SeckillPreload();
}
