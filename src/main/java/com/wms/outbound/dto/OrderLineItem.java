package com.wms.outbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 出库订单明细项。
 */
@Data
public class OrderLineItem {

    @NotNull(message = "物料不能为空")
    private Long skuId;

    @NotNull(message = "订购数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "订购数量必须为正数")
    private BigDecimal orderQty;

    private String lotNo;
}
