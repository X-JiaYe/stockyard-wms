-- =====================================================================
-- V5__partial_unique_indexes.sql  逻辑删除与唯一约束并存
--  问题：带 deleted 逻辑删除的业务表若沿用普通 UNIQUE 约束，
--        软删除后无法重建同 code / username / 单号（唯一键被已删行占用）。
--  方案：改为「部分唯一索引」WHERE deleted = 0，仅约束未删除的活跃行，
--        已删行不再参与唯一性判定，可被同值新行复用。
--  说明：stock_balance 是不可变/不可逻辑删除的聚合快照，保留原 UNIQUE 约束。
-- =====================================================================

-- 仓库：code 全局唯一（活跃行）
ALTER TABLE warehouse DROP CONSTRAINT uk_warehouse_code;
CREATE UNIQUE INDEX uk_warehouse_code ON warehouse (code) WHERE deleted = 0;

-- 库区：同一仓库内 code 唯一（活跃行）
ALTER TABLE zone DROP CONSTRAINT uk_zone_code;
CREATE UNIQUE INDEX uk_zone_code ON zone (warehouse_id, code) WHERE deleted = 0;

-- 货位：同一仓库内 code 唯一（活跃行）
ALTER TABLE location DROP CONSTRAINT uk_location_code;
CREATE UNIQUE INDEX uk_location_code ON location (warehouse_id, code) WHERE deleted = 0;

-- 物料：code 全局唯一（活跃行）
ALTER TABLE sku DROP CONSTRAINT uk_sku_code;
CREATE UNIQUE INDEX uk_sku_code ON sku (code) WHERE deleted = 0;

-- 系统用户：username 全局唯一（活跃行）
ALTER TABLE sys_user DROP CONSTRAINT uk_sys_user_username;
CREATE UNIQUE INDEX uk_sys_user_username ON sys_user (username) WHERE deleted = 0;

-- 入库单：asn_no 全局唯一（活跃行）
ALTER TABLE inbound_asn DROP CONSTRAINT uk_inbound_asn_no;
CREATE UNIQUE INDEX uk_inbound_asn_no ON inbound_asn (asn_no) WHERE deleted = 0;

-- 出库单：order_no 全局唯一（活跃行）
ALTER TABLE outbound_order DROP CONSTRAINT uk_outbound_order_no;
CREATE UNIQUE INDEX uk_outbound_order_no ON outbound_order (order_no) WHERE deleted = 0;
