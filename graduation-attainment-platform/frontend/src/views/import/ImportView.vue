<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import { importCoursesApi, importStudentClassesApi, importStudentsApi } from '@/api/import'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const activeTab = ref('course')

const courseUploadRef = ref(null)
const studentUploadRef = ref(null)
const studentClassUploadRef = ref(null)

const COURSE_TEMPLATE_HEADERS = ['所属专业代码', '课程代码', '课程名称', '学分', '状态']
const COURSE_TEMPLATE_SAMPLE = [
  ['080901', 'CS201', '数据结构', '4.0', '1'],
  ['080901', 'CS301', '操作系统', '3.0', '1'],
]

const STUDENT_TEMPLATE_HEADERS = ['学号', '姓名', '专业代码', '入学年份', '学籍状态']
const STUDENT_TEMPLATE_SAMPLE = [
  ['20240101001', '张晓晨', '080901', '2024', '1'],
  ['20240101002', '李思雨', '080901', '2024', '1'],
]

const STUDENT_CLASS_TEMPLATE_HEADERS = ['学号', '姓名', '专业代码', '入学年份', '教学班编号']
const STUDENT_CLASS_TEMPLATE_SAMPLE = [
  ['20220101001', '周一帆', '080901', '2022', 'TC2024CS01'],
  ['20220101002', '陈思远', '080901', '2022', 'TC2024CS01'],
]

const courseFile = ref(null)
const courseImporting = ref(false)
const courseResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

const studentFile = ref(null)
const studentImporting = ref(false)
const studentResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

const studentClassFile = ref(null)
const studentClassImporting = ref(false)
const studentClassResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

const targetTeachingClass = computed(() => {
  const classId = Number(route.query.teachingClassId)
  if (!classId) return null
  return {
    classId,
    classCode: route.query.teachingClassCode || '',
    className: route.query.teachingClassName || '',
  }
})

const studentClassImportSucceeded = computed(
  () => studentClassResult.summary.totalCount > 0 && studentClassResult.summary.successCount > 0,
)

function syncTabWithRoute() {
  if (route.query.type === 'students') {
    activeTab.value = 'students'
    return
  }
  if (route.query.type === 'student-classes') {
    activeTab.value = 'student-classes'
    return
  }
  activeTab.value = 'course'
}

watch(() => route.query.type, syncTabWithRoute, { immediate: true })

function resetResult(resultState) {
  resultState.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  resultState.failedItems = []
}

function beforeUpload(file) {
  const supported = file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv')
  if (!supported) {
    ElMessage.error('仅支持 .xlsx、.xls、.csv 格式')
    return false
  }
  return false
}

function downloadTemplate(headers, rows, filename) {
  const bom = '\uFEFF'
  const content = `${headers.join(',')}\n${rows.map((row) => row.join(',')).join('\n')}\n`
  const blob = new Blob([bom + content], { type: 'text/csv;charset=utf-8;' })
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
  resetResult(courseResult)
}

function handleStudentFileChange(file) {
  studentFile.value = file
  resetResult(studentResult)
}

function handleStudentClassFileChange(file) {
  studentClassFile.value = file
  resetResult(studentClassResult)
}

function resetCourseImport() {
  courseFile.value = null
  resetResult(courseResult)
  courseUploadRef.value?.clearFiles()
}

function resetStudentImport() {
  studentFile.value = null
  resetResult(studentResult)
  studentUploadRef.value?.clearFiles()
}

function resetStudentClassImport() {
  studentClassFile.value = null
  resetResult(studentClassResult)
  studentClassUploadRef.value?.clearFiles()
}

async function submitCourseImport() {
  if (!courseFile.value) {
    ElMessage.warning('请先选择课程清单文件')
    return
  }

  const formData = new FormData()
  formData.append('file', courseFile.value.raw)
  courseImporting.value = true
  try {
    const data = await importCoursesApi(formData)
    courseResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    courseResult.failedItems = data.failedItems ?? []
    ElMessage.success('课程清单导入完成')
  } catch (error) {
    ElMessage.error(error.message || '课程清单导入失败')
  } finally {
    courseImporting.value = false
  }
}

async function submitStudentImport() {
  if (!studentFile.value) {
    ElMessage.warning('请先选择学生基础信息文件')
    return
  }

  const formData = new FormData()
  formData.append('file', studentFile.value.raw)
  studentImporting.value = true
  try {
    const data = await importStudentsApi(formData)
    studentResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    studentResult.failedItems = data.failedItems ?? []
    ElMessage.success('学生基础信息导入完成')
  } catch (error) {
    ElMessage.error(error.message || '学生基础信息导入失败')
  } finally {
    studentImporting.value = false
  }
}

async function submitStudentClassImport() {
  if (!studentClassFile.value) {
    ElMessage.warning('请先选择教学班学生关联文件')
    return
  }

  const formData = new FormData()
  formData.append('file', studentClassFile.value.raw)
  studentClassImporting.value = true
  try {
    const data = await importStudentClassesApi(formData)
    studentClassResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    studentClassResult.failedItems = data.failedItems ?? []
    ElMessage.success('教学班学生关联导入完成')
  } catch (error) {
    ElMessage.error(error.message || '教学班学生关联导入失败')
  } finally {
    studentClassImporting.value = false
  }
}

function goToTeachingClassRelations() {
  if (!targetTeachingClass.value) {
    router.push({ name: ROUTE_NAMES.TEACHING_CLASS })
    return
  }
  router.push({
    name: ROUTE_NAMES.TEACHING_CLASS,
    query: { openClassId: targetTeachingClass.value.classId },
  })
}

function goToStudentList() {
  router.push({ name: ROUTE_NAMES.STUDENT_LIST })
}

const headerMeta = computed(() => ({
  title: route.meta.title || '数据导入',
  summary:
    route.meta.summary ||
    '通过 Excel 模板批量导入课程清单、学生基础信息和教学班学生关联，支持逐行校验、错误定位与结果预览。',
  moduleTitle: route.meta.moduleTitle || '模块 A',
}))
</script>

<template>
  <div class="import-page">
    <el-card class="import-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ headerMeta.moduleTitle }}</p>
            <h1>{{ headerMeta.title }}</h1>
            <p class="page-summary">{{ headerMeta.summary }}</p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="课程清单导入" name="course">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传课程清单 Excel 文件</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate(COURSE_TEMPLATE_HEADERS, COURSE_TEMPLATE_SAMPLE, '课程清单导入模板.csv')"
                >
                  下载模板
                </el-button>
              </div>
              <div class="template-tags">
                <el-tag v-for="header in COURSE_TEMPLATE_HEADERS" :key="header" size="small" effect="plain">
                  {{ header }}
                </el-tag>
              </div>
              <el-upload
                ref="courseUploadRef"
                drag
                class="upload-box"
                :auto-upload="false"
                :before-upload="beforeUpload"
                :on-change="handleCourseFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text"><em>点击选择</em> 或拖拽文件到此处</div>
                <template #tip>
                  <div class="upload-tip">支持 .xlsx、.xls、.csv</div>
                </template>
              </el-upload>
              <div class="upload-actions">
                <el-button type="primary" :loading="courseImporting" :disabled="!courseFile" @click="submitCourseImport">
                  开始导入
                </el-button>
                <el-button @click="resetCourseImport">重置</el-button>
              </div>
            </section>

            <section class="result-section">
              <ImportResultPreview
                title="课程清单导入结果"
                :summary="courseResult.summary"
                :failed-items="courseResult.failedItems"
                :loading="courseImporting"
              />
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="学生基础信息导入" name="students">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传学生基础信息 Excel 文件</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate(STUDENT_TEMPLATE_HEADERS, STUDENT_TEMPLATE_SAMPLE, '学生基础信息导入模板.csv')"
                >
                  下载模板
                </el-button>
              </div>
              <div class="template-tags">
                <el-tag v-for="header in STUDENT_TEMPLATE_HEADERS" :key="header" size="small" effect="plain">
                  {{ header }}
                </el-tag>
              </div>
              <el-upload
                ref="studentUploadRef"
                drag
                class="upload-box"
                :auto-upload="false"
                :before-upload="beforeUpload"
                :on-change="handleStudentFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text"><em>点击选择</em> 或拖拽文件到此处</div>
                <template #tip>
                  <div class="upload-tip">支持 .xlsx、.xls、.csv</div>
                </template>
              </el-upload>
              <div class="upload-actions">
                <el-button type="primary" :loading="studentImporting" :disabled="!studentFile" @click="submitStudentImport">
                  开始导入
                </el-button>
                <el-button @click="resetStudentImport">重置</el-button>
              </div>
            </section>

            <section class="result-section">
              <ImportResultPreview
                title="学生基础信息导入结果"
                :summary="studentResult.summary"
                :failed-items="studentResult.failedItems"
                :loading="studentImporting"
              >
                <template #header-actions>
                  <el-button
                    v-if="studentResult.summary.totalCount > 0 && studentResult.summary.successCount > 0"
                    type="primary"
                    plain
                    @click="goToStudentList"
                  >
                    查看学生基础信息管理
                  </el-button>
                </template>
              </ImportResultPreview>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="教学班学生关联导入" name="student-classes">
          <div class="tab-layout">
            <section class="upload-section">
              <el-alert v-if="targetTeachingClass" type="info" :closable="false" class="target-tip" show-icon>
                <template #title>
                  当前从教学班管理进入，目标教学班：{{ targetTeachingClass.className || '未命名教学班' }}
                  <span v-if="targetTeachingClass.classCode">（{{ targetTeachingClass.classCode }}）</span>
                </template>
              </el-alert>

              <div class="section-header">
                <h3>上传教学班学生关联 Excel 文件</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="
                    downloadTemplate(
                      STUDENT_CLASS_TEMPLATE_HEADERS,
                      STUDENT_CLASS_TEMPLATE_SAMPLE,
                      '教学班学生关联导入模板.csv',
                    )
                  "
                >
                  下载模板
                </el-button>
              </div>
              <div class="template-tags">
                <el-tag v-for="header in STUDENT_CLASS_TEMPLATE_HEADERS" :key="header" size="small" effect="plain">
                  {{ header }}
                </el-tag>
              </div>
              <el-upload
                ref="studentClassUploadRef"
                drag
                class="upload-box"
                :auto-upload="false"
                :before-upload="beforeUpload"
                :on-change="handleStudentClassFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text"><em>点击选择</em> 或拖拽文件到此处</div>
                <template #tip>
                  <div class="upload-tip">支持 .xlsx、.xls、.csv</div>
                </template>
              </el-upload>
              <div class="upload-actions">
                <el-button
                  type="primary"
                  :loading="studentClassImporting"
                  :disabled="!studentClassFile"
                  @click="submitStudentClassImport"
                >
                  开始导入
                </el-button>
                <el-button @click="resetStudentClassImport">重置</el-button>
              </div>
            </section>

            <section class="result-section">
              <ImportResultPreview
                title="教学班学生关联导入结果"
                :summary="studentClassResult.summary"
                :failed-items="studentClassResult.failedItems"
                :loading="studentClassImporting"
              >
                <template #header-actions>
                  <el-button
                    v-if="studentClassImportSucceeded && targetTeachingClass"
                    type="primary"
                    plain
                    @click="goToTeachingClassRelations"
                  >
                    查看教学班名单关联
                  </el-button>
                </template>
              </ImportResultPreview>
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
  width: 100%;
  max-width: none;
  color: #64748b;
  line-height: 1.75;
}

.tab-layout {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(420px, 1fr);
  gap: 20px;
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
  gap: 12px;
}

.section-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
}

.target-tip {
  margin: 0;
}

.template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-box {
  width: 100%;
}

.upload-icon {
  font-size: 44px;
  color: #94a3b8;
}

.upload-text {
  color: #475569;
}

.upload-tip {
  color: #94a3b8;
}

.upload-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 1100px) {
  .tab-layout {
    grid-template-columns: 1fr;
  }
}
</style>
