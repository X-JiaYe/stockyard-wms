package com.wms.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 现存量聚合快照（由 stock_ledger 重算校验）。
 * 注意：不继承 BaseEntity——快照表不带 deleted/created_at。
 */
@Data
@TableName("stock_balance")
public class StockBalance {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long warehouseId;

    private Long skuId;

    private String lotNo;

    private Long locationId;

    private BigDecimal quantity;

    private LocalDateTime updatedAt;
}
