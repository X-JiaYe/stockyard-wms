-- =====================================================================
-- V4__outbound.sql  M4 出库：订单 → 拣货 → 发运
--  拣货流水复用 stock_ledger（ref_type=PICK），不再单独建拣货表。
-- =====================================================================

CREATE TABLE outbound_order (
    id            BIGINT        NOT NULL,
    order_no      VARCHAR(64)   NOT NULL,
    warehouse_id  BIGINT        NOT NULL,
    customer_name VARCHAR(128),
    status        SMALLINT      NOT NULL DEFAULT 10,
    remark        VARCHAR(256),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_outbound_order PRIMARY KEY (id),
    CONSTRAINT uk_outbound_order_no UNIQUE (order_no)
);
COMMENT ON TABLE  outbound_order IS '出库订单';
COMMENT ON COLUMN outbound_order.status IS '状态:10待拣货 20拣货中 30已拣货 40已发运 90已取消';

CREATE TABLE outbound_order_line (
    id         BIGINT         NOT NULL,
    order_id   BIGINT         NOT NULL,
    sku_id     BIGINT         NOT NULL,
    order_qty  NUMERIC(18,4)  NOT NULL,
    picked_qty NUMERIC(18,4)  NOT NULL DEFAULT 0,
    lot_no     VARCHAR(64)    NOT NULL DEFAULT '',
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_outbound_order_line PRIMARY KEY (id)
);
CREATE INDEX idx_outbound_line_order ON outbound_order_line (order_id);
COMMENT ON TABLE  outbound_order_line IS '出库订单明细行';
COMMENT ON COLUMN outbound_order_line.order_qty  IS '订购数量';
COMMENT ON COLUMN outbound_order_line.picked_qty IS '已拣货数量';
