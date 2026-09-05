# Stockyard WMS

企业内部仓储管理系统（WMS），**模块化单体**架构，单仓起步、规划多仓。

## 技术栈

- Java 21 · Spring Boot 3.2
- MyBatis-Plus 3.5（BIGINT 雪花主键、逻辑删除）
- PostgreSQL 15（Flyway 迁移）
- Redis 7 / RabbitMQ 3.13（预留）
- Spring Security + JWT 自建认证
- 前端 Vue3（后续）

## 架构：模块化单体 + 包级分层

```
com.wms
├── common      # 公共：统一返回、异常、常量、工具
├── base        # 基础数据：物料/仓库/库区/货位
├── system      # 系统管理：用户/角色/权限
├── stock       # 库存中枢：流水账/现存量/盘点/移动
├── inbound     # 入库：ASN/收货/质检/上架
├── outbound    # 出库：订单/波次/拣货/发运
└── report      # 报表/看板
```

每个业务域内按 `controller / service / mapper / entity / dto` 分层，跨域只走接口契约或事件，禁止直连他域 `service` 实现类。

## 数据库铁律

- 库存相关表一律带 `warehouse_id`（多仓预留）。
- `stock_ledger` 是不可变流水账：只 INSERT，禁 UPDATE/DELETE；任何库存变动**先写账、再聚合** `stock_balance`，后者必须能被 ledger 重算校验。
- 主键统一 BIGINT 雪花 ID；业务表带 `created_at/updated_at/deleted`（逻辑删除）。
- 变更一律走 Flyway（`src/main/resources/db/migration/V{n}__desc.sql`）。

## 快速开始

```bash
# 1. 启动依赖（PostgreSQL / Redis / RabbitMQ）
docker compose up -d

# 2. 启动应用（需 JDK 21 + Maven）
mvn spring-boot:run
```

首次启动 Flyway 自动建表，并写入示例数据（账号 `admin/admin123`、电子元器件 SKU 等）。

## 里程碑

- [x] M0 工程骨架 + 基础设施
- [x] M1 认证 + 基础数据 CRUD
- [x] M2 库存中枢
- [x] M3 入库
- [x] M4 出库
- [ ] M5 集成对接（SAP/金蝶 + WCS）
- [ ] 测试与看板

## License

企业内部项目，暂未开源。
