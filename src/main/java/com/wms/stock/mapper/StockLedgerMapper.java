package com.wms.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.stock.entity.StockLedger;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 库存流水账 Mapper（只 INSERT + 聚合校验查询）。
 */
@Mapper
public interface StockLedgerMapper extends BaseMapper<StockLedger> {

    /** 按库存维度聚合流水变动量，用于重算校验 balance 快照。 */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM stock_ledger " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} " +
            "  AND lot_no = #{lotNo} AND location_id = #{locationId}")
    BigDecimal sumQuantity(@Param("warehouseId") Long warehouseId,
                           @Param("skuId") Long skuId,
                           @Param("lotNo") String lotNo,
                           @Param("locationId") Long locationId);
}
