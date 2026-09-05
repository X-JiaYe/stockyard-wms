package com.wms.outbound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发运请求。
 */
@Data
public class ShipRequest {

    @NotNull(message = "订单不能为空")
    private Long orderId;
}
