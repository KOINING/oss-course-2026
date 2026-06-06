import request from './request'

// ========== 基础数据查询 ==========

export function listMajorsForWeightApi() {
    return request.post('/supportMatrix/listMajors')
}

export function listGradesApi() {
    return request.post('/supportMatrix/listGradeYears')
}

export function listCoursesForWeightApi(data = {}) {
    return request.post('/supportMatrix/listCourses', data)
}

export function listIndicatorPointsForWeightApi(data = {}) {
    return request.post('/supportMatrix/listIndicatorPoints', data)
}

// ========== 课程目标 ==========

export function listCourseObjectivesApi(params = {}) {
    const query = {}
    if (params?.objectiveCode) query.objectiveCode = params.objectiveCode
    if (params?.courseId) query.courseId = params.courseId
    return request.get('/course-objectives', { params: query })
}

// ========== 内部权重矩阵 ==========

export function getCourseWeightApi(data = {}) {
    return request.post('/objective-indicator-contributions/query', data)
}

export function saveCourseWeightApi(data) {
    return request.post('/objective-indicator-contributions/batch-save', data)
}
