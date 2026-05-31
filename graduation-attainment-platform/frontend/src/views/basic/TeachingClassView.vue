<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  addTeachingClassApi,
  deleteTeachingClassApi,
  listTeachingClassesApi,
  updateTeachingClassApi,
  updateTeachingClassStatusApi,
} from '@/api/teachingClass'
import { listCoursesApi } from '@/api/course'
import { listAcademicTermsApi } from '@/api/academicTerm'
import { listTeachersForSelectApi } from '@/api/teacher'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)

const courseOptions = ref([])
const termOptions = ref([])
const teacherOptions = ref([])

const calcStatusOptions = [
  { value: 'unsubmitted', label: '未提交' },
  { value: 'score_imported', label: '已导入成绩' },
  { value: 'calculating', label: '计算中' },
  { value: 'locked', label: '已锁定' },
]

const filters = reactive({
  classCode: '',
  className: '',
  courseId: null,
  termId: null,
  teacherId: null,
  calcStatus: '',
})

const form = reactive({
  classId: null,
  classCode: '',
  className: '',
  courseId: null,
  termId: null,
  teacherId: null,
})

const formRules = {
  classCode: [{ required: true, message: '请输入教学班编号', trigger: 'blur' }],
  className: [{ required: true, message: '请输入教学班名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  termId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增教学班' : '编辑教学班'))

function resetFilters() {
  filters.classCode = ''
  filters.className = ''
  filters.courseId = null
  filters.termId = null
  filters.teacherId = null
  filters.calcStatus = ''
}

function resetForm() {
  form.classId = null
  form.classCode = ''
  form.className = ''
  form.courseId = null
  form.termId = null
  form.teacherId = null
}

function normalizeFilters() {
  return {
    classCode: filters.classCode || undefined,
    className: filters.className || undefined,
    courseId: filters.courseId || undefined,
    termId: filters.termId || undefined,
    teacherId: filters.teacherId || undefined,
    calcStatus: filters.calcStatus || undefined,
  }
}

async function loadOptions() {
  const [courses, terms, teachers] = await Promise.all([
    listCoursesApi({ status: 1 }),
    listAcademicTermsApi(),
    listTeachersForSelectApi(),
  ])
  courseOptions.value = courses || []
  termOptions.value = terms || []
  teacherOptions.value = teachers || []
}

async function loadRows() {
  tableLoading.value = true
  try {
    rows.value = (await listTeachingClassesApi(normalizeFilters())) || []
  } finally {
    tableLoading.value = false
  }
}

function handleResetFilters() {
  resetFilters()
  loadRows()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  Object.assign(form, {
    classId: row.classId,
    classCode: row.classCode,
    className: row.className,
    courseId: row.courseId,
    termId: row.termId,
    teacherId: row.teacherId,
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      classId: form.classId,
      classCode: form.classCode.trim(),
      className: form.className.trim(),
      courseId: form.courseId,
      termId: form.termId,
      teacherId: form.teacherId,
    }
    if (dialogMode.value === 'create') {
      await addTeachingClassApi(payload)
      ElMessage.success('教学班创建成功')
    } else {
      await updateTeachingClassApi(payload)
      ElMessage.success('教学班更新成功')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteTeachingClassApi({ classId: row.classId })
  ElMessage.success('教学班删除成功')
  await loadRows()
}

async function handleUpdateStatus(row, calcStatus) {
  await updateTeachingClassStatusApi({ classId: row.classId, calcStatus })
  ElMessage.success('教学班计算状态已更新')
  await loadRows()
}

function formatCalcStatus(value) {
  return calcStatusOptions.find((option) => option.value === value)?.label || value || '未设置'
}

function goToStudentClassImport() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'student-classes' } })
}

onMounted(async () => {
  await loadOptions()
  await loadRows()
})
</script>

<template>
  <div class="teaching-class-page">
    <el-card class="teaching-class-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="教学班编号">
          <el-input v-model.trim="filters.classCode" placeholder="请输入教学班编号" clearable />
        </el-form-item>
        <el-form-item label="教学班名称">
          <el-input v-model.trim="filters.className" placeholder="请输入教学班名称" clearable />
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="filters.courseId" placeholder="全部课程" clearable filterable style="width: 180px">
            <el-option v-for="course in courseOptions" :key="course.courseId" :label="`${course.courseCode} - ${course.courseName}`" :value="course.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="filters.termId" placeholder="全部学期" clearable style="width: 180px">
            <el-option v-for="term in termOptions" :key="term.termId" :label="term.termCode" :value="term.termId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="filters.teacherId" placeholder="全部教师" clearable filterable style="width: 160px">
            <el-option v-for="teacher in teacherOptions" :key="teacher.id" :label="teacher.teacherName" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计算状态">
          <el-select v-model="filters.calcStatus" placeholder="全部状态" clearable style="width: 150px">
            <el-option v-for="status in calcStatusOptions" :key="status.value" :label="status.label" :value="status.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRows">查询</el-button>
          <el-button @click="handleResetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <el-button type="primary" @click="openCreateDialog">新增教学班</el-button>
        <el-button type="success" plain @click="goToStudentClassImport">前往导入教学班学生名单</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border stripe>
        <el-table-column prop="classCode" label="教学班编号" min-width="140" />
        <el-table-column prop="className" label="教学班名称" min-width="180" />
        <el-table-column prop="courseCode" label="课程代码" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="termCode" label="学期" min-width="140" />
        <el-table-column prop="teacherName" label="主讲教师" min-width="120" />
        <el-table-column label="计算状态" min-width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ formatCalcStatus(row.calcStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-dropdown @command="(command) => handleUpdateStatus(row, command)">
                <el-button link type="warning">
                  更新状态
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="status in calcStatusOptions"
                      :key="status.value"
                      :command="status.value"
                    >
                      {{ status.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确认删除该教学班吗？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
          <el-form-item label="教学班编号" prop="classCode">
            <el-input v-model.trim="form.classCode" placeholder="如 TC2024CS01" maxlength="32" />
          </el-form-item>
          <el-form-item label="教学班名称" prop="className">
            <el-input v-model.trim="form.className" placeholder="请输入教学班名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="课程" prop="courseId">
            <el-select v-model="form.courseId" placeholder="请选择课程" filterable style="width: 100%">
              <el-option v-for="course in courseOptions" :key="course.courseId" :label="`${course.courseCode} - ${course.courseName}`" :value="course.courseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="学期" prop="termId">
            <el-select v-model="form.termId" placeholder="请选择学期" style="width: 100%">
              <el-option v-for="term in termOptions" :key="term.termId" :label="term.termCode" :value="term.termId" />
            </el-select>
          </el-form-item>
          <el-form-item label="主讲教师" prop="teacherId">
            <el-select v-model="form.teacherId" placeholder="请选择教师" filterable style="width: 100%">
              <el-option v-for="teacher in teacherOptions" :key="teacher.id" :label="teacher.teacherName" :value="teacher.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<style scoped>
.teaching-class-page {
  padding: 20px;
}

.teaching-class-card {
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
  color: #64748b;
  line-height: 1.75;
}

.filter-form {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
