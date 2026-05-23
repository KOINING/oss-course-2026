<template>
  <div class="home-page">
    <el-card class="home-card">
      <template #header>
        <div class="card-header">平台简介</div>
      </template>
      <p class="intro-text">{{ PLATFORM_INTRO }}</p>
    </el-card>

    <el-card class="home-card">
      <template #header>
        <div class="card-header">当前角色职责</div>
      </template>

      <div class="role-tags">
        <span class="role-tags-label">当前具备角色</span>
        <el-tag
          v-for="role in roleDetails"
          :key="role.code"
          type="primary"
          effect="plain"
        >
          {{ role.label }}
        </el-tag>
        <span v-if="!roleDetails.length" class="role-empty">角色信息待加载</span>
      </div>

      <div v-if="roleDetails.length" class="role-list">
        <div v-for="role in roleDetails" :key="role.code" class="role-item">
          <h3>{{ role.label }}</h3>
          <p>{{ role.summary }}</p>
        </div>
      </div>

      <el-empty
        v-else
        description="尚未获取到当前用户角色，稍后刷新页面后会自动恢复。"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getRoleDetails, PLATFORM_INTRO } from '@/config/navigation'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const roleDetails = computed(() => getRoleDetails(userStore.roleCodes))
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 20px;
  padding: 20px;
}

.home-card {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.card-header {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.intro-text {
  margin: 0;
  color: #4b5563;
  line-height: 1.8;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.role-tags-label {
  color: #6b7280;
  font-size: 14px;
}

.role-empty {
  color: #909399;
  font-size: 14px;
}

.role-list {
  display: grid;
  gap: 16px;
}

.role-item {
  padding: 18px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  border: 1px solid #e5e7eb;
  border-radius: 14px;
}

.role-item h3 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 16px;
}

.role-item p {
  margin: 0;
  color: #4b5563;
  line-height: 1.75;
}
</style>
