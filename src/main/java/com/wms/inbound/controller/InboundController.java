package com.wms.inbound.controller;

import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.inbound.dto.AsnCreateRequest;
import com.wms.inbound.dto.AsnDetailVO;
import com.wms.inbound.dto.PutawayRequest;
import com.wms.inbound.dto.ReceiveRequest;
import com.wms.inbound.entity.InboundAsn;
import com.wms.inbound.entity.InboundAsnLine;
import com.wms.inbound.service.InboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 入库接口：ASN → 收货 → 上架。
 */
@RestController
@RequestMapping("/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @PostMapping("/asns")
    public Result<InboundAsn> createAsn(@Valid @RequestBody AsnCreateRequest req) {
        return Result.ok(inboundService.createAsn(req));
    }

    @GetMapping("/asns")
    public Result<PageResult<InboundAsn>> page(@RequestParam(required = false) Long warehouseId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(inboundService.queryAsns(warehouseId, status, pageNum, pageSize));
    }

    @GetMapping("/asns/{id}")
    public Result<AsnDetailVO> detail(@PathVariable Long id) {
        return Result.ok(inboundService.getAsn(id));
    }

    @PostMapping("/receive")
    public Result<InboundAsnLine> receive(@Valid @RequestBody ReceiveRequest req) {
        return Result.ok(inboundService.receive(req));
    }

    @PostMapping("/putaway")
    public Result<InboundAsnLine> putaway(@Valid @RequestBody PutawayRequest req) {
        return Result.ok(inboundService.putaway(req));
    }
}
