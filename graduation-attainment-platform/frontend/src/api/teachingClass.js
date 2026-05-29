import request from './request'

export function listTeachingClassesApi(data = {}) {
  return request.post('/basic/teachingClass/list', data)
}

export function addTeachingClassApi(data) {
  return request.post('/basic/teachingClass/add', data)
}

export function updateTeachingClassApi(data) {
  return request.post('/basic/teachingClass/update', data)
}

export function deleteTeachingClassApi(data) {
  return request.post('/basic/teachingClass/delete', data)
}

export function updateTeachingClassStatusApi(data) {
  return request.post('/basic/teachingClass/updateStatus', data)
}
