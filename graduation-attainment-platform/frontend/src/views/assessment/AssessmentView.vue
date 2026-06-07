<template>
  <div class="assessment-page">
    <el-card class="assessment-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
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
          <el-tab-pane label="宏观看板" name="dashboard" lazy>
            <MacroDashboard />
          </el-tab-pane>
          <el-tab-pane label="结果查看" name="results" lazy>
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
  userStore.roleCodes.some((r) => ['program_director', 'academic_affairs'].includes(r)),
)
const hasAnyAccess = computed(() => isInstructor.value || isDirectorOrAcademic.value)
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
