<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listUsers, createUser, updateUser, deleteUser, listWarehouses } from '../api'
import Pagination from '../components/Pagination.vue'
import Modal from '../components/Modal.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const query = reactive({ pageNum: 1, pageSize: 10, username: '' })
const users = ref({ records: [], total: 0 })
const warehouses = ref([])

const showForm = ref(false)
const editing = ref(null)
const form = reactive({ username: '', password: '', nickname: '', status: 1, warehouseId: '', roleCodes: [] })
const saving = ref(false)

const ROLES = ['ADMIN', 'OPERATOR']

function warehouseName(id) {
  const w = warehouses.value.find((x) => x.id === id)
  return w ? w.name : id ? `#${id}` : '—'
}

function load() {
  const p = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.username) p.username = query.username
  listUsers(p).then((d) => (users.value = d || { records: [], total: 0 }))
}

function openCreate() {
  editing.value = null
  Object.assign(form, { username: '', password: '', nickname: '', status: 1, warehouseId: '', roleCodes: [] })
  showForm.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    status: row.status,
    warehouseId: row.warehouseId || '',
    roleCodes: []
  })
  showForm.value = true
}

async function submit() {
  saving.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password || undefined,
      nickname: form.nickname,
      status: Number(form.status),
      warehouseId: form.warehouseId || null,
      roleCodes: form.roleCodes
    }
    if (editing.value) await updateUser(editing.value.id, payload)
    else await createUser(payload)
    showForm.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  if (!window.confirm(`确认删除用户「${row.username}」？`)) return
  await deleteUser(row.id)
  load()
}

onMounted(async () => {
  load()
  const w = await listWarehouses({ pageNum: 1, pageSize: 100 })
  warehouses.value = w?.records || []
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h1 class="page-title">用户</h1>
        <p class="page-sub">RBAC 用户与角色、仓库归属（仅管理员可管理）。</p>
      </div>
      <button class="btn-primary" @click="openCreate">新建用户</button>
    </div>

    <div class="panel mt-6 overflow-hidden">
      <div class="px-6 py-4 border-b border-line flex gap-3 items-end">
        <div>
          <label class="field-label">用户名</label>
          <input v-model="query.username" class="input !w-48" placeholder="按用户名查询" @keyup.enter="query.pageNum = 1; load()" />
        </div>
        <button class="btn-primary" @click="query.pageNum = 1; load()">查询</button>
      </div>
      <table class="table">
        <thead>
          <tr><th>用户名</th><th>昵称</th><th>仓库</th><th>状态</th><th>创建时间</th><th class="text-right"></th></tr>
        </thead>
        <tbody>
          <tr v-for="r in users.records" :key="r.id">
            <td class="font-medium">{{ r.username }}</td>
            <td>{{ r.nickname || '—' }}</td>
            <td class="text-muted">{{ warehouseName(r.warehouseId) }}</td>
            <td><StatusBadge :label="r.status === 1 ? '启用' : '停用'" :tone="r.status === 1 ? 'green' : 'gray'" /></td>
            <td class="text-muted">{{ String(r.createdAt || '').replace('T', ' ').slice(0, 19) }}</td>
            <td class="text-right whitespace-nowrap">
              <button class="btn-ghost !h-8 !px-2 text-sm" @click="openEdit(r)">编辑</button>
              <button class="btn-ghost !h-8 !px-2 text-sm text-danger" :disabled="r.username === auth.username" @click="remove(r)">删除</button>
            </td>
          </tr>
          <tr v-if="users.records.length === 0">
            <td colspan="6" class="text-center text-muted py-10">暂无用户。</td>
          </tr>
        </tbody>
      </table>
      <Pagination :page-num="query.pageNum" :page-size="query.pageSize" :total="users.total" @change="(p) => { query.pageNum = p; load() }" />
    </div>

    <Modal v-if="showForm" :title="editing ? '编辑用户' : '新建用户'" width="max-w-xl" @close="showForm = false">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="field-label">用户名</label>
          <input v-model="form.username" class="input" :disabled="!!editing" placeholder="登录账号" />
        </div>
        <div>
          <label class="field-label">密码</label>
          <input v-model="form.password" type="password" class="input" :placeholder="editing ? '留空则不修改' : '登录密码'" />
        </div>
        <div>
          <label class="field-label">昵称</label>
          <input v-model="form.nickname" class="input" placeholder="显示名称" />
        </div>
        <div>
          <label class="field-label">状态</label>
          <select v-model="form.status" class="select">
            <option :value="1">启用</option><option :value="0">停用</option>
          </select>
        </div>
        <div>
          <label class="field-label">仓库（仅 OPERATOR 必填）</label>
          <select v-model="form.warehouseId" class="select">
            <option value="">— 无（管理员）</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div>
          <label class="field-label">角色</label>
          <div class="flex gap-3 h-10 items-center">
            <label v-for="r in ROLES" :key="r" class="flex items-center gap-1.5 text-sm">
              <input v-model="form.roleCodes" type="checkbox" :value="r" class="accent-[#0071e3] w-4 h-4" />
              {{ r === 'ADMIN' ? '管理员' : '操作员' }}
            </label>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="showForm = false">取消</button>
        <button class="btn-primary" :disabled="saving" @click="submit">{{ saving ? '保存中…' : '保存' }}</button>
      </template>
    </Modal>
  </div>
</template>
