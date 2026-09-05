<script setup>
import { computed } from 'vue'

const props = defineProps({
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 }
})
const emit = defineEmits(['change'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const from = computed(() => (props.total === 0 ? 0 : (props.pageNum - 1) * props.pageSize + 1))
const to = computed(() => Math.min(props.pageNum * props.pageSize, props.total))

function go(p) {
  if (p >= 1 && p <= totalPages.value && p !== props.pageNum) emit('change', p)
}
</script>

<template>
  <div class="flex items-center justify-between px-6 py-3 text-sm text-muted">
    <span>共 {{ total }} 条，第 {{ from }}–{{ to }} 条</span>
    <div class="flex items-center gap-1">
      <button class="btn-secondary !h-8 !px-2.5" :disabled="pageNum <= 1" @click="go(pageNum - 1)">上一页</button>
      <span class="px-2 num">{{ pageNum }} / {{ totalPages }}</span>
      <button class="btn-secondary !h-8 !px-2.5" :disabled="pageNum >= totalPages" @click="go(pageNum + 1)">下一页</button>
    </div>
  </div>
</template>
