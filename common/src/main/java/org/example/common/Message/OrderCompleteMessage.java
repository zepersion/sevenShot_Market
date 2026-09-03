package org.example.common.Message;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderCompleteMessage implements Serializable {
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
    private Long goodsId;
}
