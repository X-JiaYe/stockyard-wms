package com.wms.inbound.enums;

import lombok.Getter;

/**
 * ASN 状态（与 inbound_asn.status 注释口径一致）。
 */
@Getter
public enum AsnStatus {

    PENDING(10, "待收货"),
    RECEIVING(20, "收货中"),
    RECEIVED(30, "已收货"),
    COMPLETED(40, "已完成"),
    CANCELLED(90, "已取消");

    private final int value;
    private final String desc;

    AsnStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
