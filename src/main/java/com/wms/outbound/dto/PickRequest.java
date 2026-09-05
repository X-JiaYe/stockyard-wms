package com.wms.outbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 拣货请求。
 */
@Data
public class PickRequest {

    @NotNull(message = "明细行不能为空")
    private Long orderLineId;

    @NotNull(message = "货位不能为空")
    private Long locationId;

    @NotNull(message = "拣货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "拣货数量必须为正数")
    private BigDecimal qty;
}
