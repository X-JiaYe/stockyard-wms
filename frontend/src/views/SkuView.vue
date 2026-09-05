<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listSkus, createSku, updateSku, deleteSku } from '../api'
import Pagination from '../components/Pagination.vue'
import Modal from '../components/Modal.vue'
import StatusBadge from '../components/StatusBadge.vue'

const query = reactive({ pageNum: 1, pageSize: 10, code: '' })
const skus = ref({ records: [], total: 0 })

const showForm = ref(false)
const editing = ref(null) // 为 null 表示新建
const form = reactive({ code: '', name: '', spec: '', unit: '', barcode: '', trackLot: 0, trackSerial: 0, status: 1 })
const saving = ref(false)

function load() {
  const p = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.code) p.code = query.code
  listSkus(p).then((d) => (skus.value = d || { records: [], total: 0 }))
}

function openCreate() {
  editing.value = null
  Object.assign(form, { code: '', name: '', spec: '', unit: '', barcode: '', trackLot: 0, trackSerial: 0, status: 1 })
  showForm.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    code: row.code, name: row.name, spec: row.spec, unit: row.unit,
    barcode: row.barcode, trackLot: row.trackLot, trackSerial: row.trackSerial, status: row.status
  })
  showForm.value = true
}

async function submit() {
  saving.value = true
  try {
    const payload = { ...form, trackLot: Number(form.trackLot), trackSerial: Number(form.trackSerial), status: Number(form.status) }
    if (editing.value) await updateSku(editing.value.id, payload)
    else await createSku(payload)
    showForm.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  if (!window.confirm(`确认删除物料「${row.code}」？`)) return
  await deleteSku(row.id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h1 class="page-title">物料</h1>
        <p class="page-sub">SKU 基础数据（计算机电子产品示例）。</p>
      </div>
      <button class="btn-primary" @click="openCreate">新建物料</button>
    </div>

    <div class="panel mt-6 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">物料编码</label>
          <input v-model="query.code" class="input !w-48" placeholder="按编码模糊查询" @keyup.enter="query.pageNum = 1; load()" />
        </div>
        <button class="btn-primary" @click="query.pageNum = 1; load()">查询</button>
      </div>
      <table class="table">
        <thead>
          <tr><th>编码</th><th>名称</th><th>规格</th><th>单位</th><th>状态</th><th class="text-right"></th></tr>
        </thead>
        <tbody>
          <tr v-for="r in skus.records" :key="r.id">
            <td class="font-medium num">{{ r.code }}</td>
            <td>{{ r.name }}</td>
            <td class="text-muted max-w-[260px] truncate">{{ r.spec || '—' }}</td>
            <td class="text-muted">{{ r.unit }}</td>
            <td><StatusBadge :label="r.status === 1 ? '启用' : '停用'" :tone="r.status === 1 ? 'green' : 'gray'" /></td>
            <td class="text-right whitespace-nowrap">
              <button class="btn-ghost !h-8 !px-2 text-sm" @click="openEdit(r)">编辑</button>
              <button class="btn-ghost !h-8 !px-2 text-sm text-danger" @click="remove(r)">删除</button>
            </td>
          </tr>
          <tr v-if="skus.records.length === 0">
            <td colspan="6" class="text-center text-muted py-10">暂无物料。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="query.pageNum" :page-size="query.pageSize" :total="skus.total" @change="(p) => { query.pageNum = p; load() }" />
    </div>

    <Modal v-if="showForm" :title="editing ? '编辑物料' : '新建物料'" width="max-w-xl" @close="showForm = false">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="field-label">编码</label>
          <input v-model="form.code" class="input" placeholder="如 CPU-INTEL-14600K" />
        </div>
        <div>
          <label class="field-label">名称</label>
          <input v-model="form.name" class="input" placeholder="物料名称" />
        </div>
        <div class="col-span-2">
          <label class="field-label">规格</label>
          <input v-model="form.spec" class="input" placeholder="规格描述" />
        </div>
        <div>
          <label class="field-label">单位</label>
          <input v-model="form.unit" class="input" placeholder="如 颗 / 条 / 块" />
        </div>
        <div>
          <label class="field-label">条码</label>
          <input v-model="form.barcode" class="input" placeholder="可选" />
        </div>
        <div>
          <label class="field-label">批次管理</label>
          <select v-model="form.trackLot" class="select">
            <option :value="0">否</option><option :value="1">是</option>
          </select>
        </div>
        <div>
          <label class="field-label">序列号管理</label>
          <select v-model="form.trackSerial" class="select">
            <option :value="0">否</option><option :value="1">是</option>
          </select>
        </div>
        <div>
          <label class="field-label">状态</label>
          <select v-model="form.status" class="select">
            <option :value="1">启用</option><option :value="0">停用</option>
          </select>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="showForm = false">取消</button>
        <button class="btn-primary" :disabled="saving" @click="submit">{{ saving ? '保存中…' : '保存' }}</button>
      </template>
    </Modal>
  </div>
</template>
