import request from './request'

export function listStudentsApi(data = {}) {
  return request.post('/admin/listStudents', data)
}

export function addStudentApi(data) {
  return request.post('/admin/saveStudent', data)
}

export function updateStudentApi(data) {
  return request.post('/admin/saveStudent', data)
}

export function deleteStudentApi(data) {
  return request.post('/admin/deleteStudent', data)
}

export function updateStudentStatusApi(data) {
  return request.post('/admin/updateStudentStatus', data)
}

export function listStudentsForSelectApi() {
  return request.post('/admin/listStudentsForSelect')
}

export function listStudentEnrollmentYearsApi() {
  return request.post('/admin/listStudentEnrollmentYears')
}
