<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import { importCoursesApi, importStudentClassesApi } from '@/api/import'

const route = useRoute()
const activeTab = ref('course')

const COURSE_TEMPLATE_HEADERS = ['所属专业代码', '课程代码', '课程名称', '学分', '状态']
const COURSE_TEMPLATE_SAMPLE = [
  ['080901', 'CS201', '数据结构', '4.0', '1'],
  ['080901', 'CS301', '操作系统', '3.0', '1'],
]

const STUDENT_CLASS_TEMPLATE_HEADERS = ['学号', '姓名', '专业代码', '入学年份', '教学班编号']
const STUDENT_CLASS_TEMPLATE_SAMPLE = [
  ['20220101001', '张三', '080901', '2022', 'TC2024CS01'],
  ['20220101002', '李四', '080901', '2022', 'TC2024CS01'],
]

const courseFile = ref(null)
const courseImporting = ref(false)
const courseResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

const studentClassFile = ref(null)
const studentClassImporting = ref(false)
const studentClassResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

function syncTabWithRoute() {
  activeTab.value = route.query.type === 'student-classes' ? 'student-classes' : 'course'
}

watch(() => route.query.type, syncTabWithRoute, { immediate: true })

function resetResult(resultState) {
  resultState.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  resultState.failedItems = []
}

function beforeUpload(file) {
  const isSupported =
    file.name.endsWith('.xlsx') ||
    file.name.endsWith('.xls') ||
    file.name.endsWith('.csv')
  if (!isSupported) {
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

function handleStudentClassFileChange(file) {
  studentClassFile.value = file
  resetResult(studentClassResult)
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

async function submitStudentClassImport() {
  if (!studentClassFile.value) {
    ElMessage.warning('请先选择教学班学生名单文件')
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
    ElMessage.success('教学班学生名单导入完成')
  } catch (error) {
    ElMessage.error(error.message || '教学班学生名单导入失败')
  } finally {
    studentClassImporting.value = false
  }
}

const headerMeta = computed(() => ({
  title: route.meta.title || '数据导入',
  summary: route.meta.summary || '通过 Excel 模板导入课程清单和教学班学生名单，并在页面内查看结果预览。',
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
                <el-tag
                  v-for="header in COURSE_TEMPLATE_HEADERS"
                  :key="header"
                  size="small"
                  effect="plain"
                >
                  {{ header }}
                </el-tag>
              </div>
              <el-upload
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
                <el-button @click="courseFile = null; resetResult(courseResult)">重置</el-button>
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

        <el-tab-pane label="教学班学生名单导入" name="student-classes">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>上传教学班学生名单 Excel 文件</h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate(STUDENT_CLASS_TEMPLATE_HEADERS, STUDENT_CLASS_TEMPLATE_SAMPLE, '教学班学生名单导入模板.csv')"
                >
                  下载模板
                </el-button>
              </div>
              <div class="template-tags">
                <el-tag
                  v-for="header in STUDENT_CLASS_TEMPLATE_HEADERS"
                  :key="header"
                  size="small"
                  effect="plain"
                >
                  {{ header }}
                </el-tag>
              </div>
              <el-upload
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
                <el-button @click="studentClassFile = null; resetResult(studentClassResult)">重置</el-button>
              </div>
            </section>

            <section class="result-section">
              <ImportResultPreview
                title="教学班学生名单导入结果"
                :summary="studentClassResult.summary"
                :failed-items="studentClassResult.failedItems"
                :loading="studentClassImporting"
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
  max-width: 760px;
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
