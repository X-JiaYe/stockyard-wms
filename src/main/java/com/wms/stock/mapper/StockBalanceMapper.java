package com.wms.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.stock.entity.StockBalance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 现存量 Mapper：行锁 + 占位插入 + 更新。
 */
@Mapper
public interface StockBalanceMapper extends BaseMapper<StockBalance> {

    /** 按库存维度行锁读取结存（无记录返回 null）。 */
    @Select("SELECT id, warehouse_id, sku_id, lot_no, location_id, quantity, updated_at " +
            "FROM stock_balance " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} " +
            "  AND lot_no = #{lotNo} AND location_id = #{locationId} " +
            "FOR UPDATE")
    StockBalance selectForUpdate(@Param("warehouseId") Long warehouseId,
                                 @Param("skuId") Long skuId,
                                 @Param("lotNo") String lotNo,
                                 @Param("locationId") Long locationId);

    /** 首次记账时占位插入（已存在则跳过），解决行锁锁不到不存在行的问题。 */
    @Insert("INSERT INTO stock_balance (id, warehouse_id, sku_id, lot_no, location_id, quantity, updated_at) " +
            "VALUES (#{id}, #{warehouseId}, #{skuId}, #{lotNo}, #{locationId}, 0, now()) " +
            "ON CONFLICT (warehouse_id, sku_id, lot_no, location_id) DO NOTHING")
    int insertIfAbsent(StockBalance balance);

    /** 更新结存。 */
    @Update("UPDATE stock_balance SET quantity = #{quantity}, updated_at = now() WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
