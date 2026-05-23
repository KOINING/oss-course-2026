<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { APP_TITLE, DEFAULT_HOME_PATH } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
    })
    ElMessage.success('登录成功')

    const redirect = route.query.redirect || DEFAULT_HOME_PATH
    router.push(redirect)
  } catch {
    // 错误提示由请求拦截器统一处理。
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">{{ APP_TITLE }}</h1>
      <p class="login-subtitle">请输入账号密码进入系统</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #3d7ab5 100%);
}

.login-card {
  width: 420px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  line-height: 1.5;
}

.login-subtitle {
  margin: 0 0 28px;
  font-size: 14px;
  color: #909399;
  text-align: center;
}

.login-btn {
  width: 100%;
}
</style>
