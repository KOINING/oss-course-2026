import request from './request'

export function loginApi(data) {
  return request.post('/auth/login', data)
}

export function getUserInfoApi() {
  return request.get('/auth/userinfo')
}
