<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, default: '' },
  tone: { type: String, default: 'gray' } // gray | blue | indigo | green | red
})

const TONES = {
  gray: 'bg-canvas text-muted',
  blue: 'bg-accent/10 text-accent',
  indigo: 'bg-indigo-500/10 text-indigo-600',
  green: 'bg-emerald-500/15 text-emerald-700',
  red: 'bg-danger/10 text-danger'
}

const DOT = {
  gray: 'bg-muted',
  blue: 'bg-accent',
  indigo: 'bg-indigo-500',
  green: 'bg-emerald-500',
  red: 'bg-danger'
}

const cls = computed(() => TONES[props.tone] || TONES.gray)
const dot = computed(() => DOT[props.tone] || DOT.gray)
</script>

<template>
  <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="cls">
    <span class="w-1.5 h-1.5 rounded-full" :class="dot" />
    {{ label }}
  </span>
</template>
