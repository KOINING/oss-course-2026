<template>
  <div class="basic-data-page">
    <el-card class="basic-data-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础与宏观数据管理</p>
            <h1>基础数据</h1>
            <p class="page-summary">
              维护学院、专业、课程等基础主数据，为毕业要求配置和后续计算流程提供统一数据底座。
            </p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="data-tabs">

        <!-- ==================== 专业子页 ==================== -->
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
                      v-for="c in collegeOptions"
                      :key="c.collegeId"
                      :label="c.collegeName"
                      :value="c.collegeId"
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
                <el-icon><Plus /></el-icon> 新增专业
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

        <!-- ==================== 课程子页 ==================== -->
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
                      v-for="m in majorOptions"
                      :key="m.majorId"
                      :label="m.majorName"
                      :value="m.majorId"
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
                <el-icon><Plus /></el-icon> 新增课程
              </el-button>
            </div>

            <el-table v-loading="courseLoading" :data="courses" border stripe>
              <el-table-column prop="courseCode" label="课程代码" width="140" />
              <el-table-column prop="courseName" label="课程名称" min-width="160" />
              <el-table-column prop="credit" label="学分" width="80" align="center">
                <template #default="{ row }">
                  <el-tag type="success" effect="plain">{{ row.credit }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="majorName" label="所属专业" min-width="160">
                <template #default="{ row }">
                  <el-tag type="primary" effect="plain">{{ row.majorName }}</el-tag>
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

    <!-- ==================== 专业 Dialog ==================== -->
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
          <el-input v-model.trim="majorForm.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select
              v-model="majorForm.collegeId"
              placeholder="请选择所属学院"
              style="width: 100%"
          >
            <el-option
                v-for="c in collegeOptions"
                :key="c.collegeId"
                :label="c.collegeName"
                :value="c.collegeId"
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
          <el-button type="primary" :loading="majorSubmitLoading" @click="handleMajorSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ==================== 课程 Dialog ==================== -->
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
          <el-input v-model.trim="courseForm.courseName" placeholder="请输入课程名称" />
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
        <el-form-item label="所属专业" prop="majorId">
          <el-select
              v-model="courseForm.majorId"
              placeholder="请选择所属专业"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="m in majorOptions"
                :key="m.majorId"
                :label="m.majorName"
                :value="m.majorId"
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
          <el-button type="primary" :loading="courseSubmitLoading" @click="handleCourseSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listCollegesApi,
  listMajorsApi,
  listMajorsForSelectApi,
  saveMajorApi,
  updateMajorStatusApi,
  deleteMajorApi,
  listCoursesApi,
  saveCourseApi,
  updateCourseStatusApi,
  deleteCourseApi,
} from '@/api/basic'

// ==================== Tab 状态 ====================
const activeTab = ref('major')

// ==================== 字典数据 ====================
const collegeOptions = ref([])
const majorOptions = ref([])

async function loadColleges() {
  collegeOptions.value = await listCollegesApi()
}

async function loadMajorOptions() {
  majorOptions.value = await listMajorsForSelectApi()
}

watch(activeTab, (tab) => {
  if (tab === 'course' && courses.value.length === 0) {
    loadCourses()
  }
})

// ==================== 专业模块 ====================
const majorLoading = ref(false)
const majorSubmitLoading = ref(false)
const majorDialogVisible = ref(false)
const majorDialogMode = ref('create')
const majors = ref([])
const majorFormRef = ref(null)

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

function normalizeMajorFilters() {
  return {
    majorCode: majorFilters.majorCode || undefined,
    majorName: majorFilters.majorName || undefined,
    collegeId: majorFilters.collegeId || undefined,
    status: majorFilters.status === null ? undefined : majorFilters.status,
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

function resetMajorFilters() {
  majorFilters.majorCode = ''
  majorFilters.majorName = ''
  majorFilters.collegeId = null
  majorFilters.status = null
  loadMajors()
}

function resetMajorForm() {
  majorForm.majorId = null
  majorForm.majorCode = ''
  majorForm.majorName = ''
  majorForm.collegeId = null
  majorForm.status = 1
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

async function handleMajorSubmit() {
  await majorFormRef.value?.validate()
  majorSubmitLoading.value = true
  try {
    await saveMajorApi({ ...majorForm })
    ElMessage.success(majorDialogMode.value === 'create' ? '专业创建成功' : '专业更新成功')
    majorDialogVisible.value = false
    await Promise.all([loadMajors(), loadMajorOptions()])
  } finally {
    majorSubmitLoading.value = false
  }
}

async function handleToggleMajorStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateMajorStatusApi({ majorId: row.majorId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '专业已启用' : '专业已停用')
  await loadMajors()
}

async function handleDeleteMajor(row) {
  await deleteMajorApi(row.majorId)
  ElMessage.success('专业删除成功')
  await Promise.all([loadMajors(), loadMajorOptions()])
}

// ==================== 课程模块 ====================
const courseLoading = ref(false)
const courseSubmitLoading = ref(false)
const courseDialogVisible = ref(false)
const courseDialogMode = ref('create')
const courses = ref([])
const courseFormRef = ref(null)

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
  majorId: null,
  status: 1,
})

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
  majorId: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function normalizeCourseFilters() {
  return {
    courseCode: courseFilters.courseCode || undefined,
    courseName: courseFilters.courseName || undefined,
    majorId: courseFilters.majorId || undefined,
    status: courseFilters.status === null ? undefined : courseFilters.status,
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

function resetCourseFilters() {
  courseFilters.courseCode = ''
  courseFilters.courseName = ''
  courseFilters.majorId = null
  courseFilters.status = null
  loadCourses()
}

function resetCourseForm() {
  courseForm.courseId = null
  courseForm.courseCode = ''
  courseForm.courseName = ''
  courseForm.credit = 2
  courseForm.majorId = null
  courseForm.status = 1
}

function openCourseDialog(mode, row = null) {
  courseDialogMode.value = mode
  resetCourseForm()
  if (mode === 'edit' && row) {
    courseForm.courseId = row.courseId
    courseForm.courseCode = row.courseCode
    courseForm.courseName = row.courseName
    courseForm.credit = row.credit
    courseForm.majorId = row.majorId
    courseForm.status = row.status
  }
  courseDialogVisible.value = true
  nextTick(() => courseFormRef.value?.clearValidate())
}

async function handleCourseSubmit() {
  await courseFormRef.value?.validate()
  courseSubmitLoading.value = true
  try {
    await saveCourseApi({ ...courseForm })
    ElMessage.success(courseDialogMode.value === 'create' ? '课程创建成功' : '课程更新成功')
    courseDialogVisible.value = false
    await loadCourses()
  } finally {
    courseSubmitLoading.value = false
  }
}

async function handleToggleCourseStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateCourseStatusApi({ courseId: row.courseId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '课程已启用' : '课程已停用')
  await loadCourses()
}

async function handleDeleteCourse(row) {
  await deleteCourseApi(row.courseId)
  ElMessage.success('课程删除成功')
  await loadCourses()
}

// ==================== 初始化 ====================
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>