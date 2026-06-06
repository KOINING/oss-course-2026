<template>
  <div class="score-import">
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
      </div>

      <el-tag v-if="scoreContext?.calcStatus" :type="calcStatusType" effect="light">
        {{ calcStatusLabel }}
      </el-tag>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadClasses" />

    <EmptyState
      v-else-if="!selectedClassId"
      description="请选择一个教学班以导入成绩"
    />

    <template v-else>
      <el-alert
        v-if="scoreContext?.blockReasons?.length"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>当前教学班尚未满足导入前置条件</template>
        <template #default>
          <ul class="block-reason-list">
            <li v-for="reason in scoreContext.blockReasons" :key="reason">{{ reason }}</li>
          </ul>
        </template>
      </el-alert>

      <div class="import-layout" v-if="!calcLocked">
        <div class="import-upload-area">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #title>
              请使用系统模板录入成绩后上传。系统会先做预校验，再保存成绩，最后执行课程级计算并锁定。
            </template>
          </el-alert>

          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :before-upload="beforeUpload"
            :on-change="handleFileChange"
            :limit="1"
            accept=".xlsx,.xls,.csv"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">将成绩文件拖到此处，或 <em>点击选择文件</em></div>
            <template #tip>
              <div class="upload-tip">支持 .xlsx / .xls / .csv，单文件不超过 10MB</div>
            </template>
          </el-upload>

          <div class="upload-actions">
            <el-button
              type="primary"
              :loading="previewing"
              :disabled="!canPreviewImport"
              @click="previewImport"
            >
              预校验成绩文件
            </el-button>
            <el-button
              type="success"
              :loading="saving"
              :disabled="!canSaveScores"
              @click="saveImportedScores"
            >
              保存成绩
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
              :disabled="!scoreFile && importResult.summary.totalCount === 0"
              @click="clearImportState"
            >
              清空
            </el-button>
          </div>

          <el-descriptions
            v-if="scoreContext"
            border
            :column="1"
            size="small"
            class="context-summary"
          >
            <el-descriptions-item label="学生名单">{{ scoreContext.studentCount }}</el-descriptions-item>
            <el-descriptions-item label="课程目标">{{ scoreContext.courseObjectiveCount }}</el-descriptions-item>
            <el-descriptions-item label="考核点">{{ scoreContext.assessmentPointCount }}</el-descriptions-item>
            <el-descriptions-item label="内部权重 w">{{ scoreContext.internalWeightCount }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="import-result-area">
          <ImportResultPreview
            :summary="importResult.summary"
            :failed-items="importResult.failedItems"
            :loading="previewing || saving"
            title="成绩导入预校验结果"
          />

          <el-card
            v-if="courseCalcResult"
            class="calc-result-card"
            shadow="never"
          >
            <template #header>
              <div class="calc-result-header">
                <span>课程级计算结果</span>
                <el-tag type="success" effect="light">已锁定</el-tag>
              </div>
            </template>

            <el-table :data="courseCalcResult.objectiveAchievements || []" size="small" border>
              <el-table-column prop="objectiveCode" label="课程目标" width="120" />
              <el-table-column prop="description" label="目标描述" min-width="220" />
              <el-table-column prop="averageAchievement" label="班级平均达成度" width="160">
                <template #default="{ row }">{{ formatPercent(row.averageAchievement) }}</template>
              </el-table-column>
            </el-table>

            <el-table
              :data="courseCalcResult.indicatorAchievements || []"
              size="small"
              border
              style="margin-top: 12px"
            >
              <el-table-column prop="ipCode" label="指标点" width="120" />
              <el-table-column prop="ipDescription" label="指标点描述" min-width="220" />
              <el-table-column prop="achievement" label="课程级达成度 Ek" width="160">
                <template #default="{ row }">{{ formatPercent(row.achievement) }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>

      <div v-else class="calc-locked-notice">
        <el-result
          icon="success"
          title="当前教学班已锁定"
          :sub-title="`当前教学班计算状态为「${calcStatusLabel}」，教师端不能继续修改成绩。`"
        />
        <el-card
          v-if="courseCalcResult"
          class="calc-result-card"
          shadow="never"
        >
          <template #header>
            <div class="calc-result-header">
              <span>最近一次课程级计算结果</span>
              <el-tag type="success" effect="light">已锁定</el-tag>
            </div>
          </template>
          <el-table :data="courseCalcResult.objectiveAchievements || []" size="small" border>
            <el-table-column prop="objectiveCode" label="课程目标" width="120" />
            <el-table-column prop="description" label="目标描述" min-width="220" />
            <el-table-column prop="averageAchievement" label="班级平均达成度" width="160">
              <template #default="{ row }">{{ formatPercent(row.averageAchievement) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import {
  listInstructorTeachingClassesApi,
  importScorePreviewApi,
  getScoreImportContextApi,
  saveScoresApi,
  calculateCourseLevelApi,
} from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')
const contextInfo = ref(null)
const scoreContext = ref(null)

const uploadRef = ref(null)
const scoreFile = ref(null)
const previewing = ref(false)
const saving = ref(false)
const calculating = ref(false)

const previewPayload = ref([])
const courseCalcResult = ref(null)

const importResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

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

const calcLocked = computed(() => scoreContext.value?.calcStatus === 'locked')
const canPreviewImport = computed(() => !!scoreFile.value && !!selectedClassId.value && !!scoreContext.value?.canImportScore)
const canSaveScores = computed(() => previewPayload.value.length > 0 && importResult.summary.failureCount === 0 && !calcLocked.value)
const canCalculate = computed(() =>
  !!selectedClassId.value && !calcLocked.value && scoreContext.value?.calcStatus === 'score_imported',
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
  contextInfo.value = scoreContext.value?.teachingClass || classOptions.value.find((item) => item.classId === selectedClassId.value) || null
}

async function handleClassChange() {
  clearFile()
  resetResult()
  previewPayload.value = []
  courseCalcResult.value = null
  await loadContext()
}

function beforeUpload(file) {
  const isValidType = /\.(xlsx|xls|csv)$/i.test(file.name)
  if (!isValidType) {
    ElMessage.error('仅支持 .xlsx / .xls / .csv 格式的文件')
    return false
  }
  const isValidSize = file.size / 1024 / 1024 < 10
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return false
}

function handleFileChange(file) {
  scoreFile.value = file
  resetResult()
  previewPayload.value = []
}

function clearFile() {
  scoreFile.value = null
  uploadRef.value?.clearFiles()
}

function resetResult() {
  importResult.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  importResult.failedItems = []
}

function clearImportState() {
  clearFile()
  resetResult()
  previewPayload.value = []
}

function readFileAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('读取成绩文件失败'))
    reader.readAsDataURL(file)
  })
}

async function previewImport() {
  if (!scoreFile.value || !selectedClassId.value) return
  previewing.value = true
  resetResult()
  previewPayload.value = []
  courseCalcResult.value = null
  try {
    const fileBase64 = await readFileAsBase64(scoreFile.value.raw)
    const result = await importScorePreviewApi({
      classId: selectedClassId.value,
      fileName: scoreFile.value.name,
      fileBase64,
    })
    previewPayload.value = result.scoreItems || []
    importResult.summary = {
      totalCount: result.totalRows ?? 0,
      successCount: result.successCount ?? 0,
      failureCount: result.failCount ?? 0,
    }
    importResult.failedItems = (result.failRows || []).map((item) => ({
      rowNumber: item.rowIndex,
      reason: item.errorMessage,
    }))
    if (result.canSave) {
      ElMessage.success('预校验通过，可以保存成绩')
    } else {
      ElMessage.warning('预校验存在问题，请修正后重新上传')
    }
  } catch (error) {
    ElMessage.error(error.message || '成绩预校验失败')
  } finally {
    previewing.value = false
  }
}

async function saveImportedScores() {
  if (!canSaveScores.value) return
  saving.value = true
  try {
    await saveScoresApi({
      classId: selectedClassId.value,
      scores: previewPayload.value,
    })
    await loadContext()
    ElMessage.success('成绩保存成功，可以继续执行课程级计算')
  } catch (error) {
    ElMessage.error(error.message || '成绩保存失败')
  } finally {
    saving.value = false
  }
}

async function calculateCourse() {
  if (!canCalculate.value) return
  calculating.value = true
  try {
    courseCalcResult.value = await calculateCourseLevelApi({ classId: selectedClassId.value })
    await loadContext()
    clearImportState()
    ElMessage.success('课程级计算完成，当前教学班已锁定')
  } catch (error) {
    ElMessage.error(error.message || '课程级计算失败')
  } finally {
    calculating.value = false
  }
}

function formatPercent(value) {
  if (value === undefined || value === null) return '-'
  return `${(Number(value) * 100).toFixed(2)}%`
}

loadClasses()
</script>

<style scoped>
.score-import {
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

.import-layout {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 20px;
}

@media (max-width: 960px) {
  .import-layout {
    grid-template-columns: 1fr;
  }
}

.import-upload-area {
  display: flex;
  flex-direction: column;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
}

.upload-text {
  color: #606266;
  font-size: 14px;
  margin-top: 8px;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
}

.upload-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.context-summary {
  margin-top: 16px;
}

.import-result-area {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.block-reason-list {
  margin: 6px 0 0 18px;
  padding: 0;
}

.calc-result-card {
  border-radius: 8px;
}

.calc-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.calc-locked-notice {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px 0;
}
</style>
