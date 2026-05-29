<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QueryBar from '@/components/common/QueryBar.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import ImportDialog from '@/components/common/ImportDialog.vue'
import {
  addStudentApi,
  deleteStudentApi,
  listStudentsApi,
  updateStudentApi,
  updateStudentStatusApi,
  importStudentsApi,
} from '@/api/student'
import {
  STUDENT_IMPORT_TEMPLATE_FIELDS,
  STATUS_OPTIONS,
  formatStatus,
  downloadTemplate,
} from '@/constants/importTemplate'

const tableLoading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)

const defaultFilters = () => ({
  studentId: '',
  studentName: '',
  majorCode: '',
  teachingClassCode: '',
  status: null,
})

const filters = reactive(defaultFilters())

const queryFields = [
  { prop: 'studentId', label: '学号', type: 'input' },
  { prop: 'studentName', label: '姓名', type: 'input' },
  { prop: 'majorCode', label: '专业代码', type: 'input', width: 140 },
  { prop: 'teachingClassCode', label: '教学班编号', type: 'input', width: 140 },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: STATUS_OPTIONS,
    width: 140,
  },
]

const defaultForm = () => ({
  studentId: null,
  studentId_input: '',
  studentName: '',
  majorCode: '',
  enrollmentYear: new Date().getFullYear(),
  teachingClassCode: '',
  status: 1,
})

const form = reactive(defaultForm())

const formRules = {
  studentId_input: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  majorCode: [{ required: true, message: '请输入专业代码', trigger: 'blur' }],
  enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  teachingClassCode: [{ required: true, message: '请输入教学班编号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = ref('')

async function loadRows() {
  tableLoading.value = true
  try {
    const payload = {
      studentId: filters.studentId || undefined,
      studentName: filters.studentName || undefined,
      majorCode: filters.majorCode || undefined,
      teachingClassCode: filters.teachingClassCode || undefined,
      status: filters.status ?? undefined,
    }
    rows.value = (await listStudentsApi(payload)) || []
  } finally {
    tableLoading.value = false
  }
}

function resetFilters() {
  Object.assign(filters, defaultFilters())
  loadRows()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  dialogTitle.value = '新增学生'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑学生'
  Object.assign(form, {
    studentId: row.studentId,
    studentId_input: row.studentId,
    studentName: row.studentName,
    majorCode: row.majorCode,
    enrollmentYear: row.enrollmentYear,
    teachingClassCode: row.teachingClassCode,
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
      studentId: form.studentId || form.studentId_input,
      studentName: form.studentName,
      majorCode: form.majorCode,
      enrollmentYear: Number(form.enrollmentYear),
      teachingClassCode: form.teachingClassCode,
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

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateStudentStatusApi({
      studentId: row.studentId,
      status: newStatus,
    })
    ElMessage.success(`学生已${newStatus === 1 ? '启用' : '停用'}`)
    await loadRows()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

function handleDownloadTemplate() {
  downloadTemplate('student-import-template.csv', STUDENT_IMPORT_TEMPLATE_FIELDS)
  ElMessage.success('模板已下载')
}

async function handleImportConfirm(file) {
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await importStudentsApi(formData)
    ElMessage.success('学生名单导入成功')
    importDialogVisible.value = false
    await loadRows()
  } finally {
    importLoading.value = false
  }
}

onMounted(loadRows)
</script>

<template>
  <div class="student-list-page">
    <el-card class="student-list-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础与宏观数据管理</p>
            <h1>学生名单管理</h1>
            <p class="page-summary">管理教学班中的学生信息，包括学号、姓名、专业、入学年份等。</p>
          </div>
        </div>
      </template>

      <div class="student-list-toolbar">
        <QueryBar
          v-model="filters"
          :fields="queryFields"
          :loading="tableLoading"
          @search="loadRows"
          @reset="resetFilters"
        >
          <template #actions>
            <el-button @click="handleDownloadTemplate">下载导入模板</el-button>
            <el-button @click="importDialogVisible = true">导入学生名单</el-button>
            <el-button type="primary" @click="openCreateDialog">新增学生</el-button>
          </template>
        </QueryBar>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border>
        <el-table-column prop="studentId" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="majorCode" label="专业代码" min-width="120" />
        <el-table-column prop="enrollmentYear" label="入学年份" width="100" />
        <el-table-column prop="teachingClassCode" label="教学班编号" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm
              title="确认删除该学生吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <FormDialog
        v-model="dialogVisible"
        :title="dialogTitle"
        :loading="submitLoading"
        :confirm-text="dialogMode === 'create' ? '创建' : '保存'"
        @confirm="handleSubmit"
      >
        <el-form ref="formRef" :model="form" :rules="formRules">
          <el-form-item label="学号" prop="studentId_input">
            <el-input
              v-model.trim="form.studentId_input"
              placeholder="如 2024001"
              maxlength="20"
              :disabled="dialogMode === 'edit'"
            />
          </el-form-item>
          <el-form-item label="姓名" prop="studentName">
            <el-input
              v-model.trim="form.studentName"
              placeholder="请输入学生姓名"
              maxlength="50"
            />
          </el-form-item>
          <el-form-item label="专业代码" prop="majorCode">
            <el-input
              v-model.trim="form.majorCode"
              placeholder="如 CS"
              maxlength="20"
            />
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
          <el-form-item label="教学班编号" prop="teachingClassCode">
            <el-input
              v-model.trim="form.teachingClassCode"
              placeholder="如 CS101-01"
              maxlength="20"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="option in STATUS_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </FormDialog>

      <ImportDialog
        v-model="importDialogVisible"
        title="导入学生名单"
        :loading="importLoading"
        :template-fields="STUDENT_IMPORT_TEMPLATE_FIELDS"
        @confirm="handleImportConfirm"
      />
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
  margin: 4px 0 0;
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
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.student-list-toolbar {
  margin-bottom: 16px;
}
</style>
