package com.wms.outbound.service;

import com.wms.common.result.PageResult;
import com.wms.outbound.dto.OrderCreateRequest;
import com.wms.outbound.dto.OrderDetailVO;
import com.wms.outbound.dto.PickRequest;
import com.wms.outbound.dto.ShipRequest;
import com.wms.outbound.entity.OutboundOrder;
import com.wms.outbound.entity.OutboundOrderLine;

/**
 * 出库 Service：订单 → 拣货 → 发运。
 */
public interface OutboundService {

    OutboundOrder createOrder(OrderCreateRequest req);

    PageResult<OutboundOrder> queryOrders(Long warehouseId, Integer status, long pageNum, long pageSize);

    OrderDetailVO getOrder(Long id);

    OutboundOrderLine pick(PickRequest req);

    OutboundOrder ship(ShipRequest req);
}
