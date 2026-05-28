<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QueryBar from '@/components/common/QueryBar.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import {
  addAcademicTermApi,
  deleteAcademicTermApi,
  listAcademicTermsApi,
  updateAcademicTermApi,
} from '@/api/academicTerm'
import { formatSemester, SEMESTER_OPTIONS } from '@/constants/basicData'

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)

const defaultFilters = () => ({
  termCode: '',
  academicYear: null,
  semester: null,
})

const filters = reactive(defaultFilters())

const queryFields = [
  { prop: 'termCode', label: '学期编码', type: 'input' },
  { prop: 'academicYear', label: '学年', type: 'input', width: 140 },
  {
    prop: 'semester',
    label: '学期',
    type: 'select',
    options: SEMESTER_OPTIONS,
    width: 140,
  },
]

const defaultForm = () => ({
  termId: null,
  termCode: '',
  academicYear: new Date().getFullYear(),
  semester: 1,
  startDate: '',
  endDate: '',
})

const form = reactive(defaultForm())

const formRules = {
  termCode: [{ required: true, message: '请输入学期编码', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

const dialogTitle = ref('')

async function loadRows() {
  tableLoading.value = true
  try {
    const payload = {
      termCode: filters.termCode || undefined,
      academicYear: filters.academicYear ? Number(filters.academicYear) : undefined,
      semester: filters.semester ?? undefined,
    }
    rows.value = (await listAcademicTermsApi(payload)) || []
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
  dialogTitle.value = '新增学年学期'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑学年学期'
  Object.assign(form, {
    termId: row.termId,
    termCode: row.termCode,
    academicYear: row.academicYear,
    semester: row.semester,
    startDate: row.startDate,
    endDate: row.endDate,
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
      termId: form.termId,
      termCode: form.termCode,
      academicYear: Number(form.academicYear),
      semester: Number(form.semester),
      startDate: form.startDate,
      endDate: form.endDate,
    }
    if (dialogMode.value === 'create') {
      await addAcademicTermApi(payload)
      ElMessage.success('学年学期创建成功')
    } else {
      await updateAcademicTermApi(payload)
      ElMessage.success('学年学期更新成功')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteAcademicTermApi({ termId: row.termId })
  ElMessage.success('学年学期删除成功')
  await loadRows()
}

onMounted(loadRows)
</script>

<template>
  <div class="basic-panel">
    <div class="basic-panel__toolbar">
      <QueryBar
        v-model="filters"
        :fields="queryFields"
        :loading="tableLoading"
        @search="loadRows"
        @reset="resetFilters"
      >
        <template #actions>
          <el-button type="primary" @click="openCreateDialog">新增学年学期</el-button>
        </template>
      </QueryBar>
    </div>

    <el-table v-loading="tableLoading" :data="rows" border>
      <el-table-column prop="termCode" label="学期编码" min-width="140" />
      <el-table-column prop="academicYear" label="学年" width="100" />
      <el-table-column label="学期" width="120">
        <template #default="{ row }">
          {{ formatSemester(row.semester) }}
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" min-width="120" />
      <el-table-column prop="endDate" label="结束日期" min-width="120" />
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm
            title="确认删除该学年学期吗？若已被教学班引用将无法删除。"
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
        <el-form-item label="学期编码" prop="termCode">
          <el-input
            v-model.trim="form.termCode"
            placeholder="如 2024-2025-1"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input-number
            v-model="form.academicYear"
            :min="2000"
            :max="2100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="option in SEMESTER_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </FormDialog>
  </div>
</template>

<style scoped>
.basic-panel__toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
</style>
