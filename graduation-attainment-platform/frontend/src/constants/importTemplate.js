export const COURSE_IMPORT_TEMPLATE_FIELDS = [
  { key: 'majorCode', label: '所属专业代码', required: true },
  { key: 'courseCode', label: '课程代码', required: true },
  { key: 'courseName', label: '课程名称', required: true },
  { key: 'credits', label: '学分', required: true },
  { key: 'status', label: '状态', required: true },
]

export const STUDENT_IMPORT_TEMPLATE_FIELDS = [
  { key: 'studentId', label: '学号', required: true },
  { key: 'studentName', label: '姓名', required: true },
  { key: 'majorCode', label: '专业代码', required: true },
  { key: 'enrollmentYear', label: '入学年份', required: true },
  { key: 'teachingClassCode', label: '教学班编号', required: true },
]

export const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

export function formatStatus(status) {
  const item = STATUS_OPTIONS.find((option) => option.value === status)
  return item ? item.label : '未知'
}

export function downloadTemplate(filename, fields) {
  const headers = fields.map((f) => f.label)
  const csv = headers.join(',') + '\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
