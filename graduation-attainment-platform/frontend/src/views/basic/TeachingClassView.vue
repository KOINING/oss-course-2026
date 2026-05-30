<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QueryBar from '@/components/common/QueryBar.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import {
  addTeachingClassApi,
  deleteTeachingClassApi,
  listTeachingClassesApi,
  updateTeachingClassApi,
  updateTeachingClassStatusApi,
} from '@/api/teachingClass'
import { STATUS_OPTIONS, formatStatus } from '@/constants/importTemplate'

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)

const defaultFilters = () => ({
  teachingClassCode: '',
  courseCode: '',
  status: null,
})

const filters = reactive(defaultFilters())

const queryFields = [
  { prop: 'teachingClassCode', label: '教学班编号', type: 'input' },
  { prop: 'courseCode', label: '课程代码', type: 'input' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: STATUS_OPTIONS,
    width: 140,
  },
]

const defaultForm = () => ({
  teachingClassId: null,
  teachingClassCode: '',
  courseCode: '',
  instructor: '',
  capacity: 0,
  status: 1,
})

const form = reactive(defaultForm())

const formRules = {
  teachingClassCode: [{ required: true, message: '请输入教学班编号', trigger: 'blur' }],
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  instructor: [{ required: true, message: '请输入授课教师', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入班级容量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = ref('')

async function loadRows() {
  tableLoading.value = true
  try {
    const payload = {
      teachingClassCode: filters.teachingClassCode || undefined,
      courseCode: filters.courseCode || undefined,
      status: filters.status ?? undefined,
    }
    rows.value = (await listTeachingClassesApi(payload)) || []
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
  dialogTitle.value = '新增教学班'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑教学班'
  Object.assign(form, {
    teachingClassId: row.teachingClassId,
    teachingClassCode: row.teachingClassCode,
    courseCode: row.courseCode,
    instructor: row.instructor,
    capacity: row.capacity,
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
      teachingClassId: form.teachingClassId,
      teachingClassCode: form.teachingClassCode,
      courseCode: form.courseCode,
      instructor: form.instructor,
      capacity: Number(form.capacity),
      status: Number(form.status),
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
  await deleteTeachingClassApi({ teachingClassId: row.teachingClassId })
  ElMessage.success('教学班删除成功')
  await loadRows()
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateTeachingClassStatusApi({
      teachingClassId: row.teachingClassId,
      status: newStatus,
    })
    ElMessage.success(`教学班已${newStatus === 1 ? '启用' : '停用'}`)
    await loadRows()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(loadRows)
</script>

<template>
  <div class="teaching-class-page">
    <el-card class="teaching-class-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础与宏观数据管理</p>
            <h1>教学班管理</h1>
            <p class="page-summary">管理课程的教学班信息，包括班级编号、授课教师、班级容量等。</p>
          </div>
        </div>
      </template>

      <div class="teaching-class-toolbar">
        <QueryBar
          v-model="filters"
          :fields="queryFields"
          :loading="tableLoading"
          @search="loadRows"
          @reset="resetFilters"
        >
          <template #actions>
            <el-button type="primary" @click="openCreateDialog">新增教学班</el-button>
          </template>
        </QueryBar>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border>
        <el-table-column prop="teachingClassCode" label="教学班编号" min-width="140" />
        <el-table-column prop="courseCode" label="课程代码" min-width="120" />
        <el-table-column prop="instructor" label="授课教师" min-width="120" />
        <el-table-column prop="capacity" label="班级容量" width="100" />
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
              title="确认删除该教学班吗？"
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
          <el-form-item label="教学班编号" prop="teachingClassCode">
            <el-input
              v-model.trim="form.teachingClassCode"
              placeholder="如 CS101-01"
              maxlength="20"
            />
          </el-form-item>
          <el-form-item label="课程代码" prop="courseCode">
            <el-input
              v-model.trim="form.courseCode"
              placeholder="如 CS101"
              maxlength="20"
            />
          </el-form-item>
          <el-form-item label="授课教师" prop="instructor">
            <el-input
              v-model.trim="form.instructor"
              placeholder="请输入授课教师名称"
              maxlength="50"
            />
          </el-form-item>
          <el-form-item label="班级容量" prop="capacity">
            <el-input-number
              v-model="form.capacity"
              :min="0"
              :max="500"
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

.teaching-class-toolbar {
  margin-bottom: 16px;
}
</style>
