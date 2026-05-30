import request from './request'

// 查询支撑矩阵列表
export function listSupportMatrixApi(params) {
  return request.post('/matrix/list', params)
}

// 批量保存支撑矩阵配置
export function saveSupportMatrixApi(data) {
  return request.post('/matrix/save', data)
}

// 校验支撑矩阵权重是否配平
export function validateMatrixWeightApi(data) {
  return request.post('/matrix/validateWeight', data)
}
