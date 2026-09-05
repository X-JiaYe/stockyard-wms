# CLAUDE.md

本文件为 Claude Code 在本仓库工作时必须遵循的项目约定。

## 项目概览

- 企业内部 WMS（仓储管理系统），单仓起步、规划多仓。
- 技术栈：Java 21 + Spring Boot 3.2 + MyBatis-Plus + PostgreSQL 16；前端 Vue3（后续）。
- 架构：**单模块 + 包级分层（模块化单体）**，不是 Maven 多模块拆分。边界靠包名约定维护。

## 包结构约定

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

每个业务域内按 `controller / service / mapper / entity / dto` 分层，禁止跨域直接引用 `service` 实现类（只允许引用公共契约或走事件）。

## 数据库铁律

1. **所有库存相关表必须带 `warehouse_id`**；全局表（如用户）不强制。
2. **`stock_ledger` 是不可变流水账**：只 INSERT，禁止 UPDATE/DELETE。任何库存变动都必须先写 ledger，再同步聚合到 `stock_balance`；`stock_balance` 必须能被 ledger 重算校验。
3. 主键统一 `BIGINT` 雪花 ID（`id-type: assign_id`），不用数据库自增。
4. 业务表统一带 `created_at`/`updated_at`/`deleted`（逻辑删除）；ledger 类不可变表不带 `deleted`/`updated_at`。
5. 数据库变更一律走 Flyway（`src/main/resources/db/migration/V{n}__desc.sql`），禁止手工改库。

## 编码规范

- 分层：Controller 只做参数校验与转发；业务规则在 Service；SQL 在 Mapper（复杂查询用 XML）。
- 统一返回结构 `Result<T>`（`common` 包内），异常走全局 `@RestControllerAdvice`。
- 金额/数量用 `BigDecimal`（`NUMERIC`），禁止 `double`/`float`。
- 时间统一 `LocalDateTime`，时区 Asia/Shanghai。
- 依赖注入用构造器注入（`@RequiredArgsConstructor`），不用字段 `@Autowired`。
- 枚举放 `common` 或对应业务域的 `enums` 子包，值必须与数据库注释口径一致。

## 提交规范

- Conventional Commits：`feat` / `fix` / `refactor` / `docs` / `chore` / `test`。
- 提交前跑 `mvn -q test`（有测试后）与 `/review-pr` 自查。
