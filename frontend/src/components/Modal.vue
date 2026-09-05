<script setup>
defineProps({
  title: { type: String, default: '' },
  width: { type: String, default: 'max-w-lg' }
})
const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto py-10 px-4">
      <div class="fixed inset-0 bg-ink/30 backdrop-blur-[2px]" @click="emit('close')" />
      <div class="relative bg-surface rounded-2xl shadow-soft w-full" :class="width">
        <div class="flex items-center justify-between px-6 py-4 border-b border-line">
          <h3 class="text-base font-semibold text-ink">{{ title }}</h3>
          <button class="text-muted hover:text-ink text-xl leading-none p-1" aria-label="关闭" @click="emit('close')">×</button>
        </div>
        <div class="px-6 py-5">
          <slot />
        </div>
        <div v-if="$slots.footer" class="px-6 py-4 border-t border-line flex justify-end gap-2 bg-canvas/40 rounded-b-2xl">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>
