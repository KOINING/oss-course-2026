<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  addTeachingClassApi,
  deleteTeachingClassApi,
  listTeachingClassesByPageApi,
  updateTeachingClassApi,
  updateTeachingClassStatusApi,
} from '@/api/teachingClass'
import { listCoursesApi, listCourseGradeYearsApi } from '@/api/course'
import { listMajorsForSelectApi } from '@/api/basic'
import { listAcademicTermsApi } from '@/api/academicTerm'
import { listTeachersForSelectApi } from '@/api/teacher'
import { listStudentsApi } from '@/api/student'
import { listStudentsByTeachingClassApi, removeStudentFromClassApi } from '@/api/import'
import { ROUTE_NAMES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const tableLoading = ref(false)
const submitLoading = ref(false)
const relationLoading = ref(false)
const relationRemoving = ref(false)
const dialogVisible = ref(false)
const relationDrawerVisible = ref(false)
const dialogMode = ref('create')
const rows = ref([])
const relationRows = ref([])
const formRef = ref(null)
const autoOpenHandled = ref(false)

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pageSizes = [5, 10, 20, 50]

const courseOptions = ref([])
const majorOptions = ref([])
const gradeYearOptions = ref([])
const termOptions = ref([])
const teacherOptions = ref([])
const studentDirectory = ref([])

const currentTeachingClass = ref(null)

const calcStatusOptions = [
  { value: 'unsubmitted', label: '未提交' },
  { value: 'score_imported', label: '已导入成绩' },
  { value: 'calculating', label: '计算中' },
  { value: 'locked', label: '已锁定' },
]

const filters = reactive({
  classCode: '',
  className: '',
  majorId: null,
  gradeYear: null,
  courseId: null,
  termId: null,
  teacherId: null,
  calcStatus: '',
})

const form = reactive({
  classId: null,
  classCode: '',
  className: '',
  majorId: null,
  gradeYear: null,
  courseId: null,
  termId: null,
  teacherId: null,
})

const formRules = {
  classCode: [{ required: true, message: '请输入教学班编号', trigger: 'blur' }],
  className: [{ required: true, message: '请输入教学班名称', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  gradeYear: [{ required: true, message: '请选择年级', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  termId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增教学班' : '编辑教学班'))
const relationTitle = computed(() => {
  if (!currentTeachingClass.value) return '教学班名单关联'
  return `${currentTeachingClass.value.className}（${currentTeachingClass.value.classCode}）名单关联`
})

function resetFilters() {
  filters.classCode = ''
  filters.className = ''
  filters.majorId = null
  filters.gradeYear = null
  filters.courseId = null
  filters.termId = null
  filters.teacherId = null
  filters.calcStatus = ''
}

function resetForm() {
  form.classId = null
  form.classCode = ''
  form.className = ''
  form.majorId = null
  form.gradeYear = null
  form.courseId = null
  form.termId = null
  form.teacherId = null
}

function normalizeFilters() {
  return {
    classCode: filters.classCode || undefined,
    className: filters.className || undefined,
    majorId: filters.majorId || undefined,
    gradeYear: filters.gradeYear || undefined,
    courseId: filters.courseId || undefined,
    termId: filters.termId || undefined,
    teacherId: filters.teacherId || undefined,
    calcStatus: filters.calcStatus || undefined,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
  }
}

function formatCalcStatus(value) {
  return calcStatusOptions.find((option) => option.value === value)?.label || value || '未设置'
}

function getCalcStatusTagType(value) {
  switch (value) {
    case 'unsubmitted':
      return 'info'
    case 'score_imported':
      return 'warning'
    case 'calculating':
      return 'primary'
    case 'locked':
      return 'success'
    default:
      return 'info'
  }
}

function getCalcStatusTagClass(value) {
  return `calc-status-tag calc-status-tag--${value || 'default'}`
}

function getStudentById(studentId) {
  return studentDirectory.value.find((student) => student.studentId === studentId)
}

function mapRelationRows(records = []) {
  return records.map((record) => {
    const student = getStudentById(record.studentId)
    return {
      scId: record.scId,
      studentId: record.studentId,
      classId: record.classId,
      studentNo: student?.studentNo || '-',
      studentName: student?.studentName || '-',
      majorCode: student?.majorCode || '-',
      majorName: student?.majorName || '-',
      enrollmentYear: student?.enrollmentYear || '-',
      statusText: student?.statusText || '-',
    }
  })
}

async function loadOptions() {
  const [courses, majors, gradeYears, terms, teachers] = await Promise.all([
    listCoursesApi({ status: 1 }),
    listMajorsForSelectApi(),
    listCourseGradeYearsApi(),
    listAcademicTermsApi(),
    listTeachersForSelectApi(),
  ])
  courseOptions.value = courses || []
  majorOptions.value = majors || []
  gradeYearOptions.value = gradeYears || []
  termOptions.value = terms || []
  teacherOptions.value = teachers || []
}

async function loadStudentDirectory() {
  studentDirectory.value = (await listStudentsApi()) || []
}

async function tryAutoOpenRelationDrawer() {
  const openClassId = Number(route.query.openClassId || route.query.teachingClassId)
  if (!openClassId || autoOpenHandled.value) return

  const row = rows.value.find((item) => item.classId === openClassId)
  autoOpenHandled.value = true

  if (!row) {
    ElMessage.warning('未找到要查看名单关联的教学班')
    return
  }

  await openRelationDrawer(row)
  router.replace({ name: ROUTE_NAMES.TEACHING_CLASS, query: {} })
}

async function loadRows() {
  tableLoading.value = true
  try {
    const result = await listTeachingClassesByPageApi(normalizeFilters())
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
    await tryAutoOpenRelationDrawer()
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
    classId: row.classId,
    classCode: row.classCode,
    className: row.className,
    majorId: row.majorId,
    gradeYear: row.gradeYear,
    courseId: row.courseId,
    termId: row.termId,
    teacherId: row.teacherId,
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
      classId: form.classId,
      classCode: form.classCode.trim(),
      className: form.className.trim(),
      majorId: form.majorId,
      gradeYear: form.gradeYear,
      courseId: form.courseId,
      termId: form.termId,
      teacherId: form.teacherId,
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
  await deleteTeachingClassApi({ classId: row.classId })
  ElMessage.success('教学班删除成功')
  await loadRows()
}

async function handleUpdateStatus(row, calcStatus) {
  await updateTeachingClassStatusApi({ classId: row.classId, calcStatus })
  ElMessage.success('教学班计算状态已更新')
  await loadRows()
}

function goToStudentClassImport() {
  router.push({ name: ROUTE_NAMES.DATA_IMPORT, query: { type: 'student-classes' } })
}

function goToStudentClassImportForRow(row) {
  router.push({
    name: ROUTE_NAMES.DATA_IMPORT,
    query: {
      type: 'student-classes',
      teachingClassId: row.classId,
      teachingClassCode: row.classCode,
      teachingClassName: row.className,
    },
  })
}

async function openRelationDrawer(row) {
  currentTeachingClass.value = row
  relationDrawerVisible.value = true
  relationLoading.value = true
  try {
    if (!studentDirectory.value.length) {
      await loadStudentDirectory()
    }
    const records = await listStudentsByTeachingClassApi(row.classId)
    relationRows.value = mapRelationRows(records || [])
  } finally {
    relationLoading.value = false
  }
}

async function handleRemoveRelation(row) {
  relationRemoving.value = true
  try {
    await removeStudentFromClassApi(row.scId)
    ElMessage.success('学生与教学班的关联已移除')
    if (currentTeachingClass.value) {
      const records = await listStudentsByTeachingClassApi(currentTeachingClass.value.classId)
      relationRows.value = mapRelationRows(records || [])
    }
  } finally {
    relationRemoving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadStudentDirectory()])
  await loadRows()
})
</script>

<template>
  <div class="teaching-class-page">
    <el-card class="teaching-class-card">
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
          本系统中的教学班指“专业 + 年级 + 必修课程”的课程评价单元，原则上同一专业同一年级同一必修课程对应一个教学班。新增关联以“教学班学生关联导入”为主。
        </template>
      </el-alert>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="教学班编号">
          <el-input v-model.trim="filters.classCode" placeholder="请输入教学班编号" clearable />
        </el-form-item>
        <el-form-item label="教学班名称">
          <el-input v-model.trim="filters.className" placeholder="请输入教学班名称" clearable />
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
        <el-form-item label="年级">
          <el-select v-model="filters.gradeYear" placeholder="全部年级" clearable style="width: 140px">
            <el-option
              v-for="year in gradeYearOptions"
              :key="year"
              :label="`${year}级`"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="filters.courseId" placeholder="全部课程" clearable filterable style="width: 200px">
            <el-option
              v-for="course in courseOptions"
              :key="course.courseId"
              :label="`${course.courseCode} - ${course.courseName}`"
              :value="course.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="filters.termId" placeholder="全部学期" clearable filterable style="width: 180px">
            <el-option
              v-for="term in termOptions"
              :key="term.termId"
              :label="term.termCode"
              :value="term.termId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="filters.teacherId" placeholder="全部教师" clearable filterable style="width: 180px">
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.teacherName"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计算状态">
          <el-select v-model="filters.calcStatus" placeholder="全部状态" clearable style="width: 160px">
            <el-option
              v-for="status in calcStatusOptions"
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
        <el-button type="primary" @click="openCreateDialog">新增教学班</el-button>
        <el-button type="success" plain @click="goToStudentClassImport">前往教学班学生关联导入</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="rows" border stripe>
        <el-table-column prop="classCode" label="教学班编号" min-width="140" />
        <el-table-column prop="className" label="教学班名称" min-width="180" />
        <el-table-column prop="majorName" label="专业" min-width="180" />
        <el-table-column prop="gradeYear" label="年级" width="100" align="center">
          <template #default="{ row }">{{ row.gradeYear ? `${row.gradeYear}级` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="courseCode" label="课程代码" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="termCode" label="学期" min-width="140" />
        <el-table-column prop="teacherName" label="主讲教师" min-width="120" />
        <el-table-column label="计算状态" min-width="120">
          <template #default="{ row }">
            <el-tag
              effect="plain"
              :type="getCalcStatusTagType(row.calcStatus)"
              :class="getCalcStatusTagClass(row.calcStatus)"
            >
              {{ formatCalcStatus(row.calcStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openRelationDrawer(row)">查看名单</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-dropdown @command="(command) => handleUpdateStatus(row, command)">
                <el-button link type="warning">更新状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="status in calcStatusOptions"
                      :key="status.value"
                      :command="status.value"
                    >
                      {{ status.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确认删除该教学班吗？" @confirm="handleDelete(row)">
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

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
          <el-form-item label="教学班编号" prop="classCode">
            <el-input v-model.trim="form.classCode" placeholder="如 TC2024CS01" maxlength="32" />
          </el-form-item>
          <el-form-item label="教学班名称" prop="className">
            <el-input v-model.trim="form.className" placeholder="如 计算机科学与技术2022级-数据结构" maxlength="50" />
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
          <el-form-item label="年级" prop="gradeYear">
            <el-select v-model="form.gradeYear" placeholder="请选择年级" style="width: 100%">
              <el-option
                v-for="year in gradeYearOptions"
                :key="year"
                :label="`${year}级`"
                :value="year"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="课程" prop="courseId">
            <el-select v-model="form.courseId" placeholder="请选择课程" filterable style="width: 100%">
              <el-option
                v-for="course in courseOptions"
                :key="course.courseId"
                :label="`${course.courseCode} - ${course.courseName}`"
                :value="course.courseId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学期" prop="termId">
            <el-select v-model="form.termId" placeholder="请选择学期" style="width: 100%">
              <el-option
                v-for="term in termOptions"
                :key="term.termId"
                :label="term.termCode"
                :value="term.termId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="主讲教师" prop="teacherId">
            <el-select v-model="form.teacherId" placeholder="请选择教师" filterable style="width: 100%">
              <el-option
                v-for="teacher in teacherOptions"
                :key="teacher.id"
                :label="teacher.teacherName"
                :value="teacher.id"
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

    <el-drawer v-model="relationDrawerVisible" :title="relationTitle" size="720px" destroy-on-close>
      <div class="relation-drawer">
        <div class="relation-drawer__header">
          <div>
            <p class="relation-drawer__meta">
              {{ currentTeachingClass?.courseName || '-' }} / {{ currentTeachingClass?.termCode || '-' }}
            </p>
            <h3>教学班学生关联</h3>
            <p class="relation-drawer__hint">该名单应覆盖本专业本年级修读该必修课程的学生。</p>
          </div>
          <el-button
            type="primary"
            plain
            @click="currentTeachingClass && goToStudentClassImportForRow(currentTeachingClass)"
          >
            前往教学班学生关联导入
          </el-button>
        </div>

        <el-table v-loading="relationLoading" :data="relationRows" border stripe>
          <el-table-column prop="studentNo" label="学号" min-width="140" />
          <el-table-column prop="studentName" label="姓名" min-width="120" />
          <el-table-column prop="majorCode" label="专业代码" min-width="120" />
          <el-table-column prop="majorName" label="专业名称" min-width="180" />
          <el-table-column prop="enrollmentYear" label="入学年份" min-width="110" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确认移除该学生与教学班的关联吗？" @confirm="handleRemoveRelation(row)">
                <template #reference>
                  <el-button link type="danger" :loading="relationRemoving">移除关联</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!relationLoading && relationRows.length === 0" description="当前教学班暂无学生关联" :image-size="88" />
      </div>
    </el-drawer>
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
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

:deep(.calc-status-tag) {
  font-weight: 600;
}

:deep(.calc-status-tag--unsubmitted) {
  background-color: #eff6ff;
  border-color: #93c5fd;
  color: #2563eb;
}

:deep(.calc-status-tag--score_imported) {
  background-color: #fff7ed;
  border-color: #fdba74;
  color: #ea580c;
}

:deep(.calc-status-tag--calculating) {
  background-color: #eef2ff;
  border-color: #a5b4fc;
  color: #4f46e5;
}

:deep(.calc-status-tag--locked) {
  background-color: #ecfdf5;
  border-color: #86efac;
  color: #15803d;
}

.relation-drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.relation-drawer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.relation-drawer__header h3 {
  margin: 4px 0 0;
  font-size: 20px;
  color: #0f172a;
}

.relation-drawer__meta {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}
</style>
