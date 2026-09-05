package com.wms.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存移动请求：从 fromLocation 移到 toLocation。
 */
@Data
public class StockMoveRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @NotNull(message = "物料不能为空")
    private Long skuId;

    private String lotNo;

    @NotNull(message = "源货位不能为空")
    private Long fromLocationId;

    @NotNull(message = "目标货位不能为空")
    private Long toLocationId;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "数量必须为正数")
    private BigDecimal quantity;

    @NotBlank(message = "来源单据号不能为空")
    private String refNo;
}
