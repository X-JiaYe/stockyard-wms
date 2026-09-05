package com.wms.outbound.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 出库订单明细行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order_line")
public class OutboundOrderLine extends BaseEntity {

    private Long orderId;

    private Long skuId;

    private BigDecimal orderQty;

    private BigDecimal pickedQty;

    private String lotNo;
}
