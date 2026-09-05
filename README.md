# Stockyard WMS

面向企业内部的仓储管理系统（WMS），**模块化单体**架构，单仓起步、架构上规划多仓扩展。

> 该项目聚焦后端核心能力：库存账实一致、并发正确性、多仓数据隔离、权限模型与架构边界。

## 技术栈

| 层 | 选型 | 说明 |
|---|---|---|
| 语言 | Java 21 | LTS |
| 框架 | Spring Boot 3.2 | 主框架 |
| ORM | MyBatis-Plus 3.5 | 雪花主键、逻辑删除、Lambda 条件 |
| 数据库 | PostgreSQL 15 | Flyway 版本化迁移 |
| 安全 | Spring Security + JWT | 无状态认证 + RBAC + 方法级权限 |
| 架构守卫 | ArchUnit | 固化模块边界 |
| 测试 | JUnit 5 + Mockito + MockMvc | 单元 / 集成 / 架构三层 |

> Redis / RabbitMQ 已在 `docker-compose.yml` 预留，代码尚未接入（按需启用，不强行堆中间件）。

## 架构

```
                    ┌──────────────────────────────────────┐
                    │        HTTP /api/**（无状态 JWT）      │
                    └──────────────────┬───────────────────┘
                                       │
        ┌──────────────────────────────▼──────────────────────────────┐
        │  Spring Security                                           │
        │   JwtAuthenticationFilter ──► @PreAuthorize 方法级权限      │
        │   （解析注入 LoginUser）        （base/system/stock:manage…） │
        └──────────────────────────────┬─────────────────────────────┘
                                       │
   ┌───────────┬───────────┬───────────┼───────────┬───────────┐
   ▼           ▼           ▼           ▼           ▼           ▼
 base        system      inbound     outbound     stock      report
 主数据      用户/RBAC    ASN→收货    订单→发运    库存中枢    看板(待建)
   │           │           │           │           ▲           │
   └───────────┴───────────┴─────┬─────┴───────────┘           │
                                 ▼                               │
                        StockService（接口，跨域唯一契约）──────────┘
```

- **模块化单体**：`com.wms.{common, base, system, stock, inbound, outbound, report}`，域内 `controller / service / mapper / entity / dto` 分层。
- **跨域只走接口契约**：`inbound`、`outbound` 通过 `StockService` 接口写库存，不直连 `stock` 的实现类；由 ArchUnit 架构测试兜底。

## 核心设计

### 1. 库存账实一致（不可变流水账）

- `stock_ledger` **只 INSERT**、禁 UPDATE/DELETE，是库存唯一事实源；`stock_balance` 是聚合快照，可由 ledger 重算校验（`/stock/recalculate`）。
- **并发扣减**：`SELECT ... FOR UPDATE` 行锁 + `ON CONFLICT DO NOTHING` 占位插入，解决「锁不到不存在行」的竞态；扣减前校验结存防负库存（防超卖）。
- **幂等入账**：`stock_ledger` 幂等唯一索引（`uk_stock_ledger_idem`），同一单据对同一库存维度只入账一次，防 ERP 消息重投导致双记。

### 2. 多仓数据隔离与越权防护（IDOR）

- `AuthContext` 提供 `checkWarehouse`（写校验）/ `scopeWarehouse`（读收敛）/ `requireAdmin`。
- 单仓绑定模型：`sys_user.warehouse_id` 非空 = 只能操作本仓，NULL = 管理员全局；越权访问统一返回 403。

### 3. RBAC + 方法级权限

- 四表模型：`sys_role` / `sys_permission` / `sys_user_role` / `sys_role_permission`。
- 登录签发权限码，接口用 `@PreAuthorize("hasAuthority('xxx:manage')")` 精细控制。
- 用户创建/更新走显式 DTO（`SysUserSaveRequest`），消除实体 mass assignment。

### 4. 架构守卫（ArchUnit）

- `ArchitectureTest` 固化三类规则：禁跨域引用 service 实现、Controller 不直连 Mapper、模块无环依赖。破坏边界 → 测试失败。

## 数据库

核心表：`warehouse / zone / location / sku`（主数据）、`stock_ledger / stock_balance`（库存）、`inbound_asn / asn_line / receive`（入库）、`outbound_order / order_line`（出库）、`sys_user / sys_role / sys_permission / sys_user_role / sys_role_permission`（权限）。

铁律：库存表带 `warehouse_id`；ledger 不可变；主键雪花 ID；业务表逻辑删除（`deleted`）；变更走 Flyway（`V1`~`V8`）。

## 快速开始

```bash
# 1. 启动 PostgreSQL（Redis/RabbitMQ 一并预留）
docker compose up -d postgres

# 2. 启动应用（JDK 21 + Maven）
mvn spring-boot:run

# 3. 运行测试
mvn test
```

- 服务地址：`http://localhost:8080/api`
- 首次启动 Flyway 自动建表并写入示例数据，默认账号 `admin / admin123`

## 测试

`mvn test` 共 50+ 用例，三层覆盖：

- **单元测试**：库存中枢（防超卖/幂等/盘点/重算）、用户服务（密码加密/角色绑定/仓库校验）。
- **集成测试**：权限矩阵（未认证 401 / 无权限 403 / 有权限 200）、跨仓越权拦截。
- **架构测试**：ArchUnit 固化模块边界。

## 进度

- [x] M0 工程骨架
- [x] M1 基础数据 + RBAC + JWT 认证
- [x] M2 库存中枢（流水账/现存量/盘点/移动/重算/幂等）
- [x] M3 入库（ASN → 收货 → 上架）
- [x] M4 出库（订单 → 拣货 → 发运）
- [x] 安全加固（多仓隔离 / 越权防护 / 方法级权限 / 架构守卫）
- [ ] M5 集成（ERP 适配 + WCS + MQ 事件）
- [ ] 前端 Vue3
