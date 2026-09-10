package org.example.common.VO.GoodsAllVO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsCategoryVO {
    private Integer id;

    private String name;

    private   Integer parentId;

    private String icon;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private List<GoodsCategoryVO> children;
}
