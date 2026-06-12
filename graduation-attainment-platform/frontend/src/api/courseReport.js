import request from './request'

/**
 * 获取课程级评价报表数据
 * @param {Object} params - { courseId, gradeYear }
 */
export function getCourseReportApi(params) {
  return request.post('/teacher/report/data', params)
}

/**
 * 导出课程级评价报表 - Excel格式
 * @param {Object} params - { courseId, gradeYear }
 * @returns {Promise<Blob>}
 */
export function exportCourseReportExcelApi(params) {
  return request.post('/teacher/report/export/excel', params, {
    responseType: 'blob',
  })
}

/**
 * 导出课程级评价报表 - PDF格式
 * @param {Object} params - { courseId, gradeYear }
 * @returns {Promise<Blob>}
 */
export function exportCourseReportPdfApi(params) {
  return request.post('/teacher/report/export/pdf', params, {
    responseType: 'blob',
  })
}
