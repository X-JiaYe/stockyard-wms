package com.wms.inbound.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入库到货通知单（ASN）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_asn")
public class InboundAsn extends BaseEntity {

    private String asnNo;

    private Long warehouseId;

    private String supplierName;

    /** 状态：10待收货 20收货中 30已收货 40已完成 90已取消 */
    private Integer status;

    private String remark;
}
