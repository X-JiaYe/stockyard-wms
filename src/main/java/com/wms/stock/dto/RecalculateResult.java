package com.wms.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 重算校验结果：ledger 聚合结存 vs balance 快照。
 */
@Data
public class RecalculateResult {

    private BigDecimal ledgerQuantity;

    private BigDecimal balanceQuantity;

    private boolean consistent;

    public static RecalculateResult of(BigDecimal ledger, BigDecimal balance) {
        RecalculateResult r = new RecalculateResult();
        r.ledgerQuantity = ledger;
        r.balanceQuantity = balance;
        r.consistent = ledger.compareTo(balance) == 0;
        return r;
    }
}
