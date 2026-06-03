<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import {
  getSupportMatrixApi,
  listCoursesApi,
  listGradeYearsForMatrixApi,
  listIndicatorPointsForMatrixApi,
  listMajorsForMatrixApi,
  resetSupportMatrixApi,
  saveSupportMatrixApi,
} from '@/api/supportmatrix'

const filters = reactive({
  majorId: null,
  gradeYear: null,
  courseId: null,
})

const majorOptions = ref([])
const gradeYearOptions = ref([])
const courseOptions = ref([])
const tableLoading = ref(false)
const saveLoading = ref(false)
const resetLoading = ref(false)

const courses = ref([])
const indicators = ref([])
const matrixMap = ref({})
let matrixSnapshot = {}

function formatGradeYear(gradeYear) {
  return gradeYear ? `${gradeYear}级` : '-'
}

const indicatorGroups = computed(() => {
  const groups = []
  const groupMap = new Map()
  indicators.value.forEach((indicator) => {
    const key = `${indicator.grId}_${indicator.gradeYear}`
    if (!groupMap.has(key)) {
      const group = {
        grId: indicator.grId,
        grCode: indicator.grCode,
        grDescription: indicator.grDescription,
        gradeYear: indicator.gradeYear,
        indicators: [],
      }
      groupMap.set(key, group)
      groups.push(group)
    }
    groupMap.get(key).indicators.push(indicator)
  })
  return groups
})

const graduationRequirementCount = computed(() => indicatorGroups.value.length)

const visibleCourses = computed(() => {
  if (!filters.courseId) return courses.value
  return courses.value.filter((course) => course.courseId === filters.courseId)
})

async function loadMajorOptions() {
  majorOptions.value = (await listMajorsForMatrixApi()) || []
}

async function loadGradeYearOptions(majorId) {
  gradeYearOptions.value = majorId ? (await listGradeYearsForMatrixApi({ majorId })) || [] : []
}

async function loadCourseOptions() {
  if (!filters.majorId || !filters.gradeYear) {
    courseOptions.value = []
    return
  }
  courseOptions.value = (await listCoursesApi({ majorId: filters.majorId, gradeYear: filters.gradeYear })) || []
}

function onMajorChange() {
  filters.gradeYear = null
  filters.courseId = null
  gradeYearOptions.value = []
  courseOptions.value = []
  courses.value = []
  indicators.value = []
  matrixMap.value = {}
  matrixSnapshot = {}
  if (filters.majorId) {
    loadGradeYearOptions(filters.majorId)
  }
}

async function onGradeYearChange() {
  filters.courseId = null
  courses.value = []
  indicators.value = []
  matrixMap.value = {}
  matrixSnapshot = {}
  await loadCourseOptions()
}

function buildMatrixKey(courseId, ipId) {
  return `${courseId}_${ipId}`
}

function buildMatrixMap(relations) {
  const nextMap = {}
  relations.forEach((row) => {
    const weight = Number(row.totalWeight ?? row.weight ?? 0)
    nextMap[buildMatrixKey(row.courseId, row.ipId)] = {
      checked: true,
      totalWeight: Number.isFinite(weight) ? weight : 0,
    }
  })
  return nextMap
}

async function loadMatrix() {
  if (!filters.majorId || !filters.gradeYear) {
    ElMessage.warning('请先选择专业和年级。')
    return
  }

  tableLoading.value = true
  try {
    const [courseRows, indicatorRows, relationRows] = await Promise.all([
      listCoursesApi({ majorId: filters.majorId, gradeYear: filters.gradeYear }),
      listIndicatorPointsForMatrixApi({ majorId: filters.majorId, gradeYear: filters.gradeYear }),
      getSupportMatrixApi({ majorId: filters.majorId, gradeYear: filters.gradeYear }),
    ])
    courses.value = courseRows || []
    courseOptions.value = courseRows || []
    indicators.value = indicatorRows || []
    matrixMap.value = buildMatrixMap(relationRows || [])
    matrixSnapshot = JSON.parse(JSON.stringify(matrixMap.value))
  } finally {
    tableLoading.value = false
  }
}

function resetFilters() {
  filters.majorId = null
  filters.gradeYear = null
  filters.courseId = null
  gradeYearOptions.value = []
  courseOptions.value = []
  courses.value = []
  indicators.value = []
  matrixMap.value = {}
  matrixSnapshot = {}
}

function handleLocalReset() {
  matrixMap.value = JSON.parse(JSON.stringify(matrixSnapshot))
}

function isChecked(courseId, ipId) {
  return Boolean(matrixMap.value[buildMatrixKey(courseId, ipId)]?.checked)
}

function getWeight(courseId, ipId) {
  return matrixMap.value[buildMatrixKey(courseId, ipId)]?.totalWeight ?? 0
}

function onCheckChange(courseId, ipId, checked) {
  const key = buildMatrixKey(courseId, ipId)
  if (!checked) {
    delete matrixMap.value[key]
    matrixMap.value = { ...matrixMap.value }
    return
  }
  matrixMap.value[key] = {
    checked: true,
    totalWeight: matrixMap.value[key]?.totalWeight ?? 0,
  }
  matrixMap.value = { ...matrixMap.value }
}

function onWeightChange(courseId, ipId, value) {
  const key = buildMatrixKey(courseId, ipId)
  matrixMap.value[key] = {
    checked: true,
    totalWeight: Number(value ?? 0),
  }
  matrixMap.value = { ...matrixMap.value }
}

function getColumnSum(ipId) {
  return courses.value.reduce((sum, course) => {
    const item = matrixMap.value[buildMatrixKey(course.courseId, ipId)]
    if (!item?.checked) return sum
    return sum + Number(item.totalWeight || 0)
  }, 0)
}

function isColumnValid(ipId) {
  return Math.abs(getColumnSum(ipId) - 1) < 0.0001
}

async function handleSave() {
  if (!filters.majorId || !filters.gradeYear) {
    ElMessage.warning('请先选择专业和年级。')
    return
  }

  const rows = Object.entries(matrixMap.value)
    .filter(([, item]) => item?.checked)
    .map(([key, item]) => {
      const [courseId, ipId] = key.split('_').map(Number)
      return {
        courseId,
        ipId,
        totalWeight: Number(item.totalWeight || 0),
      }
    })

  saveLoading.value = true
  try {
    await saveSupportMatrixApi({
      majorId: filters.majorId,
      gradeYear: filters.gradeYear,
      rows,
    })
    ElMessage.success('支撑矩阵保存成功。')
    await loadMatrix()
  } finally {
    saveLoading.value = false
  }
}

async function handleServerReset() {
  if (!filters.majorId || !filters.gradeYear) {
    ElMessage.warning('请先选择专业和年级。')
    return
  }

  await ElMessageBox.confirm(
    `确定清空当前专业 ${formatGradeYear(filters.gradeYear)} 的全部支撑矩阵数据吗？`,
    '提示',
    { type: 'warning' },
  )

  resetLoading.value = true
  try {
    await resetSupportMatrixApi({
      majorId: filters.majorId,
      gradeYear: filters.gradeYear,
    })
    ElMessage.success('当前版本支撑矩阵已清空。')
    await loadMatrix()
  } finally {
    resetLoading.value = false
  }
}

onMounted(loadMajorOptions)
</script>

<template>
  <div class="support-matrix-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A</p>
            <h1>支撑矩阵配置</h1>
            <p class="page-summary">
              按专业和年级配置课程对指标点的支撑关系，列权重校验始终基于当前版本下的全部课程。
            </p>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="专业">
          <el-select v-model="filters.majorId" placeholder="请选择专业" clearable filterable style="width: 200px" @change="onMajorChange">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="年级">
          <el-select v-model="filters.gradeYear" placeholder="请选择年级" clearable filterable style="width: 160px" @change="onGradeYearChange">
            <el-option
              v-for="gradeYear in gradeYearOptions"
              :key="gradeYear"
              :label="formatGradeYear(gradeYear)"
              :value="gradeYear"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="课程">
          <el-select v-model="filters.courseId" placeholder="全部课程" clearable filterable style="width: 240px">
            <el-option
              v-for="course in courseOptions"
              :key="`${course.courseId}-${course.gradeYear}`"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
            <el-button type="primary" :loading="tableLoading" @click="loadMatrix">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" show-icon class="notice-alert">
        毕业要求和指标点表头支持换行展示，指标点列已压缩宽度以便在单页中显示更多信息。
      </el-alert>

      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty v-if="!tableLoading && indicators.length === 0" description="请选择专业和年级后查询支撑矩阵。" />

        <div v-else class="matrix-scroll">
          <table class="matrix-table">
            <thead>
              <tr>
                <th class="corner-cell" rowspan="2">课程 / 指标点</th>
                <th
                  v-for="group in indicatorGroups"
                  :key="`${group.grId}-${group.gradeYear}`"
                  :colspan="group.indicators.length"
                  class="group-cell"
                >
                  <div class="group-code">{{ group.grCode }}</div>
                  <div class="group-year">{{ formatGradeYear(group.gradeYear) }}</div>
                  <div class="group-desc">{{ group.grDescription }}</div>
                </th>
              </tr>
              <tr>
                <th v-for="indicator in indicators" :key="indicator.ipId" class="indicator-cell">
                  <div class="indicator-code">{{ indicator.ipCode }}</div>
                  <div class="indicator-desc">{{ indicator.ipDescription }}</div>
                </th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="course in visibleCourses" :key="course.courseId">
                <td class="course-cell">{{ course.courseName }}</td>
                <td v-for="indicator in indicators" :key="indicator.ipId" class="value-cell">
                  <div class="cell-inner">
                    <el-checkbox
                      :model-value="isChecked(course.courseId, indicator.ipId)"
                      @change="(value) => onCheckChange(course.courseId, indicator.ipId, value)"
                    />
                    <el-input-number
                      :model-value="getWeight(course.courseId, indicator.ipId)"
                      :disabled="!isChecked(course.courseId, indicator.ipId)"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      class="weight-input"
                      @change="(value) => onWeightChange(course.courseId, indicator.ipId, value)"
                    />
                  </div>
                </td>
              </tr>

              <tr class="sum-row">
                <td class="sum-label">列权重合计</td>
                <td v-for="indicator in indicators" :key="indicator.ipId" class="sum-cell">
                  <div class="sum-inner">
                    <span :class="isColumnValid(indicator.ipId) ? 'sum-valid' : 'sum-invalid'">
                      {{ getColumnSum(indicator.ipId).toFixed(2) }}
                    </span>
                    <el-icon v-if="isColumnValid(indicator.ipId)" color="#16a34a"><CircleCheck /></el-icon>
                    <el-icon v-else color="#dc2626"><CircleClose /></el-icon>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bottom-actions">
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存矩阵</el-button>
        <el-button :loading="resetLoading" @click="handleServerReset">清空当前版本</el-button>
        <el-button @click="handleLocalReset">恢复本次查询结果</el-button>
      </div>

      <div class="summary-bar" v-if="indicators.length > 0">
        <span>毕业要求：{{ graduationRequirementCount }}</span>
        <span>指标点：{{ indicators.length }}</span>
        <span>课程：{{ courses.length }}</span>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.support-matrix-page {
  padding: 16px;
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

.notice-alert {
  margin-bottom: 16px;
}

.matrix-wrap {
  min-height: 240px;
}

.matrix-scroll {
  overflow-x: auto;
}

.matrix-table {
  width: 100%;
  min-width: max-content;
  border-collapse: collapse;
}

.matrix-table th,
.matrix-table td {
  border: 1px solid #dbe2ea;
  padding: 10px 8px;
  vertical-align: top;
}

.corner-cell {
  min-width: 160px;
  background: #f8fafc;
  font-weight: 600;
}

.group-cell {
  min-width: 144px;
  max-width: 144px;
  background: #f8fafc;
}

.group-code,
.indicator-code {
  font-weight: 600;
}

.group-year {
  margin-top: 4px;
  color: #475569;
  font-size: 12px;
}

.group-desc,
.indicator-desc {
  margin-top: 6px;
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  color: #475569;
  font-size: 12px;
}

.indicator-cell {
  min-width: 144px;
  max-width: 144px;
  background: #f8fafc;
}

.course-cell {
  min-width: 180px;
  font-weight: 500;
}

.value-cell {
  text-align: center;
}

.cell-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.weight-input {
  width: 78px;
}

.sum-row {
  background: #f8fafc;
}

.sum-label {
  font-weight: 600;
}

.sum-cell {
  text-align: center;
}

.sum-inner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.sum-valid {
  color: #16a34a;
  font-weight: 600;
}

.sum-invalid {
  color: #dc2626;
  font-weight: 600;
}

.bottom-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.summary-bar {
  display: flex;
  gap: 20px;
  margin-top: 12px;
  color: #64748b;
  font-size: 13px;
}
</style>
