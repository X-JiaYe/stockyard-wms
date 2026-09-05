package com.wms.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水账（不可变，只 INSERT，禁止 UPDATE/DELETE）。
 * 注意：不继承 BaseEntity——不可变表不带 deleted/updated_at。
 */
@Data
@TableName("stock_ledger")
public class StockLedger {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long warehouseId;

    private Long skuId;

    private String lotNo;

    private Long locationId;

    /** 变动数量（正=入，负=出） */
    private BigDecimal quantity;

    /** 变动后结存 */
    private BigDecimal balance;

    /** 方向：1入库 2出库 */
    private Integer direction;

    private String refType;

    private String refNo;

    private Long refLineId;

    private LocalDateTime createdAt;

    private Long createdBy;
}
