import request from './request'

export function listStudentsApi(data = {}) {
  return request.post('/basic/student/list', data)
}

export function addStudentApi(data) {
  return request.post('/basic/student/add', data)
}

export function updateStudentApi(data) {
  return request.post('/basic/student/update', data)
}

export function deleteStudentApi(data) {
  return request.post('/basic/student/delete', data)
}

export function updateStudentStatusApi(data) {
  return request.post('/basic/student/updateStatus', data)
}

export function importStudentsApi(data) {
  return request.post('/basic/student/import', data)
}

export function getStudentImportTemplateApi() {
  return request.get('/basic/student/import-template')
}
