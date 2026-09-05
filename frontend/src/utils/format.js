// 状态字典与展示格式化（口径与后端枚举一致）

export const ASN_STATUS = {
  10: '待收货',
  20: '收货中',
  30: '已收货',
  40: '已完成',
  90: '已取消'
}

export const OUTBOUND_STATUS = {
  10: '待拣货',
  20: '拣货中',
  30: '已拣货',
  40: '已发运',
  90: '已取消'
}

export const QC_RESULT = {
  1: '合格',
  2: '不合格'
}

export const DIRECTION = {
  1: '入库',
  2: '出库'
}

// 状态徽章配色：gray / blue / indigo / green / red
export function asnTone(status) {
  return { 10: 'gray', 20: 'blue', 30: 'indigo', 40: 'green', 90: 'gray' }[status] || 'gray'
}

export function outboundTone(status) {
  return { 10: 'gray', 20: 'blue', 30: 'indigo', 40: 'green', 90: 'gray' }[status] || 'gray'
}

export function qcTone(qc) {
  return qc === 1 ? 'green' : 'red'
}

export function fmtQty(v) {
  if (v === null || v === undefined) return '—'
  const n = Number(v)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

export function fmtDateTime(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 19)
}

// 是否可用（状态 1 = 启用）
export function enabled(status) {
  return status === 1
}
