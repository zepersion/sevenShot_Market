package org.example.goodsservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_like")
public class GoodsLike {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer goodsId;

    private LocalDateTime createTime;
}
