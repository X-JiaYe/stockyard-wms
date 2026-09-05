package com.wms.inbound.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 收货明细（含质检结论）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_receive")
public class InboundReceive extends BaseEntity {

    private Long asnId;

    private Long asnLineId;

    private Long skuId;

    private BigDecimal receiveQty;

    /** 质检结论：1合格 2不合格 */
    private Integer qcResult;

    private String remark;
}
