<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listMajorsForSelectApi } from '@/api/basic'
import {
  addStudentApi,
  deleteStudentApi,
  listStudentEnrollmentYearsApi,
  listStudentsByPageApi,
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
const enrollmentYearOptions = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pageSizes = [5, 10, 20, 50]

const statusOptions = [
  { value: 1, label: '在读' },
  { value: 2, label: '毕业' },
  { value: 3, label: '休学' },
  { value: 0, label: '退学' },
]
const currentYear = new Date().getFullYear()

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
  enrollmentYear: currentYear,
  status: 1,
})

const formRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  status: [{ required: true, message: '请选择学籍状态', trigger: 'change' }],
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
  form.enrollmentYear = currentYear
  form.status = 1
}

function normalizeFilters() {
  return {
    studentNo: filters.studentNo || undefined,
    studentName: filters.studentName || undefined,
    majorId: filters.majorId || undefined,
    enrollmentYear: filters.enrollmentYear || undefined,
    status: filters.status === null ? undefined : filters.status,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
  }
}

async function loadOptions() {
  const [majors, enrollmentYears] = await Promise.all([
    listMajorsForSelectApi(),
    listStudentEnrollmentYearsApi(),
  ])
  majorOptions.value = majors || []
  enrollmentYearOptions.value = enrollmentYears || []
}

async function loadRows() {
  tableLoading.value = true
  try {
    const result = await listStudentsByPageApi(normalizeFilters())
    if (result) {
      rows.value = result.records || []
      total.value = result.total || 0
      pageNum.value = result.pageNum || 1
      pageSize.value = result.pageSize || 10
      if (rows.value.length === 0 && pageNum.value > 1) {
        pageNum.value -= 1
        await loadRows()
        return
      }
    }
  } finally {
    tableLoading.value = false
  }
}

function handlePageChange(newPage) {
  pageNum.value = newPage
  loadRows()
}

function handlePageSizeChange(newSize) {
  pageSize.value = newSize
  pageNum.value = 1
  loadRows()
}

function handleResetFilters() {
  resetFilters()
  pageNum.value = 1
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
    await loadOptions()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteStudentApi({ studentId: row.studentId })
  ElMessage.success('学生删除成功')
  await loadRows()
  await loadOptions()
}

async function handleUpdateStatus(row, status) {
  await updateStudentStatusApi({ studentId: row.studentId, status })
  ElMessage.success('学生学籍状态已更新')
  await loadRows()
}

function goToStudentImport() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'students' } })
}

function formatStatus(status) {
  return statusOptions.find((item) => item.value === status)?.label || '-'
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
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-alert type="info" :closable="false" class="page-tip" show-icon>
        <template #title>
          本页仅维护学生主数据，包括学号、姓名、专业、入学年份和学籍状态。教学班学生关联请在教学班管理或数据导入页维护；教学班代表该专业该届某门必修课程的评价单元。
        </template>
      </el-alert>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="学号">
          <el-input v-model.trim="filters.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="filters.studentName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="filters.majorId" placeholder="全部专业" clearable filterable style="width: 200px">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-select v-model="filters.enrollmentYear" placeholder="全部年份" clearable filterable style="width: 160px">
            <el-option
              v-for="year in enrollmentYearOptions"
              :key="year"
              :label="`${year}年`"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学籍状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="pageNum = 1; loadRows()">查询</el-button>
          <el-button @click="handleResetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <el-button type="primary" @click="openCreateDialog">新增学生</el-button>
        <el-button type="success" plain @click="goToStudentImport">前往批量导入学生基础信息</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border stripe>
        <el-table-column prop="studentNo" label="学号" min-width="140" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="majorCode" label="专业代码" min-width="120" />
        <el-table-column prop="majorName" label="专业名称" min-width="180" />
        <el-table-column prop="enrollmentYear" label="入学年份" min-width="110" />
        <el-table-column label="学籍状态" min-width="110">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.statusText || formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
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

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px">
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
          <el-form-item label="学籍状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择学籍状态" style="width: 100%">
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

.page-tip {
  margin-bottom: 16px;
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
