package org.example.common.Message.AllGoodsMsg;

import lombok.Data;

import java.util.List;

@Data
public class GoodsAuditTaskMessage {
    private Long goodsId;
    private Long userId;
    private String Title;
    private String Description;
    private List Tags;
}
