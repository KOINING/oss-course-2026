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

const COURSE_TEMPLATE_HEADERS = ['鎵€灞炰笓涓氫唬鐮?, '閫傜敤骞寸骇', '璇剧▼浠ｇ爜', '璇剧▼鍚嶇О', '瀛﹀垎', '鐘舵€?]
const STUDENT_TEMPLATE_HEADERS = ['瀛﹀彿', '濮撳悕', '涓撲笟浠ｇ爜', '鍏ュ骞翠唤', '鐘舵€?]
const STUDENT_CLASS_TEMPLATE_HEADERS = ['瀛﹀彿', '濮撳悕', '涓撲笟浠ｇ爜', '鍏ュ骞翠唤', '鏁欏鐝唬鐮?]

const courseFile = ref(null)
const courseImporting = ref(false)
const courseResult = reactive({
  summary: { totalCount: 0, successCount: 0, skippedCount: undefined, failureCount: 0 },
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

const headerMeta = computed(() => ({
  title: route.meta.title || '鏁版嵁瀵煎叆',
  summary:
    route.meta.summary ||
    '鏀寔璇剧▼娓呭崟銆佸鐢熶俊鎭拰鏁欏鐝垚鍛樺叧绯诲鍏ワ紝骞舵彁渚涢€愯鏍￠獙缁撴灉銆?,
  moduleTitle: route.meta.moduleTitle || '妯″潡 A',
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
  const supported = file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv')
  if (!supported) {
    ElMessage.error('浠呮敮鎸?.xlsx銆?xls 鍜?.csv 鏂囦欢銆?)
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
    ElMessage.warning('璇峰厛閫夋嫨璇剧▼瀵煎叆鏂囦欢銆?)
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
    ElMessage.success('璇剧▼瀵煎叆瀹屾垚銆?)
  } catch (error) {
    ElMessage.error(error.message || '璇剧▼瀵煎叆澶辫触銆?)
  } finally {
    courseImporting.value = false
  }
}

async function submitStudentImport() {
  if (!studentFile.value) {
    ElMessage.warning('璇峰厛閫夋嫨瀛︾敓瀵煎叆鏂囦欢銆?)
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
    ElMessage.success('瀛︾敓瀵煎叆瀹屾垚銆?)
  } catch (error) {
    ElMessage.error(error.message || '瀛︾敓瀵煎叆澶辫触銆?)
  } finally {
    studentImporting.value = false
  }
}

async function submitStudentClassImport() {
  if (!studentClassFile.value) {
    ElMessage.warning('璇峰厛閫夋嫨瀛︾敓鏁欏鐝叧绯诲鍏ユ枃浠躲€?)
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
    ElMessage.success('瀛︾敓鏁欏鐝叧绯诲鍏ュ畬鎴愩€?)
  } catch (error) {
    ElMessage.error(error.message || '瀛︾敓鏁欏鐝叧绯诲鍏ュけ璐ャ€?)
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
        <el-tab-pane label="璇剧▼瀵煎叆" name="course">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>涓婁紶璇剧▼娓呭崟</h3>
                <el-button type="primary" plain :icon="Download" @click="downloadCourseTemplate">涓嬭浇妯℃澘</el-button>
              </div>

              <div class="template-tags">
                <el-tag v-for="header in COURSE_TEMPLATE_HEADERS" :key="header" effect="plain">{{ header }}</el-tag>
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
                <div class="el-upload__text">灏嗘枃浠舵嫋鍒版澶勶紝鎴栫偣鍑婚€夋嫨鏂囦欢銆?/div>
                <div class="upload-tip">鏀寔瀵煎叆 .xlsx銆?xls銆?csv锛屾帹鑽愪紭鍏堜娇鐢?.xlsx銆?/div>
              </el-upload>

              <div class="action-row">
                <el-button type="primary" :loading="courseImporting" @click="submitCourseImport">寮€濮嬪鍏?/el-button>
                <el-button @click="resetCourseImport">娓呯┖</el-button>
              </div>
            </section>

            <ImportResultPreview
              title="璇剧▼瀵煎叆缁撴灉"
              :summary="courseResult.summary"
              :failed-items="courseResult.failedItems"
              :loading="courseImporting"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="瀛︾敓瀵煎叆" name="students">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>涓婁紶瀛︾敓淇℃伅</h3>
                <el-button type="primary" plain :icon="Download" @click="downloadTemplate('/templates/students.xlsx', 'students-template.xlsx')">
                  涓嬭浇妯℃澘
                </el-button>
              </div>

              <div class="template-tags">
                <el-tag v-for="header in STUDENT_TEMPLATE_HEADERS" :key="header" effect="plain">{{ header }}</el-tag>
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
                <div class="el-upload__text">灏嗘枃浠舵嫋鍒版澶勶紝鎴栫偣鍑婚€夋嫨鏂囦欢銆?/div>
                <div class="upload-tip">鏀寔瀵煎叆 .xlsx銆?xls銆?csv锛屾帹鑽愪紭鍏堜娇鐢?.xlsx銆?/div>
              </el-upload>

              <div class="action-row">
                <el-button type="primary" :loading="studentImporting" @click="submitStudentImport">寮€濮嬪鍏?/el-button>
                <el-button @click="resetStudentImport">娓呯┖</el-button>
              </div>
            </section>

            <ImportResultPreview
              title="瀛︾敓瀵煎叆缁撴灉"
              :summary="studentResult.summary"
              :failed-items="studentResult.failedItems"
              :loading="studentImporting"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="瀛︾敓鏁欏鐝叧绯诲鍏? name="student-classes">
          <div class="tab-layout">
            <section class="upload-section">
              <div class="section-header">
                <h3>涓婁紶瀛︾敓鏁欏鐝叧绯?/h3>
                <el-button
                  type="primary"
                  plain
                  :icon="Download"
                  @click="downloadTemplate('/templates/student-classes.xlsx', 'student-class-template.xlsx')"
                >
                  涓嬭浇妯℃澘
                </el-button>
              </div>

              <div class="template-tags">
                <el-tag v-for="header in STUDENT_CLASS_TEMPLATE_HEADERS" :key="header" effect="plain">{{ header }}</el-tag>
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
                <div class="el-upload__text">灏嗘枃浠舵嫋鍒版澶勶紝鎴栫偣鍑婚€夋嫨鏂囦欢銆?/div>
                <div class="upload-tip">鏀寔瀵煎叆 .xlsx銆?xls銆?csv锛屾帹鑽愪紭鍏堜娇鐢?.xlsx銆?/div>
              </el-upload>

              <div class="action-row">
                <el-button type="primary" :loading="studentClassImporting" @click="submitStudentClassImport">寮€濮嬪鍏?/el-button>
                <el-button @click="resetStudentClassImport">娓呯┖</el-button>
              </div>

              <div v-if="studentClassImportSucceeded" class="link-row">
                <el-button type="primary" link @click="goToTeachingClassRelations">鎵撳紑鏁欏鐝〉闈?/el-button>
                <el-button type="primary" link @click="goToStudentList">鎵撳紑瀛︾敓鍒楄〃</el-button>
              </div>

              <div v-if="targetTeachingClass" class="target-card">
                <div class="target-title">褰撳墠鏁欏鐝?/div>
                <div class="target-content">
                  <span>{{ targetTeachingClass.classCode }}</span>
                  <span>{{ targetTeachingClass.className }}</span>
                </div>
              </div>
            </section>

            <ImportResultPreview
              title="瀛︾敓鏁欏鐝叧绯诲鍏ョ粨鏋?
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
