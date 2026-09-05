-- =====================================================================
-- V7__rbac.sql  RBAC 角色/权限体系
-- 引入显式角色模型，移除「warehouse_id = NULL 即管理员」的隐式约定：
--   管理员判定改由用户所拥有的 ROLE_ADMIN 角色决定，避免 fail-open。
-- =====================================================================

-- ---------------------------------------------------------------
-- 角色
-- ---------------------------------------------------------------
CREATE TABLE sys_role (
    id         BIGINT       NOT NULL,
    code       VARCHAR(32)  NOT NULL,
    name       VARCHAR(64)  NOT NULL,
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_role PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);
COMMENT ON TABLE  sys_role IS '角色';
COMMENT ON COLUMN sys_role.code IS '角色编码:ADMIN/OPERATOR';
COMMENT ON COLUMN sys_role.status IS '状态:1启用 0停用';

-- ---------------------------------------------------------------
-- 权限
-- ---------------------------------------------------------------
CREATE TABLE sys_permission (
    id         BIGINT       NOT NULL,
    code       VARCHAR(64)  NOT NULL,
    name       VARCHAR(64)  NOT NULL,
    type       SMALLINT     NOT NULL DEFAULT 3,
    parent_id  BIGINT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_permission PRIMARY KEY (id),
    CONSTRAINT uk_sys_permission_code UNIQUE (code)
);
COMMENT ON TABLE  sys_permission IS '权限';
COMMENT ON COLUMN sys_permission.type IS '类型:1目录 2菜单 3按钮/接口';

-- ---------------------------------------------------------------
-- 用户-角色 关联
-- ---------------------------------------------------------------
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_sys_user_role PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE sys_user_role IS '用户-角色关联';

-- ---------------------------------------------------------------
-- 角色-权限 关联
-- ---------------------------------------------------------------
CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_sys_role_permission PRIMARY KEY (role_id, permission_id)
);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联';

-- ---------------------------------------------------------------
-- 种子数据（固定 ID，与雪花 ID 无冲突）
-- ---------------------------------------------------------------
INSERT INTO sys_role (id, code, name, status) VALUES
  (1, 'ADMIN',    '管理员',     1),
  (2, 'OPERATOR', '仓库操作员', 1);

INSERT INTO sys_permission (id, code, name, type, parent_id) VALUES
  (100, 'dashboard:view',  '看板',     2, NULL),
  (101, 'base:manage',     '基础数据', 2, NULL),
  (102, 'inbound:manage',  '入库管理', 2, NULL),
  (103, 'outbound:manage', '出库管理', 2, NULL),
  (104, 'stock:manage',    '库存管理', 2, NULL),
  (105, 'system:manage',   '系统管理', 2, NULL);

-- ADMIN 拥有全部权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
  (1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105);

-- OPERATOR 拥有看板/入库/出库/库存
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
  (2, 100), (2, 102), (2, 103), (2, 104);
