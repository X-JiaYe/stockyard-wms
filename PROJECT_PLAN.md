# WMS 项目计划书

> 面向企业内部的仓储管理系统（WMS）
> 生成日期：2026-09-05

---

## 一、调研结果复述

### 1.1 GitHub 企业级 Claude Skills 调研

调研方式：GitHub REST API + 仓库 marketplace 清单逐项核实（2026-09-05）。

**核心结论：**
- 系统已内置 `/code-review`、`/security-review`，无需重复安装。
- 可安装的第三方企业级插件市场按可信度排序：

| 来源 | 市场 | 内容 | License | 状态 |
|---|---|---|---|---|
| anthropics/claude-code | 官方第一方 | commit-commands、pr-review-toolkit、security-guidance 等 | Apache/官方 | ✅ 已装 |
| trailofbits/skills | 安全审计 | 差分审查、静态分析、供应链风险审计等 | CC-BY-SA | 备选 |
| obra/superpowers-marketplace | 工程方法论 | brainstorm→plan→execute、强制 TDD | MIT | 备选 |
| getsentry/skills 等 | 提交/PR 规范 | conventional commits、review | MIT | 备选 |

**风险提示：** 社区大杂烩（如 `claude-community` 2282 个未审核插件）不可整包装，已发生过后门类 hook；只装官方或可信来源。

**已安装（本项目）：**
- `commit-commands` → `/commit`、`/commit-push-pr`、`/clean_gone`
- `pr-review-toolkit` → `/review-pr` + 6 个评审 agent
- `security-guidance` → 后台安全审查 hook

### 1.2 GitHub 高星 WMS 项目调研（技术栈选型依据）

**关键事实：高星 ≠ 生产级 WMS。** ERPNext(~38.9k★)、Odoo(~54k★) 是 ERP，库存模块缺库位/上架任务/波次引擎；且本项目已有 SAP/金蝶 ERP，重造 ERP 不划算。

纯 WMS 主力项目：

| 项目 | ★ | 后端 | 前端/DB | 定位 |
|---|---|---|---|---|
| GreaterWMS | 4.4k | Python/Django | Quasar(Vue2)/SQLite·MySQL·PG | 功能最全纯 WMS；上游停更~1年 |
| ModernWMS | 1.7k | .NET 7 | Vue3/MySQL·SQLServer·PG | 简洁，参考架构佳 |
| wms-ruoyi | 1.2k | Java SpringBoot3 | Vue+Element/MySQL+Redis | 中文 CRUD+打印最佳，缺 WMS 引擎 |
| OpenWMS.org | 0.7k | Java/Spring 微服务 | 各服务 RDBMS+RabbitMQ | 唯一真微服务+MFC/WCS，部分闭源 |
| OpenBoxes | 0.9k | Java/Groovy Grails | /MySQL | 医疗域，Eclipse 协议 |
| JeeWMS | 0.15k | Java Spring(Cloud) | Vue+UNI-APP/MySQL | 3PL计费+AGV/RFID，中文最强，GPL |
| KopSoftWms / WMS-BS | 0.9k/0.06k | C#/.NET | SQL Server | 中文 .NET 派系，含 PDA、WCS 接口点 |

**三个硬结论：**
1. 成熟 WMS 后端集中在 **Java/Spring、Python(Django/Frappe/Odoo)、C#/.NET** 三条线；DB 赢家是 **PostgreSQL/MySQL + Redis**。
2. 中文区大量 SpringBoot+Vue、.NET 仓库是 CRUD 演示/小企业工具；真正有仓储深度的（上架策略/波次/动态任务/3PL计费）是 GreaterWMS、Odoo+OCA、OpenWMS、JeeWMS，各有框架税。
3. **WCS/自动化协议层几乎没有干净开源可抄**——是自建差异点。

---

## 二、项目定位与目标

- **定位：** 面向企业内部 WMS，服务自有仓/自有货，非 SaaS 多租户售卖。
- **范围：** 单仓起步，架构上规划多仓扩展（`warehouse_id` 贯穿全部库存表）。
- **集成：** 上游对接 ERP（SAP / 金蝶），下游对接设备层（AGV / 立库 / RF 手持 / WCS）。
- **演进：** 模块化单体起步，随业务拆微服务；不自证「微服务正确性」。

---

## 三、技术栈选型（Java Spring Boot 3 自研）

| 层 | 选型 | 版本 | 说明 |
|---|---|---|---|
| 语言 | Java | 17（可升 21） | LTS，Spring Boot 3 基线 |
| 框架 | Spring Boot | 3.2.x | 主框架 |
| 微服务储备 | Spring Cloud Alibaba | 2023.x | Nacos 注册/配置、Sentinel 限流、Seata（后续按需启用，不在单体阶段强制上） |
| ORM | MyBatis-Plus | 3.5.x | 中文生态主流，SQL 可控 |
| 权限 | Spring Security + JWT | — | RBAC，参考若依模型 |
| 前端 | Vue 3 + TypeScript | — | SPA |
| UI | Element Plus + Vite | — | 中文后台主流 |
| 数据库 | **PostgreSQL** 16（或 MySQL 8） | — | 首选 PG；若团队更熟 MySQL 亦可 |
| 缓存 | Redis | 7.x | 会话、库存热点、分布式锁 |
| 消息 | RabbitMQ（或 Kafka） | — | ERP/WCS 事件解耦、波次/任务分发 |
| 数据迁移 | Flyway | — | 数据库版本化，建表即带多仓字段 |
| 文档 | springdoc-openapi (Knife4j) | — | 接口文档 |
| 定时任务 | XXL-Job | — | 库存任务、日结、报表 |
| 对象映射 | MapStruct + Lombok | — | 样板代码 |
| 单体→微服务桥梁 | Spring Modulith（可选） | — | 模块边界约束 |

**WCS/自动化协议层（自建）：** PLC/TCP、OPC-UA、AGV 调度、RF 手持，通过 MQ 与主流程解耦，不写死在请求响应里。

---

## 四、系统架构

```
                    ┌────────────────────────────────────┐
   SAP / 金蝶 ERP ◄─►│  ERP 适配层（ASN入 / 出库单出 / 状态回写）│
                    └───────────────┬────────────────────┘
                                    │ 事件 / MQ
   ┌────────────────────────────────▼────────────────────────────────┐
   │                     WMS 核心（模块化单体）                        │
   │  基础数据 │ 入库 │ 出库 │ 库存 │ 波次/任务引擎 │ 报表看板 │ 系统管理 │
   └────────────────────────────────┬────────────────────────────────┘
                                    │ 任务 / MQ
                    ┌───────────────▼────────────────────┐
                    │  WCS 适配层（AGV / 立库 / RF / PLC）│
                    └────────────────────────────────────┘
```

- **核心原则：** 单仓起步用模块化单体 + 清晰服务边界 + 消息集成点；ERP/WCS 都是**适配器缝隙**，不让外部模型渗入仓储领域模型。
- **领域模型硬约束（建表第一天就要做对）：**
  - 所有可库存表带 `warehouse_id`（多仓预留，可选 tenant/appid）
  - 库区/货架/货位层级、批次/序列号/效期
  - **不可变库存流水账（stock ledger）** + 估值钩子——成熟产品（Odoo 双分录移动、ERPNext stock ledger+GL、GreaterWMS 库存状态）都以此为中枢，CRUD 演示项目全栽在这里。

---

## 五、功能模块划分

### 5.1 基础数据
- 物料/商品主数据、包装单位、条码
- 仓库 → 库区 → 货架 → 货位层级
- 批次/序列号/效期规则、供应商/客户主数据

### 5.2 入库
- ASN（预到货通知）接收（ERP 推入）
- 收货登记、质检（合格/待检/不合格）
- 上架策略（固定/动态/ABC）、上架任务

### 5.3 出库
- 出库单（销售/调拨/领料）
- **波次规划（wave）**、订单合并、拣货策略（摘果/播种/分区）
- 拣货任务、复核打包、发运交接

### 5.4 库存
- 库存台账/查询、库存移动（移库/调拨）
- **盘点（cycle count）**、冻结/解冻、效期预警、安全库存

### 5.5 任务与设备
- 任务引擎（拣货/上架/补货任务生成与分配）
- WCS 接口（AGV 调度、立库、RF 手持）

### 5.6 报表与看板
- 库存看板、吞吐量、库龄、作业效率

### 5.7 系统管理
- 用户/角色/权限（RBAC）、菜单、字典、日志、租户预留

### 5.8 集成（可选二期）
- 3PL 计费引擎、ERP 财务对账

---

## 六、数据库设计要点（关键表）

| 域 | 核心表 | 关键字段/约束 |
|---|---|---|
| 基础 | warehouse / zone / location | 层级关系、多仓维度 |
| 基础 | sku / sku_pack / sku_barcode | 单位换算、条码 |
| 库存 | stock_ledger（不可变） | `warehouse_id, sku_id, lot_no, qty, dir(+/-), ref_type, ref_no, balance` |
| 库存 | stock_balance（聚合） | 现存量，供查询，可重算自 ledger |
| 入库 | asn / asn_line / receive / putaway_task | 来源单号、质检状态 |
| 出库 | outbound_order / wave / pick_task / pack / ship | 波次、拣货、复核 |
| 盘点 | cycle_count / cc_line | 盘点任务与差异 |

**约束：** 库存变动必须写 `stock_ledger`，禁止直接改 `stock_balance`；`balance` 由 ledger 重算校验，保证账实一致。

---

## 七、里程碑（迭代计划）

| 阶段 | 目标 | 交付物 |
|---|---|---|
| **M0 工程骨架** | 搭环境 | Maven 多模块、Spring Boot 3 骨架、Flyway、PG/Redis/RabbitMQ docker-compose、CI |
| **M1 基础数据 + 系统管理** | 主数据与权限 | 物料/库区/货位 CRUD、RBAC、登录鉴权 |
| **M2 库存中枢** | 核心账 | stock_ledger/balance、移动、库存查询、盘点 |
| **M3 入库闭环** | 收货到上架 | ASN→收货→质检→上架任务 |
| **M4 出库闭环** | 下单到发运 | 出库单→波次→拣货→复核→发运 |
| **M5 集成** | 打通上下游 | ERP 适配层、WCS/RF 对接、消息事件 |
| **M6 报表 + 上线加固** | 生产化 | 看板、效期/库龄、性能与安全加固 |

---

## 八、风险与对策

| 风险 | 对策 |
|---|---|
| 过早微服务导致运维成本失控 | 模块化单体起步，Spring Modulith 约束边界，按需拆 |
| 库存账实不符 | 不可变 ledger 为唯一事实源，聚合表可重算 |
| ERP/WCS 耦合过深 | 适配层 + MQ 事件解耦，外部模型不渗入领域模型 |
| WCS 协议层无开源可抄 | 自建设备适配，先覆盖核心设备（RF 手持 + 一种 AGV） |
| 团队对 PG 不熟 | 若更熟 MySQL 可切 MySQL 8，迁移成本低 |

---

## 九、下一步

1. 初始化 Maven 多模块工程骨架（M0）
2. docker-compose 起 PostgreSQL + Redis + RabbitMQ
3. 建立 CLAUDE.md（编码规范、模块边界、提交规范）
4. 落地 Flyway 首个 migration（基础表 + 多仓字段 + ledger）
