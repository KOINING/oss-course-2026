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
          :label="`${c.classCode} - ${c.courseName || c.courseCode}`"
          :value="c.classId"
        />
      </el-select>

      <div v-if="contextInfo" class="context-info">
        <el-tag type="info" effect="plain">{{ contextInfo.gradeYear ? `${contextInfo.gradeYear}级` : '-' }}</el-tag>
        <el-tag type="info" effect="plain">{{ contextInfo.termCode || '-' }}</el-tag>
        <el-tag type="success" effect="plain">{{ contextInfo.courseName || '-' }}</el-tag>
      </div>

      <div class="context-actions">
        <el-button :disabled="!selectedClassId" @click="loadPreview">刷新预览</el-button>
        <el-button
          type="primary"
          :loading="downloading"
          :disabled="!selectedClassId || !scoreContext?.canGenerateTemplate"
          @click="downloadTemplate"
        >
          下载模板
        </el-button>
      </div>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadClasses" />

    <EmptyState
      v-else-if="!selectedClassId"
      description="请先选择一个教学班以预览成绩模板"
    />

    <template v-else>
      <el-alert
        v-if="scoreContext && scoreContext.blockReasons?.length"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班尚未满足模板生成前置条件</template>
        <template #default>
          <ul class="block-reason-list">
            <li v-for="reason in scoreContext.blockReasons" :key="reason">{{ reason }}</li>
          </ul>
        </template>
      </el-alert>

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
        v-else-if="!previewLoading && !scoreContext?.canGenerateTemplate"
        description="当前教学班前置条件未满足，暂不能生成成绩模板"
      />

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
          <el-table-column prop="studentNo" label="学号" width="140" fixed="left" />
          <el-table-column prop="studentName" label="姓名" width="120" fixed="left" />

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
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import {
  listInstructorTeachingClassesApi,
  getTemplatePreviewDataApi,
  getScoreImportContextApi,
  downloadTemplateApi,
} from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')

const previewLoading = ref(false)
const previewError = ref('')
const downloading = ref(false)
const students = ref([])
const assessmentPoints = ref([])
const contextInfo = ref(null)
const scoreContext = ref(null)

const tableData = computed(() =>
  students.value.map((student) => ({
    studentNo: student.studentNo,
    studentName: student.studentName,
    studentId: student.studentId,
  })),
)

async function loadClasses() {
  classLoading.value = true
  loadError.value = ''
  try {
    classOptions.value = (await listInstructorTeachingClassesApi()) || []
  } catch (error) {
    loadError.value = error.message || '加载教学班列表失败'
    classOptions.value = []
  } finally {
    classLoading.value = false
  }
}

async function loadContext() {
  if (!selectedClassId.value) return
  scoreContext.value = await getScoreImportContextApi({ classId: selectedClassId.value })
  contextInfo.value = scoreContext.value?.teachingClass || classOptions.value.find((c) => c.classId === selectedClassId.value) || null
}

async function loadPreview() {
  if (!selectedClassId.value) return
  previewLoading.value = true
  previewError.value = ''
  try {
    await loadContext()
    if (!scoreContext.value?.canGenerateTemplate) {
      students.value = []
      assessmentPoints.value = []
      return
    }
    const data = await getTemplatePreviewDataApi({ classId: selectedClassId.value })
    students.value = data.rows || []
    assessmentPoints.value = data.dynamicHeaders || []
  } catch (error) {
    previewError.value = error.message || '加载模板预览失败'
    students.value = []
    assessmentPoints.value = []
  } finally {
    previewLoading.value = false
  }
}

async function handleClassChange() {
  students.value = []
  assessmentPoints.value = []
  scoreContext.value = null
  previewError.value = ''
  await loadPreview()
}

function getFileNameFromDisposition(disposition) {
  const utf8Match = disposition?.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }
  const plainMatch = disposition?.match(/filename="?([^"]+)"?/i)
  return plainMatch?.[1] || `score-template-${selectedClassId.value}.xlsx`
}

async function downloadTemplate() {
  if (!selectedClassId.value || !scoreContext.value?.canGenerateTemplate) return
  downloading.value = true
  try {
    const response = await downloadTemplateApi(selectedClassId.value)
    const blob = new Blob([response.data], {
      type: response.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = getFileNameFromDisposition(response.headers['content-disposition'])
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '模板下载失败')
  } finally {
    downloading.value = false
  }
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

.context-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.template-info {
  margin-bottom: 4px;
}

.block-reason-list {
  margin: 6px 0 0 18px;
  padding: 0;
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
