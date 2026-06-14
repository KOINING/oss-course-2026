<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
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
const route = useRoute()

const majorOptions = ref([])
const gradeYearOptions = ref([])
const courseOptions = ref([])
const tableLoading = ref(false)
const saveLoading = ref(false)
const resetLoading = ref(false)

const courses = ref([])
const indicators = ref([])
const matrixMap = ref({})
const matrixScrollRef = ref(null)
const groupHeaderRowRef = ref(null)
const indicatorHeaderRowRef = ref(null)
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

function syncStickyMetrics() {
  const matrixScroll = matrixScrollRef.value
  if (!matrixScroll) return
  const groupHeaderHeight = groupHeaderRowRef.value?.offsetHeight ?? 0
  const indicatorHeaderHeight = indicatorHeaderRowRef.value?.offsetHeight ?? 0
  matrixScroll.style.setProperty('--matrix-group-header-height', `${groupHeaderHeight}px`)
  matrixScroll.style.setProperty('--matrix-indicator-header-top', `${groupHeaderHeight}px`)
  matrixScroll.style.setProperty('--matrix-header-total-height', `${groupHeaderHeight + indicatorHeaderHeight}px`)
}

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
    await nextTick()
    syncStickyMetrics()
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
  syncStickyMetrics()
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

watch(
  () => [indicatorGroups.value.length, indicators.value.length, visibleCourses.value.length],
  async () => {
    await nextTick()
    syncStickyMetrics()
  },
)

onMounted(() => {
  loadMajorOptions()
  window.addEventListener('resize', syncStickyMetrics)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncStickyMetrics)
})
</script>

<template>
  <div class="support-matrix-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
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

      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty v-if="!tableLoading && indicators.length === 0" description="请选择专业和年级后查询支撑矩阵。" />

        <div v-else ref="matrixScrollRef" class="matrix-scroll">
          <table class="matrix-table">
            <thead>
              <tr ref="groupHeaderRowRef">
                <th class="corner-cell" rowspan="2">
                  <div class="corner-title">课程 / 毕业要求</div>
                  <div class="corner-stats">
                    <span>毕业要求: {{ graduationRequirementCount }}</span>
                    <span>指标点: {{ indicators.length }}</span>
                    <span>课程: {{ courses.length }}</span>
                  </div>
                </th>
                <th
                  v-for="group in indicatorGroups"
                  :key="`${group.grId}-${group.gradeYear}`"
                  :colspan="group.indicators.length"
                  class="group-cell"
                >
                  <div class="group-code">{{ group.grCode }}</div>
                  <div class="group-desc">{{ group.grDescription }}</div>
                </th>
              </tr>
              <tr ref="indicatorHeaderRowRef">
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
    </el-card>
  </div>
</template>

<style scoped>
.support-matrix-page {
  padding: 16px;
}

.page-card {
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

.matrix-wrap {
  min-height: 240px;
}

.matrix-scroll {
  --matrix-group-header-height: 0px;
  --matrix-indicator-header-top: 0px;
  --matrix-header-total-height: 0px;
  position: relative;
  max-height: 78vh;
  overflow-x: auto;
  overflow-y: auto;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
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
  background: #fff;
}

.corner-cell {
  position: sticky;
  top: 0;
  left: 0;
  min-width: 180px;
  background: #eef4ff;
  font-weight: 600;
  z-index: 7;
  border-bottom: 1px solid #cbd5e1;
  box-shadow: inset 0 -1px 0 #cbd5e1, 1px 0 0 #dbe2ea;
}

.corner-title {
  font-size: 18px;
  line-height: 1.4;
}

.corner-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 10px;
  color: #475569;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}

.group-cell {
  position: sticky;
  top: 0;
  min-width: 144px;
  max-width: 144px;
  background: #eef4ff;
  z-index: 5;
  border-bottom: 1px solid #cbd5e1;
  box-shadow: inset 0 -1px 0 #cbd5e1;
}

.group-code,
.indicator-code {
  font-weight: 600;
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
  position: sticky;
  top: var(--matrix-indicator-header-top);
  min-width: 144px;
  max-width: 144px;
  background: #f7faff;
  z-index: 4;
  border-bottom: 1px solid #cbd5e1;
  box-shadow: inset 0 -1px 0 #cbd5e1;
}

.course-cell {
  position: sticky;
  left: 0;
  min-width: 180px;
  background: #f8fafc;
  font-weight: 500;
  z-index: 3;
  box-shadow: inset 0 -1px 0 #e2e8f0, 1px 0 0 #dbe2ea;
}

.value-cell {
  text-align: center;
  background: #fff;
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
  position: sticky;
  left: 0;
  bottom: 0;
  font-weight: 600;
  background: #f8fafc !important;
  z-index: 6;
  border-top: 1px solid #cbd5e1;
  box-shadow: inset 0 1px 0 #cbd5e1, 1px 0 0 #dbe2ea;
}

.sum-cell {
  position: sticky;
  bottom: 0;
  text-align: center;
  background: #f8fafc !important;
  z-index: 2;
  border-top: 1px solid #cbd5e1;
  box-shadow: inset 0 1px 0 #cbd5e1;
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
</style>
