import request from './request'

// ========== 基础数据查询 ==========

export function listMajorsForMatrixApi() {
    return request.post('/supportMatrix/listMajors')
}

export function listCoursesApi(data = {}) {
    return request.post('/supportMatrix/listCourses', data)
}

export function listAcademicTermsApi() {
    return request.post('/supportMatrix/listAcademicTerms')
}

export function listIndicatorPointsForMatrixApi(data = {}) {
    return request.post('/supportMatrix/listIndicatorPoints', data)
}

export function listGraduationRequirementsApi(data = {}) {
    return request.post('/supportMatrix/listGraduationRequirements', data)
}

// ========== 矩阵数据 ==========

export function getSupportMatrixApi(data = {}) {
    return request.post('/supportMatrix/getSupportMatrix', data)
}

export function saveSupportMatrixApi(data) {
    return request.post('/supportMatrix/saveSupportMatrix', data)
}

export function resetSupportMatrixApi(data) {
    return request.post('/supportMatrix/resetSupportMatrix', data)
}