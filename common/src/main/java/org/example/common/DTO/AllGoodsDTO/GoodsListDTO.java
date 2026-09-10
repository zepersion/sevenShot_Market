package org.example.common.DTO.AllGoodsDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsListDTO {
private String keyWord;
private Long categoryId;
private String categoryName;
private BigDecimal minprice;
private BigDecimal maxprice;
private Integer degree;
private  Integer sortType;
private Integer page;
private Integer size;
private String school;





}
