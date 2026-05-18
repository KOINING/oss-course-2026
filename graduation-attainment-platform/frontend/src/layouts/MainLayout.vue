<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">面向专业认证的毕业要求达成度统一计算平台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item :index="DEFAULT_HOME_PATH">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <span>欢迎，{{ userStore.userInfo?.username ?? '用户' }}</span>
        <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { DEFAULT_HOME_PATH } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  border-bottom: 1px solid #435068;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}
.layout-main {
  background-color: #f0f2f5;
}
</style>
