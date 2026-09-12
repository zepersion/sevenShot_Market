package org.example.common.dto.AllGoodsDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsCategoryDTO {
   private Integer id;

   private String name;

  private   Integer parentId;

   private String icon;

   private Integer sort;

   private Integer status;

  private   LocalDateTime createTime;
}
