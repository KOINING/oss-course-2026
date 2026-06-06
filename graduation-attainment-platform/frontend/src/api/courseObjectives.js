import request from './request'

export function listCoursesForInstructorApi(data = {}) {
  return request.post('/teacherContext/listMyTeachingClasses', data).then((rows = []) => {
    const seen = new Set()
    return rows.filter((row) => {
      if (!row?.courseId || seen.has(row.courseId)) {
        return false
      }
      seen.add(row.courseId)
      return true
    })
  })
}

export function listTeachingClassesApi(data = {}) {
  return request.post('/teacherContext/listMyTeachingClasses', data)
}

export function getContextApi(data) {
  return request
    .post('/teacherContext/listMyTeachingClasses', { courseId: data?.courseId })
    .then((rows = []) => {
      const current = rows.find((row) => row.classId === data?.teachingClassId)
      return current ? { ...current, hasSupportIndicatorPoints: true } : null
    })
}

export function listCourseObjectivesApi(data = {}) {
  return request.get('/course-objectives', { params: data })
}

export function addCourseObjectiveApi(data) {
  return request.post('/course-objectives', data)
}

export function updateCourseObjectiveApi(data) {
  return request.put(`/course-objectives/${data.coId}`, data)
}

export function deleteCourseObjectiveApi(data) {
  const coId = data?.coId ?? data?.objectiveId
  return request.delete(`/course-objectives/${coId}`)
}

export function checkAssessmentPointReferencesApi(data) {
  return request
    .get('/assessment-points', { params: { coId: data?.objectiveId ?? data?.coId } })
    .then((rows = []) => ({ isReferenced: rows.length > 0 }))
}

export function checkWeightConfigurationApi(data) {
  return Promise.resolve({ isInConfiguration: false, ...data })
}

export function checkSupportIndicatorPointsApi(data) {
  return Promise.resolve({ hasSupportIndicatorPoints: true, ...data })
}
