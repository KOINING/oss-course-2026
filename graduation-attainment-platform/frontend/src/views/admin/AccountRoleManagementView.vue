<template>
  <div class="account-page">
    <el-card class="account-card">
      <template #header>
        <div class="page-header">
          <div>
            <h1>账号与角色管理</h1>
            <p class="page-summary">
              管理教务管理员、专业负责人和课程主讲教师账号，并完成角色分配与启停控制。
            </p>
          </div>
          <el-button type="primary" @click="openCreateDialog">新增账号</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="用户名">
          <el-input
            v-model.trim="filters.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model.trim="filters.realName"
            placeholder="请输入姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            clearable
            style="width: 160px"
          >
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="tableLoading" :data="users" border style="margin-bottom: 16px">
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <div class="role-tags">
              <el-tag
                v-for="roleName in row.roleNames"
                :key="`${row.id}-${roleName}`"
                type="primary"
                effect="plain"
              >
                {{ roleName }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">
                编辑
              </el-button>
              <el-popconfirm
                title="确认将该账号密码重置为 123456 吗？"
                @confirm="handleResetPassword(row)"
              >
                <template #reference>
                  <el-button link type="warning">
                    重置密码
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                :title="row.status === 1 ? '确认禁用该账号吗？' : '确认启用该账号吗？'"
                @confirm="handleToggleStatus(row)"
              >
                <template #reference>
                  <el-button
                    link
                    :type="row.status === 1 ? 'danger' : 'success'"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增账号' : '编辑账号'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="96px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            :disabled="dialogMode === 'edit'"
            placeholder="请输入登录用户名"
          />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="初始密码" prop="password">
          <el-input
            v-model.trim="form.password"
            type="password"
            show-password
            placeholder="请输入初始密码"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model.trim="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色分配" prop="roleCodes">
          <el-select
            v-model="form.roleCodes"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择业务角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  addUserApi,
  listAssignableRolesApi,
  listUsersByPageApi,
  resetUserPasswordApi,
  updateUserApi,
  updateUserStatusApi,
} from '@/api/admin'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.roleCodes.includes('admin'))

const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const users = ref([])
const roleOptions = ref([])
const formRef = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const filters = reactive({
  username: '',
  realName: '',
  status: null,
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  status: 1,
  roleCodes: [],
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需为 3 到 50 位', trigger: 'blur' },
  ],
  password: [
    {
      validator: (_, value, callback) => {
        if (dialogMode.value === 'create' && !value) {
          callback(new Error('请输入初始密码'))
          return
        }
        if (value && value.length < 6) {
          callback(new Error('密码长度不能少于 6 位'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }],
  roleCodes: [
    {
      type: 'array',
      required: true,
      min: 1,
      message: '请至少选择一个业务角色',
      trigger: 'change',
    },
  ],
}

function normalizeFilters() {
  return {
    username: filters.username || undefined,
    realName: filters.realName || undefined,
    status: filters.status === null || filters.status === '' ? undefined : filters.status,
  }
}

function resetForm() {
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.status = 1
  form.roleCodes = []
}

async function loadRoleOptions() {
  roleOptions.value = await listAssignableRolesApi()
}

async function loadUsers() {
  tableLoading.value = true
  try {
    const result = await listUsersByPageApi({
      ...normalizeFilters(),
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    users.value = result.records
    pagination.total = result.total
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadUsers()
}

function resetFilters() {
  filters.username = ''
  filters.realName = ''
  filters.status = null
  handleSearch()
}

function handleSizeChange() {
  pagination.pageNum = 1
  loadUsers()
}

function handleCurrentChange() {
  loadUsers()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  resetForm()
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.status = row.status
  form.roleCodes = Array.isArray(row.roleCodes) ? [...row.roleCodes] : []
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  await formRef.value?.validate()

  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await addUserApi({
        username: form.username,
        password: form.password,
        realName: form.realName,
        status: form.status,
        roleCodes: form.roleCodes,
      })
      ElMessage.success('账号创建成功')
    } else {
      await updateUserApi({
        id: form.id,
        realName: form.realName,
        status: form.status,
        roleCodes: form.roleCodes,
      })
      ElMessage.success('账号更新成功')
    }

    dialogVisible.value = false
    await loadUsers()
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateUserStatusApi({
    id: row.id,
    status: nextStatus,
  })
  ElMessage.success(nextStatus === 1 ? '账号已启用' : '账号已禁用')
  await loadUsers()
}

async function handleResetPassword(row) {
  await resetUserPasswordApi({ id: row.id })
  ElMessage.success(`账号“${row.username}”密码已重置为 123456`)
}

onMounted(async () => {
  if (!isAdmin.value) {
    ElMessage.error('当前账号无权访问账号与角色管理页面')
    router.replace(DEFAULT_HOME_PATH)
    return
  }

  await Promise.all([loadRoleOptions(), loadUsers()])
})
</script>

<style scoped>
.account-page {
  padding: 20px;
}

.account-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
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

.filter-form {
  margin-bottom: 20px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>
