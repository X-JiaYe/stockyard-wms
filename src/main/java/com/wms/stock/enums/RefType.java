package com.wms.stock.enums;

import lombok.Getter;

/**
 * 库存流水来源单据类型（与 stock_ledger.ref_type 注释口径一致）。
 */
@Getter
public enum RefType {

    INIT("INIT", "期初导入"),
    ASN("ASN", "到货通知"),
    RECEIVE("RECEIVE", "收货"),
    PUTAWAY("PUTAWAY", "上架"),
    PICK("PICK", "拣货"),
    ADJUST("ADJUST", "调整"),
    CYCLE_COUNT("CYCLE_COUNT", "盘点"),
    MOVE("MOVE", "移动");

    private final String value;
    private final String desc;

    RefType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
