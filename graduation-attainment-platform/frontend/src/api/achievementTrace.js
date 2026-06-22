import request from './request'

export function getMajorToCourseTraceApi(data) {
  return request.post('/achievementTrace/getMajorToCourseTrace', data)
}

export function getCourseToObjectiveTraceApi(data) {
  return request.post('/achievementTrace/getCourseToObjectiveTrace', data)
}

export function getObjectiveToScoreTraceApi(data) {
  return request.post('/achievementTrace/getObjectiveToScoreTrace', data)
}

export function exportAchievementLedgerApi(data) {
  return request.post('/achievementTrace/exportAchievementLedger', data, {
    responseType: 'blob',
    timeout: 120000,
    suppressGlobalError: true,
  })
}
