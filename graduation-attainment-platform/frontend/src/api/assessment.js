import request from './request'

// ==================== Assessment Point CRUD ====================

export function listAssessmentPointsApi(data = {}) {
  return request.post('/assessment/listAssessmentPoints', data)
}

export function addAssessmentPointApi(data) {
  return request.post('/assessment/addAssessmentPoint', data)
}

export function updateAssessmentPointApi(data) {
  return request.post('/assessment/updateAssessmentPoint', data)
}

export function deleteAssessmentPointApi(data) {
  return request.post('/assessment/deleteAssessmentPoint', data)
}

// ==================== Course Objective Lookup ====================

export function listCourseObjectivesApi(data = {}) {
  return request.post('/assessment/listCourseObjectives', data)
}

// ==================== Instructor Teaching Context ====================

export function listInstructorTeachingClassesApi(data = {}) {
  return request.post('/assessment/listTeachingClasses', data)
}

export function listStudentsByClassApi(data) {
  return request.post('/assessment/listStudentsByClass', data)
}

// ==================== Template Preview ====================

export function getTemplatePreviewDataApi(data) {
  return request.post('/assessment/getTemplatePreviewData', data)
}

// ==================== Score Import ====================

export function importScoresApi(formData) {
  return request.post('/assessment/importScores', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getScoreImportContextApi(data) {
  return request.post('/assessment/getScoreImportContext', data)
}

// ==================== Course Calculation ====================

export function calculateCourseLevelApi(data) {
  return request.post('/assessment/calculateCourseLevel', data)
}

export function getCourseCalcResultApi(data) {
  return request.post('/assessment/getCourseCalcResult', data)
}

// ==================== Macro Dashboard ====================

export function getMacroDashboardDataApi(data) {
  return request.post('/assessment/getMacroDashboard', data)
}

export function listMajorGradeYearTermsApi(data = {}) {
  return request.post('/assessment/listMajorGradeYearTerms', data)
}

// ==================== Major Aggregation ====================

export function calculateMajorLevelApi(data) {
  return request.post('/assessment/calculateMajorLevel', data)
}

export function getMajorCalcResultApi(data) {
  return request.post('/assessment/getMajorCalcResult', data)
}
