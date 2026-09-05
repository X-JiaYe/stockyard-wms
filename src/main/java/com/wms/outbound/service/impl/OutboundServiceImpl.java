package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.event.DomainEvent;
import com.wms.common.event.DomainEventPublisher;
import com.wms.common.exception.BizException;
import com.wms.common.result.PageResult;
import com.wms.outbound.dto.OrderCreateRequest;
import com.wms.outbound.dto.OrderDetailVO;
import com.wms.outbound.dto.OrderLineItem;
import com.wms.outbound.dto.PickRequest;
import com.wms.outbound.dto.ShipRequest;
import com.wms.outbound.entity.OutboundOrder;
import com.wms.outbound.entity.OutboundOrderLine;
import com.wms.outbound.enums.OutboundStatus;
import com.wms.outbound.mapper.OutboundOrderLineMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.service.OutboundService;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.enums.RefType;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.service.StockService;
import com.wms.system.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 出库实现：订单 → 拣货（扣库存）→ 发运。
 */
@Service
@RequiredArgsConstructor
public class OutboundServiceImpl implements OutboundService {

    private final OutboundOrderMapper orderMapper;
    private final OutboundOrderLineMapper orderLineMapper;
    private final StockService stockService;
    private final AuthContext authContext;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrder createOrder(OrderCreateRequest req) {
        authContext.checkWarehouse(req.getWarehouseId());
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(genOrderNo());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerName(req.getCustomerName());
        order.setRemark(req.getRemark());
        order.setStatus(OutboundStatus.PENDING.getValue());
        orderMapper.insert(order);

        for (OrderLineItem item : req.getLines()) {
            OutboundOrderLine line = new OutboundOrderLine();
            line.setOrderId(order.getId());
            line.setSkuId(item.getSkuId());
            line.setOrderQty(item.getOrderQty());
            line.setPickedQty(BigDecimal.ZERO);
            line.setLotNo(item.getLotNo() == null ? "" : item.getLotNo());
            orderLineMapper.insert(line);
        }
        return order;
    }

    @Override
    public PageResult<OutboundOrder> queryOrders(Long warehouseId, Integer status, long pageNum, long pageSize) {
        warehouseId = authContext.scopeWarehouse(warehouseId);
        LambdaQueryWrapper<OutboundOrder> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(OutboundOrder::getWarehouseId, warehouseId);
        }
        if (status != null) {
            qw.eq(OutboundOrder::getStatus, status);
        }
        qw.orderByDesc(OutboundOrder::getId);
        return PageResult.of(orderMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @Override
    public OrderDetailVO getOrder(Long id) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        authContext.checkWarehouse(order.getWarehouseId());
        List<OutboundOrderLine> lines = orderLineMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderLine>()
                        .eq(OutboundOrderLine::getOrderId, id)
                        .orderByAsc(OutboundOrderLine::getId));
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrder(order);
        vo.setLines(lines);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrderLine pick(PickRequest req) {
        OutboundOrderLine line = orderLineMapper.selectForUpdateById(req.getOrderLineId());
        if (line == null) {
            throw new BizException("订单明细行不存在");
        }
        BigDecimal qty = req.getQty();
        if (line.getPickedQty().add(qty).compareTo(line.getOrderQty()) > 0) {
            throw new BizException("拣货数量超出订购量，可拣 " +
                    line.getOrderQty().subtract(line.getPickedQty()));
        }
        OutboundOrder order = orderMapper.selectById(line.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }
        authContext.checkWarehouse(order.getWarehouseId());

        // 扣库存（先记账后聚合，与订单更新同事务；库存不足由 StockService 抛出）
        StockChangeRequest change = new StockChangeRequest();
        change.setWarehouseId(order.getWarehouseId());
        change.setSkuId(line.getSkuId());
        change.setLotNo(line.getLotNo() == null ? "" : line.getLotNo());
        change.setLocationId(req.getLocationId());
        change.setQuantity(qty.negate());
        change.setDirection(StockDirection.OUT.getValue());
        change.setRefType(RefType.PICK.getValue());
        change.setRefNo(order.getOrderNo());
        change.setRefLineId(line.getId());
        stockService.changeStock(change);

        line.setPickedQty(line.getPickedQty().add(qty));
        orderLineMapper.updateById(line);

        refreshOrderStatus(line.getOrderId());
        return line;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrder ship(ShipRequest req) {
        OutboundOrder order = orderMapper.selectById(req.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }
        authContext.checkWarehouse(order.getWarehouseId());
        List<OutboundOrderLine> lines = orderLineMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderLine>().eq(OutboundOrderLine::getOrderId, order.getId()));
        boolean allPicked = lines.stream()
                .allMatch(l -> l.getPickedQty().compareTo(l.getOrderQty()) >= 0);
        if (!allPicked) {
            throw new BizException("存在未拣完的明细，不能发运");
        }
        OutboundOrder update = new OutboundOrder();
        update.setId(order.getId());
        update.setStatus(OutboundStatus.SHIPPED.getValue());
        orderMapper.updateById(update);
        order.setStatus(OutboundStatus.SHIPPED.getValue());

        // 发运完成 → 领域事件（事务提交后投递 MQ，通知 ERP 出库完成）
        domainEventPublisher.publish(DomainEvent.of("outbound.shipped", order.getWarehouseId(), order.getOrderNo(),
                Map.of("orderId", order.getId())));
        return order;
    }

    /** 刷新订单状态：全部拣完→PICKED，部分→PICKING。 */
    private void refreshOrderStatus(Long orderId) {
        List<OutboundOrderLine> lines = orderLineMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderLine>().eq(OutboundOrderLine::getOrderId, orderId));
        boolean anyPicked = lines.stream()
                .anyMatch(l -> l.getPickedQty().compareTo(BigDecimal.ZERO) > 0);
        boolean allPicked = lines.stream()
                .allMatch(l -> l.getPickedQty().compareTo(l.getOrderQty()) >= 0);

        int status = allPicked ? OutboundStatus.PICKED.getValue()
                : anyPicked ? OutboundStatus.PICKING.getValue()
                : OutboundStatus.PENDING.getValue();
        OutboundOrder update = new OutboundOrder();
        update.setId(orderId);
        update.setStatus(status);
        orderMapper.updateById(update);
    }

    private String genOrderNo() {
        return "OUT" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
