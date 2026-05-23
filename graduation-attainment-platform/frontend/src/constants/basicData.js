export const SEMESTER_OPTIONS = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 },
]

export function formatSemester(semester) {
  const item = SEMESTER_OPTIONS.find((option) => option.value === semester)
  return item ? item.label : `第${semester}学期`
}
