package com.wms.inbound.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * ASN 明细行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_asn_line")
public class InboundAsnLine extends BaseEntity {

    private Long asnId;

    private Long skuId;

    private BigDecimal expectedQty;

    private BigDecimal receivedQty;

    private BigDecimal qualifiedQty;

    private BigDecimal putawayQty;

    private String lotNo;
}
