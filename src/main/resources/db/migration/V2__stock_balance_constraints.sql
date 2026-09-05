-- =====================================================================
-- V2__stock_balance_constraints.sql  M2 库存中枢：加固并发正确性
--  1) location_id 必填（默认 0 表示未绑定货位），
--     使唯一约束 uk_stock_balance(warehouse_id, sku_id, lot_no, location_id)
--     在库存变动时完整生效（NULL 会破坏 UNIQUE 约束）。
--  2) 结存不允许为负：数据库兜底，防超卖。
--  3) 流水变动量不允许为 0：防脏账。
-- =====================================================================

ALTER TABLE stock_balance ALTER COLUMN location_id SET NOT NULL;
ALTER TABLE stock_balance ALTER COLUMN location_id SET DEFAULT 0;

ALTER TABLE stock_balance ADD CONSTRAINT ck_stock_balance_qty CHECK (quantity >= 0);

ALTER TABLE stock_ledger ADD CONSTRAINT ck_stock_ledger_qty CHECK (quantity <> 0);
