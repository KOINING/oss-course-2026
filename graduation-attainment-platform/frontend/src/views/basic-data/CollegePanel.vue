<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QueryBar from '@/components/common/QueryBar.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import {
  addCollegeApi,
  deleteCollegeApi,
  listCollegesApi,
  updateCollegeApi,
} from '@/api/college'

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const formRef = ref(null)

const defaultFilters = () => ({
  collegeCode: '',
  collegeName: '',
})

const filters = reactive(defaultFilters())

const queryFields = [
  { prop: 'collegeCode', label: '学院编码', type: 'input' },
  { prop: 'collegeName', label: '学院名称', type: 'input' },
]

const defaultForm = () => ({
  collegeId: null,
  collegeCode: '',
  collegeName: '',
})

const form = reactive(defaultForm())

const formRules = {
  collegeCode: [{ required: true, message: '请输入学院编码', trigger: 'blur' }],
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
}

const dialogTitle = ref('')

async function loadRows() {
  tableLoading.value = true
  try {
    const payload = {
      collegeCode: filters.collegeCode || undefined,
      collegeName: filters.collegeName || undefined,
    }
    rows.value = (await listCollegesApi(payload)) || []
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
  dialogTitle.value = '新增学院'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑学院'
  Object.assign(form, {
    collegeId: row.collegeId,
    collegeCode: row.collegeCode,
    collegeName: row.collegeName,
  })
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      collegeId: form.collegeId,
      collegeCode: form.collegeCode,
      collegeName: form.collegeName,
    }
    if (dialogMode.value === 'create') {
      await addCollegeApi(payload)
      ElMessage.success('学院创建成功')
    } else {
      await updateCollegeApi(payload)
      ElMessage.success('学院更新成功')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteCollegeApi({ collegeId: row.collegeId })
  ElMessage.success('学院删除成功')
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
          <el-button type="primary" @click="openCreateDialog">新增学院</el-button>
        </template>
      </QueryBar>
    </div>

    <el-table v-loading="tableLoading" :data="rows" border>
      <el-table-column prop="collegeCode" label="学院编码" min-width="140" />
      <el-table-column prop="collegeName" label="学院名称" min-width="200" />
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm
            title="确认删除该学院吗？若已被专业引用将无法删除。"
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
        <el-form-item label="学院编码" prop="collegeCode">
          <el-input
            v-model.trim="form.collegeCode"
            placeholder="请输入学院编码"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="学院名称" prop="collegeName">
          <el-input
            v-model.trim="form.collegeName"
            placeholder="请输入学院名称"
            maxlength="100"
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
