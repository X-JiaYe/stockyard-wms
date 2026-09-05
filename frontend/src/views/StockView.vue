<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { listBalances, listLedgers, recalculate } from '../api'
import StatusBadge from '../components/StatusBadge.vue'
import Pagination from '../components/Pagination.vue'
import Modal from '../components/Modal.vue'
import { DIRECTION, fmtQty, fmtDateTime } from '../utils/format'

const tab = ref('balances')

const balanceQuery = reactive({ pageNum: 1, pageSize: 10, skuId: '', warehouseId: '' })
const balances = ref({ records: [], total: 0 })
const balanceLoading = ref(false)

const ledgerQuery = reactive({ pageNum: 1, pageSize: 10, skuId: '', refNo: '' })
const ledgers = ref({ records: [], total: 0 })
const ledgerLoading = ref(false)

const recalcRow = ref(null)
const recalcResult = ref(null)

function loadBalances() {
  balanceLoading.value = true
  const p = { pageNum: balanceQuery.pageNum, pageSize: balanceQuery.pageSize }
  if (balanceQuery.skuId) p.skuId = balanceQuery.skuId
  if (balanceQuery.warehouseId) p.warehouseId = balanceQuery.warehouseId
  listBalances(p)
    .then((d) => (balances.value = d || { records: [], total: 0 }))
    .finally(() => (balanceLoading.value = false))
}

function loadLedgers() {
  ledgerLoading.value = true
  const p = { pageNum: ledgerQuery.pageNum, pageSize: ledgerQuery.pageSize }
  if (ledgerQuery.skuId) p.skuId = ledgerQuery.skuId
  if (ledgerQuery.refNo) p.refNo = ledgerQuery.refNo
  listLedgers(p)
    .then((d) => (ledgers.value = d || { records: [], total: 0 }))
    .finally(() => (ledgerLoading.value = false))
}

async function openRecalc(row) {
  recalcRow.value = row
  recalcResult.value = null
  try {
    recalcResult.value = await recalculate({
      warehouseId: row.warehouseId,
      skuId: row.skuId,
      lotNo: row.lotNo,
      locationId: row.locationId
    })
  } catch (e) {
    recalcResult.value = { error: e.message }
  }
}

onMounted(() => {
  loadBalances()
  loadLedgers()
})

watch(tab, (v) => {
  if (v === 'balances') loadBalances()
  else loadLedgers()
})
</script>

<template>
  <div>
    <h1 class="page-title">库存</h1>
    <p class="page-sub">现存量快照与不可变流水账，支持账实一致性校验。</p>

    <div class="flex items-center gap-1 mt-6 border-b border-line">
      <button
        class="px-4 py-2.5 text-sm font-medium -mb-px border-b-2 transition-colors"
        :class="tab === 'balances' ? 'border-accent text-accent' : 'border-transparent text-muted hover:text-ink'"
        @click="tab = 'balances'"
      >现存量</button>
      <button
        class="px-4 py-2.5 text-sm font-medium -mb-px border-b-2 transition-colors"
        :class="tab === 'ledgers' ? 'border-accent text-accent' : 'border-transparent text-muted hover:text-ink'"
        @click="tab = 'ledgers'"
      >流水账</button>
    </div>

    <!-- 现存量 -->
    <div v-if="tab === 'balances'" class="panel mt-5 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">物料 ID</label>
          <input v-model="balanceQuery.skuId" class="input !w-44" placeholder="如 123456" @keyup.enter="balanceQuery.pageNum = 1; loadBalances()" />
        </div>
        <button class="btn-primary" @click="balanceQuery.pageNum = 1; loadBalances()">查询</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>物料 ID</th><th>批次</th><th>货位 ID</th>
            <th class="text-right">现存量</th><th>更新时间</th><th class="text-right"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in balances.records" :key="r.id">
            <td class="num">{{ r.skuId }}</td>
            <td class="text-muted">{{ r.lotNo || '—' }}</td>
            <td class="num">{{ r.locationId }}</td>
            <td class="text-right num font-medium">{{ fmtQty(r.quantity) }}</td>
            <td class="text-muted">{{ fmtDateTime(r.updatedAt) }}</td>
            <td class="text-right"><button class="btn-ghost !h-8" @click="openRecalc(r)">校验</button></td>
          </tr>
          <tr v-if="!balanceLoading && balances.records.length === 0">
            <td colspan="6" class="text-center text-muted py-10">暂无现存量。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="balanceQuery.pageNum" :page-size="balanceQuery.pageSize" :total="balances.total" @change="(p) => { balanceQuery.pageNum = p; loadBalances() }" />
    </div>

    <!-- 流水账 -->
    <div v-else class="panel mt-5 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">单号</label>
          <input v-model="ledgerQuery.refNo" class="input !w-44" placeholder="如 ASN2026…" @keyup.enter="ledgerQuery.pageNum = 1; loadLedgers()" />
        </div>
        <div>
          <label class="field-label">物料 ID</label>
          <input v-model="ledgerQuery.skuId" class="input !w-40" placeholder="可选" @keyup.enter="ledgerQuery.pageNum = 1; loadLedgers()" />
        </div>
        <button class="btn-primary" @click="ledgerQuery.pageNum = 1; loadLedgers()">查询</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>方向</th><th>单号</th><th>物料 ID</th><th>批次</th>
            <th class="text-right">变动</th><th class="text-right">结存</th><th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in ledgers.records" :key="r.id">
            <td><StatusBadge :label="DIRECTION[r.direction] || '—'" :tone="r.direction === 1 ? 'green' : 'red'" /></td>
            <td class="text-muted">{{ r.refNo }}</td>
            <td class="num">{{ r.skuId }}</td>
            <td class="text-muted">{{ r.lotNo || '—' }}</td>
            <td class="text-right num" :class="Number(r.quantity) >= 0 ? 'text-emerald-600' : 'text-danger'">{{ Number(r.quantity) >= 0 ? '+' : '' }}{{ fmtQty(r.quantity) }}</td>
            <td class="text-right num">{{ fmtQty(r.balance) }}</td>
            <td class="text-muted">{{ fmtDateTime(r.createdAt) }}</td>
          </tr>
          <tr v-if="!ledgerLoading && ledgers.records.length === 0">
            <td colspan="7" class="text-center text-muted py-10">暂无流水。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="ledgerQuery.pageNum" :page-size="ledgerQuery.pageSize" :total="ledgers.total" @change="(p) => { ledgerQuery.pageNum = p; loadLedgers() }" />
    </div>

    <!-- 重算校验结果 -->
    <Modal v-if="recalcRow" title="账实一致性校验" @close="recalcRow = null">
      <div v-if="recalcResult?.error" class="text-sm text-danger">{{ recalcResult.error }}</div>
      <div v-else-if="recalcResult" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="panel !bg-canvas p-4">
            <div class="text-xs text-muted">流水账聚合结存</div>
            <div class="text-xl font-semibold num mt-1">{{ fmtQty(recalcResult.ledgerQuantity) }}</div>
          </div>
          <div class="panel !bg-canvas p-4">
            <div class="text-xs text-muted">现存量快照</div>
            <div class="text-xl font-semibold num mt-1">{{ fmtQty(recalcResult.balanceQuantity) }}</div>
          </div>
        </div>
        <div class="flex items-center gap-2.5">
          <span class="w-2 h-2 rounded-full" :class="recalcResult.consistent ? 'bg-emerald-500' : 'bg-danger'" />
          <span class="text-sm font-medium" :class="recalcResult.consistent ? 'text-emerald-700' : 'text-danger'">
            {{ recalcResult.consistent ? '账实一致，余额可由流水重算校验' : '账实不一致，需排查入账链路' }}
          </span>
        </div>
      </div>
      <div v-else class="text-sm text-muted">校验中…</div>
    </Modal>
  </div>
</template>
