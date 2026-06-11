import request from './request'

export function listUsersApi(data = {}) {
  return request.post('/admin/listUsers', data)
}

export function listAssignableRolesApi() {
  return request.post('/admin/listAssignableRoles')
}

export function addUserApi(data) {
  return request.post('/admin/addUser', data)
}

export function updateUserApi(data) {
  return request.post('/admin/updateUser', data)
}

export function updateUserStatusApi(data) {
  return request.post('/admin/updateUserStatus', data)
}

export function resetUserPasswordApi(data) {
  return request.post('/admin/resetUserPassword', data)
}
