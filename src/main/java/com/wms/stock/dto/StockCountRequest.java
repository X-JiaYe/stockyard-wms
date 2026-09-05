package com.wms.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点请求：按实盘数量生成差异调整。
 */
@Data
public class StockCountRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @NotNull(message = "物料不能为空")
    private Long skuId;

    private String lotNo;

    @NotNull(message = "货位不能为空")
    private Long locationId;

    @NotNull(message = "实盘数量不能为空")
    @DecimalMin(value = "0", message = "实盘数量不能为负")
    private BigDecimal actualQty;

    @NotBlank(message = "盘点单号不能为空")
    private String refNo;
}
