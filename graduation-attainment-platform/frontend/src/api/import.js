import request from './request'

// 全专业课程清单导入（Excel）
export function importCoursesApi(formData) {
  return request.post('/admin/importCourses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 教学班学生名单导入（Excel）
export function importStudentClassesApi(formData) {
  return request.post('/admin/importStudentClasses', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 按教学班查询学生列表
export function listStudentsByTeachingClassApi(teachingClassId) {
  const params = new URLSearchParams()
  params.append('teachingClassId', teachingClassId)
  return request.post('/admin/listStudentsByTeachingClass', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

// 按学生查询所属教学班列表
export function listTeachingClassesByStudentApi(studentId) {
  const params = new URLSearchParams()
  params.append('studentId', studentId)
  return request.post('/admin/listTeachingClassesByStudent', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

// 从教学班移除学生
export function removeStudentFromClassApi(scId) {
  const params = new URLSearchParams()
  params.append('scId', scId)
  return request.post('/admin/removeStudentFromClass', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}
