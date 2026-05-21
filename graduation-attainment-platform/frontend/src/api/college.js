import request from './request'

export function listCollegesApi(data = {}) {
  return request.post('/basic/college/list', data)
}

export function addCollegeApi(data) {
  return request.post('/basic/college/add', data)
}

export function updateCollegeApi(data) {
  return request.post('/basic/college/update', data)
}

export function deleteCollegeApi(data) {
  return request.post('/basic/college/delete', data)
}
