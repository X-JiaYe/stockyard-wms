package com.wms.outbound.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出库订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity {

    private String orderNo;

    private Long warehouseId;

    private String customerName;

    /** 状态：10待拣货 20拣货中 30已拣货 40已发运 90已取消 */
    private Integer status;

    private String remark;
}
