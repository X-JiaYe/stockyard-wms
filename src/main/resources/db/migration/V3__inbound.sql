-- =====================================================================
-- V3__inbound.sql  M3 入库：ASN 到货通知 → 收货 → 上架
--  上架流水复用 stock_ledger（ref_type=PUTAWAY），不再单独建上架表。
-- =====================================================================

CREATE TABLE inbound_asn (
    id            BIGINT        NOT NULL,
    asn_no        VARCHAR(64)   NOT NULL,
    warehouse_id  BIGINT        NOT NULL,
    supplier_name VARCHAR(128),
    status        SMALLINT      NOT NULL DEFAULT 10,
    remark        VARCHAR(256),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_inbound_asn PRIMARY KEY (id),
    CONSTRAINT uk_inbound_asn_no UNIQUE (asn_no)
);
COMMENT ON TABLE  inbound_asn IS '入库到货通知单(ASN)';
COMMENT ON COLUMN inbound_asn.status IS '状态:10待收货 20收货中 30已收货 40已完成 90已取消';

CREATE TABLE inbound_asn_line (
    id            BIGINT         NOT NULL,
    asn_id        BIGINT         NOT NULL,
    sku_id        BIGINT         NOT NULL,
    expected_qty  NUMERIC(18,4)  NOT NULL,
    received_qty  NUMERIC(18,4)  NOT NULL DEFAULT 0,
    qualified_qty NUMERIC(18,4)  NOT NULL DEFAULT 0,
    putaway_qty   NUMERIC(18,4)  NOT NULL DEFAULT 0,
    lot_no        VARCHAR(64)    NOT NULL DEFAULT '',
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_inbound_asn_line PRIMARY KEY (id)
);
CREATE INDEX idx_asn_line_asn ON inbound_asn_line (asn_id);
COMMENT ON TABLE  inbound_asn_line IS 'ASN明细行';
COMMENT ON COLUMN inbound_asn_line.received_qty  IS '已收货数量';
COMMENT ON COLUMN inbound_asn_line.qualified_qty IS '质检合格数量';
COMMENT ON COLUMN inbound_asn_line.putaway_qty   IS '已上架数量';

CREATE TABLE inbound_receive (
    id          BIGINT         NOT NULL,
    asn_id      BIGINT         NOT NULL,
    asn_line_id BIGINT         NOT NULL,
    sku_id      BIGINT         NOT NULL,
    receive_qty NUMERIC(18,4)  NOT NULL,
    qc_result   SMALLINT       NOT NULL DEFAULT 1,
    remark      VARCHAR(256),
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_inbound_receive PRIMARY KEY (id)
);
CREATE INDEX idx_receive_asn_line ON inbound_receive (asn_line_id);
COMMENT ON TABLE  inbound_receive IS '收货明细(含质检结论)';
COMMENT ON COLUMN inbound_receive.qc_result IS '质检结论:1合格 2不合格';
