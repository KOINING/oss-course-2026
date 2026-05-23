import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfoApi, loginApi } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const profileLoaded = ref(false)
  let profilePromise = null

  const isLoggedIn = computed(() => Boolean(token.value))
  const roleCodes = computed(() =>
    Array.isArray(userInfo.value?.roles) ? userInfo.value.roles : [],
  )
  const displayName = computed(
    () => userInfo.value?.realName || userInfo.value?.username || '用户',
  )

  async function login(credentials) {
    const data = await loginApi(credentials)
    const accessToken = data?.token ?? data?.accessToken

    if (!accessToken) {
      throw new Error('登录响应缺少 token')
    }

    token.value = accessToken
    setToken(accessToken)
    userInfo.value = data?.userInfo ?? data?.user ?? null
    profileLoaded.value = Boolean(userInfo.value)

    return data
  }

  async function fetchUserInfo(force = false) {
    if (!token.value) {
      userInfo.value = null
      profileLoaded.value = false
      return null
    }

    if (!force && profileLoaded.value && userInfo.value) {
      return userInfo.value
    }

    if (!force && profilePromise) {
      return profilePromise
    }

    profilePromise = getUserInfoApi()
      .then((info) => {
        userInfo.value = info
        profileLoaded.value = true
        return info
      })
      .finally(() => {
        profilePromise = null
      })

    return profilePromise
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    profileLoaded.value = false
    removeToken()
  }

  function setUserInfo(info) {
    userInfo.value = info
    profileLoaded.value = Boolean(info)
  }

  return {
    token,
    userInfo,
    profileLoaded,
    isLoggedIn,
    roleCodes,
    displayName,
    login,
    fetchUserInfo,
    logout,
    setUserInfo,
  }
})
