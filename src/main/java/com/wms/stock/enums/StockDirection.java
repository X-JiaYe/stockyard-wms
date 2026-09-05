package com.wms.stock.enums;

import lombok.Getter;

/**
 * 库存变动方向（与 stock_ledger.direction 注释口径一致）。
 */
@Getter
public enum StockDirection {

    IN(1, "入库"),
    OUT(2, "出库");

    private final int value;
    private final String desc;

    StockDirection(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
