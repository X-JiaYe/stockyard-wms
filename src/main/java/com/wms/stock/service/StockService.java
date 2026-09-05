package com.wms.stock.service;

import com.wms.common.result.PageResult;
import com.wms.stock.dto.RecalculateResult;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.dto.StockCountRequest;
import com.wms.stock.dto.StockMoveRequest;
import com.wms.stock.entity.StockBalance;
import com.wms.stock.entity.StockLedger;

/**
 * 库存中枢 Service。
 */
public interface StockService {

    /** 核心：库存变动（quantity 带符号）。先写 ledger，再聚合 balance。 */
    StockBalance changeStock(StockChangeRequest req);

    /** 移动：出源货位 + 入目标货位。 */
    void move(StockMoveRequest req);

    /** 盘点：按实盘数生成差异调整。 */
    StockBalance count(StockCountRequest req);

    /** 现存量分页查询。 */
    PageResult<StockBalance> queryBalances(Long warehouseId, Long skuId, String lotNo,
                                           Long locationId, long pageNum, long pageSize);

    /** 流水账分页查询。 */
    PageResult<StockLedger> queryLedgers(Long warehouseId, Long skuId, String refType,
                                         String refNo, long pageNum, long pageSize);

    /** 重算校验：ledger 聚合结存 vs balance 快照。 */
    RecalculateResult recalculate(Long warehouseId, Long skuId, String lotNo, Long locationId);
}
