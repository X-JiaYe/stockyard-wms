# Stockyard WMS 前端

Vue3 + Vite + Tailwind CSS + Pinia + Vue Router + axios，面向后端 `http://localhost:8080/api`。

## 快速开始

```bash
# 1. 启动后端（含 PostgreSQL，可选 Redis/RabbitMQ）
#    docker compose up -d postgres redis rabbitmq
#    mvn spring-boot:run

# 2. 启动前端（开发期 /api 已代理到 8080）
npm install
npm run dev        # http://localhost:5173

# 3. 生产构建
npm run build
```

默认账号 `admin / admin123`。

## 设计

- **设计语言**：参考 Apple 系统的克制美学——`#f5f5f7` 画布底、`#1d1d1f` 主文字、`#0071e3` 单一强调色、系统字体栈、细描边卡片、半透明毛玻璃侧边栏（`backdrop-blur`）。
- **刻意避开的模板化套路**（来自 frontend-design 原则）：暖奶油底 + 陶土色、全大写眉题、等宽数据标签、`01/02/03` 编号、卡片硬阴影 + 渐变水印。
- **动效克制**：仅交互反馈（按钮按压 `active:scale`、行 hover），无逐段淡入。

## 页面

| 路由 | 页面 | 说明 |
|---|---|---|
| `/login` | 登录 | JWT 登录，登出走后端黑名单 |
| `/dashboard` | 概览 | KPI 计数 + 最近库存流水 |
| `/stock` | 库存 | 现存量 / 流水账 + 账实一致性校验 |
| `/inbound` | 入库 | ASN 列表 + 新建 + 收货（质检）+ 上架 |
| `/outbound` | 出库 | 订单列表 + 新建 + 拣货 + 发运 |
| `/skus` | 物料 | SKU CRUD |
| `/users` | 用户 | 用户/角色/仓库归属（仅管理员） |

## 目录

```
src/
├── api/          http 实例（JWT 拦截 + Result 解包）与接口
├── stores/       Pinia（auth）
├── router/       路由 + 登录守卫
├── components/   布局 / 徽章 / 分页 / 弹窗
├── utils/        状态字典与格式化
└── views/        各业务页
```
