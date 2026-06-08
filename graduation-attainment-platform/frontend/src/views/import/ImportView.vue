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

const COURSE_TEMPLATE_HEADERS = ['所属专业代码', '适用年级', '课程代码', '课程名称', '学分', '状态']
const STUDENT_TEMPLATE_HEADERS = ['学号', '姓名', '专业代码', '入学年份', '状态']
const STUDENT_CLASS_TEMPLATE_HEADERS = ['学号', '姓名', '专业代码', '入学年份', '教学班代码']

const courseFile = ref(null)
const studentFile = ref(null)
const studentClassFile = ref(null)

const courseImporting = ref(false)
const studentImporting = ref(false)
const studentClassImporting = ref(false)

const courseResult = reactive({
  summary: { totalCount: 0, successCount: 0, skippedCount: undefined, failureCount: 0 },
  failedItems: [],
})

const studentResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

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

const headerMeta = computed(() => ({
  title: route.meta.title || '数据导入',
  summary:
    route.meta.summary || '支持课程清单、学生信息和教学班学生关系导入，并提供逐行校验结果。',
  moduleTitle: route.meta.moduleTitle || '模块 A',
}))

watch(
  () => route.query.type,
  (type) => {
    if (type === 'students') {
      activeTab.value = 'students'
      return
    }
    if (type === 'student-classes') {
      activeTab.value = 'student-classes'
      return
    }
    activeTab.value = 'course'
  },
  { immediate: true },
)

function resetResult(resultState) {
  const hasSkippedCount = Object.prototype.hasOwnProperty.call(resultState.summary || {}, 'skippedCount')
  resultState.summary = hasSkippedCount
    ? { totalCount: 0, successCount: 0, skippedCount: undefined, failureCount: 0 }
    : { totalCount: 0, successCount: 0, failureCount: 0 }
  resultState.failedItems = []
}

function beforeUpload(file) {
  const supported =
    file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv')
  if (!supported) {
    ElMessage.error('仅支持 .xlsx、.xls 和 .csv 文件。')
    return false
  }
  return false
}

function downloadTemplate(path, filename) {
  const url = `${import.meta.env.BASE_URL}${path.replace(/^\/+/, '')}`
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function downloadCourseTemplate() {
  downloadTemplate('/templates/course-import-template.xlsx', 'course-import-template.xlsx')
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
    ElMessage.warning('请先选择课程导入文件。')
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
      skippedCount: data.skippedCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    courseResult.failedItems = data.failedItems ?? []
    ElMessage.success('课程导入完成。')
  } catch (error) {
    ElMessage.error(error.message || '课程导入失败。')
  } finally {
    courseImporting.value = false
  }
}

async function submitStudentImport() {
  if (!studentFile.value) {
    ElMessage.warning('请先选择学生导入文件。')
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
    ElMessage.success('学生导入完成。')
  } catch (error) {
    ElMessage.error(error.message || '学生导入失败。')
  } finally {
    studentImporting.value = false
  }
}

async function submitStudentClassImport() {
  if (!studentClassFile.value) {
    ElMessage.warning('请先选择学生教学班关系导入文件。')
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
    ElMessage.success('学生教学班关系导入完成。')
  } catch (error) {
    ElMessage.error(error.message || '学生教学班关系导入失败。')
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
        <el-tab-pane label="课程导入" name="course">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传课程清单</h3>
                <el-button type="primary" plain :icon="Download" @click="downloadCourseTemplate">
                  下载模板
                </el-button>
              </div>

              <div class="template-tags">
                <el-tag v-for="header in COURSE_TEMPLATE_HEADERS" :key="header" effect="plain">
                  {{ header }}
                </el-tag>
              </div>

              <el-upload
                ref="courseUploadRef"
                drag
                :auto-upload="false"
                :show-file-list="true"
                :before-upload="beforeUpload"
                :on-change="handleCourseFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
                class="upload-box"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="el-upload__text">将文件拖到此处，或点击选择文件。</div>
                <div class="upload-tip">支持导入 .xlsx、.xls、.csv，推荐优先使用 .xlsx。</div>
              </el-upload>

              <div class="action-row">
                <el-button type="primary" :loading="courseImporting" @click="submitCourseImport">
                  开始导入
                </el-button>
                <el-button @click="resetCourseImport">清空</el-button>
              </div>
            </section>

            <ImportResultPreview
              title="课程导入结果"
              :summary="courseResult.summary"
              :failed-items="courseResult.failedItems"
              :loading="courseImporting"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="学生导入" name="students">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传学生信息</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate('/templates/student-import-template.xlsx', 'students-template.xlsx')"
                >
                  下载模板
                </el-button>
              </div>

              <div class="template-tags">
                <el-tag v-for="header in STUDENT_TEMPLATE_HEADERS" :key="header" effect="plain">
                  {{ header }}
                </el-tag>
              </div>

              <el-upload
                ref="studentUploadRef"
                drag
                :auto-upload="false"
                :show-file-list="true"
                :before-upload="beforeUpload"
                :on-change="handleStudentFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
                class="upload-box"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="el-upload__text">将文件拖到此处，或点击选择文件。</div>
                <div class="upload-tip">支持导入 .xlsx、.xls、.csv，推荐优先使用 .xlsx。</div>
              </el-upload>

              <div class="action-row">
                <el-button type="primary" :loading="studentImporting" @click="submitStudentImport">
                  开始导入
                </el-button>
                <el-button @click="resetStudentImport">清空</el-button>
              </div>
            </section>

            <ImportResultPreview
              title="学生导入结果"
              :summary="studentResult.summary"
              :failed-items="studentResult.failedItems"
              :loading="studentImporting"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="学生教学班关系导入" name="student-classes">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传学生教学班关系</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate('/templates/student-class-import-template.xlsx', 'student-class-template.xlsx')"
                >
                  下载模板
                </el-button>
              </div>

              <div class="template-tags">
                <el-tag
                  v-for="header in STUDENT_CLASS_TEMPLATE_HEADERS"
                  :key="header"
                  effect="plain"
                >
                  {{ header }}
                </el-tag>
              </div>

              <el-upload
                ref="studentClassUploadRef"
                drag
                :auto-upload="false"
                :show-file-list="true"
                :before-upload="beforeUpload"
                :on-change="handleStudentClassFileChange"
                :limit="1"
                accept=".xlsx,.xls,.csv"
                class="upload-box"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="el-upload__text">将文件拖到此处，或点击选择文件。</div>
                <div class="upload-tip">支持导入 .xlsx、.xls、.csv，推荐优先使用 .xlsx。</div>
              </el-upload>

              <div class="action-row">
                <el-button
                  type="primary"
                  :loading="studentClassImporting"
                  @click="submitStudentClassImport"
                >
                  开始导入
                </el-button>
                <el-button @click="resetStudentClassImport">清空</el-button>
              </div>

              <div v-if="studentClassImportSucceeded" class="link-row">
                <el-button type="primary" link @click="goToTeachingClassRelations">
                  打开教学班页面
                </el-button>
                <el-button type="primary" link @click="goToStudentList">打开学生列表</el-button>
              </div>

              <div v-if="targetTeachingClass" class="target-card">
                <div class="target-title">当前教学班</div>
                <div class="target-content">
                  <span>{{ targetTeachingClass.classCode }}</span>
                  <span>{{ targetTeachingClass.className }}</span>
                </div>
              </div>
            </section>

            <ImportResultPreview
              title="学生教学班关系导入结果"
              :summary="studentClassResult.summary"
              :failed-items="studentClassResult.failedItems"
              :loading="studentClassImporting"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.import-page {
  padding: 16px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.page-section {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.page-summary {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.tab-layout {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 20px;
}

.upload-section {
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
  font-size: 28px;
  color: #3b82f6;
}

.upload-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.action-row {
  display: flex;
  gap: 12px;
}

.link-row {
  display: flex;
  gap: 16px;
}

.target-card {
  padding: 12px;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
  background: #f8fafc;
}

.target-title {
  font-size: 12px;
  color: #64748b;
}

.target-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
}

@media (max-width: 960px) {
  .tab-layout {
    grid-template-columns: 1fr;
  }
}
</style>
