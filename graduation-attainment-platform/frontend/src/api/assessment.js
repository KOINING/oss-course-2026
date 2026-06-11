import axios from 'axios'
import request from './request'
import { getToken } from '@/utils/auth'

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

function authorizedAxios() {
  const token = getToken()
  return axios.create({
    baseURL: apiBaseURL,
    timeout: 20000,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}

export function listAssessmentPointsApi(data = {}) {
  return request.get('/assessment-points', { params: data })
}

export function addAssessmentPointApi(data) {
  return request.post('/assessment-points', data)
}

export function updateAssessmentPointApi(data) {
  return request.put(`/assessment-points/${data.apId}`, data)
}

export function deleteAssessmentPointApi(data) {
  return request.delete(`/assessment-points/${data.apId}`)
}

export function listCourseObjectivesApi(data = {}) {
  const params = {}
  if (data?.objectiveCode) params.objectiveCode = data.objectiveCode
  if (data?.courseId) params.courseId = data.courseId
  return request.get('/course-objectives', { params })
}

export function listInstructorTeachingClassesApi(data = {}) {
  return request.post('/teacherContext/listMyTeachingClasses', data)
}

export function listStudentsByClassApi(data) {
  return request.post('/teacherContext/listMyClassStudents', data)
}

export function getTemplatePreviewDataApi(data) {
  return request.post('/teacher/previewTemplate', null, {
    params: { classId: data.classId },
  })
}

export function downloadTemplateApi(classId) {
  return authorizedAxios().get('/teacher/downloadTemplate', {
    params: { classId },
    responseType: 'blob',
  })
}

export function importScorePreviewApi(data) {
  return request.post('/teacher/importScorePreview', data)
}

export function saveScoresApi(data) {
  return request.post('/teacher/saveScores', data)
}

export function getScoreImportContextApi(data) {
  return request.post('/teacherContext/getScoreImportContext', data)
}

export function calculateCourseLevelApi(data) {
  return request.post('/teacher/calcCourseAchievement', data)
}

export function getCourseObjectiveDashboardApi(data) {
  return request.post('/teacher/getCourseObjectiveDashboard', null, {
    params: { classId: data.classId },
  })
}

export function requestUnlockApi(data) {
  return request.post('/teacher/requestUnlock', data)
}

export function getCourseCalcResultApi(data) {
  return request.post('/assessment/getCourseCalcResult', data)
}

export function getMacroDashboardDataApi(data) {
  return request.post('/assessment/getMacroDashboard', data)
}

export function listMajorGradeYearTermsApi(data = {}) {
  return request.post('/assessment/listMajorGradeYearTerms', data)
}

export function calculateMajorLevelApi(data) {
  return request.post('/teacher/calcMajorAchievement', data)
}

export function getMajorCalcResultApi(data) {
  return request.post('/assessment/getMajorCalcResult', data)
}

export function approveUnlockApi(data) {
  return request.post('/assessment/approveUnlock', data)
}

export function exportMajorReportApi(data) {
  return authorizedAxios().post('/assessment/exportMajorReport', data, {
    responseType: 'blob',
  })
}
