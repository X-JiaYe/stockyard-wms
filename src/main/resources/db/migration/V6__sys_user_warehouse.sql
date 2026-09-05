-- =====================================================================
-- V6__sys_user_warehouse.sql  用户绑定仓库（多仓数据隔离）
--  模型：单仓绑定 + 管理员全局。
--   sys_user.warehouse_id 为 NULL 表示管理员（可访问所有仓、管理主数据/用户），
--   非 NULL 表示该用户只允许操作所属仓库的数据。
-- =====================================================================

ALTER TABLE sys_user ADD COLUMN warehouse_id BIGINT;

COMMENT ON COLUMN sys_user.warehouse_id IS '所属仓库(NULL=管理员全局)';

CREATE INDEX idx_sys_user_warehouse ON sys_user (warehouse_id) WHERE deleted = 0;
