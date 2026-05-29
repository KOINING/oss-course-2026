import request from './request'

export function listCoursesApi(data = {}) {
  return request.post('/basic/course/list', data)
}

export function addCourseApi(data) {
  return request.post('/basic/course/add', data)
}

export function updateCourseApi(data) {
  return request.post('/basic/course/update', data)
}

export function deleteCourseApi(data) {
  return request.post('/basic/course/delete', data)
}

export function importCoursesApi(data) {
  return request.post('/basic/course/import', data)
}

export function getCourseImportTemplateApi() {
  return request.get('/basic/course/import-template')
}
