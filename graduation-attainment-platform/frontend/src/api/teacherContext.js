import request from './request'

export function listMyTeachingClassesApi(data = {}) {
  return request.post('/teacherContext/listMyTeachingClasses', data)
}

export function listMyClassStudentsApi(data) {
  return request.post('/teacherContext/listMyClassStudents', data)
}

export function getScoreImportContextApi(data) {
  return request.post('/teacherContext/getScoreImportContext', data)
}
