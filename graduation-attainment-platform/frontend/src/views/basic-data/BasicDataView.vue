<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AcademicTermPanel from './AcademicTermPanel.vue'
import CollegePanel from './CollegePanel.vue'
import { useUserStore } from '@/stores/user'
import { ROUTE_NAMES } from '@/utils/constants'
import {
  deleteMajorApi,
  listCollegesApi,
  listMajorsApi,
  saveMajorApi,
  updateMajorStatusApi,
} from '@/api/basic'
import {
  addCourseApi,
  deleteCourseApi,
  listCoursesApi,
  updateCourseApi,
  updateCourseStatusApi,
} from '@/api/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAcademicAffairs = computed(() => userStore.roleCodes.includes('academic_affairs'))
const activeTab = ref('academic-term')

const collegeOptions = ref([])
const majorOptions = ref([])

const majorLoading = ref(false)
const majorDialogVisible = ref(false)
const majorDialogMode = ref('create')
const majorSubmitLoading = ref(false)
const majorFormRef = ref(null)
const majors = ref([])

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
  majorCode: [{ required: true, message: '请输入专业代码', trigger: 'blur' }],
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const courseLoading = ref(false)
const courseDialogVisible = ref(false)
const courseDialogMode = ref('create')
const courseSubmitLoading = ref(false)
const courseFormRef = ref(null)
const courses = ref([])

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
  credit: 0,
  majorIds: [],
  status: 1,
})

const courseFormRules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'change' }],
  majorIds: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
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

function normalizeCourseFilters() {
  return {
    courseCode: courseFilters.courseCode || undefined,
    courseName: courseFilters.courseName || undefined,
    majorId: courseFilters.majorId || undefined,
    status: courseFilters.status === null ? undefined : courseFilters.status,
  }
}

async function loadSharedOptions() {
  const [colleges, majorsForSelect] = await Promise.all([
    listCollegesApi(),
    listMajorsApi({ status: 1 }),
  ])
  collegeOptions.value = colleges || []
  majorOptions.value = majorsForSelect || []
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
  courseForm.credit = 0
  courseForm.majorIds = []
  courseForm.status = 1
}

function openMajorDialog(mode, row = null) {
  majorDialogMode.value = mode
  resetMajorForm()
  if (mode === 'edit' && row) {
    Object.assign(majorForm, {
      majorId: row.majorId,
      majorCode: row.majorCode,
      majorName: row.majorName,
      collegeId: row.collegeId,
      status: row.status,
    })
  }
  majorDialogVisible.value = true
  nextTick(() => majorFormRef.value?.clearValidate())
}

function openCourseDialog(mode, row = null) {
  courseDialogMode.value = mode
  resetCourseForm()
  if (mode === 'edit' && row) {
    Object.assign(courseForm, {
      courseId: row.courseId,
      courseCode: row.courseCode,
      courseName: row.courseName,
      credit: row.credit,
      majorIds: row.majorIds || [],
      status: row.status,
    })
  }
  courseDialogVisible.value = true
  nextTick(() => courseFormRef.value?.clearValidate())
}

async function handleMajorSubmit() {
  await majorFormRef.value?.validate()
  majorSubmitLoading.value = true
  try {
    await saveMajorApi({ ...majorForm })
    ElMessage.success(majorDialogMode.value === 'create' ? '专业创建成功' : '专业更新成功')
    majorDialogVisible.value = false
    await Promise.all([loadMajors(), loadSharedOptions()])
  } finally {
    majorSubmitLoading.value = false
  }
}

async function handleCourseSubmit() {
  await courseFormRef.value?.validate()
  courseSubmitLoading.value = true
  try {
    const payload = {
      courseId: courseForm.courseId,
      courseCode: courseForm.courseCode.trim(),
      courseName: courseForm.courseName.trim(),
      credit: Number(courseForm.credit),
      majorIds: courseForm.majorIds,
      status: Number(courseForm.status),
    }
    if (courseDialogMode.value === 'create') {
      await addCourseApi(payload)
      ElMessage.success('课程创建成功')
    } else {
      await updateCourseApi(payload)
      ElMessage.success('课程更新成功')
    }
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
  await Promise.all([loadMajors(), loadSharedOptions()])
}

async function handleDeleteCourse(row) {
  await deleteCourseApi({ courseId: row.courseId })
  ElMessage.success('课程删除成功')
  await loadCourses()
}

function goToCourseImport() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'course' } })
}

onMounted(async () => {
  await loadSharedOptions()
  await Promise.all([loadMajors(), loadCourses()])
})
</script>

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
                <el-input v-model.trim="majorFilters.majorCode" placeholder="请输入专业代码" clearable />
              </el-form-item>
              <el-form-item label="专业名称">
                <el-input v-model.trim="majorFilters.majorName" placeholder="请输入专业名称" clearable />
              </el-form-item>
              <el-form-item label="所属学院">
                <el-select v-model="majorFilters.collegeId" placeholder="全部学院" clearable style="width: 160px">
                  <el-option v-for="college in collegeOptions" :key="college.collegeId" :label="college.collegeName" :value="college.collegeId" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="majorFilters.status" placeholder="全部状态" clearable style="width: 120px">
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
              <el-table-column prop="collegeName" label="所属学院" min-width="180" />
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
                    <el-button link type="primary" @click="openMajorDialog('edit', row)">编辑</el-button>
                    <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleMajorStatus(row)">
                      {{ row.status === 1 ? '停用' : '启用' }}
                    </el-button>
                    <el-popconfirm title="确认删除该专业吗？" @confirm="handleDeleteMajor(row)">
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
                <el-input v-model.trim="courseFilters.courseCode" placeholder="请输入课程代码" clearable />
              </el-form-item>
              <el-form-item label="课程名称">
                <el-input v-model.trim="courseFilters.courseName" placeholder="请输入课程名称" clearable />
              </el-form-item>
              <el-form-item label="所属专业">
                <el-select v-model="courseFilters.majorId" placeholder="全部专业" clearable filterable style="width: 180px">
                  <el-option v-for="major in majorOptions" :key="major.majorId" :label="major.majorName" :value="major.majorId" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="courseFilters.status" placeholder="全部状态" clearable style="width: 120px">
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
              <el-button v-if="isAcademicAffairs" type="success" plain @click="goToCourseImport">
                前往数据导入
              </el-button>
            </div>

            <el-table v-loading="courseLoading" :data="courses" border stripe>
              <el-table-column prop="courseCode" label="课程代码" width="140" />
              <el-table-column prop="courseName" label="课程名称" min-width="180" />
              <el-table-column prop="credit" label="学分" width="100" />
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
                    <el-button link type="primary" @click="openCourseDialog('edit', row)">编辑</el-button>
                    <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleCourseStatus(row)">
                      {{ row.status === 1 ? '停用' : '启用' }}
                    </el-button>
                    <el-popconfirm title="确认删除该课程吗？" @confirm="handleDeleteCourse(row)">
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

    <el-dialog v-model="majorDialogVisible" :title="majorDialogMode === 'create' ? '新增专业' : '编辑专业'" width="500px" destroy-on-close>
      <el-form ref="majorFormRef" :model="majorForm" :rules="majorFormRules" label-width="90px">
        <el-form-item label="专业代码" prop="majorCode">
          <el-input v-model.trim="majorForm.majorCode" maxlength="20" :disabled="majorDialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model.trim="majorForm.majorName" maxlength="100" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="majorForm.collegeId" placeholder="请选择所属学院" style="width: 100%">
            <el-option v-for="college in collegeOptions" :key="college.collegeId" :label="college.collegeName" :value="college.collegeId" />
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
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="majorSubmitLoading" @click="handleMajorSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="courseDialogVisible" :title="courseDialogMode === 'create' ? '新增课程' : '编辑课程'" width="560px" destroy-on-close>
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseFormRules" label-width="90px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model.trim="courseForm.courseCode" maxlength="20" :disabled="courseDialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model.trim="courseForm.courseName" maxlength="100" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0" :max="20" :precision="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorIds">
          <el-select v-model="courseForm.majorIds" multiple filterable placeholder="请选择所属专业" style="width: 100%">
            <el-option v-for="major in majorOptions" :key="major.majorId" :label="major.majorName" :value="major.majorId" />
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
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="courseSubmitLoading" @click="handleCourseSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.basic-data-page {
  padding: 20px;
}

.basic-data-card {
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

.tab-content {
  padding: 4px 0;
}

.filter-form {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
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
</style>
