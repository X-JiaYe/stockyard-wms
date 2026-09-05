-- =====================================================================
-- V1__init.sql  基础表 + 库存中枢
-- 设计要点：
--   1) 所有库存相关表都带 warehouse_id（多仓预留，全局表用 0 表示全局）。
--   2) stock_ledger 是「不可变流水账」，只 INSERT、不 UPDATE/DELETE，
--      是库存的唯一致实来源；stock_balance 是聚合快照，可由 ledger 重算。
--   3) 主键统一 BIGINT 雪花 ID（由 MyBatis-Plus 生成），不用自增。
-- =====================================================================

-- ---------------------------------------------------------------
-- 仓库
-- ---------------------------------------------------------------
CREATE TABLE warehouse (
    id          BIGINT        NOT NULL,
    code        VARCHAR(32)   NOT NULL,
    name        VARCHAR(128)  NOT NULL,
    status      SMALLINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_warehouse PRIMARY KEY (id),
    CONSTRAINT uk_warehouse_code UNIQUE (code)
);
COMMENT ON TABLE  warehouse IS '仓库';
COMMENT ON COLUMN warehouse.status IS '状态:1启用 0停用';
COMMENT ON COLUMN warehouse.deleted IS '逻辑删除:0正常 1已删';

-- ---------------------------------------------------------------
-- 库区
-- ---------------------------------------------------------------
CREATE TABLE zone (
    id           BIGINT        NOT NULL,
    warehouse_id BIGINT        NOT NULL,
    code         VARCHAR(32)   NOT NULL,
    name         VARCHAR(128)  NOT NULL,
    zone_type    SMALLINT      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_zone PRIMARY KEY (id),
    CONSTRAINT uk_zone_code UNIQUE (warehouse_id, code)
);
COMMENT ON TABLE  zone IS '库区';
COMMENT ON COLUMN zone.zone_type IS '类型:1存储区 2暂存区 3退货区 4质检区';

-- ---------------------------------------------------------------
-- 货位
-- ---------------------------------------------------------------
CREATE TABLE location (
    id           BIGINT        NOT NULL,
    warehouse_id BIGINT        NOT NULL,
    zone_id      BIGINT        NOT NULL,
    code         VARCHAR(64)   NOT NULL,
    loc_type     SMALLINT      NOT NULL DEFAULT 1,
    status       SMALLINT      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_location PRIMARY KEY (id),
    CONSTRAINT uk_location_code UNIQUE (warehouse_id, code)
);
COMMENT ON TABLE  location IS '货位';
COMMENT ON COLUMN location.loc_type IS '类型:1拣货位 2存储位 3暂存位';

-- ---------------------------------------------------------------
-- 物料（SKU）
-- ---------------------------------------------------------------
CREATE TABLE sku (
    id           BIGINT        NOT NULL,
    code         VARCHAR(64)   NOT NULL,
    name         VARCHAR(256)  NOT NULL,
    spec         VARCHAR(256),
    unit         VARCHAR(16)   NOT NULL DEFAULT '件',
    barcode      VARCHAR(64),
    track_lot    SMALLINT      NOT NULL DEFAULT 0,
    track_serial SMALLINT      NOT NULL DEFAULT 0,
    status       SMALLINT      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_sku PRIMARY KEY (id),
    CONSTRAINT uk_sku_code UNIQUE (code)
);
COMMENT ON TABLE  sku IS '物料/商品主数据';
COMMENT ON COLUMN sku.track_lot    IS '是否批次管理:0否 1是';
COMMENT ON COLUMN sku.track_serial IS '是否序列号管理:0否 1是';

-- ---------------------------------------------------------------
-- 库存流水账（不可变，只增不改）
-- ---------------------------------------------------------------
CREATE TABLE stock_ledger (
    id           BIGINT         NOT NULL,
    warehouse_id BIGINT         NOT NULL,
    sku_id       BIGINT         NOT NULL,
    lot_no       VARCHAR(64),
    location_id  BIGINT,
    quantity     NUMERIC(18,4)  NOT NULL,
    balance      NUMERIC(18,4)  NOT NULL,
    direction    SMALLINT       NOT NULL,
    ref_type     VARCHAR(32)    NOT NULL,
    ref_no       VARCHAR(64)    NOT NULL,
    ref_line_id  BIGINT,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    CONSTRAINT pk_stock_ledger PRIMARY KEY (id)
);
COMMENT ON TABLE  stock_ledger IS '库存流水账（不可变，只增不改）';
COMMENT ON COLUMN stock_ledger.quantity   IS '变动数量(正=入,负=出)';
COMMENT ON COLUMN stock_ledger.balance    IS '变动后结存';
COMMENT ON COLUMN stock_ledger.direction  IS '方向:1入库 2出库';
COMMENT ON COLUMN stock_ledger.ref_type   IS '来源单据类型:ASN/RECEIVE/PUTAWAY/PICK/ADJUST/CYCLE_COUNT/MOVE';
COMMENT ON COLUMN stock_ledger.ref_no     IS '来源单据号';

CREATE INDEX idx_stock_ledger_sku
    ON stock_ledger (warehouse_id, sku_id, lot_no, created_at);

-- ---------------------------------------------------------------
-- 现存量（聚合快照，由 ledger 重算校验）
-- ---------------------------------------------------------------
CREATE TABLE stock_balance (
    id           BIGINT         NOT NULL,
    warehouse_id BIGINT         NOT NULL,
    sku_id       BIGINT         NOT NULL,
    lot_no       VARCHAR(64)    NOT NULL DEFAULT '',
    location_id  BIGINT,
    quantity     NUMERIC(18,4)  NOT NULL DEFAULT 0,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_stock_balance PRIMARY KEY (id),
    CONSTRAINT uk_stock_balance UNIQUE (warehouse_id, sku_id, lot_no, location_id)
);
COMMENT ON TABLE stock_balance IS '现存量聚合快照';

-- ---------------------------------------------------------------
-- 系统用户（RBAC 在 M1 展开）
-- ---------------------------------------------------------------
CREATE TABLE sys_user (
    id         BIGINT        NOT NULL,
    username   VARCHAR(64)   NOT NULL,
    password   VARCHAR(128)  NOT NULL,
    nickname   VARCHAR(64),
    status     SMALLINT      NOT NULL DEFAULT 1,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_user PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);
COMMENT ON TABLE  sys_user IS '系统用户';
COMMENT ON COLUMN sys_user.password IS '密码(加密存储)';
COMMENT ON COLUMN sys_user.status   IS '状态:1启用 0停用';
