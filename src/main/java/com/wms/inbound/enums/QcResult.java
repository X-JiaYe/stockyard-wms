package com.wms.inbound.enums;

import lombok.Getter;

/**
 * 质检结论（与 inbound_receive.qc_result 注释口径一致）。
 */
@Getter
public enum QcResult {

    PASS(1, "合格"),
    FAIL(2, "不合格");

    private final int value;
    private final String desc;

    QcResult(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
