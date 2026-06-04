import request from './request'

export function listCollegesApi(params = {}) {
  return request.get('/colleges', { params })
}

export function addCollegeApi(data) {
  return request.post('/colleges', data)
}

export function updateCollegeApi(data) {
  return request.put(`/colleges/${data.collegeId}`, data)
}

export function deleteCollegeApi(data) {
  return request.delete(`/colleges/${data.collegeId}`)
}
