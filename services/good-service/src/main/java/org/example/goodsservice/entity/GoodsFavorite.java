package org.example.goodsservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("goods_favorite")
public class GoodsFavorite {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer userId;

    private LocalDateTime createTime;
}
