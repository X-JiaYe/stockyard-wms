package com.wms.outbound.dto;

import com.wms.outbound.entity.OutboundOrder;
import com.wms.outbound.entity.OutboundOrderLine;
import lombok.Data;

import java.util.List;

/**
 * 出库订单详情（单头 + 明细行）。
 */
@Data
public class OrderDetailVO {

    private OutboundOrder order;

    private List<OutboundOrderLine> lines;
}
