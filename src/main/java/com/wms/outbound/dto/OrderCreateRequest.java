package com.wms.outbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建出库订单请求。
 */
@Data
public class OrderCreateRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String customerName;

    private String remark;

    @NotEmpty(message = "明细不能为空")
    @Valid
    private List<OrderLineItem> lines;
}
