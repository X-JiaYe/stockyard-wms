package com.wms.inbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建 ASN 请求。
 */
@Data
public class AsnCreateRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String supplierName;

    private String remark;

    @NotEmpty(message = "明细不能为空")
    @Valid
    private List<AsnLineItem> lines;
}
