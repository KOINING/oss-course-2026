import request from './request'

export function listAcademicTermsApi(params = {}) {
  return request.get('/academic-terms', { params })
}

export function addAcademicTermApi(data) {
  return request.post('/academic-terms', data)
}

export function updateAcademicTermApi(data) {
  return request.put(`/academic-terms/${data.termId}`, data)
}

export function deleteAcademicTermApi(data) {
  return request.delete(`/academic-terms/${data.termId}`)
}
