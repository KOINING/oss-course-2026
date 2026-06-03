import request from './request'

export function listGraduationRequirementsApi(data = {}) {
  return request.post('/requirements/listGraduationRequirements', data)
}

export function addGraduationRequirementApi(data) {
  return request.post('/requirements/addGraduationRequirement', data)
}

export function updateGraduationRequirementApi(data) {
  return request.post('/requirements/updateGraduationRequirement', data)
}

export function deleteGraduationRequirementApi(data) {
  return request.post('/requirements/deleteGraduationRequirement', data)
}

export function listIndicatorPointsApi(data = {}) {
  return request.post('/requirements/listIndicatorPoints', data)
}

export function addIndicatorPointApi(data) {
  return request.post('/requirements/addIndicatorPoint', data)
}

export function updateIndicatorPointApi(data) {
  return request.post('/requirements/updateIndicatorPoint', data)
}

export function deleteIndicatorPointApi(data) {
  return request.post('/requirements/deleteIndicatorPoint', data)
}

export function listMajorsApi() {
  return request.post('/requirements/listMajors')
}

export function listRequirementGradeYearsApi(data = {}) {
  return request.post('/requirements/listGradeYears', data)
}
