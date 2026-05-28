<template>
  <div class="basic-data-page">
    <el-card class="basic-data-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="data-tabs">
        <el-tab-pane label="学年学期" name="academic-term">
          <AcademicTermPanel />
        </el-tab-pane>

        <el-tab-pane label="学院" name="college">
          <CollegePanel />
        </el-tab-pane>

        <el-tab-pane label="专业管理" name="major">
          <div class="tab-content">
            <el-form :inline="true" :model="majorFilters" class="filter-form">
              <el-form-item label="专业代码">
                <el-input
                  v-model.trim="majorFilters.majorCode"
                  placeholder="请输入专业代码"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="专业名称">
                <el-input
                  v-model.trim="majorFilters.majorName"
                  placeholder="请输入专业名称"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="所属学院">
                <el-select
                  v-model="majorFilters.collegeId"
                  placeholder="全部学院"
                  clearable
                  style="width: 140px"
                >
                  <el-option
                    v-for="college in collegeOptions"
                    :key="college.collegeId"
                    :label="college.collegeName"
                    :value="college.collegeId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="majorFilters.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 100px"
                >
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="停用" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadMajors">查询</el-button>
                <el-button @click="resetMajorFilters">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-toolbar">
              <el-button type="primary" @click="openMajorDialog('create')">
                <el-icon><Plus /></el-icon>
                新增专业
              </el-button>
            </div>

            <el-table v-loading="majorLoading" :data="majors" border stripe>
              <el-table-column prop="majorCode" label="专业代码" width="140" />
              <el-table-column prop="majorName" label="专业名称" min-width="180" />
              <el-table-column prop="collegeName" label="所属学院" min-width="180">
                <template #default="{ row }">
                  <el-tag type="info" effect="plain">{{ row.collegeName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <div class="table-actions">
                    <el-button link type="primary" @click="openMajorDialog('edit', row)">
                      编辑
                    </el-button>
                    <el-popconfirm
                      :title="row.status === 1 ? '确认停用该专业吗？' : '确认启用该专业吗？'"
                      @confirm="handleToggleMajorStatus(row)"
                    >
                      <template #reference>
                        <el-button link :type="row.status === 1 ? 'warning' : 'success'">
                          {{ row.status === 1 ? '停用' : '启用' }}
                        </el-button>
                      </template>
                    </el-popconfirm>
                    <el-popconfirm
                      title="确认删除该专业吗？删除后不可恢复。"
                      @confirm="handleDeleteMajor(row)"
                    >
                      <template #reference>
                        <el-button link type="danger">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程管理" name="course">
          <div class="tab-content">
            <el-form :inline="true" :model="courseFilters" class="filter-form">
              <el-form-item label="课程代码">
                <el-input
                  v-model.trim="courseFilters.courseCode"
                  placeholder="请输入课程代码"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="课程名称">
                <el-input
                  v-model.trim="courseFilters.courseName"
                  placeholder="请输入课程名称"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="所属专业">
                <el-select
                  v-model="courseFilters.majorId"
                  placeholder="全部专业"
                  clearable
                  style="width: 140px"
                >
                  <el-option
                    v-for="major in majorOptions"
                    :key="major.majorId"
                    :label="major.majorName"
                    :value="major.majorId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="courseFilters.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 100px"
                >
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="停用" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadCourses">查询</el-button>
                <el-button @click="resetCourseFilters">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-toolbar">
              <el-button type="primary" @click="openCourseDialog('create')">
                <el-icon><Plus /></el-icon>
                新增课程
              </el-button>
              <el-button
                v-if="isAcademicAffairs"
                type="success"
                @click="showImportSection = !showImportSection; resetImportState()"
              >
                <el-icon><UploadFilled /></el-icon>
                {{ showImportSection ? '收起导入' : '导入课程清单' }}
              </el-button>
              <el-button
                v-if="isAcademicAffairs"
                plain
                size="small"
                :icon="Download"
                @click="downloadCourseTemplate"
              >
                下载模板
              </el-button>
            </div>

            <!-- 课程清单导入区域（仅教务管理人员可见） -->
            <div v-if="isAcademicAffairs && showImportSection" class="import-section">
              <el-divider />
              <div class="import-area">
                <div class="import-area__header">
                  <h4>全专业课程清单 Excel 导入</h4>
                  <div class="import-template-hints">
                    <span class="template-label">模板字段：</span>
                    <el-tag v-for="f in courseTemplateHeaders" :key="f" size="small" effect="plain">{{ f }}</el-tag>
                  </div>
                </div>

                <el-upload
                  drag
                  :auto-upload="false"
                  :before-upload="beforeCourseUpload"
                  :on-change="handleCourseFileChange"
                  :limit="1"
                  accept=".xlsx,.xls,.csv"
                >
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">
                    <span class="upload-link">点击选择</span>
                    <span> 或将 Excel 文件拖拽到此区域</span>
                  </div>
                  <template #tip>
                    <div class="upload-tip">支持 .xlsx、.xls、.csv 格式</div>
                  </template>
                </el-upload>

                <div class="import-action">
                  <el-button
                    type="primary"
                    :loading="courseImporting"
                    :disabled="!courseFile"
                    @click="handleCourseImport"
                  >
                    开始导入
                  </el-button>
                  <el-button
                    v-if="importFinished || importError"
                    @click="resetImportState"
                  >
                    重新导入
                  </el-button>
                </div>

                <!-- 导入结果预览（始终可见） -->
                <div class="import-result">
                  <ImportResultPreview
                    title="全专业课程清单导入结果"
                    :summary="importResult.summary"
                    :failed-items="importResult.failedItems"
                    :loading="courseImporting"
                  />
                </div>
              </div>
            </div>

            <el-table v-loading="courseLoading" :data="courses" border stripe>
              <el-table-column prop="courseCode" label="课程代码" width="140" />
              <el-table-column prop="courseName" label="课程名称" min-width="160" />
              <el-table-column prop="credit" label="学分" width="80" align="center">
                <template #default="{ row }">
                  <el-tag type="success" effect="plain">{{ row.credit }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="所属专业" min-width="220">
                <template #default="{ row }">
                  <div class="major-tag-list">
                    <el-tag
                      v-for="majorName in row.majorNames || []"
                      :key="majorName"
                      type="primary"
                      effect="plain"
                    >
                      {{ majorName }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <div class="table-actions">
                    <el-button link type="primary" @click="openCourseDialog('edit', row)">
                      编辑
                    </el-button>
                    <el-popconfirm
                      :title="row.status === 1 ? '确认停用该课程吗？' : '确认启用该课程吗？'"
                      @confirm="handleToggleCourseStatus(row)"
                    >
                      <template #reference>
                        <el-button link :type="row.status === 1 ? 'warning' : 'success'">
                          {{ row.status === 1 ? '停用' : '启用' }}
                        </el-button>
                      </template>
                    </el-popconfirm>
                    <el-popconfirm
                      title="确认删除该课程吗？删除后不可恢复。"
                      @confirm="handleDeleteCourse(row)"
                    >
                      <template #reference>
                        <el-button link type="danger">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="majorDialogVisible"
      :title="majorDialogMode === 'create' ? '新增专业' : '编辑专业'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="majorFormRef"
        :model="majorForm"
        :rules="majorFormRules"
        label-width="90px"
      >
        <el-form-item label="专业代码" prop="majorCode">
          <el-input
            v-model.trim="majorForm.majorCode"
            placeholder="请输入专业代码，如 080901"
            :disabled="majorDialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input
            v-model.trim="majorForm.majorName"
            placeholder="请输入专业名称"
          />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select
            v-model="majorForm.collegeId"
            placeholder="请选择所属学院"
            style="width: 100%"
          >
            <el-option
              v-for="college in collegeOptions"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="majorForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="majorDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="majorSubmitLoading"
            @click="handleMajorSubmit"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="courseDialogVisible"
      :title="courseDialogMode === 'create' ? '新增课程' : '编辑课程'"
      width="520px"
      destroy-on-close
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseFormRules"
        label-width="90px"
      >
        <el-form-item label="课程代码" prop="courseCode">
          <el-input
            v-model.trim="courseForm.courseCode"
            placeholder="请输入课程代码，如 CS201"
            :disabled="courseDialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input
            v-model.trim="courseForm.courseName"
            placeholder="请输入课程名称"
          />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number
            v-model="courseForm.credit"
            :min="0.5"
            :max="20"
            :step="0.5"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorIds">
          <el-select
            v-model="courseForm.majorIds"
            placeholder="请选择所属专业"
            style="width: 100%"
            filterable
            multiple
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="courseDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="courseSubmitLoading"
            @click="handleCourseSubmit"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Plus, UploadFilled } from '@element-plus/icons-vue'
import AcademicTermPanel from './AcademicTermPanel.vue'
import CollegePanel from './CollegePanel.vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import { useUserStore } from '@/stores/user'
import {
  deleteCourseApi,
  deleteMajorApi,
  listCollegesApi,
  listCoursesApi,
  listMajorsApi,
  listMajorsForSelectApi,
  saveCourseApi,
  saveMajorApi,
  updateCourseStatusApi,
  updateMajorStatusApi,
} from '@/api/basic'
import { importCoursesApi } from '@/api/import'

const route = useRoute()
const userStore = useUserStore()
const isAcademicAffairs = computed(() => userStore.roleCodes.includes('academic_affairs'))
const activeTab = ref('academic-term')

const collegeOptions = ref([])
const majorOptions = ref([])

const majorLoading = ref(false)
const majorSubmitLoading = ref(false)
const majorDialogVisible = ref(false)
const majorDialogMode = ref('create')
const majors = ref([])
const majorFormRef = ref(null)

const courseLoading = ref(false)
const courseSubmitLoading = ref(false)
const courseDialogVisible = ref(false)
const courseDialogMode = ref('create')
const courses = ref([])
const courseFormRef = ref(null)

const majorFilters = reactive({
  majorCode: '',
  majorName: '',
  collegeId: null,
  status: null,
})

const majorForm = reactive({
  majorId: null,
  majorCode: '',
  majorName: '',
  collegeId: null,
  status: 1,
})

const courseFilters = reactive({
  courseCode: '',
  courseName: '',
  majorId: null,
  status: null,
})

const courseForm = reactive({
  courseId: null,
  courseCode: '',
  courseName: '',
  credit: 2,
  majorIds: [],
  status: 1,
})

const majorFormRules = {
  majorCode: [
    { required: true, message: '请输入专业代码', trigger: 'blur' },
    { max: 20, message: '专业代码最长 20 位', trigger: 'blur' },
  ],
  majorName: [
    { required: true, message: '请输入专业名称', trigger: 'blur' },
    { max: 100, message: '专业名称最长 100 位', trigger: 'blur' },
  ],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const courseFormRules = {
  courseCode: [
    { required: true, message: '请输入课程代码', trigger: 'blur' },
    { max: 20, message: '课程代码最长 20 位', trigger: 'blur' },
  ],
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { max: 100, message: '课程名称最长 100 位', trigger: 'blur' },
  ],
  credit: [{ required: true, message: '请输入学分', trigger: 'change' }],
  majorIds: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

watch(activeTab, (tab) => {
  if (tab === 'course' && courses.value.length === 0) {
    loadCourses()
  }
})

async function loadColleges() {
  collegeOptions.value = await listCollegesApi()
}

async function loadMajorOptions() {
  majorOptions.value = await listMajorsForSelectApi()
}

function normalizeMajorFilters() {
  return {
    majorCode: majorFilters.majorCode || undefined,
    majorName: majorFilters.majorName || undefined,
    collegeId: majorFilters.collegeId || undefined,
    status: majorFilters.status === null ? undefined : majorFilters.status,
  }
}

function normalizeCourseFilters() {
  return {
    courseCode: courseFilters.courseCode || undefined,
    courseName: courseFilters.courseName || undefined,
    majorId: courseFilters.majorId || undefined,
    status: courseFilters.status === null ? undefined : courseFilters.status,
  }
}

async function loadMajors() {
  majorLoading.value = true
  try {
    majors.value = await listMajorsApi(normalizeMajorFilters())
  } finally {
    majorLoading.value = false
  }
}

async function loadCourses() {
  courseLoading.value = true
  try {
    courses.value = await listCoursesApi(normalizeCourseFilters())
  } finally {
    courseLoading.value = false
  }
}

function resetMajorFilters() {
  majorFilters.majorCode = ''
  majorFilters.majorName = ''
  majorFilters.collegeId = null
  majorFilters.status = null
  loadMajors()
}

function resetCourseFilters() {
  courseFilters.courseCode = ''
  courseFilters.courseName = ''
  courseFilters.majorId = null
  courseFilters.status = null
  loadCourses()
}

function resetMajorForm() {
  majorForm.majorId = null
  majorForm.majorCode = ''
  majorForm.majorName = ''
  majorForm.collegeId = null
  majorForm.status = 1
}

function resetCourseForm() {
  courseForm.courseId = null
  courseForm.courseCode = ''
  courseForm.courseName = ''
  courseForm.credit = 2
  courseForm.majorIds = []
  courseForm.status = 1
}

function openMajorDialog(mode, row = null) {
  majorDialogMode.value = mode
  resetMajorForm()
  if (mode === 'edit' && row) {
    majorForm.majorId = row.majorId
    majorForm.majorCode = row.majorCode
    majorForm.majorName = row.majorName
    majorForm.collegeId = row.collegeId
    majorForm.status = row.status
  }
  majorDialogVisible.value = true
  nextTick(() => majorFormRef.value?.clearValidate())
}

function openCourseDialog(mode, row = null) {
  courseDialogMode.value = mode
  resetCourseForm()
  if (mode === 'edit' && row) {
    courseForm.courseId = row.courseId
    courseForm.courseCode = row.courseCode
    courseForm.courseName = row.courseName
    courseForm.credit = row.credit
    courseForm.majorIds = Array.isArray(row.majorIds) ? [...row.majorIds] : []
    courseForm.status = row.status
  }
  courseDialogVisible.value = true
  nextTick(() => courseFormRef.value?.clearValidate())
}

async function handleMajorSubmit() {
  await majorFormRef.value?.validate()
  majorSubmitLoading.value = true
  try {
    await saveMajorApi({ ...majorForm })
    ElMessage.success(
      majorDialogMode.value === 'create' ? '专业创建成功' : '专业更新成功',
    )
    majorDialogVisible.value = false
    await Promise.all([loadMajors(), loadMajorOptions()])
  } finally {
    majorSubmitLoading.value = false
  }
}

async function handleCourseSubmit() {
  await courseFormRef.value?.validate()
  courseSubmitLoading.value = true
  try {
    await saveCourseApi({ ...courseForm })
    ElMessage.success(
      courseDialogMode.value === 'create' ? '课程创建成功' : '课程更新成功',
    )
    courseDialogVisible.value = false
    await loadCourses()
  } finally {
    courseSubmitLoading.value = false
  }
}

async function handleToggleMajorStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateMajorStatusApi({ majorId: row.majorId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '专业已启用' : '专业已停用')
  await loadMajors()
}

async function handleToggleCourseStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateCourseStatusApi({ courseId: row.courseId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '课程已启用' : '课程已停用')
  await loadCourses()
}

async function handleDeleteMajor(row) {
  await deleteMajorApi(row.majorId)
  ElMessage.success('专业删除成功')
  await Promise.all([loadMajors(), loadMajorOptions()])
}

async function handleDeleteCourse(row) {
  await deleteCourseApi(row.courseId)
  ElMessage.success('课程删除成功')
  await loadCourses()
}

// ---- 课程清单导入 ----
const courseTemplateHeaders = ['所属专业代码', '课程代码', '课程名称', '学分', '状态']
const courseTemplateSample = [
  ['080901', 'CS201', '数据结构', '4.0', '1'],
  ['080901', 'CS301', '操作系统', '3.0', '1'],
]

const showImportSection = ref(false)
const courseFile = ref(null)
const courseImporting = ref(false)
const importResult = reactive({
  summary: { totalCount: 0, successCount: 0, failureCount: 0 },
  failedItems: [],
})

function downloadCourseTemplate() {
  const BOM = '﻿'
  const headers = courseTemplateHeaders.join(',')
  const sampleLines = courseTemplateSample.map((r) => r.join(',')).join('\n')
  const csv = BOM + headers + '\n' + sampleLines + '\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '课程清单导入模板.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function handleCourseFileChange(file) {
  courseFile.value = file
}

function beforeCourseUpload(file) {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv')
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx、.xls 或 .csv 格式')
    return false
  }
  return false
}

function resetImportState() {
  importResult.summary = { totalCount: 0, successCount: 0, failureCount: 0 }
  importResult.failedItems = []
  courseFile.value = null
}

async function handleCourseImport() {
  if (!courseFile.value) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }
  const formData = new FormData()
  formData.append('file', courseFile.value.raw)
  courseImporting.value = true
  try {
    const data = await importCoursesApi(formData)
    importResult.summary = {
      totalCount: data.totalCount ?? 0,
      successCount: data.successCount ?? 0,
      failureCount: data.failureCount ?? 0,
    }
    importResult.failedItems = data.failedItems ?? []
    if (data.successCount > 0) {
      await loadCourses()
    }
    ElMessage.success('课程清单导入完成')
  } catch (e) {
    ElMessage.error(e.message || '导入请求失败')
  } finally {
    courseImporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadColleges(), loadMajorOptions(), loadMajors()])
})
</script>

<style scoped>
.basic-data-page {
  padding: 20px;
}

.basic-data-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
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

.data-tabs {
  margin-top: -8px;
}

.tab-content {
  padding: 4px 0;
}

.filter-form {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.major-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.import-section {
  margin-top: 4px;
}

.import-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.import-area__header h4 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
}

.import-template-hints {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.template-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.upload-icon {
  font-size: 48px;
  color: #94a3b8;
}

.upload-text {
  margin-top: 8px;
  color: #64748b;
  font-size: 14px;
}

.upload-link {
  color: #2563eb;
  cursor: pointer;
}

.upload-tip {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.import-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.import-result {
  margin-top: 4px;
}
</style>
