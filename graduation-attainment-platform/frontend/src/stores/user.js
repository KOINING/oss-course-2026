import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => Boolean(token.value))

  async function login(credentials) {
    const data = await loginApi(credentials)
    const accessToken = data?.token ?? data?.accessToken

    if (!accessToken) {
      throw new Error('登录响应缺少 token')
    }

    token.value = accessToken
    setToken(accessToken)
    userInfo.value = data?.userInfo ?? data?.user ?? null

    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    setUserInfo,
  }
})
