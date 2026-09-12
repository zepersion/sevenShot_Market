package org.example.goodsservice.controller;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.example.common.Result;
import org.example.common.dto.AllGoodsDTO.*;
import org.example.common.vo.GoodsAllVO.GoodsCategoryVO;
import org.example.common.vo.GoodsAllVO.GoodsPublishVO;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.vo.GoodsAllVO.HotRankVO;
import org.example.common.vo.PageVO;

import org.example.goodsservice.service.GoodsCategoryService;
import org.example.goodsservice.service.GoodsFavoriteService;
import org.example.goodsservice.service.GoodsLikeService;
import org.example.goodsservice.service.GoodsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    @Resource
private GoodsService goodsService;
    @Resource
    private GoodsLikeService goodsLikeService;
    @Resource
    private GoodsFavoriteService goodsFavoriteService;
    @Resource
    private GoodsCategoryService goodsCategoryService;
    @PostMapping("/publish")
    public Result publishGoods(@RequestBody @Valid GoodsPublishDTO dto) {
        GoodsPublishVO goodsVO =goodsService.publish(dto);
        return Result.success(goodsVO,"发布成功，正在审核");
    }
    @GetMapping("/list")
    public Result goodsList(GoodsListDTO dto, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
      PageVO<GoodsVO> vo= goodsService.goodsToList(dto,page,size);
        return Result.success(vo);
    }
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) throws InterruptedException {
        GoodsVO vo=goodsService.goodsDetail(id);
        return Result.success(vo);
    }
    //点赞功能
    @PostMapping("/like/{id}")
    public Result isLike(@RequestBody GoodLikeDTO goodLikeDTO){
        goodsLikeService.isLike(goodLikeDTO);
        return Result.success();
    }
    //收藏功能
    @PostMapping("/favorite/{id}")
    public Result isFavorite(@RequestBody GoodsFavoriteDTO goodFavoriteDTO ){
               goodsFavoriteService.isFavorite(goodFavoriteDTO);
               return Result.success();
    }
    @GetMapping("/favorite/list")
    public Result goodsFavoriteList(GoodsFavoriteDTO dto,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20") Integer size) {
        PageVO<GoodsVO> vo= goodsFavoriteService.goodsFavoriteToList(dto,page,size);
        return Result.success(vo);
    }
    @GetMapping("/my/list")
    public Result myList(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20") Integer size) {

        PageVO<GoodsVO> vo=goodsService.myList(page,size);
        return Result.success(vo);
    }
    @PostMapping("offline/{id}")
    public  Result offSale(@PathVariable @NotNull Long goodsId){
        goodsService.offSaleGoods(goodsId);
        return Result.success();
    }
    @GetMapping("/categories")
    public Result goodsCategoriesList(GoodsCategoryDTO dto ){
            List<GoodsCategoryVO> vo=goodsCategoryService.categoresList(dto);
        return Result.success(vo);
    }
    //商品热度排行榜（Top N）
    @GetMapping("/ranking/hot")
    public Result goodsRankingHot(HotRankDTO dto){
       List <HotRankVO> vo= goodsService.getHotRank(dto);
        return Result.success(vo);
    }
    // 提供接口：查询审核通过、上架的商品列表
    @GetMapping("/listValidGoods")
    public Result<List<GoodsVO>> listValidGoods(){
        List<GoodsVO> list = goodsService.getValidOnSaleGoods();
        return Result.success(list);
    }

    // 根据id批量查询商品VO
    @PostMapping("/listByIds")
    public Result<List<GoodsVO>> listByIds(@RequestBody List<Long> ids){
        return Result.success(goodsService.getGoodsVoByIds(ids));
    }

}
