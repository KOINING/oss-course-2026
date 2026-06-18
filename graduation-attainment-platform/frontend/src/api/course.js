import request from './request'

export function listCoursesByPageApi(data = {}) {
  return request.post('/admin/listCoursesByPage', data)
}

export async function listCoursesApi(data = {}) {
  const pageResult = await request.post('/admin/listCoursesByPage', data)
  return pageResult.records
}

export function listCourseGradeYearsApi() {
  return request.post('/admin/listGradeYears')
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
