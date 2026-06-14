<template>
  <div class="assessment-page">
    <el-card class="assessment-card">
      <template #header>
        <div class="page-header">
          <div>
            <h1>{{ pageTitle }}</h1>
            <p class="page-summary">{{ pageSummary }}</p>
          </div>
        </div>
      </template>

      <NoPermission v-if="!hasAnyAccess" required-role="课程主讲教师、专业负责人或教务管理员" />

      <template v-else>
        <el-tabs v-if="isInstructor" v-model="instructorTab" class="assessment-tabs">
          <el-tab-pane label="成绩预览" name="template" lazy>
            <TemplatePreview />
          </el-tab-pane>
          <el-tab-pane label="成绩录入" name="import" lazy>
            <ScoreImport />
          </el-tab-pane>
        </el-tabs>

        <el-tabs v-else-if="isDirectorOrAcademic" v-model="directorTab" class="assessment-tabs">
          <el-tab-pane label="专业级计算看板" name="dashboard" lazy>
            <MacroDashboard />
          </el-tab-pane>
          <el-tab-pane label="专业级结果查看" name="results" lazy>
            <ResultViewEntry />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NoPermission from '@/components/common/NoPermission.vue'
import TemplatePreview from './TemplatePreview.vue'
import ScoreImport from './ScoreImport.vue'
import MacroDashboard from './MacroDashboard.vue'
import ResultViewEntry from './ResultViewEntry.vue'

const route = useRoute()
const userStore = useUserStore()

const instructorTab = ref('template')
const directorTab = ref('dashboard')

const isInstructor = computed(() => userStore.roleCodes.includes('instructor'))
const isDirectorOrAcademic = computed(() =>
  userStore.roleCodes.some((role) => ['program_director', 'academic_affairs'].includes(role)),
)
const hasAnyAccess = computed(() => isInstructor.value || isDirectorOrAcademic.value)

const pageTitle = computed(() => {
  if (isInstructor.value) {
    return instructorTab.value === 'template' ? '课程成绩预览与课程级结果' : '课程成绩录入'
  }
  if (isDirectorOrAcademic.value) {
    return directorTab.value === 'dashboard' ? '专业级全局达成度计算看板' : '专业级达成度结果查看'
  }
  return route.meta.title
})

const pageSummary = computed(() => {
  if (isInstructor.value) {
    return instructorTab.value === 'template'
      ? '查看当前教学班成绩、锁定状态、课程目标级达成度以及课程级毕业要求指标点达成度，并在满足条件后执行课程级计算。'
      : '按系统模板导入和保存原始成绩，为课程级达成度计算提供可追溯的数据输入。'
  }
  if (isDirectorOrAcademic.value) {
    return directorTab.value === 'dashboard'
      ? '按专业、年级查看当前届学生涉及的全部支撑课程状态，处理解锁申请，并在全部课程锁定后执行专业级全局达成度计算。'
      : '查看已持久化的专业级毕业要求指标点达成度结果，作为后续报表与分析模块的统一输出口径。'
  }
  return route.meta.summary
})
</script>

<style scoped>
.assessment-page {
  padding: 20px;
}

.assessment-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 8px 0 6px;
  font-size: 28px;
  color: #0f172a;
}

.page-section {
  margin: 0;
  font-size: 13px;
  color: #2563eb;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-summary {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.assessment-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}
</style>
