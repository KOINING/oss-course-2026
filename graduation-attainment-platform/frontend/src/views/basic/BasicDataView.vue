<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import AcademicTermPanel from './AcademicTermPanel.vue'
import CollegePanel from './CollegePanel.vue'
import CoursePanel from './CoursePanel.vue'

const route = useRoute()
const activeTab = ref('academic-term')
</script>

<template>
  <div class="basic-data-page">
    <el-card class="basic-data-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="basic-data-tabs">
        <el-tab-pane label="学年学期" name="academic-term">
          <AcademicTermPanel />
        </el-tab-pane>
        <el-tab-pane label="学院" name="college">
          <CollegePanel />
        </el-tab-pane>
        <el-tab-pane label="课程清单" name="course">
          <CoursePanel />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.basic-data-page {
  padding: 20px;
}

.basic-data-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 4px 0 0;
  color: #1f2937;
  font-size: 26px;
}

.page-section {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.page-summary {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.basic-data-tabs {
  margin-top: 4px;
}
</style>
