package org.example.goodsservice.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.dto.AllGoodsDTO.GoodsListDTO;
import org.example.common.dto.AllGoodsDTO.GoodsPublishDTO;
import org.example.common.dto.AllGoodsDTO.HotRankDTO;
import org.example.common.dto.AllGoodsDTO.SeckillSaleDTO;
import org.example.common.vo.AllSeckillVO.SeckillGoodsDataVO;
import org.example.common.vo.GoodsAllVO.GoodsPublishVO;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.vo.GoodsAllVO.HotRankVO;
import org.example.common.vo.PageVO;

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


    List<GoodsVO> getGoodsVoByIds(List<Long> ids);

    List<GoodsVO> getValidOnSaleGoods();
}
