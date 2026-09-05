package com.wms.inbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ASN 明细项。
 */
@Data
public class AsnLineItem {

    @NotNull(message = "物料不能为空")
    private Long skuId;

    @NotNull(message = "预期数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "预期数量必须为正数")
    private BigDecimal expectedQty;

    private String lotNo;
}
