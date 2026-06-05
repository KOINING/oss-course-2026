import request from './request'

export function listCoursesForInstructorApi(data = {}) {
  return request.post('/course-objectives/listCourses', data)
}

export function listTeachingClassesApi(data = {}) {
  return request.post('/course-objectives/listTeachingClasses', data)
}

export function getContextApi(data) {
  return request.post('/course-objectives/getContext', data)
}

export function listCourseObjectivesApi(data = {}) {
  return request.post('/course-objectives/list', data)
}

export function addCourseObjectiveApi(data) {
  return request.post('/course-objectives/add', data)
}

export function updateCourseObjectiveApi(data) {
  return request.post('/course-objectives/update', data)
}

export function deleteCourseObjectiveApi(data) {
  return request.post('/course-objectives/delete', data)
}

export function checkAssessmentPointReferencesApi(data) {
  return request.post('/course-objectives/checkAssessmentPointReferences', data)
}

export function checkWeightConfigurationApi(data) {
  return request.post('/course-objectives/checkWeightConfiguration', data)
}

export function checkSupportIndicatorPointsApi(data) {
  return request.post('/course-objectives/checkSupportIndicatorPoints', data)
}
