<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="router.push(DEFAULT_HOME_PATH)">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DEFAULT_HOME_PATH } from '@/utils/constants'

const router = useRouter()
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 16px;
}
h1 {
  font-size: 80px;
  margin: 0;
  color: #909399;
}
</style>