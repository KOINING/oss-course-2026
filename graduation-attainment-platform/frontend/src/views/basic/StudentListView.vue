<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listMajorsForSelectApi } from '@/api/basic'
import {
  addStudentApi,
  deleteStudentApi,
  listStudentsApi,
  updateStudentApi,
  updateStudentStatusApi,
} from '@/api/student'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)
const majorOptions = ref([])

const statusOptions = [
  { value: 1, label: '在读' },
  { value: 2, label: '毕业' },
  { value: 3, label: '休学' },
  { value: 0, label: '退学' },
]

const filters = reactive({
  studentNo: '',
  studentName: '',
  majorId: null,
  enrollmentYear: null,
  status: null,
})

const form = reactive({
  studentId: null,
  studentNo: '',
  studentName: '',
  majorId: null,
  enrollmentYear: new Date().getFullYear(),
  status: 1,
})

const formRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增学生' : '编辑学生'))

function resetFilters() {
  filters.studentNo = ''
  filters.studentName = ''
  filters.majorId = null
  filters.enrollmentYear = null
  filters.status = null
}

function resetForm() {
  form.studentId = null
  form.studentNo = ''
  form.studentName = ''
  form.majorId = null
  form.enrollmentYear = new Date().getFullYear()
  form.status = 1
}

function normalizeFilters() {
  return {
    studentNo: filters.studentNo || undefined,
    studentName: filters.studentName || undefined,
    majorId: filters.majorId || undefined,
    enrollmentYear: filters.enrollmentYear || undefined,
    status: filters.status === null ? undefined : filters.status,
  }
}

async function loadOptions() {
  majorOptions.value = await listMajorsForSelectApi()
}

async function loadRows() {
  tableLoading.value = true
  try {
    rows.value = (await listStudentsApi(normalizeFilters())) || []
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
    studentId: row.studentId,
    studentNo: row.studentNo,
    studentName: row.studentName,
    majorId: row.majorId,
    enrollmentYear: row.enrollmentYear,
    status: row.status,
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
      studentId: form.studentId,
      studentNo: form.studentNo.trim(),
      studentName: form.studentName.trim(),
      majorId: form.majorId,
      enrollmentYear: Number(form.enrollmentYear),
      status: Number(form.status),
    }
    if (dialogMode.value === 'create') {
      await addStudentApi(payload)
      ElMessage.success('学生创建成功')
    } else {
      await updateStudentApi(payload)
      ElMessage.success('学生更新成功')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteStudentApi({ studentId: row.studentId })
  ElMessage.success('学生删除成功')
  await loadRows()
}

async function handleUpdateStatus(row, status) {
  await updateStudentStatusApi({ studentId: row.studentId, status })
  ElMessage.success('学生状态已更新')
  await loadRows()
}

function goToImportPage() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'student-classes' } })
}

onMounted(async () => {
  await loadOptions()
  await loadRows()
})
</script>

<template>
  <div class="student-list-page">
    <el-card class="student-list-card">
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
        <el-form-item label="学号">
          <el-input v-model.trim="filters.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="filters.studentName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="filters.majorId" placeholder="全部专业" clearable filterable style="width: 180px">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-input-number v-model="filters.enrollmentYear" :min="2000" :max="2100" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRows">查询</el-button>
          <el-button @click="handleResetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <el-button type="primary" @click="openCreateDialog">新增学生</el-button>
        <el-button type="success" plain @click="goToImportPage">前往导入教学班学生名单</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border stripe>
        <el-table-column prop="studentNo" label="学号" min-width="140" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="majorCode" label="专业代码" min-width="120" />
        <el-table-column prop="majorName" label="专业名称" min-width="180" />
        <el-table-column prop="enrollmentYear" label="入学年份" min-width="100" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag effect="plain">
              {{ row.statusText || statusOptions.find((item) => item.value === row.status)?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-dropdown @command="(command) => handleUpdateStatus(row, command)">
                <el-button link type="warning">更新状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="status in statusOptions"
                      :key="status.value"
                      :command="status.value"
                    >
                      {{ status.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确认删除该学生吗？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model.trim="form.studentNo" maxlength="20" />
          </el-form-item>
          <el-form-item label="姓名" prop="studentName">
            <el-input v-model.trim="form.studentName" maxlength="50" />
          </el-form-item>
          <el-form-item label="专业" prop="majorId">
            <el-select v-model="form.majorId" placeholder="请选择专业" filterable style="width: 100%">
              <el-option
                v-for="major in majorOptions"
                :key="major.majorId"
                :label="major.majorName"
                :value="major.majorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="入学年份" prop="enrollmentYear">
            <el-input-number
              v-model="form.enrollmentYear"
              :min="2000"
              :max="2100"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
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
.student-list-page {
  padding: 20px;
}

.student-list-card {
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
  align-items: center;
  gap: 12px;
}
</style>
