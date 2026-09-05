import http from './http'

// —— 认证 ——
export const login = (payload) => http.post('/auth/login', payload)
export const logout = () => http.post('/auth/logout')

// —— 基础数据 ——
export const listWarehouses = (params) => http.get('/base/warehouses', { params })
export const listLocations = (params) => http.get('/base/locations', { params })
export const listSkus = (params) => http.get('/base/skus', { params })
export const createSku = (payload) => http.post('/base/skus', payload)
export const updateSku = (id, payload) => http.put(`/base/skus/${id}`, payload)
export const deleteSku = (id) => http.delete(`/base/skus/${id}`)

// —— 库存 ——
export const listBalances = (params) => http.get('/stock/balances', { params })
export const listLedgers = (params) => http.get('/stock/ledgers', { params })
export const recalculate = (params) => http.get('/stock/recalculate', { params })

// —— 入库 ——
export const listAsns = (params) => http.get('/inbound/asns', { params })
export const createAsn = (payload) => http.post('/inbound/asns', payload)
export const getAsn = (id) => http.get(`/inbound/asns/${id}`)
export const receiveAsnLine = (payload) => http.post('/inbound/receive', payload)
export const putawayAsnLine = (payload) => http.post('/inbound/putaway', payload)

// —— 出库 ——
export const listOrders = (params) => http.get('/outbound/orders', { params })
export const createOrder = (payload) => http.post('/outbound/orders', payload)
export const getOrder = (id) => http.get(`/outbound/orders/${id}`)
export const pickOrderLine = (payload) => http.post('/outbound/pick', payload)
export const shipOrder = (payload) => http.post('/outbound/ship', payload)

// —— 系统用户 ——
export const listUsers = (params) => http.get('/sys/users', { params })
export const createUser = (payload) => http.post('/sys/users', payload)
export const updateUser = (id, payload) => http.put(`/sys/users/${id}`, payload)
export const deleteUser = (id) => http.delete(`/sys/users/${id}`)
