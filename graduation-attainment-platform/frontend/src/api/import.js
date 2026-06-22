import request from './request'

function postMultipart(url, formData) {
  return request.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function importCoursesApi(formData) {
  return postMultipart('/admin/importCourses', formData)
}

export function importStudentsApi(formData) {
  return postMultipart('/admin/importStudents', formData)
}

export function importStudentClassesApi(formData) {
  return postMultipart('/admin/importStudentClasses', formData)
}

export function generateStudentClassesApi(teachingClassId) {
  return request.post('/admin/generateStudentClasses', { teachingClassId })
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
