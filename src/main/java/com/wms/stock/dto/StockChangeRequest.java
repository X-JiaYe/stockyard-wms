package com.wms.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存变动核心请求：quantity 带符号（正=入，负=出）。
 */
@Data
public class StockChangeRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @NotNull(message = "物料不能为空")
    private Long skuId;

    /** 批次，无批次传空串 */
    private String lotNo;

    @NotNull(message = "货位不能为空")
    private Long locationId;

    @NotNull(message = "变动数量不能为空")
    private BigDecimal quantity;

    /** 方向：1入库 2出库 */
    @NotNull(message = "方向不能为空")
    private Integer direction;

    @NotBlank(message = "来源类型不能为空")
    private String refType;

    @NotBlank(message = "来源单据号不能为空")
    private String refNo;

    private Long refLineId;

    private Long createdBy;
}
