package org.example.goodsservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.goodsservice.entity.GoodsFavorite;

@Mapper
public interface GoodsFavoriteMapper extends BaseMapper<GoodsFavorite> {
}
