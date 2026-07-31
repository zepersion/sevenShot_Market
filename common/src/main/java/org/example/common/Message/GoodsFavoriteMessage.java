package org.example.common.Message;

import lombok.Data;

@Data
public class GoodsFavoriteMessage {
    private Long goodsId;
    private Long userId;
    private Integer type;//0代表取关,1代表关注
}
