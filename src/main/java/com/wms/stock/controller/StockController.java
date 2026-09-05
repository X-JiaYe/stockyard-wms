package com.wms.stock.controller;

import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.stock.dto.RecalculateResult;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.dto.StockCountRequest;
import com.wms.stock.dto.StockMoveRequest;
import com.wms.stock.dto.StockOpRequest;
import com.wms.stock.entity.StockBalance;
import com.wms.stock.entity.StockLedger;
import com.wms.stock.enums.RefType;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 库存中枢接口。
 */
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/inbound")
    @PreAuthorize("hasAuthority('stock:manage')")
    public Result<StockBalance> inbound(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.changeStock(toChange(req, req.getQuantity(),
                StockDirection.IN.getValue(), req.getRefType() == null ? RefType.RECEIVE.getValue() : req.getRefType())));
    }

    @PostMapping("/outbound")
    @PreAuthorize("hasAuthority('stock:manage')")
    public Result<StockBalance> outbound(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.changeStock(toChange(req, req.getQuantity().negate(),
                StockDirection.OUT.getValue(), req.getRefType() == null ? RefType.PICK.getValue() : req.getRefType())));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('stock:manage')")
    public Result<StockBalance> adjust(@Valid @RequestBody StockChangeRequest req) {
        return Result.ok(stockService.changeStock(req));
    }

    @PostMapping("/move")
    @PreAuthorize("hasAuthority('stock:manage')")
    public Result<Void> move(@Valid @RequestBody StockMoveRequest req) {
        stockService.move(req);
        return Result.ok();
    }

    @PostMapping("/count")
    @PreAuthorize("hasAuthority('stock:manage')")
    public Result<StockBalance> count(@Valid @RequestBody StockCountRequest req) {
        return Result.ok(stockService.count(req));
    }

    @GetMapping("/balances")
    public Result<PageResult<StockBalance>> balances(@RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) Long skuId,
                                                     @RequestParam(required = false) String lotNo,
                                                     @RequestParam(required = false) Long locationId,
                                                     @RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(stockService.queryBalances(warehouseId, skuId, lotNo, locationId, pageNum, pageSize));
    }

    @GetMapping("/ledgers")
    public Result<PageResult<StockLedger>> ledgers(@RequestParam(required = false) Long warehouseId,
                                                   @RequestParam(required = false) Long skuId,
                                                   @RequestParam(required = false) String refType,
                                                   @RequestParam(required = false) String refNo,
                                                   @RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(stockService.queryLedgers(warehouseId, skuId, refType, refNo, pageNum, pageSize));
    }

    @GetMapping("/recalculate")
    public Result<RecalculateResult> recalculate(@RequestParam Long warehouseId,
                                                 @RequestParam Long skuId,
                                                 @RequestParam(required = false) String lotNo,
                                                 @RequestParam Long locationId) {
        return Result.ok(stockService.recalculate(warehouseId, skuId, lotNo, locationId));
    }

    private StockChangeRequest toChange(StockOpRequest req, java.math.BigDecimal qty, int direction, String refType) {
        StockChangeRequest c = new StockChangeRequest();
        c.setWarehouseId(req.getWarehouseId());
        c.setSkuId(req.getSkuId());
        c.setLotNo(req.getLotNo());
        c.setLocationId(req.getLocationId());
        c.setQuantity(qty);
        c.setDirection(direction);
        c.setRefType(refType);
        c.setRefNo(req.getRefNo());
        c.setRefLineId(req.getRefLineId());
        return c;
    }
}
