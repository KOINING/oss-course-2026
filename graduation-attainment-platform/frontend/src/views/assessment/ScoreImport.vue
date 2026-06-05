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
          :label="`${c.classCode} - ${c.courseName || c.courseCode} (${c.studentCount || 0}人)`"
          :value="c.classId"
        />
      </el-select>

      <div v-if="contextInfo" class="context-info">
        <el-tag type="info" effect="plain">{{ contextInfo.gradeYear ? `${contextInfo.gradeYear}级` : '' }}</el-tag>
        <el-tag type="info" effect="plain">{{ contextInfo.termName || '-' }}</el-tag>
      </div>

      <el-tag v-if="contextInfo && contextInfo.calcStatus" :type="calcStatusType" effect="light">
        {{ calcStatusLabel }}
      </el-tag>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadClasses" />

    <EmptyState
      v-else-if="!selectedClassId"
      description="请先选择一个教学班以导入成绩"
    />

    <template v-else>
      <div class="import-layout" v-if="!calcLocked">
        <div class="import-upload-area">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #title>
              请按照模板格式上传成绩 Excel 文件。固定列为<strong>学号 | 姓名</strong>，动态列为各考核点成绩。
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
            <div class="upload-text">将成绩文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="upload-tip">支持 .xlsx / .xls / .csv 格式，单文件不超过 10MB</div>
            </template>
          </el-upload>

          <div class="upload-actions">
            <el-button
              type="primary"
              :loading="importing"
              :disabled="!scoreFile"
              @click="submitImport"
            >
              开始导入
            </el-button>
            <el-button :disabled="!scoreFile" @click="clearFile">清空</el-button>
          </div>
        </div>

        <div class="import-result-area">
          <ImportResultPreview
            :summary="importResult.summary"
            :failed-items="importResult.failedItems"
            :loading="importing"
            title="成绩导入结果"
          />
        </div>
      </div>

      <div v-else class="calc-locked-notice">
        <el-result
          icon="warning"
          title="成绩已锁定"
          :sub-title="`当前教学班计算状态为「${calcStatusLabel}」，不允许继续导入或修改成绩。如需重新导入，请联系专业负责人或教务管理员先解锁。`"
        />
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
import { listInstructorTeachingClassesApi, importScoresApi } from '@/api/assessment'

const classLoading = ref(false)
const classOptions = ref([])
const selectedClassId = ref(null)
const loadError = ref('')
const contextInfo = ref(null)

const uploadRef = ref(null)
const scoreFile = ref(null)
const importing = ref(false)

const importResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

const calcStatusLabel = computed(() => {
  const map = {
    unsubmitted: '未提交',
    score_imported: '已提交未计算',
    calculating: '计算中',
    locked: '已锁定',
  }
  return map[contextInfo.value?.calcStatus] || contextInfo.value?.calcStatus || '未提交'
})

const calcStatusType = computed(() => {
  const map = {
    unsubmitted: 'info',
    score_imported: 'warning',
    calculating: 'primary',
    locked: 'success',
  }
  return map[contextInfo.value?.calcStatus] || 'info'
})

const calcLocked = computed(() => contextInfo.value?.calcStatus === 'locked')

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

function handleClassChange(classId) {
  const selected = classOptions.value.find((c) => c.classId === classId)
  contextInfo.value = selected || null
  clearFile()
  resetResult()
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
}

function clearFile() {
  scoreFile.value = null
  uploadRef.value?.clearFiles()
  resetResult()
}

function resetResult() {
  importResult.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  importResult.failedItems = []
}

async function submitImport() {
  if (!scoreFile.value) {
    ElMessage.warning('请先选择成绩文件')
    return
  }
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', scoreFile.value.raw)
    formData.append('classId', selectedClassId.value)

    const result = await importScoresApi(formData)
    importResult.summary = {
      totalCount: result.totalCount ?? 0,
      successCount: result.successCount ?? 0,
      failureCount: result.failureCount ?? 0,
    }
    importResult.failedItems = result.failedItems || []

    const failCount = importResult.summary.failureCount
    if (failCount > 0) {
      ElMessage.warning(`导入完成，${failCount} 条记录存在问题`)
    } else {
      ElMessage.success('成绩导入成功')
    }

    clearFile()
    await loadClasses()
  } catch (e) {
    ElMessage.error(e.message || '成绩导入失败')
  } finally {
    importing.value = false
  }
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
  gap: 10px;
}

.import-result-area {
  min-width: 0;
}

.calc-locked-notice {
  padding: 48px 0;
}
</style>
