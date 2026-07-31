package org.example.goodsservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_category")
public class GoodsCategory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private  String name;

    private Integer parentId;

   private String icon;

   private Integer sort;

   private Integer status;

   private LocalDateTime createTime;
}
