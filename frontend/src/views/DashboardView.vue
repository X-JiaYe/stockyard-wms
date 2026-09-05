<script setup>
import { onMounted, ref } from 'vue'
import { listSkus, listBalances, listAsns, listOrders, listLedgers } from '../api'
import StatusBadge from '../components/StatusBadge.vue'
import { DIRECTION, fmtQty, fmtDateTime } from '../utils/format'

const stats = ref({ skus: 0, balances: 0, asns: 0, orders: 0 })
const ledgers = ref([])

const KPIS = [
  { key: 'skus', label: '物料 SKU' },
  { key: 'balances', label: '库存维度' },
  { key: 'asns', label: '入库单' },
  { key: 'orders', label: '出库单' }
]

onMounted(async () => {
  try {
    const [s, b, a, o, l] = await Promise.all([
      listSkus({ pageNum: 1, pageSize: 1 }),
      listBalances({ pageNum: 1, pageSize: 1 }),
      listAsns({ pageNum: 1, pageSize: 1 }),
      listOrders({ pageNum: 1, pageSize: 1 }),
      listLedgers({ pageNum: 1, pageSize: 6 })
    ])
    stats.value = { skus: s?.total || 0, balances: b?.total || 0, asns: a?.total || 0, orders: o?.total || 0 }
    ledgers.value = l?.records || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div>
    <h1 class="page-title">概览</h1>
    <p class="page-sub">仓储运营关键指标与最近库存变动。</p>

    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
      <div v-for="kpi in KPIS" :key="kpi.key" class="panel panel-pad !py-5">
        <div class="text-sm text-muted">{{ kpi.label }}</div>
        <div class="text-[32px] font-semibold tracking-tight text-ink mt-1 num">{{ stats[kpi.key] }}</div>
      </div>
    </div>

    <div class="panel mt-6 overflow-hidden">
      <div class="px-6 py-4 border-b border-line">
        <h2 class="text-base font-semibold text-ink">最近库存流水</h2>
        <p class="text-sm text-muted">来自不可变流水账 stock_ledger，只增不改。</p>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>方向</th>
            <th>单号</th>
            <th class="text-right">变动数量</th>
            <th class="text-right">结存</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in ledgers" :key="r.id">
            <td>
              <StatusBadge :label="DIRECTION[r.direction] || '—'" :tone="r.direction === 1 ? 'green' : 'red'" />
            </td>
            <td class="text-muted">{{ r.refNo }}</td>
            <td class="text-right num" :class="Number(r.quantity) >= 0 ? 'text-emerald-600' : 'text-danger'">
              {{ Number(r.quantity) >= 0 ? '+' : '' }}{{ fmtQty(r.quantity) }}
            </td>
            <td class="text-right num">{{ fmtQty(r.balance) }}</td>
            <td class="text-muted">{{ fmtDateTime(r.createdAt) }}</td>
          </tr>
          <tr v-if="ledgers.length === 0">
            <td colspan="5" class="text-center text-muted py-10">暂无流水，完成一笔入库或出库后会在此展示。</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
