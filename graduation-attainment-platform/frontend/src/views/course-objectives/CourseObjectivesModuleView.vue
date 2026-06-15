<template>
  <div class="module-b-page">
    <el-card class="module-shell-card">
      <el-tabs v-model="activeTab" class="module-tabs">
        <el-tab-pane label="课程目标配置" name="objectives" lazy>
          <CourseObjectivesView />
        </el-tab-pane>
        <el-tab-pane label="内部权重配置" name="weights" lazy>
          <CourseWeightView />
        </el-tab-pane>
        <el-tab-pane label="考核点配置" name="assessment-points" lazy>
          <AssessmentPointConfig />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CourseObjectivesView from './CourseObjectivesView.vue'
import CourseWeightView from '@/views/requirements/CourseWeightView.vue'
import AssessmentPointConfig from '@/views/assessment/AssessmentPointConfig.vue'

const route = useRoute()
const router = useRouter()
const validTabs = new Set(['objectives', 'weights', 'assessment-points'])

const activeTab = ref(validTabs.has(route.query.tab) ? route.query.tab : 'objectives')

watch(
  () => route.query.tab,
  (tab) => {
    if (validTabs.has(tab) && tab !== activeTab.value) {
      activeTab.value = tab
    }
  },
)

watch(activeTab, (tab) => {
  if (route.query.tab === tab) {
    return
  }
  router.replace({
    query: {
      ...route.query,
      tab,
    },
  })
})
</script>

<style scoped>
.module-b-page {
  padding: 20px;
}

.module-shell-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.module-shell-card :deep(.el-card__body) {
  padding: 0;
}

.module-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 24px;
}

.module-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e5e7eb;
}

.module-tabs :deep(.el-tabs__item) {
  height: 54px;
  padding: 0 28px;
  font-size: 18px;
  font-weight: 500;
}

.module-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 600;
}

.module-tabs :deep(.el-tabs__content) {
  padding: 28px 24px 24px;
}
</style>
