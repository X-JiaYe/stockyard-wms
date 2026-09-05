package com.wms.inbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收货请求（含质检结论）。
 */
@Data
public class ReceiveRequest {

    @NotNull(message = "明细行不能为空")
    private Long asnLineId;

    @NotNull(message = "收货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "收货数量必须为正数")
    private BigDecimal qty;

    /** 质检结论：1合格 2不合格 */
    @NotNull(message = "质检结论不能为空")
    private Integer qcResult;

    private String remark;
}
