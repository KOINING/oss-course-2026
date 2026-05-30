<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import { importCoursesApi, importStudentClassesApi } from '@/api/import'

const activeTab = ref('course')

// ---- Template definitions ----
const courseTemplateHeaders = ['所属专业代码', '课程代码', '课程名称', '学分', '状态']
const courseTemplateSample = [
  ['080901', 'CS201', '数据结构', '4.0', '1'],
  ['080901', 'CS301', '操作系统', '3.0', '1'],
]

const studentTemplateHeaders = ['学号', '姓名', '专业代码', '入学年份', '教学班编号']
const studentTemplateSample = [
  ['20220101001', '张三', '080901', '2022', 'TC2024CS01'],
  ['20220101002', '李四', '080901', '2022', 'TC2024CS01'],
]

// ---- Common error descriptions ----
const commonCourseErrors = [
  { rowNumber: '示例', reason: '课程代码不能为空' },
  { rowNumber: '示例', reason: '课程代码在导入模板中重复: CS201' },
  { rowNumber: '示例', reason: '所属专业代码不存在: BIO' },
  { rowNumber: '示例', reason: '学分必须为合法数值: abc' },
  { rowNumber: '示例', reason: '状态值必须为0或1: 2' },
]

const commonStudentErrors = [
  { rowNumber: '示例', reason: '学号不能为空' },
  { rowNumber: '示例', reason: '姓名不能为空' },
  { rowNumber: '示例', reason: '专业代码不存在: BIO' },
  { rowNumber: '示例', reason: '入学年份不合法: 1899' },
  { rowNumber: '示例', reason: '教学班编号不存在: TC999' },
  { rowNumber: '示例', reason: '学生 2024001 在同一批次中重复导入到教学班 TC2024CS01' },
  { rowNumber: '示例', reason: '学生 2021005 已存在于教学班 TC2024CS01 中' },
]

// ---- Course import state ----
const courseFile = ref(null)
const courseImporting = ref(false)
const courseResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})
const courseFinished = ref(false)
const courseError = ref('')

// ---- Student import state ----
const studentFile = ref(null)
const studentImporting = ref(false)
const studentResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})
const studentFinished = ref(false)
const studentError = ref('')

function downloadTemplate(headers, sampleRows, filename) {
  const BOM = '﻿'
  const headerLine = headers.join(',')
  const sampleLines = sampleRows.map((row) => row.join(',')).join('\n')
  const csv = BOM + headerLine + '\n' + sampleLines + '\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function handleCourseFileChange(file) {
  courseFile.value = file
  courseFinished.value = false
  courseError.value = ''
}

function handleStudentFileChange(file) {
  studentFile.value = file
  studentFinished.value = false
  studentError.value = ''
}

function resetCourseState() {
  courseFile.value = null
  courseResult.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  courseResult.failedItems = []
  courseFinished.value = false
  courseError.value = ''
}

function resetStudentState() {
  studentFile.value = null
  studentResult.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  studentResult.failedItems = []
  studentFinished.value = false
  studentError.value = ''
}

async function handleCourseImport() {
  if (!courseFile.value) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }
  const formData = new FormData()
  formData.append('file', courseFile.value.raw)
  courseImporting.value = true
  courseFinished.value = false
  courseError.value = ''
  try {
    const data = await importCoursesApi(formData)
    courseResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    courseResult.failedItems = data.failedItems ?? []
    courseFinished.value = true
    ElMessage.success('课程清单导入完成')
  } catch (e) {
    courseError.value = e.message || '导入请求失败'
    courseFinished.value = true
  } finally {
    courseImporting.value = false
  }
}

async function handleStudentImport() {
  if (!studentFile.value) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }
  const formData = new FormData()
  formData.append('file', studentFile.value.raw)
  studentImporting.value = true
  studentFinished.value = false
  studentError.value = ''
  try {
    const data = await importStudentClassesApi(formData)
    studentResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    studentResult.failedItems = data.failedItems ?? []
    studentFinished.value = true
    ElMessage.success('学生名单导入完成')
  } catch (e) {
    studentError.value = e.message || '导入请求失败'
    studentFinished.value = true
  } finally {
    studentImporting.value = false
  }
}

function beforeUpload(file) {
  const isExcel =
    file.name.endsWith('.xlsx') ||
    file.name.endsWith('.xls') ||
    file.name.endsWith('.csv')
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx、.xls 或 .csv 格式的文件')
    return false
  }
  return false
}
</script>

<template>
  <div class="import-page">
    <el-card class="import-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A / 教务管理</p>
            <h1>数据导入</h1>
            <p class="page-summary">
              通过 Excel 模板批量导入全专业课程清单和教学班学生名单，导入完成后下方预览区展示校验结果。
            </p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- ==================== 全专业课程清单导入 ==================== -->
        <el-tab-pane label="全专业课程清单导入" name="course">
          <div class="tab-layout">
            <!-- 上部：上传区 -->
            <section class="upload-section">
              <div class="section-header">
                <h3>步骤一：上传课程清单 Excel 文件</h3>
                <el-button type="primary" plain :icon="Download" @click="downloadTemplate(courseTemplateHeaders, courseTemplateSample, '课程清单导入模板.csv')">
                  下载导入模板
                </el-button>
              </div>

              <div class="template-hints">
                <span class="hint-label">模板字段：</span>
                <el-tag v-for="f in courseTemplateHeaders" :key="f" size="small" effect="plain">{{ f }}</el-tag>
                <el-popover placement="bottom" :width="340" trigger="hover">
                  <template #reference>
                    <el-button link type="primary" size="small">常见错误提示示例</el-button>
                  </template>
                  <div v-for="e in commonCourseErrors" :key="e.reason" style="margin-bottom:6px;font-size:12px;color:#dc2626">
                    {{ e.rowNumber }}: {{ e.reason }}
                  </div>
                </el-popover>
              </div>

              <el-upload
                drag
                :auto-upload="false"
                :before-upload="beforeUpload"
                :on-change="handleCourseFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
                class="upload-box"
              >
                <el-icon class="upload-box__icon"><UploadFilled /></el-icon>
                <div class="upload-box__text">
                  <em>点击选择</em> 或将 Excel 文件拖拽到此区域
                </div>
                <template #tip>
                  <div class="upload-box__tip">支持 .xlsx、.xls、.csv 格式，单次不超过 500 条</div>
                </template>
              </el-upload>

              <div class="upload-actions">
                <el-button type="primary" size="large" :loading="courseImporting" :disabled="!courseFile" @click="handleCourseImport">
                  <el-icon><UploadFilled /></el-icon> 开始导入
                </el-button>
                <el-button v-if="courseFinished || courseFile" size="large" @click="resetCourseState">重置</el-button>
              </div>
            </section>

            <!-- 下部：导入结果预览区（始终可见） -->
            <section class="result-section">
              <div class="section-header">
                <h3>步骤二：查看导入结果预览</h3>
              </div>

              <ImportResultPreview
                title="全专业课程清单导入结果"
                :summary="courseResult.summary"
                :failed-items="courseResult.failedItems"
                :loading="courseImporting"
              />
            </section>
          </div>
        </el-tab-pane>

        <!-- ==================== 教学班学生名单导入 ==================== -->
        <el-tab-pane label="教学班学生名单导入" name="student">
          <div class="tab-layout">
            <!-- 上部：上传区 -->
            <section class="upload-section">
              <div class="section-header">
                <h3>步骤一：上传学生名单 Excel 文件</h3>
                <el-button type="primary" plain :icon="Download" @click="downloadTemplate(studentTemplateHeaders, studentTemplateSample, '学生名单导入模板.csv')">
                  下载导入模板
                </el-button>
              </div>

              <div class="template-hints">
                <span class="hint-label">模板字段：</span>
                <el-tag v-for="f in studentTemplateHeaders" :key="f" size="small" effect="plain">{{ f }}</el-tag>
                <el-popover placement="bottom" :width="360" trigger="hover">
                  <template #reference>
                    <el-button link type="primary" size="small">常见错误提示示例</el-button>
                  </template>
                  <div v-for="e in commonStudentErrors" :key="e.reason" style="margin-bottom:6px;font-size:12px;color:#dc2626">
                    {{ e.rowNumber }}: {{ e.reason }}
                  </div>
                </el-popover>
              </div>

              <el-upload
                drag
                :auto-upload="false"
                :before-upload="beforeUpload"
                :on-change="handleStudentFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
                class="upload-box"
              >
                <el-icon class="upload-box__icon"><UploadFilled /></el-icon>
                <div class="upload-box__text">
                  <em>点击选择</em> 或将 Excel 文件拖拽到此区域
                </div>
                <template #tip>
                  <div class="upload-box__tip">支持 .xlsx、.xls、.csv 格式，单次不超过 500 条</div>
                </template>
              </el-upload>

              <div class="upload-actions">
                <el-button type="primary" size="large" :loading="studentImporting" :disabled="!studentFile" @click="handleStudentImport">
                  <el-icon><UploadFilled /></el-icon> 开始导入
                </el-button>
                <el-button v-if="studentFinished || studentFile" size="large" @click="resetStudentState">重置</el-button>
              </div>
            </section>

            <!-- 下部：导入结果预览区（始终可见） -->
            <section class="result-section">
              <div class="section-header">
                <h3>步骤二：查看导入结果预览</h3>
              </div>

              <ImportResultPreview
                title="教学班学生名单导入结果"
                :summary="studentResult.summary"
                :failed-items="studentResult.failedItems"
                :loading="studentImporting"
              />
            </section>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.import-page {
  padding: 20px;
}

.import-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 4px 0 8px;
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
  margin: 0;
  max-width: 720px;
  color: #64748b;
  line-height: 1.75;
}

.tab-layout {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 8px 0;
}

.upload-section,
.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.template-hints {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.hint-label {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  margin-right: 4px;
}

.upload-box {
  width: 100%;
}

.upload-box__icon {
  font-size: 48px;
  color: #94a3b8;
}

.upload-box__text {
  margin-top: 8px;
  color: #64748b;
  font-size: 14px;
}

.upload-box__text em {
  color: #2563eb;
  font-style: normal;
  cursor: pointer;
}

.upload-box__tip {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-section {
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}
</style>
