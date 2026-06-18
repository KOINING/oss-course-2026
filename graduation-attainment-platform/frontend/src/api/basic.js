import request from './request'

// ==================== College ====================
export function listCollegesApi() {
    return request.post('/admin/listColleges')
}

// ==================== Major ====================
export function listMajorsApi(data = {}) {
    return request.post('/admin/listMajors', data)
}

export function listMajorsByPageApi(data = {}) {
    return request.post('/admin/listMajorsByPage', data)
}

export function listMajorsForSelectApi() {
    return request.post('/admin/listMajorsForSelect')
}

export function saveMajorApi(data) {
    return request.post('/admin/saveMajor', data)
}

export function updateMajorStatusApi(data) {
    return request.post('/admin/updateMajorStatus', data)
}

export function deleteMajorApi(majorId) {
    return request.post('/admin/deleteMajor', { majorId })
}

// ==================== Course ====================
export function listCoursesApi(data = {}) {
    return request.post('/admin/listCoursesByPage', data)
}

export function saveCourseApi(data) {
    return request.post('/admin/saveCourse', data)
}

export function updateCourseStatusApi(data) {
    return request.post('/admin/updateCourseStatus', data)
}

export function deleteCourseApi(courseId) {
    return request.post('/admin/deleteCourse', { courseId })
}