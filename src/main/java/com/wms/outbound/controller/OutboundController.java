package com.wms.outbound.controller;

import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.outbound.dto.OrderCreateRequest;
import com.wms.outbound.dto.OrderDetailVO;
import com.wms.outbound.dto.PickRequest;
import com.wms.outbound.dto.ShipRequest;
import com.wms.outbound.entity.OutboundOrder;
import com.wms.outbound.entity.OutboundOrderLine;
import com.wms.outbound.service.OutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 出库接口：订单 → 拣货 → 发运。
 */
@RestController
@RequestMapping("/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('outbound:manage')")
    public Result<OutboundOrder> createOrder(@Valid @RequestBody OrderCreateRequest req) {
        return Result.ok(outboundService.createOrder(req));
    }

    @GetMapping("/orders")
    public Result<PageResult<OutboundOrder>> page(@RequestParam(required = false) Long warehouseId,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(outboundService.queryOrders(warehouseId, status, pageNum, pageSize));
    }

    @GetMapping("/orders/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(outboundService.getOrder(id));
    }

    @PostMapping("/pick")
    @PreAuthorize("hasAuthority('outbound:manage')")
    public Result<OutboundOrderLine> pick(@Valid @RequestBody PickRequest req) {
        return Result.ok(outboundService.pick(req));
    }

    @PostMapping("/ship")
    @PreAuthorize("hasAuthority('outbound:manage')")
    public Result<OutboundOrder> ship(@Valid @RequestBody ShipRequest req) {
        return Result.ok(outboundService.ship(req));
    }
}
