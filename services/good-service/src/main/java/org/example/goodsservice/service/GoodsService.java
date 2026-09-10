package org.example.goodsservice.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.DTO.AllGoodsDTO.GoodsListDTO;
import org.example.common.DTO.AllGoodsDTO.GoodsPublishDTO;
import org.example.common.DTO.AllGoodsDTO.HotRankDTO;
import org.example.common.DTO.AllGoodsDTO.SeckillSaleDTO;
import org.example.common.VO.*;
import org.example.common.VO.AllSeckillVO.SeckillGoodsDataVO;
import org.example.common.VO.GoodsAllVO.GoodsPublishVO;
import org.example.common.VO.GoodsAllVO.GoodsVO;
import org.example.common.VO.GoodsAllVO.HotRankVO;

import java.util.List;

public interface GoodsService {
    GoodsPublishVO publish(@Valid GoodsPublishDTO dto);


    PageVO<GoodsVO> goodsToList(GoodsListDTO dto, Integer page, Integer size);

    GoodsVO goodsDetail( Long id) throws InterruptedException;

    PageVO<GoodsVO> myList(Integer page, Integer size);

    void offSaleGoods(@NotNull Long id);

   List<HotRankVO> getHotRank(HotRankDTO dto);

    SeckillGoodsDataVO seckillList();

   void seckill(Long goodsId, SeckillSaleDTO dto);

    void SeckillPreload();



}
