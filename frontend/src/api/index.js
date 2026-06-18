import request from '@/utils/request'

export const screenList = () => request({ url: '/screen/list', method: 'get' })
export const screenListEnabled = () => request({ url: '/screen/list-enabled', method: 'get' })
export const screenGet = id => request({ url: `/screen/${id}`, method: 'get' })
export const screenSave = data => request({ url: '/screen', method: 'post', data })
export const screenUpdate = data => request({ url: '/screen', method: 'put', data })
export const screenDelete = id => request({ url: `/screen/${id}`, method: 'delete' })

export const materialSubmit = data => request({ url: '/material/submit', method: 'post', data })
export const materialAudit = data => request({ url: '/material/audit', method: 'post', data })
export const materialListAudited = () => request({ url: '/material/list-audited', method: 'get' })
export const materialPage = params => request({ url: '/material/page', method: 'get', params })
export const materialGet = id => request({ url: `/material/${id}`, method: 'get' })
export const materialDelete = id => request({ url: `/material/${id}`, method: 'delete' })

export const scheduleCreate = data => request({ url: '/schedule', method: 'post', data })
export const scheduleUpdate = data => request({ url: '/schedule', method: 'put', data })
export const scheduleCancel = (id, operator) => request({ url: `/schedule/cancel/${id}`, method: 'post', params: { operator } })
export const schedulePage = params => request({ url: '/schedule/page', method: 'get', params })
export const scheduleGet = id => request({ url: `/schedule/${id}`, method: 'get' })
export const scheduleListByScreenDate = params => request({ url: '/schedule/list-by-screen-date', method: 'get', params })
export const scheduleCheckConflict = params => request({ url: '/schedule/check-conflict', method: 'get', params })
export const scheduleDelete = id => request({ url: `/schedule/${id}`, method: 'delete' })

export const proofSubmit = data => request({ url: '/proof/submit', method: 'post', data })
export const proofPage = params => request({ url: '/proof/page', method: 'get', params })
export const proofListBySchedule = scheduleId => request({ url: `/proof/list-by-schedule/${scheduleId}`, method: 'get' })
export const proofGet = id => request({ url: `/proof/${id}`, method: 'get' })
