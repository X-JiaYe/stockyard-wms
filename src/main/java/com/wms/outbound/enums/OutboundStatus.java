package com.wms.outbound.enums;

import lombok.Getter;

/**
 * 出库订单状态（与 outbound_order.status 注释口径一致）。
 */
@Getter
public enum OutboundStatus {

    PENDING(10, "待拣货"),
    PICKING(20, "拣货中"),
    PICKED(30, "已拣货"),
    SHIPPED(40, "已发运"),
    CANCELLED(90, "已取消");

    private final int value;
    private final String desc;

    OutboundStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
