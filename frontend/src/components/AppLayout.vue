<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { logout as apiLogout } from '../api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const NAV = [
  { name: 'dashboard', label: '概览', icon: 'grid' },
  { name: 'stock', label: '库存', icon: 'stack' },
  { name: 'inbound', label: '入库', icon: 'in' },
  { name: 'outbound', label: '出库', icon: 'out' },
  { name: 'skus', label: '物料', icon: 'cube' },
  { name: 'users', label: '用户', icon: 'person', adminOnly: true }
]

const items = computed(() => NAV.filter((n) => !n.adminOnly || auth.isAdmin))

async function handleLogout() {
  try {
    await apiLogout()
  } catch {
    // 登出接口失败（如 Redis 不可用）仍继续本地清理
  }
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen flex">
    <!-- 侧边栏：半透明 + 毛玻璃，类似 macOS 设置面板 -->
    <aside class="w-[232px] shrink-0 bg-white/70 backdrop-blur-xl border-r border-line flex flex-col sticky top-0 h-screen">
      <div class="px-5 pt-6 pb-5">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-[9px] bg-ink text-white grid place-items-center font-semibold text-sm">W</div>
          <div class="leading-tight">
            <div class="text-[15px] font-semibold text-ink">Stockyard</div>
            <div class="text-[11px] text-muted tracking-wide">仓储管理系统</div>
          </div>
        </div>
      </div>

      <nav class="px-3 flex-1 space-y-0.5">
        <router-link
          v-for="item in items"
          :key="item.name"
          :to="{ name: item.name }"
          class="flex items-center gap-3 h-9 px-3 rounded-lg text-sm transition-colors"
          :class="route.name === item.name ? 'bg-accent/10 text-accent font-medium' : 'text-ink/80 hover:bg-black/[0.04]'"
        >
          <svg viewBox="0 0 24 24" class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
            <template v-if="item.icon === 'grid'">
              <rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" />
            </template>
            <template v-else-if="item.icon === 'stack'">
              <path d="M12 3l9 5-9 5-9-5 9-5z" /><path d="M3 13l9 5 9-5" />
            </template>
            <template v-else-if="item.icon === 'in'">
              <path d="M12 3v12" /><path d="M7 10l5 5 5-5" /><path d="M4 21h16" />
            </template>
            <template v-else-if="item.icon === 'out'">
              <path d="M12 21V9" /><path d="M7 14l5-5 5 5" /><path d="M4 3h16" />
            </template>
            <template v-else-if="item.icon === 'cube'">
              <path d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3z" /><path d="M12 12l8-4.5" /><path d="M12 12v9" /><path d="M12 12L4 7.5" />
            </template>
            <template v-else-if="item.icon === 'person'">
              <circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 3.6-6 8-6s8 2 8 6" />
            </template>
          </svg>
          {{ item.label }}
        </router-link>
      </nav>

      <div class="p-3 border-t border-line">
        <div class="flex items-center gap-3 px-2 py-2">
          <div class="w-8 h-8 rounded-full bg-accent/15 text-accent grid place-items-center font-semibold text-sm">
            {{ auth.displayName.slice(0, 1) }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-ink truncate">{{ auth.displayName }}</div>
            <div class="text-[11px] text-muted">{{ auth.isAdmin ? '管理员' : '操作员' }}</div>
          </div>
          <button class="text-muted hover:text-danger transition-colors" title="退出登录" @click="handleLogout">
            <svg viewBox="0 0 24 24" class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" />
            </svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 min-w-0">
      <div class="max-w-6xl mx-auto px-8 py-8">
        <router-view />
      </div>
    </main>
  </div>
</template>
