import request from './request'

export function importCoursesApi(formData) {
  return request.post('/admin/importCourses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function importStudentClassesApi(formData) {
  return request.post('/admin/importStudentClasses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function listStudentsByTeachingClassApi(teachingClassId) {
  return request.post('/admin/listStudentsByTeachingClass', { teachingClassId })
}

export function listTeachingClassesByStudentApi(studentId) {
  return request.post('/admin/listTeachingClassesByStudent', { studentId })
}

export function removeStudentFromClassApi(scId) {
  return request.post('/admin/removeStudentFromClass', { scId })
}
