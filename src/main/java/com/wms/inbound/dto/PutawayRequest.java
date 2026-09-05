package com.wms.inbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 上架请求。
 */
@Data
public class PutawayRequest {

    @NotNull(message = "明细行不能为空")
    private Long asnLineId;

    @NotNull(message = "货位不能为空")
    private Long locationId;

    @NotNull(message = "上架数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "上架数量必须为正数")
    private BigDecimal qty;
}
