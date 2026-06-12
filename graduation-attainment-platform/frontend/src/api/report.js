import request from './request'

/**
 * 获取专业级评价报告数据（报表统一结果源）
 * @param {Object} params - { majorId, gradeYear, termId? }
 */
export function getMajorReportApi(params) {
  return request.post('/report/majorReport', params)
}

/**
 * 触发浏览器下载文件
 * @param {Blob} blob
 * @param {string} filename
 */
export function triggerDownload(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
