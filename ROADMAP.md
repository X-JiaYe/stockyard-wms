# 简历导向迭代方针（ROADMAP）

> 生成日期：2026-09-05
> 定位：本项目**为简历/面试服务**，无实际企业生产需求。一切迭代以「让简历有说服力、经得起面试追问」为唯一目标。
> 背景计划见 [PROJECT_PLAN.md](./PROJECT_PLAN.md)（企业视角的原始计划书）。

---

## 一、总方针

1. **深度 > 广度**：把一个点做透、能讲清楚「为什么这么设计」，胜过十项浅尝辄止的功能。
2. **可证明 > 自述**：凡声称的设计，都要有测试或代码证据背书（单测 / 架构测试 / 唯一约束），不能只写在注释或 README 里。
3. **有故事 > 功能全**：每个投入都要能回答「你最难/最有意思的点是什么」这类必问题。
4. **不堆中间件**：不为用而用 Redis/MQ/微服务；只补能讲出「为什么」的东西。

---

## 二、已具备的核心深度点（简历已有货，勿重复造）

| 深度点 | 现状 | 面试可讲点 |
|---|---|---|
| 不可变库存流水账 | `stock_ledger` 只 INSERT + `stock_balance` 聚合快照 | 账实一致、唯一事实源 |
| 重算对账 | `recalculate`：ledger 聚合 vs balance 比对 | 余额可被流水重算校验 |
| **并发扣减** | `FOR UPDATE` 行锁 + `ON CONFLICT DO NOTHING` 占位插入 + 防负库存 | 锁不到不存在行的竞态怎么解 |
| 多仓数据隔离 + 越权防护 | `AuthContext.checkWarehouse/scopeWarehouse` + IDOR 修复 | 跨仓越权怎么防 |
| RBAC + 方法级权限 | 4 表 + `@PreAuthorize` 权限点 | 权限模型与方法级鉴权 |
| 逻辑删除 + 唯一约束并存 | 部分唯一索引 `WHERE deleted = 0` | 软删后唯一键复用 |
| 显式 DTO 防批量赋值 | `SysUserSaveRequest` | mass assignment 怎么防 |

**结论**：主打点（库存一致性）已具备主体，本方针聚焦「补证据 + 补缺口 + 补可演示」。

---

## 三、待办优先级（执行顺序）

### P0-1 幂等入账（防重复入账）—— 库存一致性主打的最后一块缺口
- **缺口**：`stock_ledger` 无防重唯一约束，同一单据重复推入会**双记库存**。
- **做法**：`V8` migration 加幂等唯一索引（`warehouse_id, sku_id, lot_no, location_id, ref_type, ref_no, ref_line_id`，null 用 COALESCE 归一），`changeStock` 捕获唯一冲突转业务异常。
- **验收**：单测覆盖「重复 ref 入账被拒绝」；索引在迁移里可见。
- **面试点**：ERP 消息重投 / 重复推送不会导致库存双记。

### P0-2 ArchUnit 架构守卫 —— 把「模块化单体边界」变成可测证据
- **缺口**：CLAUDE.md 声明「禁止跨域引用 service 实现类」，但无代码兜底。
- **做法**：引入 ArchUnit，写架构测试固化包边界（`base/system/stock/inbound/outbound` 互不依赖 service 实现）。
- **验收**：`mvn test` 跑通架构规则；故意跨域引用会测试失败。

### P0-3 权限矩阵集成测试 —— 让安全链路可证明
- **缺口**：`@PreAuthorize` + IDOR 无 Controller 层测试背书。
- **做法**：`@WebMvcTest` + MockMvc + `@WithMockUser`，覆盖 OPERATOR 访问 `system:manage` 应 403、跨仓查询应被过滤/拒绝。
- **验收**：新增若干 Controller 层测试，`mvn test` 全绿。

### P1-4 README + 架构图 —— 可 3 分钟讲完的门面
- **做法**：领域模型说明（ledger 为什么不可变）、模块划分、安全设计、docker-compose 一键起 PG 跑通。
- **验收**：新人按 README 能本地跑起来 + 看懂架构。

### P1-5 遗留功能限制修复
- `SysUserServiceImpl.updateById` 无法清空 `warehouseId`（MyBatis-Plus `NOT_NULL` 策略），换仓/降级管理员时旧值残留 → 改 DTO 区分「未传」与「显式置空」。

---

## 四、明确不做（避免浪费）

- ❌ 波次规划 / 质检独立流程 / 上架策略 / 3PL 计费 —— 广度堆料，问深即露馅。
- ❌ XXL-Job —— 无定时任务真实场景，仍不做。
- ❌ 微服务拆分 / Spring Cloud Alibaba —— 与「模块化单体」定位冲突。

> 2026-09-05 调整：原「不做 Redis/MQ/前端」在用户明确要求下反转——
> **Redis** 补「无状态 JWT 无法主动失效」缺口（黑名单，能讲出为什么）；**RabbitMQ** 做「出入库完成领域事件」解耦 ERP/WCS 通知（`AFTER_COMMIT` 投递，能讲一致性取舍）；**Vue3 前端** 做全栈能力展示，设计走 Apple 克制风格。三者均非「为用而用」，故从「不做」划出。

---

## 五、执行记录

| 日期 | 事项 | 状态 |
|---|---|---|
| 2026-09-05 | 接口级 `@PreAuthorize` + roleCodes mass assignment 修复 | ✅ 已提交 3d18b1a/672cfc0/abb2138 |
| 2026-09-05 | 建立本方针文件 | ✅ |
| 2026-09-05 | P0-1 幂等入账 + P0-2 ArchUnit + P0-3 权限矩阵 + P1-4 README + P1-5 仓库清空修复 | ✅ |
| 2026-09-05 | 落地 Redis（JWT 黑名单）+ RabbitMQ（领域事件），60 测试全绿 | ✅ |
| 2026-09-05 | Vue3 前端（Apple 风格）：登录/概览/库存/入库/出库/物料/用户 | ✅ |
