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
          v-for="item in classOptions"
          :key="item.classId"
          :label="`${item.classCode} - ${item.courseName || item.courseCode}`"
          :value="item.classId"
        />
      </el-select>

      <div v-if="contextInfo" class="context-info">
        <el-tag type="info" effect="plain">{{ contextInfo.gradeYear ? `${contextInfo.gradeYear}级` : '-' }}</el-tag>
        <el-tag type="info" effect="plain">{{ contextInfo.termCode || '-' }}</el-tag>
        <el-tag type="success" effect="plain">{{ contextInfo.courseName || '-' }}</el-tag>
      </div>

      <div class="context-actions">
        <el-button :disabled="!selectedClassId" @click="loadPreview">刷新</el-button>
        <el-button
          type="warning"
          :loading="calculating"
          :disabled="!canCalculate"
          @click="calculateCourse"
        >
          计算并锁定
        </el-button>
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

    <template v-else>
      <el-alert
        v-if="scoreContext?.blockReasons?.length"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班尚未满足模板与成绩预览前置条件</template>
        <template #default>
          <ul class="block-reason-list">
            <li v-for="reason in scoreContext.blockReasons" :key="reason">{{ reason }}</li>
          </ul>
        </template>
      </el-alert>

      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          固定列：<strong>学号 / 姓名</strong>
          ，动态列：<strong>{{ assessmentPoints.length }}</strong> 个考核点，
          学生数：<strong>{{ students.length }}</strong>
        </template>
      </el-alert>

      <ErrorState v-if="previewError" :message="previewError" @retry="loadPreview" />

      <EmptyState
        v-else-if="!previewLoading && !selectedClassId"
        description="当前没有可用教学班"
      />

      <EmptyState
        v-else-if="!previewLoading && !scoreContext?.canGenerateTemplate"
        description="当前教学班前置条件未满足，暂不能生成模板或预览成绩"
      />

      <EmptyState
        v-else-if="!previewLoading && assessmentPoints.length === 0"
        description="当前课程下暂无考核点，请先完成考核点配置"
      />

      <EmptyState
        v-else-if="!previewLoading && students.length === 0"
        description="当前教学班暂无学生名单"
      />

      <div v-else class="template-table-wrapper" v-loading="previewLoading">
        <ScoreSheetTable :rows="students" :headers="assessmentPoints" :max-height="620" />
      </div>

      <ObjectiveAchievementDashboard
        v-if="objectiveDashboard"
        :dashboard="objectiveDashboard"
      />
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ObjectiveAchievementDashboard from '@/components/assessment/ObjectiveAchievementDashboard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import ScoreSheetTable from '@/components/assessment/ScoreSheetTable.vue'
import {
  calculateCourseLevelApi,
  downloadTemplateApi,
  getCourseObjectiveDashboardApi,
  getScoreImportContextApi,
  getTemplatePreviewDataApi,
  listInstructorTeachingClassesApi,
} from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')

const previewLoading = ref(false)
const previewError = ref('')
const downloading = ref(false)
const calculating = ref(false)
const students = ref([])
const assessmentPoints = ref([])
const contextInfo = ref(null)
const scoreContext = ref(null)
const courseCalcResult = ref(null)
const objectiveDashboard = ref(null)

const expectedScoreCount = computed(() => students.value.length * assessmentPoints.value.length)
const filledScoreCount = computed(() =>
  students.value.reduce(
    (total, row) => total + (row.scores || []).filter((item) => item !== null && item !== undefined && item !== '').length,
    0,
  ),
)
const canCalculate = computed(() =>
  !!selectedClassId.value
  && scoreContext.value?.calcStatus === 'score_imported'
  && expectedScoreCount.value > 0
  && filledScoreCount.value === expectedScoreCount.value,
)

async function loadClasses() {
  classLoading.value = true
  loadError.value = ''
  try {
    classOptions.value = (await listInstructorTeachingClassesApi()) || []
    if (!classOptions.value.length) {
      selectedClassId.value = null
      contextInfo.value = null
      students.value = []
      assessmentPoints.value = []
      return
    }

    if (!selectedClassId.value || !classOptions.value.some((item) => item.classId === selectedClassId.value)) {
      selectedClassId.value = classOptions.value[0].classId
    }
    await loadPreview()
  } catch (error) {
    loadError.value = error.message || '加载教学班列表失败'
    classOptions.value = []
    selectedClassId.value = null
  } finally {
    classLoading.value = false
  }
}

async function loadContext() {
  if (!selectedClassId.value) {
    scoreContext.value = null
    contextInfo.value = null
    return
  }
  scoreContext.value = await getScoreImportContextApi({ classId: selectedClassId.value })
  contextInfo.value = scoreContext.value?.teachingClass
    || classOptions.value.find((item) => item.classId === selectedClassId.value)
    || null
}

async function loadPreview() {
  if (!selectedClassId.value) {
    return
  }

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
    await loadObjectiveDashboard()
  } catch (error) {
    previewError.value = error.message || '加载成绩预览失败'
    students.value = []
    assessmentPoints.value = []
    objectiveDashboard.value = null
  } finally {
    previewLoading.value = false
  }
}

async function handleClassChange() {
  students.value = []
  assessmentPoints.value = []
  previewError.value = ''
  courseCalcResult.value = null
  objectiveDashboard.value = null
  await loadPreview()
}

async function loadObjectiveDashboard() {
  if (!selectedClassId.value) {
    objectiveDashboard.value = null
    return
  }
  try {
    objectiveDashboard.value = await getCourseObjectiveDashboardApi({ classId: selectedClassId.value })
  } catch {
    objectiveDashboard.value = null
  }
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

async function calculateCourse() {
  if (!canCalculate.value) return
  calculating.value = true
  try {
    courseCalcResult.value = await calculateCourseLevelApi({ classId: selectedClassId.value })
    await loadPreview()
    await loadObjectiveDashboard()
    ElMessage.success('课程级计算完成，当前教学班已锁定')
  } catch (error) {
    ElMessage.error(error.message || '课程级计算失败')
  } finally {
    calculating.value = false
  }
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

.block-reason-list {
  margin: 6px 0 0 18px;
  padding: 0;
}

.template-table-wrapper {
  border-radius: 8px;
  overflow: hidden;
}
</style>
