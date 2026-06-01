import request from './request'

export function listCoursesApi(data = {}) {
  return request.post('/admin/listCourses', data)
}

export function addCourseApi(data) {
  return request.post('/admin/saveCourse', data)
}

export function updateCourseApi(data) {
  return request.post('/admin/saveCourse', data)
}

export function updateCourseStatusApi(data) {
  return request.post('/admin/updateCourseStatus', data)
}

export function deleteCourseApi(data) {
  return request.post('/admin/deleteCourse', data)
}

export function importCoursesApi(formData) {
  return request.post('/admin/importCourses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
