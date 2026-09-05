-- =====================================================================
-- V8__ledger_idempotency.sql  流水账幂等防重
--  问题：stock_ledger 无防重约束，同一单据（ERP 消息重投 / 重复推送）
--        会对同一库存维度重复入账，导致库存双记。
--  方案：加「幂等唯一索引」—— 同一 (仓库, 物料, 批次, 货位, 单据类型, 单据号, 单据行)
--        只允许入账一次。可空维度用 COALESCE 归一，避免 NULL 不参与判等导致约束失效。
--  说明：move 的一单两行（出源货位 / 入目标货位）location_id 不同，互不影响。
-- =====================================================================

CREATE UNIQUE INDEX uk_stock_ledger_idem ON stock_ledger (
    warehouse_id,
    sku_id,
    COALESCE(lot_no, ''),
    COALESCE(location_id, 0),
    ref_type,
    ref_no,
    COALESCE(ref_line_id, 0)
);
