package com.wms.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.exception.BizException;
import com.wms.common.result.PageResult;
import com.wms.stock.dto.RecalculateResult;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.dto.StockCountRequest;
import com.wms.stock.dto.StockMoveRequest;
import com.wms.stock.entity.StockBalance;
import com.wms.stock.entity.StockLedger;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.mapper.StockBalanceMapper;
import com.wms.stock.mapper.StockLedgerMapper;
import com.wms.stock.service.StockService;
import com.wms.system.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存中枢实现。
 * 核心约定：批次 lot_no 统一用空串表示「无批次」；库存必须落到具体货位。
 * 并发正确性：事务内先行锁结存行（不存在则占位插入再锁），写 ledger，再更新 balance。
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockLedgerMapper stockLedgerMapper;
    private final StockBalanceMapper stockBalanceMapper;
    private final AuthContext authContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockBalance changeStock(StockChangeRequest req) {
        authContext.checkWarehouse(req.getWarehouseId());
        BigDecimal delta = req.getQuantity();
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new BizException("变动数量不能为 0");
        }
        boolean isIn = delta.compareTo(BigDecimal.ZERO) > 0;
        int direction = req.getDirection();
        if ((isIn && direction != StockDirection.IN.getValue())
                || (!isIn && direction != StockDirection.OUT.getValue())) {
            throw new BizException("变动方向与数量符号不一致");
        }
        String lotNo = normLot(req.getLotNo());
        Long whId = req.getWarehouseId();
        Long skuId = req.getSkuId();
        Long locId = req.getLocationId();

        // 1. 锁定结存行（首次记账先占位插入，再锁）
        StockBalance bal = lockBalance(whId, skuId, lotNo, locId);

        // 2. 计算新结存，防超卖
        BigDecimal newQty = bal.getQuantity().add(delta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("库存不足：当前结存 " + bal.getQuantity() + "，本次变动 " + delta);
        }

        // 3. 先写流水账（不可变，铁律）
        StockLedger ledger = new StockLedger();
        ledger.setWarehouseId(whId);
        ledger.setSkuId(skuId);
        ledger.setLotNo(lotNo);
        ledger.setLocationId(locId);
        ledger.setQuantity(delta);
        ledger.setBalance(newQty);
        ledger.setDirection(req.getDirection());
        ledger.setRefType(req.getRefType());
        ledger.setRefNo(req.getRefNo());
        ledger.setRefLineId(req.getRefLineId());
        ledger.setCreatedAt(LocalDateTime.now());
        ledger.setCreatedBy(req.getCreatedBy());
        stockLedgerMapper.insert(ledger);

        // 4. 同步聚合到 balance 快照
        stockBalanceMapper.updateQuantity(bal.getId(), newQty);
        bal.setQuantity(newQty);
        return bal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(StockMoveRequest req) {
        authContext.checkWarehouse(req.getWarehouseId());
        String lotNo = normLot(req.getLotNo());
        // 出源货位
        StockChangeRequest out = new StockChangeRequest();
        out.setWarehouseId(req.getWarehouseId());
        out.setSkuId(req.getSkuId());
        out.setLotNo(lotNo);
        out.setLocationId(req.getFromLocationId());
        out.setQuantity(req.getQuantity().negate());
        out.setDirection(StockDirection.OUT.getValue());
        out.setRefType(com.wms.stock.enums.RefType.MOVE.getValue());
        out.setRefNo(req.getRefNo());
        changeStock(out);

        // 入目标货位
        StockChangeRequest in = new StockChangeRequest();
        in.setWarehouseId(req.getWarehouseId());
        in.setSkuId(req.getSkuId());
        in.setLotNo(lotNo);
        in.setLocationId(req.getToLocationId());
        in.setQuantity(req.getQuantity());
        in.setDirection(StockDirection.IN.getValue());
        in.setRefType(com.wms.stock.enums.RefType.MOVE.getValue());
        in.setRefNo(req.getRefNo());
        changeStock(in);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockBalance count(StockCountRequest req) {
        authContext.checkWarehouse(req.getWarehouseId());
        String lotNo = normLot(req.getLotNo());
        // 锁定读当前结存，消除盘点与出入库并发下的 TOCTOU 竞态
        StockBalance locked = lockBalance(req.getWarehouseId(), req.getSkuId(), lotNo, req.getLocationId());
        BigDecimal diff = req.getActualQty().subtract(locked.getQuantity());
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return locked;
        }
        StockChangeRequest c = new StockChangeRequest();
        c.setWarehouseId(req.getWarehouseId());
        c.setSkuId(req.getSkuId());
        c.setLotNo(lotNo);
        c.setLocationId(req.getLocationId());
        c.setQuantity(diff);
        c.setDirection(diff.compareTo(BigDecimal.ZERO) > 0
                ? StockDirection.IN.getValue() : StockDirection.OUT.getValue());
        c.setRefType(com.wms.stock.enums.RefType.CYCLE_COUNT.getValue());
        c.setRefNo(req.getRefNo());
        return changeStock(c);
    }

    @Override
    public PageResult<StockBalance> queryBalances(Long warehouseId, Long skuId, String lotNo,
                                                  Long locationId, long pageNum, long pageSize) {
        warehouseId = authContext.scopeWarehouse(warehouseId);
        LambdaQueryWrapper<StockBalance> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(StockBalance::getWarehouseId, warehouseId);
        }
        if (skuId != null) {
            qw.eq(StockBalance::getSkuId, skuId);
        }
        if (lotNo != null) {
            qw.eq(StockBalance::getLotNo, lotNo);
        }
        if (locationId != null) {
            qw.eq(StockBalance::getLocationId, locationId);
        }
        qw.orderByDesc(StockBalance::getId);
        return PageResult.of(stockBalanceMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @Override
    public PageResult<StockLedger> queryLedgers(Long warehouseId, Long skuId, String refType,
                                                String refNo, long pageNum, long pageSize) {
        warehouseId = authContext.scopeWarehouse(warehouseId);
        LambdaQueryWrapper<StockLedger> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(StockLedger::getWarehouseId, warehouseId);
        }
        if (skuId != null) {
            qw.eq(StockLedger::getSkuId, skuId);
        }
        if (refType != null) {
            qw.eq(StockLedger::getRefType, refType);
        }
        if (refNo != null) {
            qw.eq(StockLedger::getRefNo, refNo);
        }
        qw.orderByDesc(StockLedger::getId);
        return PageResult.of(stockLedgerMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @Override
    public RecalculateResult recalculate(Long warehouseId, Long skuId, String lotNo, Long locationId) {
        authContext.checkWarehouse(warehouseId);
        String l = normLot(lotNo);
        BigDecimal ledgerSum = stockLedgerMapper.sumQuantity(warehouseId, skuId, l, locationId);
        StockBalance bal = stockBalanceMapper.selectOne(new LambdaQueryWrapper<StockBalance>()
                .eq(StockBalance::getWarehouseId, warehouseId)
                .eq(StockBalance::getSkuId, skuId)
                .eq(StockBalance::getLotNo, l)
                .eq(StockBalance::getLocationId, locationId));
        BigDecimal balanceQty = bal == null ? BigDecimal.ZERO : bal.getQuantity();
        return RecalculateResult.of(ledgerSum, balanceQty);
    }

    /** 锁定结存行；不存在则占位插入后再锁。 */
    private StockBalance lockBalance(Long warehouseId, Long skuId, String lotNo, Long locationId) {
        StockBalance bal = stockBalanceMapper.selectForUpdate(warehouseId, skuId, lotNo, locationId);
        if (bal != null) {
            return bal;
        }
        StockBalance seed = new StockBalance();
        seed.setId(IdWorker.getId());
        seed.setWarehouseId(warehouseId);
        seed.setSkuId(skuId);
        seed.setLotNo(lotNo);
        seed.setLocationId(locationId);
        stockBalanceMapper.insertIfAbsent(seed);
        bal = stockBalanceMapper.selectForUpdate(warehouseId, skuId, lotNo, locationId);
        if (bal == null) {
            throw new BizException("结存初始化失败，请重试");
        }
        return bal;
    }

    private String normLot(String lotNo) {
        return lotNo == null ? "" : lotNo;
    }
}
