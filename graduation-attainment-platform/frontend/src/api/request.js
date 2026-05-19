import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import { ROUTE_NAMES } from '@/utils/constants'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0 || payload.code === 200) {
        return payload.data !== undefined ? payload.data : payload
      }

      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }

    return payload
  },
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      removeToken()
      if (router.currentRoute.value.name !== ROUTE_NAMES.LOGIN) {
        router.push({ name: ROUTE_NAMES.LOGIN })
      }
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }

    return Promise.reject(error)
  },
)

export default request
