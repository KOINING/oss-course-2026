<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listMajorsApi } from '@/api/basic'
import {
  addCourseApi,
  deleteCourseApi,
  listCoursesApi,
  updateCourseApi,
  updateCourseStatusApi,
} from '@/api/course'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const courseLoading = ref(false)
const courseDialogVisible = ref(false)
const courseDialogMode = ref('create')
const courseSubmitLoading = ref(false)
const courseFormRef = ref(null)
const courses = ref([])
const majorOptions = ref([])

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

function normalizeCourseFilters() {
  return {
    courseCode: courseFilters.courseCode || undefined,
    courseName: courseFilters.courseName || undefined,
    majorId: courseFilters.majorId || undefined,
    status: courseFilters.status === null ? undefined : courseFilters.status,
  }
}

async function loadMajors() {
  majorOptions.value = (await listMajorsApi({ status: 1 })) || []
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
  courseForm.credit = 0
  courseForm.majorIds = []
  courseForm.status = 1
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

async function handleToggleCourseStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateCourseStatusApi({ courseId: row.courseId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '课程已启用' : '课程已停用')
  await loadCourses()
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
  await loadMajors()
  await loadCourses()
})
</script>

<template>
  <div class="course-management-page">
    <el-card class="course-management-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="courseFilters" class="filter-form">
        <el-form-item label="课程代码">
          <el-input v-model.trim="courseFilters.courseCode" placeholder="请输入课程代码" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model.trim="courseFilters.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select v-model="courseFilters.majorId" placeholder="全部专业" clearable filterable style="width: 180px">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
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
        <el-button type="success" plain @click="goToCourseImport">前往数据导入</el-button>
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
    </el-card>

    <el-dialog v-model="courseDialogVisible" :title="courseDialogMode === 'create' ? '新增课程' : '编辑课程'" width="560px" destroy-on-close>
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseFormRules" label-width="100px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model.trim="courseForm.courseCode" maxlength="32" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model.trim="courseForm.courseName" maxlength="50" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0" :step="0.5" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorIds">
          <el-select v-model="courseForm.majorIds" placeholder="请选择专业" multiple filterable style="width: 100%">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="courseForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
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
.course-management-page {
  padding: 20px;
}

.course-management-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 8px 0 6px;
  font-size: 28px;
  color: #0f172a;
}

.page-section {
  margin: 0;
  font-size: 13px;
  color: #2563eb;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-summary {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
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
  gap: 12px;
  align-items: center;
}

.major-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
