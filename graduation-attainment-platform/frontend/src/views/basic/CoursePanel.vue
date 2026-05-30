<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QueryBar from '@/components/common/QueryBar.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import ImportDialog from '@/components/common/ImportDialog.vue'
import {
  addCourseApi,
  deleteCourseApi,
  listCoursesApi,
  updateCourseApi,
  importCoursesApi,
} from '@/api/course'
import {
  COURSE_IMPORT_TEMPLATE_FIELDS,
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
  courseCode: '',
  courseName: '',
  majorCode: '',
  status: null,
})

const filters = reactive(defaultFilters())

const queryFields = [
  { prop: 'courseCode', label: '课程代码', type: 'input' },
  { prop: 'courseName', label: '课程名称', type: 'input' },
  { prop: 'majorCode', label: '所属专业代码', type: 'input', width: 140 },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: STATUS_OPTIONS,
    width: 140,
  },
]

const defaultForm = () => ({
  courseId: null,
  courseCode: '',
  courseName: '',
  majorCode: '',
  credits: 0,
  status: 1,
})

const form = reactive(defaultForm())

const formRules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  majorCode: [{ required: true, message: '请输入所属专业代码', trigger: 'blur' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = ref('')

async function loadRows() {
  tableLoading.value = true
  try {
    const payload = {
      courseCode: filters.courseCode || undefined,
      courseName: filters.courseName || undefined,
      majorCode: filters.majorCode || undefined,
      status: filters.status ?? undefined,
    }
    rows.value = (await listCoursesApi(payload)) || []
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
  dialogTitle.value = '新增课程'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑课程'
  Object.assign(form, {
    courseId: row.courseId,
    courseCode: row.courseCode,
    courseName: row.courseName,
    majorCode: row.majorCode,
    credits: row.credits,
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
      courseId: form.courseId,
      courseCode: form.courseCode,
      courseName: form.courseName,
      majorCode: form.majorCode,
      credits: Number(form.credits),
      status: Number(form.status),
    }
    if (dialogMode.value === 'create') {
      await addCourseApi(payload)
      ElMessage.success('课程创建成功')
    } else {
      await updateCourseApi(payload)
      ElMessage.success('课程更新成功')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteCourseApi({ courseId: row.courseId })
  ElMessage.success('课程删除成功')
  await loadRows()
}

function handleDownloadTemplate() {
  downloadTemplate('course-import-template.csv', COURSE_IMPORT_TEMPLATE_FIELDS)
  ElMessage.success('模板已下载')
}

async function handleImportConfirm(file) {
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await importCoursesApi(formData)
    ElMessage.success('课程导入成功')
    importDialogVisible.value = false
    await loadRows()
  } finally {
    importLoading.value = false
  }
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
          <el-button @click="handleDownloadTemplate">下载导入模板</el-button>
          <el-button @click="importDialogVisible = true">导入课程清单</el-button>
          <el-button type="primary" @click="openCreateDialog">新增课程</el-button>
        </template>
      </QueryBar>
    </div>

    <el-table v-loading="tableLoading" :data="rows" border>
      <el-table-column prop="courseCode" label="课程代码" min-width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="140" />
      <el-table-column prop="majorCode" label="所属专业代码" min-width="140" />
      <el-table-column prop="credits" label="学分" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ formatStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm
            title="确认删除该课程吗？"
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
        <el-form-item label="课程代码" prop="courseCode">
          <el-input
            v-model.trim="form.courseCode"
            placeholder="如 CS101"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input
            v-model.trim="form.courseName"
            placeholder="请输入课程名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="所属专业代码" prop="majorCode">
          <el-input
            v-model.trim="form.majorCode"
            placeholder="如 CS"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number
            v-model="form.credits"
            :min="0"
            :max="10"
            controls-position="right"
            style="width: 100%"
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
      title="导入课程清单"
      :loading="importLoading"
      :template-fields="COURSE_IMPORT_TEMPLATE_FIELDS"
      @confirm="handleImportConfirm"
    />
  </div>
</template>

<style scoped>
.basic-panel__toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
