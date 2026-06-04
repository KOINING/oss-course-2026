<template>
  <div class="template-preview">
    <div class="context-bar">
      <el-select
        v-model="selectedClassId"
        placeholder="选择教学班"
        style="width: 320px"
        :loading="classLoading"
        @change="handleClassChange"
      >
        <el-option
          v-for="c in classOptions"
          :key="c.classId"
          :label="`${c.classCode} - ${c.courseName || c.courseCode} (${c.studentCount || 0}人)`"
          :value="c.classId"
        />
      </el-select>

      <div v-if="contextInfo" class="context-info">
        <el-tag type="info" effect="plain">{{ contextInfo.gradeYear ? `${contextInfo.gradeYear}级` : '' }}</el-tag>
        <el-tag type="info" effect="plain">{{ contextInfo.termName || '-' }}</el-tag>
        <el-tag type="success" effect="plain">{{ contextInfo.courseName || '-' }}</el-tag>
      </div>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadClasses" />

    <EmptyState
      v-else-if="!selectedClassId"
      description="请先选择一个教学班以预览成绩模板"
    />

    <div v-else>
      <div class="template-info">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <span>
              固定列：<strong>学号 | 姓名</strong>
              &nbsp;&nbsp;|&nbsp;&nbsp;
              动态列：<strong>{{ assessmentPoints.length }} 个考核点</strong>
              &nbsp;&nbsp;|&nbsp;&nbsp;
              学生数：<strong>{{ students.length }}</strong>
            </span>
          </template>
        </el-alert>
      </div>

      <ErrorState v-if="previewError" :message="previewError" @retry="loadPreview" />

      <EmptyState
        v-else-if="!previewLoading && assessmentPoints.length === 0"
        description="当前教学班对应课程下暂无考核点，请先配置考核点"
      />

      <EmptyState
        v-else-if="!previewLoading && students.length === 0"
        description="当前教学班暂无学生，请先导入学生名单"
      />

      <div v-else class="template-table-wrapper">
        <el-table
          v-loading="previewLoading"
          :data="tableData"
          border
          stripe
          :max-height="480"
          show-summary
          :summary-method="getSummary"
        >
          <el-table-column
            prop="studentNo"
            label="学号"
            width="140"
            fixed="left"
          />
          <el-table-column
            prop="studentName"
            label="姓名"
            width="120"
            fixed="left"
          />

          <el-table-column
            v-for="ap in assessmentPoints"
            :key="ap.apId"
            :min-width="140"
            align="center"
          >
            <template #header>
              <div class="dynamic-header">
                <div class="dynamic-header__name">{{ ap.apName }}</div>
                <div class="dynamic-header__meta">
                  <span>满分: {{ ap.fullScore }}</span>
                  <el-tag size="small" type="primary" effect="plain">{{ ap.objectiveCode }}</el-tag>
                </div>
              </div>
            </template>
            <template #default>
              <span class="score-placeholder">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import { listInstructorTeachingClassesApi, getTemplatePreviewDataApi } from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')

const previewLoading = ref(false)
const previewError = ref('')
const students = ref([])
const assessmentPoints = ref([])
const contextInfo = ref(null)

const tableData = computed(() =>
  students.value.map((s) => ({
    studentNo: s.studentNo,
    studentName: s.studentName,
    studentId: s.studentId,
  })),
)

async function loadClasses() {
  classLoading.value = true
  loadError.value = ''
  try {
    classOptions.value = (await listInstructorTeachingClassesApi()) || []
  } catch (e) {
    loadError.value = e.message || '加载教学班列表失败'
    classOptions.value = []
  } finally {
    classLoading.value = false
  }
}

async function loadPreview() {
  if (!selectedClassId.value) return
  previewLoading.value = true
  previewError.value = ''
  try {
    const data = await getTemplatePreviewDataApi({ classId: selectedClassId.value })
    students.value = data.students || []
    assessmentPoints.value = data.assessmentPoints || []
  } catch (e) {
    previewError.value = e.message || '加载模板预览失败'
    students.value = []
    assessmentPoints.value = []
  } finally {
    previewLoading.value = false
  }
}

function handleClassChange(classId) {
  const selected = classOptions.value.find((c) => c.classId === classId)
  contextInfo.value = selected || null
  loadPreview()
}

function getSummary() {
  const sums = [`共 ${students.value.length} 名学生`, '']
  assessmentPoints.value.forEach(() => sums.push(''))
  return sums
}

loadClasses()
</script>

<style scoped>
.template-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.context-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.context-info {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.template-info {
  margin-bottom: 4px;
}

.template-table-wrapper {
  border-radius: 8px;
  overflow: hidden;
}

.dynamic-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.dynamic-header__name {
  font-weight: 600;
  color: #1f2937;
}

.dynamic-header__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.score-placeholder {
  color: #c0c4cc;
}
</style>
