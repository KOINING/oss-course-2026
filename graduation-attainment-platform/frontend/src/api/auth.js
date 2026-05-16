import request from './request'

/**
 * 登录
 * @param {{ username: string, password: string }} data
 * @returns {Promise<{ token: string, userInfo?: object }>}
 */
export function loginApi(data) {
  return request.post('/auth/login', data)
}
