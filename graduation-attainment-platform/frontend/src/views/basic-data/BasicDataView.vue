<template>
  <div class="basic-data-page">
    <el-card class="basic-data-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="page-summary">{{ route.meta.summary }}</p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="data-tabs">
        <el-tab-pane label="学年学期" name="academic-term">
          <AcademicTermPanel />
        </el-tab-pane>

        <el-tab-pane label="学院" name="college">
          <CollegePanel />
        </el-tab-pane>

        <el-tab-pane label="专业管理" name="major">
          <div class="tab-content">
            <el-form :inline="true" :model="majorFilters" class="filter-form">
              <el-form-item label="专业代码">
                <el-input
                  v-model.trim="majorFilters.majorCode"
                  placeholder="请输入专业代码"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="专业名称">
                <el-input
                  v-model.trim="majorFilters.majorName"
                  placeholder="请输入专业名称"
                  clearable
                  style="width: 120px"
                />
              </el-form-item>
              <el-form-item label="所属学院">
                <el-select
                  v-model="majorFilters.collegeId"
                  placeholder="全部学院"
                  clearable
                  style="width: 140px"
                >
                  <el-option
                    v-for="college in collegeOptions"
                    :key="college.collegeId"
                    :label="college.collegeName"
                    :value="college.collegeId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="majorFilters.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 100px"
                >
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="停用" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadMajors">查询</el-button>
                <el-button @click="resetMajorFilters">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-toolbar">
              <el-button type="primary" @click="openMajorDialog('create')">
                <el-icon><Plus /></el-icon>
                新增专业
              </el-button>
            </div>

            <el-table v-loading="majorLoading" :data="majors" border stripe>
              <el-table-column prop="majorCode" label="专业代码" width="140" />
              <el-table-column prop="majorName" label="专业名称" min-width="180" />
              <el-table-column prop="collegeName" label="所属学院" min-width="180">
                <template #default="{ row }">
                  <el-tag type="info" effect="plain">{{ row.collegeName }}</el-tag>
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
                    <el-button link type="primary" @click="openMajorDialog('edit', row)">
                      编辑
                    </el-button>
                    <el-popconfirm
                      :title="row.status === 1 ? '确认停用该专业吗？' : '确认启用该专业吗？'"
                      @confirm="handleToggleMajorStatus(row)"
                    >
                      <template #reference>
                        <el-button link :type="row.status === 1 ? 'warning' : 'success'">
                          {{ row.status === 1 ? '停用' : '启用' }}
                        </el-button>
                      </template>
                    </el-popconfirm>
                    <el-popconfirm
                      title="确认删除该专业吗？删除后不可恢复。"
                      @confirm="handleDeleteMajor(row)"
                    >
                      <template #reference>
                        <el-button link type="danger">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程清单" name="course">
          <CoursePanel />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="majorDialogVisible"
      :title="majorDialogMode === 'create' ? '新增专业' : '编辑专业'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="majorFormRef"
        :model="majorForm"
        :rules="majorFormRules"
        label-width="90px"
      >
        <el-form-item label="专业代码" prop="majorCode">
          <el-input
            v-model.trim="majorForm.majorCode"
            placeholder="请输入专业代码，如 080901"
            :disabled="majorDialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input
            v-model.trim="majorForm.majorName"
            placeholder="请输入专业名称"
          />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select
            v-model="majorForm.collegeId"
            placeholder="请选择所属学院"
            style="width: 100%"
          >
            <el-option
              v-for="college in collegeOptions"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="majorForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="majorDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="majorSubmitLoading"
            @click="handleMajorSubmit"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AcademicTermPanel from './AcademicTermPanel.vue'
import CollegePanel from './CollegePanel.vue'
import CoursePanel from '@/views/basic/CoursePanel.vue'
import {
  deleteMajorApi,
  listCollegesApi,
  listMajorsApi,
  saveMajorApi,
  updateMajorStatusApi,
} from '@/api/basic'

const route = useRoute()
const activeTab = ref('academic-term')

const collegeOptions = ref([])

const majorLoading = ref(false)
const majorSubmitLoading = ref(false)
const majorDialogVisible = ref(false)
const majorDialogMode = ref('create')
const majors = ref([])
const majorFormRef = ref(null)

const majorFilters = reactive({
  majorCode: '',
  majorName: '',
  collegeId: null,
  status: null,
})

const majorForm = reactive({
  majorId: null,
  majorCode: '',
  majorName: '',
  collegeId: null,
  status: 1,
})

const majorFormRules = {
  majorCode: [
    { required: true, message: '请输入专业代码', trigger: 'blur' },
    { max: 20, message: '专业代码最长 20 位', trigger: 'blur' },
  ],
  majorName: [
    { required: true, message: '请输入专业名称', trigger: 'blur' },
    { max: 100, message: '专业名称最长 100 位', trigger: 'blur' },
  ],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

async function loadColleges() {
  collegeOptions.value = await listCollegesApi()
}

function normalizeMajorFilters() {
  return {
    majorCode: majorFilters.majorCode || undefined,
    majorName: majorFilters.majorName || undefined,
    collegeId: majorFilters.collegeId || undefined,
    status: majorFilters.status === null ? undefined : majorFilters.status,
  }
}

async function loadMajors() {
  majorLoading.value = true
  try {
    majors.value = await listMajorsApi(normalizeMajorFilters())
  } finally {
    majorLoading.value = false
  }
}

function resetMajorFilters() {
  majorFilters.majorCode = ''
  majorFilters.majorName = ''
  majorFilters.collegeId = null
  majorFilters.status = null
  loadMajors()
}

function resetMajorForm() {
  majorForm.majorId = null
  majorForm.majorCode = ''
  majorForm.majorName = ''
  majorForm.collegeId = null
  majorForm.status = 1
}

function openMajorDialog(mode, row = null) {
  majorDialogMode.value = mode
  resetMajorForm()
  if (mode === 'edit' && row) {
    majorForm.majorId = row.majorId
    majorForm.majorCode = row.majorCode
    majorForm.majorName = row.majorName
    majorForm.collegeId = row.collegeId
    majorForm.status = row.status
  }
  majorDialogVisible.value = true
  nextTick(() => majorFormRef.value?.clearValidate())
}

async function handleMajorSubmit() {
  await majorFormRef.value?.validate()
  majorSubmitLoading.value = true
  try {
    await saveMajorApi({ ...majorForm })
    ElMessage.success(
      majorDialogMode.value === 'create' ? '专业创建成功' : '专业更新成功',
    )
    majorDialogVisible.value = false
    await loadMajors()
  } finally {
    majorSubmitLoading.value = false
  }
}

async function handleToggleMajorStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateMajorStatusApi({ majorId: row.majorId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '专业已启用' : '专业已停用')
  await loadMajors()
}

async function handleDeleteMajor(row) {
  await deleteMajorApi(row.majorId)
  ElMessage.success('专业删除成功')
  await loadMajors()
}

onMounted(async () => {
  await Promise.all([loadColleges(), loadMajors()])
})
</script>

<style scoped>
.basic-data-page {
  padding: 20px;
}

.basic-data-card {
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

.data-tabs {
  margin-top: -8px;
}

.tab-content {
  padding: 4px 0;
}

.filter-form {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
