<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  listAsns, createAsn, getAsn, receiveAsnLine, putawayAsnLine,
  listWarehouses, listSkus, listLocations
} from '../api'
import StatusBadge from '../components/StatusBadge.vue'
import Pagination from '../components/Pagination.vue'
import Modal from '../components/Modal.vue'
import { ASN_STATUS, asnTone, QC_RESULT, qcTone, fmtQty, fmtDateTime } from '../utils/format'

const query = reactive({ pageNum: 1, pageSize: 10, status: '' })
const asns = ref({ records: [], total: 0 })

const warehouses = ref([])
const skus = ref([])
const locations = ref([])

// 创建入库单
const showCreate = ref(false)
const createForm = reactive({ warehouseId: '', supplierName: '', remark: '', lines: [{ skuId: '', expectedQty: '', lotNo: '' }] })
const creating = ref(false)

// 详情
const showDetail = ref(false)
const detail = ref(null)
const activeAsn = ref(null)

// 收货 / 上架
const action = ref(null) // { type: 'receive'|'putaway', line }

const skuMap = ref({})
const locMap = ref({})

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

function loadAsns() {
  const p = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.status) p.status = query.status
  listAsns(p).then((d) => (asns.value = d || { records: [], total: 0 }))
}

async function openDetail(row) {
  activeAsn.value = row
  detail.value = await getAsn(row.id)
  showDetail.value = true
  await loadLocations(row.warehouseId)
}

function addLine() {
  createForm.lines.push({ skuId: '', expectedQty: '', lotNo: '' })
}
function removeLine(i) {
  createForm.lines.splice(i, 1)
}

async function submitCreate() {
  creating.value = true
  try {
    const payload = {
      warehouseId: createForm.warehouseId,
      supplierName: createForm.supplierName,
      remark: createForm.remark,
      lines: createForm.lines.map((l) => ({
        skuId: l.skuId,
        expectedQty: l.expectedQty,
        lotNo: l.lotNo || null
      }))
    }
    await createAsn(payload)
    showCreate.value = false
    loadAsns()
  } finally {
    creating.value = false
  }
}

// 收货
function openReceive(line) {
  action.value = { type: 'receive', line, qty: '', qcResult: 1, remark: '' }
}
// 上架
function openPutaway(line) {
  action.value = { type: 'putaway', line, qty: '', locationId: '' }
}

async function submitAction() {
  if (action.value.type === 'receive') {
    await receiveAsnLine({
      asnLineId: action.value.line.id,
      qty: action.value.qty,
      qcResult: Number(action.value.qcResult),
      remark: action.value.remark
    })
  } else {
    await putawayAsnLine({
      asnLineId: action.value.line.id,
      locationId: action.value.locationId,
      qty: action.value.qty
    })
  }
  action.value = null
  detail.value = await getAsn(activeAsn.value.id)
  loadAsns()
}

onMounted(() => {
  loadBase()
  loadAsns()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h1 class="page-title">入库</h1>
        <p class="page-sub">ASN → 收货（含质检）→ 上架落库存。</p>
      </div>
      <button class="btn-primary" @click="showCreate = true">新建入库单</button>
    </div>

    <div class="panel mt-6 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">状态</label>
          <select v-model="query.status" class="select !w-40" @change="query.pageNum = 1; loadAsns()">
            <option value="">全部</option>
            <option v-for="(label, val) in ASN_STATUS" :key="val" :value="val">{{ label }}</option>
          </select>
        </div>
      </div>
      <table class="table">
        <thead>
          <tr><th>入库单号</th><th>供应商</th><th>状态</th><th>创建时间</th><th class="text-right"></th></tr>
        </thead>
        <tbody>
          <tr v-for="r in asns.records" :key="r.id">
            <td class="font-medium num">{{ r.asnNo }}</td>
            <td class="text-muted">{{ r.supplierName || '—' }}</td>
            <td><StatusBadge :label="ASN_STATUS[r.status]" :tone="asnTone(r.status)" /></td>
            <td class="text-muted">{{ fmtDateTime(r.createdAt) }}</td>
            <td class="text-right"><button class="btn-ghost !h-8" @click="openDetail(r)">详情</button></td>
          </tr>
          <tr v-if="asns.records.length === 0">
            <td colspan="5" class="text-center text-muted py-10">暂无入库单，点击右上角新建。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="query.pageNum" :page-size="query.pageSize" :total="asns.total" @change="(p) => { query.pageNum = p; loadAsns() }" />
    </div>

    <!-- 新建入库单 -->
    <Modal v-if="showCreate" title="新建入库单" width="max-w-2xl" @close="showCreate = false">
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
            <label class="field-label">供应商</label>
            <input v-model="createForm.supplierName" class="input" placeholder="供应商名称" />
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
            <input v-model="line.expectedQty" class="input !w-24" placeholder="数量" />
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
    <Modal v-if="showDetail && detail" :title="detail.asn.asnNo" width="max-w-3xl" @close="showDetail = false">
      <div class="flex flex-wrap gap-x-8 gap-y-2 text-sm mb-4">
        <div><span class="text-muted">供应商：</span>{{ detail.asn.supplierName || '—' }}</div>
        <div><span class="text-muted">状态：</span><StatusBadge class="ml-1" :label="ASN_STATUS[detail.asn.status]" :tone="asnTone(detail.asn.status)" /></div>
        <div><span class="text-muted">创建：</span>{{ fmtDateTime(detail.asn.createdAt) }}</div>
      </div>
      <table class="table !rounded-lg border border-line">
        <thead>
          <tr>
            <th>物料</th><th class="text-right">预期</th><th class="text-right">已收</th>
            <th class="text-right">合格</th><th class="text-right">已上架</th><th class="text-right"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in detail.lines" :key="l.id">
            <td>{{ skuLabel(l.skuId) }}<span v-if="l.lotNo" class="text-muted text-xs"> · {{ l.lotNo }}</span></td>
            <td class="text-right num">{{ fmtQty(l.expectedQty) }}</td>
            <td class="text-right num">{{ fmtQty(l.receivedQty) }}</td>
            <td class="text-right num">{{ fmtQty(l.qualifiedQty) }}</td>
            <td class="text-right num">{{ fmtQty(l.putawayQty) }}</td>
            <td class="text-right whitespace-nowrap">
              <button class="btn-ghost !h-7 !px-2 text-sm" @click="openReceive(l)">收货</button>
              <button class="btn-ghost !h-7 !px-2 text-sm" @click="openPutaway(l)">上架</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Modal>

    <!-- 收货 / 上架 -->
    <Modal v-if="action" :title="action.type === 'receive' ? '收货（质检）' : '上架'"
           width="max-w-md" @close="action = null">
      <div v-if="action.type === 'receive'" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="field-label">数量</label>
            <input v-model="action.qty" class="input" type="number" min="0" placeholder="收货数量" />
          </div>
          <div>
            <label class="field-label">质检结论</label>
            <select v-model="action.qcResult" class="select">
              <option :value="1">合格</option>
              <option :value="2">不合格</option>
            </select>
          </div>
        </div>
        <div>
          <label class="field-label">备注</label>
          <input v-model="action.remark" class="input" placeholder="可选" />
        </div>
      </div>
      <div v-else class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="field-label">上架数量</label>
            <input v-model="action.qty" class="input" type="number" min="0" placeholder="数量" />
          </div>
          <div>
            <label class="field-label">目标货位</label>
            <select v-model="action.locationId" class="select">
              <option value="" disabled>选择货位</option>
              <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.code }}</option>
            </select>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="action = null">取消</button>
        <button class="btn-primary" @click="submitAction">确认</button>
      </template>
    </Modal>
  </div>
</template>
