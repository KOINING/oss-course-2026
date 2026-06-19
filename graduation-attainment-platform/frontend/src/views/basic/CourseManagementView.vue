<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { listMajorsApi } from '@/api/basic'
import {
  addCourseApi,
  deleteCourseApi,
  listCourseGradeYearsApi,
  listCoursesByPageApi,
  updateCourseApi,
  updateCourseStatusApi,
} from '@/api/course'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const courseLoading = ref(false)
const courseDialogVisible = ref(false)
const courseDialogMode = ref('create')
const courseSubmitLoading = ref(false)
const courseFormRef = ref(null)

const courses = ref([])
const majorOptions = ref([])
const gradeYearOptions = ref([])

const coursePagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const courseFilters = reactive({
  courseCode: '',
  courseName: '',
  majorId: null,
  gradeYear: null,
  status: null,
})

const courseForm = reactive({
  courseId: null,
  courseCode: '',
  courseName: '',
  credit: 0,
  majorGradeYearBindings: [],
  status: 1,
})

const courseFormRules = {
  courseCode: [{ required: true, message: '请输入课程代码。', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称。', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分。', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态。', trigger: 'change' }],
}

function formatGradeYear(gradeYear) {
  return gradeYear ? `${gradeYear}级` : '-'
}

function normalizeCourseFilters() {
  return {
    courseCode: courseFilters.courseCode || undefined,
    courseName: courseFilters.courseName || undefined,
    majorId: courseFilters.majorId || undefined,
    gradeYear: courseFilters.gradeYear || undefined,
    status: courseFilters.status === null ? undefined : courseFilters.status,
  }
}

async function loadMajors() {
  majorOptions.value = (await listMajorsApi({ status: 1 })) || []
}

async function loadGradeYears() {
  gradeYearOptions.value = (await listCourseGradeYearsApi()) || [2022]
}

async function loadCourses() {
  courseLoading.value = true
  try {
    const result = await listCoursesByPageApi({
      ...normalizeCourseFilters(),
      pageNum: coursePagination.pageNum,
      pageSize: coursePagination.pageSize,
    })
    courses.value = result.records || []
    coursePagination.total = result.total
  } finally {
    courseLoading.value = false
  }
}

function handleCourseSearch() {
  coursePagination.pageNum = 1
  loadCourses()
}

function resetCourseFilters() {
  courseFilters.courseCode = ''
  courseFilters.courseName = ''
  courseFilters.majorId = null
  courseFilters.gradeYear = null
  courseFilters.status = null
  handleCourseSearch()
}

function handleCourseSizeChange() {
  coursePagination.pageNum = 1
  loadCourses()
}

function handleCourseCurrentChange() {
  loadCourses()
}

function createEmptyBinding() {
  return {
    majorId: null,
    gradeYears: [],
  }
}

function resetCourseForm() {
  courseForm.courseId = null
  courseForm.courseCode = ''
  courseForm.courseName = ''
  courseForm.credit = 0
  courseForm.majorGradeYearBindings = [createEmptyBinding()]
  courseForm.status = 1
}

function addBindingRow() {
  courseForm.majorGradeYearBindings.push(createEmptyBinding())
}

function removeBindingRow(index) {
  if (courseForm.majorGradeYearBindings.length === 1) {
    courseForm.majorGradeYearBindings.splice(0, 1, createEmptyBinding())
    return
  }
  courseForm.majorGradeYearBindings.splice(index, 1)
}

function openCourseDialog(mode, row = null) {
  courseDialogMode.value = mode
  resetCourseForm()
  if (mode === 'edit' && row) {
    Object.assign(courseForm, {
      courseId: row.courseId,
      courseCode: row.courseCode,
      courseName: row.courseName,
      credit: row.credit,
      majorGradeYearBindings:
        row.majorGradeYearBindings?.length > 0
          ? row.majorGradeYearBindings.map((binding) => ({
              majorId: binding.majorId,
              gradeYears: [...(binding.gradeYears || [])],
            }))
          : [createEmptyBinding()],
      status: row.status,
    })
  }
  courseDialogVisible.value = true
  nextTick(() => courseFormRef.value?.clearValidate())
}

function normalizeBindingsForSubmit() {
  return courseForm.majorGradeYearBindings
    .filter((binding) => binding.majorId && Array.isArray(binding.gradeYears) && binding.gradeYears.length > 0)
    .map((binding) => ({
      majorId: binding.majorId,
      gradeYears: [...new Set(binding.gradeYears)].sort((left, right) => left - right),
    }))
}

async function handleCourseSubmit() {
  await courseFormRef.value?.validate()

  const majorGradeYearBindings = normalizeBindingsForSubmit()
  if (majorGradeYearBindings.length === 0) {
    ElMessage.warning('请至少配置一条专业-年级绑定。')
    return
  }

  const payload = {
    courseId: courseForm.courseId,
    courseCode: courseForm.courseCode.trim(),
    courseName: courseForm.courseName.trim(),
    credit: Number(courseForm.credit),
    majorIds: [...new Set(majorGradeYearBindings.map((binding) => binding.majorId))],
    majorGradeYearBindings,
    status: Number(courseForm.status),
  }

  courseSubmitLoading.value = true
  try {
    if (courseDialogMode.value === 'create') {
      await addCourseApi(payload)
      ElMessage.success('课程新增成功。')
    } else {
      await updateCourseApi(payload)
      ElMessage.success('课程更新成功。')
    }
    courseDialogVisible.value = false
    await loadGradeYears()
    await loadCourses()
  } finally {
    courseSubmitLoading.value = false
  }
}

async function handleToggleCourseStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateCourseStatusApi({ courseId: row.courseId, status: nextStatus })
  ElMessage.success(nextStatus === 1 ? '课程已启用。' : '课程已停用。')
  await loadCourses()
}

async function handleDeleteCourse(row) {
  await ElMessageBox.confirm(`确定删除课程“${row.courseName}”吗？`, '提示', { type: 'warning' })
  await deleteCourseApi({ courseId: row.courseId })
  ElMessage.success('课程删除成功。')
  await loadCourses()
}

function goToCourseImport() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'course' } })
}

function formatBindingText(binding) {
  const years = (binding.gradeYears || []).map(formatGradeYear).join(', ')
  return `${binding.majorName || '-'} · ${years || '-'}`
}

onMounted(async () => {
  await Promise.all([loadMajors(), loadGradeYears()])
  await loadCourses()
})
</script>

<template>
  <div class="course-management-page">
    <el-card class="course-management-card">
      <template #header>
        <div class="page-header">
          <div>
            <h1>{{ route.meta.title || '课程管理' }}</h1>
            <p class="page-summary">
              {{ route.meta.summary || '维护课程基础信息及其在不同专业、不同年级培养方案中的适用关系。' }}
            </p>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="courseFilters" class="filter-form">
        <el-form-item label="课程代码">
          <el-input v-model.trim="courseFilters.courseCode" placeholder="请输入课程代码" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model.trim="courseFilters.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="courseFilters.majorId" placeholder="全部专业" clearable filterable style="width: 180px">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="courseFilters.gradeYear" placeholder="全部年级" clearable filterable style="width: 140px">
            <el-option
              v-for="gradeYear in gradeYearOptions"
              :key="gradeYear"
              :label="formatGradeYear(gradeYear)"
              :value="gradeYear"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="courseFilters.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCourseSearch">查询</el-button>
          <el-button @click="resetCourseFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="openCourseDialog('create')">新增课程</el-button>
        <el-button type="success" plain @click="goToCourseImport">前往批量导入课程清单</el-button>
      </div>

      <el-table v-loading="courseLoading" :data="courses" border>
        <el-table-column prop="courseCode" label="课程代码" width="130" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="credit" label="学分" width="100" />
        <el-table-column label="专业-年级绑定" min-width="260">
          <template #default="{ row }">
            <div class="binding-tags">
              <el-tag
                v-for="binding in row.majorGradeYearBindings || []"
                :key="`${binding.majorId}-${(binding.gradeYears || []).join('-')}`"
                effect="plain"
              >
                {{ formatBindingText(binding) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openCourseDialog('edit', row)">编辑</el-button>
              <el-button link type="primary" @click="handleToggleCourseStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDeleteCourse(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="coursePagination.pageNum"
          v-model:page-size="coursePagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="coursePagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleCourseSizeChange"
          @current-change="handleCourseCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="courseDialogVisible"
      :title="courseDialogMode === 'create' ? '新增课程' : '编辑课程'"
      width="720px"
      destroy-on-close
    >
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseFormRules" label-width="110px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model.trim="courseForm.courseCode" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model.trim="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0" :step="0.5" style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="绑定关系">
          <div class="binding-editor">
            <div
              v-for="(binding, index) in courseForm.majorGradeYearBindings"
              :key="index"
              class="binding-row"
            >
              <el-select v-model="binding.majorId" placeholder="请选择专业" filterable style="width: 220px">
                <el-option
                  v-for="major in majorOptions"
                  :key="major.majorId"
                  :label="major.majorName"
                  :value="major.majorId"
                />
              </el-select>

              <el-select
                v-model="binding.gradeYears"
                placeholder="请选择年级"
                multiple
                collapse-tags
                collapse-tags-tooltip
                filterable
                style="width: 260px"
              >
                <el-option
                  v-for="gradeYear in gradeYearOptions"
                  :key="gradeYear"
                  :label="formatGradeYear(gradeYear)"
                  :value="gradeYear"
                />
              </el-select>

              <el-button :icon="Delete" circle @click="removeBindingRow(index)" />
            </div>

            <el-button type="primary" plain :icon="Plus" @click="addBindingRow">新增一行</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="courseSubmitLoading" @click="handleCourseSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.course-management-page {
  padding: 16px;
}

.course-management-card {
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.page-section {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.page-summary {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.filter-form {
  margin-bottom: 12px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.binding-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.binding-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.binding-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .binding-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
