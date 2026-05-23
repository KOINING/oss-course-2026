import request from './request'

export function listAcademicTermsApi(data = {}) {
  return request.post('/basic/academicTerm/list', data)
}

export function addAcademicTermApi(data) {
  return request.post('/basic/academicTerm/add', data)
}

export function updateAcademicTermApi(data) {
  return request.post('/basic/academicTerm/update', data)
}

export function deleteAcademicTermApi(data) {
  return request.post('/basic/academicTerm/delete', data)
}
