import request from './request'

export function listTeachingClassesApi(data = {}) {
  return request.post('/admin/listTeachingClasses', data)
}

export function listTeachingClassesByPageApi(data = {}) {
  return request.post('/admin/listTeachingClassesByPage', data)
}

export function listTeachingClassesForSelectApi() {
  return request.post('/admin/listTeachingClassesForSelect')
}

export function addTeachingClassApi(data) {
  return request.post('/admin/saveTeachingClass', data)
}

export function updateTeachingClassApi(data) {
  return request.post('/admin/saveTeachingClass', data)
}

export function deleteTeachingClassApi(data) {
  return request.post('/admin/deleteTeachingClass', data)
}

export function updateTeachingClassStatusApi(data) {
  return request.post('/admin/updateTeachingClassStatus', data)
}
