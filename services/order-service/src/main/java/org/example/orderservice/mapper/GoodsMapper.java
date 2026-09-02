package org.example.orderservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface GoodsMapper {

    @Select("SELECT id, seller_id, title, cover_image, selling_price, stock, status FROM goods WHERE id = #{goodsId}")
    Map<String, Object> selectGoodsById(Long goodsId);

    @Select("SELECT * FROM goods WHERE id = #{goodsId}")
    Map<String, Object> selectGoodsAllById(Long goodsId);

    @Update("UPDATE goods SET status = #{status}, update_time = NOW() WHERE id = #{goodsId}")
    int updateStatus(Long goodsId, Integer status);
    @Update("update goods Set stock=stock+1 where id=#{goodsid}")
    int updateStock(Long goodsId);
    @Update("update goods Set stock=stock-1 where id=#{goodsid}")
    int updateStock1(Long goodsId);
    @Update("update goods Set seckill_stock=seckill_stock+1 where id=#{goodsid}")
    int updateSeckillStock(Long goodsId);
    @Update("update goods Set seckill_stock=seckill_stock-1 where id=#{goodsid}")
    int updateSeckillStock1(Long goodsId);
}
