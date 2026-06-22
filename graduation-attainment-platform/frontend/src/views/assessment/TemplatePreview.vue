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

      <el-tag v-if="scoreContext?.calcStatus" :type="calcStatusType" effect="light">
        {{ calcStatusLabel }}
      </el-tag>

      <div class="context-actions">
        <el-button :disabled="!selectedClassId" @click="loadPreview">刷新</el-button>
        <el-button
          type="primary"
          :loading="savingInlineScores"
          :disabled="!canSaveInlineScores"
          @click="saveInlineScores"
        >
          保存成绩
        </el-button>
        <el-button
          v-if="scoreContext?.calcStatus === 'locked'"
          :disabled="objectiveDashboard?.unlockRequested"
          @click="requestUnlock"
        >
          {{ objectiveDashboard?.unlockRequested ? '解锁申请已提交' : '申请解锁' }}
        </el-button>
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
        v-if="visibleBlockReasons.length"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班评价单元尚未满足成绩预览与录入前置条件</template>
        <template #default>
          <ul class="block-reason-list">
            <li v-for="reason in visibleBlockReasons" :key="reason">{{ reason }}</li>
          </ul>
        </template>
      </el-alert>

      <el-alert
        v-if="scoreContext?.calcStatus === 'locked'"
        type="success"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班评价单元已完成课程级计算并锁定</template>
        <template #default>
          当前成绩与课程级结果仅允许查看。
          <span v-if="objectiveDashboard?.unlockRequested">
            已提交解锁申请：{{ objectiveDashboard.unlockRequestReason || '等待专业负责人或教务管理员审批' }}
          </span>
          <span v-else>如需更改成绩，请先提交解锁申请，审批通过后可修改或重新导入成绩，并重新计算。</span>
        </template>
      </el-alert>

      <el-alert
        v-if="canCalculate"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>成绩已保存，待执行课程级计算</template>
        <template #default>
          当前成绩已保存且满足计算条件，可点击“计算并锁定”生成课程目标达成度和课程级指标点达成度。
        </template>
      </el-alert>

      <el-alert
        v-if="scoreContext?.calcStatus === 'unsubmitted' && scoreContext?.canGenerateTemplate && !visibleBlockReasons.length"
        type="info"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班评价单元可录入成绩</template>
        <template #default>
          当前可预览模板并录入成绩，成绩补齐并保存后可执行课程级计算。
        </template>
      </el-alert>
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          固定列：<strong>学号 / 姓名</strong>，动态列：<strong>{{ assessmentPoints.length }}</strong> 个考核点，
          学生数：<strong>{{ students.length }}</strong>
        </template>
      </el-alert>

      <ErrorState v-if="previewError" :message="previewError" @retry="loadPreview" />

      <EmptyState
        v-else-if="!previewLoading && !selectedClassId"
        description="当前没有可用教学班评价单元。"
      />

      <EmptyState
        v-else-if="!previewLoading && !scoreContext?.canGenerateTemplate"
        description="当前教学班评价单元前置条件未满足，暂不能生成模板或预览成绩。"
      />

      <EmptyState
        v-else-if="!previewLoading && assessmentPoints.length === 0"
        description="当前课程下暂无考核点，请先完成考核点配置。"
      />

      <EmptyState
        v-else-if="!previewLoading && students.length === 0"
        description="当前教学班评价单元暂无学生名单。"
      />

      <div v-else class="template-table-wrapper" v-loading="previewLoading">
        <ScoreSheetTable
          :rows="students"
          :headers="assessmentPoints"
          :max-height="620"
          :editable="canEditScores"
          @update-score="updateScoreCell"
        />
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
import { ElMessage, ElMessageBox } from 'element-plus'
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
  requestUnlockApi,
  saveScoresApi,
} from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')

const previewLoading = ref(false)
const previewError = ref('')
const downloading = ref(false)
const calculating = ref(false)
const savingInlineScores = ref(false)
const students = ref([])
const assessmentPoints = ref([])
const contextInfo = ref(null)
const scoreContext = ref(null)
const objectiveDashboard = ref(null)

const expectedScoreCount = computed(() => students.value.length * assessmentPoints.value.length)
const visibleBlockReasons = computed(() => {
  const reasons = scoreContext.value?.blockReasons || []
  return reasons.filter((reason) => !String(reason).includes('locked'))
})
const filledScoreCount = computed(() =>
  students.value.reduce(
    (total, row) => total + (row.scores || []).filter((item) => item !== null && item !== undefined && item !== '').length,
    0,
  ),
)
const canCalculate = computed(() =>
  !calculating.value
  && !!selectedClassId.value
  && scoreContext.value?.calcStatus === 'score_imported'
  && expectedScoreCount.value > 0
  && filledScoreCount.value === expectedScoreCount.value,
)
const canEditScores = computed(() =>
  !!selectedClassId.value
  && scoreContext.value?.canGenerateTemplate
  && scoreContext.value?.calcStatus !== 'locked',
)
const canSaveInlineScores = computed(() => !savingInlineScores.value && canEditScores.value && collectScorePayload().length > 0)
const calcStatusLabel = computed(() => {
  const map = {
    unsubmitted: '未提交',
    score_imported: '已提交未计算',
    calculating: '已计算未锁定',
    locked: '已锁定',
  }
  return map[scoreContext.value?.calcStatus] || scoreContext.value?.calcStatus || '未提交'
})
const calcStatusType = computed(() => {
  const map = {
    unsubmitted: 'info',
    score_imported: 'warning',
    calculating: 'primary',
    locked: 'success',
  }
  return map[scoreContext.value?.calcStatus] || 'info'
})

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
      objectiveDashboard.value = null
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

function updateScoreCell({ rowIndex, columnIndex, value }) {
  if (!students.value[rowIndex]) return
  const currentScores = Array.isArray(students.value[rowIndex].scores) ? [...students.value[rowIndex].scores] : []
  currentScores[columnIndex] = value
  students.value[rowIndex] = {
    ...students.value[rowIndex],
    scores: currentScores,
  }
}

function collectScorePayload() {
  const payload = []
  students.value.forEach((row) => {
    if (!row?.studentId) return
    ;(row.scores || []).forEach((score, index) => {
      const header = assessmentPoints.value[index]
      if (!header?.apId || score === null || score === undefined || score === '') {
        return
      }
      payload.push({
        studentId: row.studentId,
        apId: header.apId,
        actualScore: Number(score),
      })
    })
  })
  return payload
}

function formatSaveScoreError(error) {
  if (error?.code === 'ECONNABORTED' || String(error?.message || '').includes('timeout')) {
    return '成绩保存耗时较长，请稍后刷新成绩预览确认保存结果'
  }
  return error?.response?.data?.message || error?.message || '成绩保存失败'
}

async function saveInlineScores() {
  if (!canSaveInlineScores.value) return
  savingInlineScores.value = true
  try {
    await saveScoresApi({
      classId: selectedClassId.value,
      scores: collectScorePayload(),
    })
    await loadPreview()
    ElMessage.success('成绩保存成功，可继续补录或执行课程级计算')
  } catch (error) {
    ElMessage.error(formatSaveScoreError(error))
  } finally {
    savingInlineScores.value = false
  }
}

function formatCalculateCourseError(error) {
  if (error?.code === 'ECONNABORTED' || String(error?.message || '').includes('timeout')) {
    return '课程级计算耗时较长，请稍后刷新页面确认计算状态'
  }
  return error?.response?.data?.message || error?.message || '课程级计算失败'
}

async function calculateCourse() {
  if (!canCalculate.value) return
  calculating.value = true
  try {
    await calculateCourseLevelApi({ classId: selectedClassId.value })
    await loadPreview()
    ElMessage.success('课程级计算完成，当前教学班已锁定')
  } catch (error) {
    ElMessage.error(formatCalculateCourseError(error))
  } finally {
    calculating.value = false
  }
}

async function requestUnlock() {
  if (!selectedClassId.value || scoreContext.value?.calcStatus !== 'locked') return
  try {
    const { value } = await ElMessageBox.prompt('请填写申请解锁原因', '申请解锁', {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：录入成绩后发现部分分值有误，需要更正后重新计算',
      inputValidator: (input) => (input?.trim() ? true : '请填写解锁原因'),
    })
    await requestUnlockApi({
      classId: selectedClassId.value,
      reason: value.trim(),
    })
    ElMessage.success('解锁申请已提交')
    await loadObjectiveDashboard()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error?.message || '提交解锁申请失败')
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
