package com.wms.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库/出库请求：quantity 为正数，方向由端点决定。
 */
@Data
public class StockOpRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @NotNull(message = "物料不能为空")
    private Long skuId;

    private String lotNo;

    @NotNull(message = "货位不能为空")
    private Long locationId;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "数量必须为正数")
    private BigDecimal quantity;

    /** 来源类型（不传则按端点默认） */
    private String refType;

    @NotBlank(message = "来源单据号不能为空")
    private String refNo;

    private Long refLineId;
}
