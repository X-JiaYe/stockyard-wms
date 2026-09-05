<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: 'admin', password: '' })
const error = ref('')
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    auth.setLogin(data)
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen grid place-items-center px-4">
    <div class="w-full max-w-sm">
      <div class="flex justify-center mb-8">
        <div class="w-12 h-12 rounded-2xl bg-ink text-white grid place-items-center font-semibold text-xl">W</div>
      </div>
      <div class="text-center mb-8">
        <h1 class="text-[28px] font-semibold tracking-tight text-ink">登录 Stockyard</h1>
        <p class="text-sm text-muted mt-2">企业内部仓储管理系统</p>
      </div>

      <form class="panel panel-pad space-y-4" @submit.prevent="submit">
        <div>
          <label class="field-label" for="username">用户名</label>
          <input id="username" v-model="form.username" class="input" autocomplete="username" placeholder="admin" />
        </div>
        <div>
          <label class="field-label" for="password">密码</label>
          <input id="password" v-model="form.password" type="password" class="input" autocomplete="current-password" placeholder="请输入密码" />
        </div>

        <p v-if="error" class="text-sm text-danger">{{ error }}</p>

        <button type="submit" class="btn-primary w-full !h-11" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="text-center text-xs text-muted mt-6">默认账号 admin / admin123</p>
    </div>
  </div>
</template>
