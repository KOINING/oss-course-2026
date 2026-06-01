import request from './request'

export function listTeachersForSelectApi() {
  return request.post('/admin/listTeachersForSelect')
}
