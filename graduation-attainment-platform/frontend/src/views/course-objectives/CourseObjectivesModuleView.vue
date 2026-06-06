<template>
  <div class="module-b-page">
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CourseObjectivesView from './CourseObjectivesView.vue'
import CourseWeightView from '@/views/requirements/CourseWeightView.vue'
import AssessmentPointConfig from '@/views/assessment/AssessmentPointConfig.vue'

const activeTab = ref('objectives')
</script>

<style scoped>
.module-b-page {
  padding: 0;
}

.module-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
  padding: 0 20px;
}
</style>
