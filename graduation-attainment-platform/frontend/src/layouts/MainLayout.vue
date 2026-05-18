<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="layout-aside">
      <div class="logo">{{ APP_TITLE }}</div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <template v-for="section in visibleSections" :key="section.key">
          <el-menu-item
            v-if="section.key === 'home'"
            :index="section.children[0].path"
          >
            <el-icon>
              <component :is="resolveIcon(section.icon)" />
            </el-icon>
            <span>{{ section.children[0].label }}</span>
          </el-menu-item>

          <el-sub-menu v-else :index="section.key">
            <template #title>
              <el-icon>
                <component :is="resolveIcon(section.icon)" />
              </el-icon>
              <span>{{ section.label }}</span>
            </template>

            <el-menu-item
              v-for="item in section.children"
              :key="item.key"
              :index="item.path"
            >
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-user">
          <span class="header-greeting">欢迎，{{ userStore.displayName }}</span>
          <span class="header-roles">{{ roleLabels }}</span>
        </div>
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
import {
  DataAnalysis,
  Document,
  HomeFilled,
  Reading,
  Setting,
} from '@element-plus/icons-vue'
import { getRoleDetails, getVisibleSections } from '@/config/navigation'
import { useUserStore } from '@/stores/user'
import { APP_TITLE, ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const iconMap = {
  HomeFilled,
  Setting,
  Reading,
  DataAnalysis,
  Document,
}

const activeMenu = computed(() => route.path)

const visibleSections = computed(() =>
  getVisibleSections(userStore.roleCodes),
)

const roleLabels = computed(() => {
  const roles = getRoleDetails(userStore.roleCodes)
  return roles.length ? roles.map((role) => role.label).join(' / ') : '角色信息待加载'
})

function resolveIcon(iconName) {
  return iconMap[iconName] || HomeFilled
}

function handleLogout() {
  userStore.logout()
  router.push({ name: ROUTE_NAMES.LOGIN })
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
  min-height: 88px;
  padding: 18px 16px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
  text-align: center;
  white-space: normal;
  word-break: break-word;
  border-bottom: 1px solid #435068;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.header-user {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-greeting {
  color: #303133;
  font-weight: 600;
}

.header-roles {
  color: #909399;
  font-size: 13px;
}

.layout-main {
  background-color: #f0f2f5;
}
</style>
