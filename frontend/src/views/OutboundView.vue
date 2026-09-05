<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  listOrders, createOrder, getOrder, pickOrderLine, shipOrder,
  listWarehouses, listSkus, listLocations
} from '../api'
import StatusBadge from '../components/StatusBadge.vue'
import Pagination from '../components/Pagination.vue'
import Modal from '../components/Modal.vue'
import { OUTBOUND_STATUS, outboundTone, fmtQty, fmtDateTime } from '../utils/format'

const query = reactive({ pageNum: 1, pageSize: 10, status: '' })
const orders = ref({ records: [], total: 0 })

const warehouses = ref([])
const skus = ref([])
const locations = ref([])
const skuMap = ref({})
const locMap = ref({})

const showCreate = ref(false)
const createForm = reactive({ warehouseId: '', customerName: '', remark: '', lines: [{ skuId: '', orderQty: '', lotNo: '' }] })
const creating = ref(false)

const showDetail = ref(false)
const detail = ref(null)
const activeOrder = ref(null)

const action = ref(null) // { type: 'pick', line, qty, locationId }

function skuLabel(id) {
  const s = skuMap.value[id]
  return s ? `${s.code} · ${s.name}` : `#${id}`
}
function locLabel(id) {
  const l = locMap.value[id]
  return l ? l.code : `#${id}`
}

async function loadBase() {
  const [w, s] = await Promise.all([
    listWarehouses({ pageNum: 1, pageSize: 100 }),
    listSkus({ pageNum: 1, pageSize: 200 })
  ])
  warehouses.value = w?.records || []
  skus.value = s?.records || []
  skuMap.value = Object.fromEntries(skus.value.map((x) => [x.id, x]))
}

async function loadLocations(warehouseId) {
  const d = await listLocations({ pageNum: 1, pageSize: 200, warehouseId })
  locations.value = d?.records || []
  locMap.value = Object.fromEntries(locations.value.map((x) => [x.id, x]))
}

function loadOrders() {
  const p = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.status) p.status = query.status
  listOrders(p).then((d) => (orders.value = d || { records: [], total: 0 }))
}

async function openDetail(row) {
  activeOrder.value = row
  detail.value = await getOrder(row.id)
  showDetail.value = true
  await loadLocations(row.warehouseId)
}

function addLine() {
  createForm.lines.push({ skuId: '', orderQty: '', lotNo: '' })
}
function removeLine(i) {
  createForm.lines.splice(i, 1)
}

async function submitCreate() {
  creating.value = true
  try {
    await createOrder({
      warehouseId: createForm.warehouseId,
      customerName: createForm.customerName,
      remark: createForm.remark,
      lines: createForm.lines.map((l) => ({ skuId: l.skuId, orderQty: l.orderQty, lotNo: l.lotNo || null }))
    })
    showCreate.value = false
    loadOrders()
  } finally {
    creating.value = false
  }
}

function openPick(line) {
  action.value = { type: 'pick', line, qty: '', locationId: '' }
}

async function submitPick() {
  await pickOrderLine({
    orderLineId: action.value.line.id,
    locationId: action.value.locationId,
    qty: action.value.qty
  })
  action.value = null
  detail.value = await getOrder(activeOrder.value.id)
  loadOrders()
}

async function submitShip() {
  await shipOrder({ orderId: activeOrder.value.id })
  detail.value = await getOrder(activeOrder.value.id)
  loadOrders()
}

onMounted(() => {
  loadBase()
  loadOrders()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h1 class="page-title">出库</h1>
        <p class="page-sub">订单 → 拣货（扣库存）→ 发运。</p>
      </div>
      <button class="btn-primary" @click="showCreate = true">新建出库单</button>
    </div>

    <div class="panel mt-6 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">状态</label>
          <select v-model="query.status" class="select !w-40" @change="query.pageNum = 1; loadOrders()">
            <option value="">全部</option>
            <option v-for="(label, val) in OUTBOUND_STATUS" :key="val" :value="val">{{ label }}</option>
          </select>
        </div>
      </div>
      <table class="table">
        <thead>
          <tr><th>出库单号</th><th>客户</th><th>状态</th><th>创建时间</th><th class="text-right"></th></tr>
        </thead>
        <tbody>
          <tr v-for="r in orders.records" :key="r.id">
            <td class="font-medium num">{{ r.orderNo }}</td>
            <td class="text-muted">{{ r.customerName || '—' }}</td>
            <td><StatusBadge :label="OUTBOUND_STATUS[r.status]" :tone="outboundTone(r.status)" /></td>
            <td class="text-muted">{{ fmtDateTime(r.createdAt) }}</td>
            <td class="text-right"><button class="btn-ghost !h-8" @click="openDetail(r)">详情</button></td>
          </tr>
          <tr v-if="orders.records.length === 0">
            <td colspan="5" class="text-center text-muted py-10">暂无出库单，点击右上角新建。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="query.pageNum" :page-size="query.pageSize" :total="orders.total" @change="(p) => { query.pageNum = p; loadOrders() }" />
    </div>

    <!-- 新建出库单 -->
    <Modal v-if="showCreate" title="新建出库单" width="max-w-2xl" @close="showCreate = false">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="field-label">仓库</label>
            <select v-model="createForm.warehouseId" class="select">
              <option value="" disabled>选择仓库</option>
              <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
            </select>
          </div>
          <div>
            <label class="field-label">客户</label>
            <input v-model="createForm.customerName" class="input" placeholder="客户名称" />
          </div>
        </div>
        <div>
          <label class="field-label">备注</label>
          <input v-model="createForm.remark" class="input" placeholder="可选" />
        </div>

        <div>
          <div class="flex items-center justify-between mb-2">
            <label class="text-sm font-medium text-ink">明细</label>
            <button class="btn-ghost !h-7 !px-2 text-sm" @click="addLine">+ 添加明细</button>
          </div>
          <div v-for="(line, i) in createForm.lines" :key="i" class="flex gap-2 mb-2 items-center">
            <select v-model="line.skuId" class="select flex-1">
              <option value="" disabled>选择物料</option>
              <option v-for="s in skus" :key="s.id" :value="s.id">{{ s.code }} · {{ s.name }}</option>
            </select>
            <input v-model="line.orderQty" class="input !w-24" placeholder="数量" />
            <input v-model="line.lotNo" class="input !w-32" placeholder="批次(可选)" />
            <button class="text-muted hover:text-danger p-1" @click="removeLine(i)">×</button>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="showCreate = false">取消</button>
        <button class="btn-primary" :disabled="creating" @click="submitCreate">{{ creating ? '提交中…' : '创建' }}</button>
      </template>
    </Modal>

    <!-- 详情 -->
    <Modal v-if="showDetail && detail" :title="detail.order.orderNo" width="max-w-3xl" @close="showDetail = false">
      <div class="flex flex-wrap gap-x-8 gap-y-2 text-sm mb-4 items-center">
        <div><span class="text-muted">客户：</span>{{ detail.order.customerName || '—' }}</div>
        <div><span class="text-muted">状态：</span><StatusBadge class="ml-1" :label="OUTBOUND_STATUS[detail.order.status]" :tone="outboundTone(detail.order.status)" /></div>
        <div><span class="text-muted">创建：</span>{{ fmtDateTime(detail.order.createdAt) }}</div>
      </div>
      <table class="table !rounded-lg border border-line">
        <thead>
          <tr><th>物料</th><th class="text-right">订购</th><th class="text-right">已拣</th><th class="text-right"></th></tr>
        </thead>
        <tbody>
          <tr v-for="l in detail.lines" :key="l.id">
            <td>{{ skuLabel(l.skuId) }}<span v-if="l.lotNo" class="text-muted text-xs"> · {{ l.lotNo }}</span></td>
            <td class="text-right num">{{ fmtQty(l.orderQty) }}</td>
            <td class="text-right num">{{ fmtQty(l.pickedQty) }}</td>
            <td class="text-right">
              <button class="btn-ghost !h-7 !px-2 text-sm" @click="openPick(l)">拣货</button>
            </td>
          </tr>
        </tbody>
      </table>
      <template #footer>
        <button class="btn-secondary" @click="showDetail = false">关闭</button>
        <button class="btn-primary" :disabled="detail.order.status === 40" @click="submitShip">发运</button>
      </template>
    </Modal>

    <!-- 拣货 -->
    <Modal v-if="action" title="拣货" width="max-w-md" @close="action = null">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="field-label">拣货数量</label>
            <input v-model="action.qty" class="input" type="number" min="0" placeholder="数量" />
          </div>
          <div>
            <label class="field-label">来源货位</label>
            <select v-model="action.locationId" class="select">
              <option value="" disabled>选择货位</option>
              <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.code }}</option>
            </select>
          </div>
        </div>
        <p class="text-xs text-muted">拣货会同步扣减库存，库存不足将被后端拦截。</p>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="action = null">取消</button>
        <button class="btn-primary" @click="submitPick">确认拣货</button>
      </template>
    </Modal>
  </div>
</template>
