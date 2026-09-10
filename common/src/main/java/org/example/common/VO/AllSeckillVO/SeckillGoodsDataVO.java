package org.example.common.VO.AllSeckillVO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillGoodsDataVO {

private LocalDateTime seckillStartTime;

private LocalDateTime seckillEndTime;

private List<SeckillGoodsObjectVO> seckillGoodsList;
}
