<script setup>
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const matrixTableRef = ref(null)
const summaryScrollRef = ref(null)
const summaryColumnWidths = ref([])
const hoveredCourseId = ref(null)
const hoveredIndicatorId = ref(null)
let matrixSnapshot = {}
let tableBodyScrollEl = null
let syncingScroll = false

function formatGradeYear(gradeYear) {
  return gradeYear ? `${gradeYear}级` : '-'
}

function formatCourseLabel(course) {
  if (!course) return '-'
  const code = course.courseCode || ''
  const name = course.courseName || ''
  return code && name ? `${code} - ${name}` : code || name || '-'
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

function getTableBodyScrollEl() {
  const tableEl = matrixTableRef.value?.$el
  return tableEl?.querySelector('.el-table__body-wrapper .el-scrollbar__wrap') ?? null
}

function handleTableBodyScroll() {
  if (syncingScroll || !tableBodyScrollEl || !summaryScrollRef.value) return
  syncingScroll = true
  summaryScrollRef.value.scrollLeft = tableBodyScrollEl.scrollLeft
  syncingScroll = false
}

function handleSummaryScroll() {
  if (syncingScroll || !tableBodyScrollEl || !summaryScrollRef.value) return
  syncingScroll = true
  tableBodyScrollEl.scrollLeft = summaryScrollRef.value.scrollLeft
  syncingScroll = false
}

function getMatrixLeafColumnWidths() {
  const tableEl = matrixTableRef.value?.$el
  const cols = tableEl?.querySelectorAll('.el-table__body-wrapper colgroup col') || []
  const widths = Array.from(cols)
    .map((col) => Number(col.getAttribute('width') || parseFloat(col.style.width) || 0))
    .filter((width) => Number.isFinite(width) && width > 0)
  const expectedCount = indicators.value.length + 1
  return widths.length >= expectedCount ? widths.slice(0, expectedCount) : []
}

async function updateSummaryColumnWidths() {
  await nextTick()
  const widths = getMatrixLeafColumnWidths()
  summaryColumnWidths.value = widths.length
    ? widths
    : [220, ...indicators.value.map(() => 178)]
}

function getSummaryGridStyle() {
  const widths = summaryColumnWidths.value.length
    ? summaryColumnWidths.value
    : [220, ...indicators.value.map(() => 178)]
  const totalWidth = widths.reduce((sum, width) => sum + width, 0)
  return {
    minWidth: `${totalWidth}px`,
    gridTemplateColumns: widths.map((width) => `${width}px`).join(' '),
  }
}

function handleHeaderDragEnd() {
  updateSummaryColumnWidths()
}

function detachMatrixScrollSync() {
  tableBodyScrollEl?.removeEventListener('scroll', handleTableBodyScroll)
  summaryScrollRef.value?.removeEventListener('scroll', handleSummaryScroll)
  tableBodyScrollEl = null
}

async function setupMatrixScrollSync() {
  await nextTick()
  detachMatrixScrollSync()
  tableBodyScrollEl = getTableBodyScrollEl()
  if (!tableBodyScrollEl || !summaryScrollRef.value) return
  tableBodyScrollEl.addEventListener('scroll', handleTableBodyScroll)
  summaryScrollRef.value.addEventListener('scroll', handleSummaryScroll)
  summaryScrollRef.value.scrollLeft = tableBodyScrollEl.scrollLeft
  await updateSummaryColumnWidths()
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
    await setupMatrixScrollSync()
  } finally {
    tableLoading.value = false
  }
}

async function initializeDefaultMatrix() {
  await loadMajorOptions()
  if (!majorOptions.value.length) return

  const queryMajorId = Number(route.query.majorId)
  const matchedMajor = Number.isFinite(queryMajorId)
    ? majorOptions.value.find((major) => Number(major.majorId) === queryMajorId)
    : null
  filters.majorId = matchedMajor?.majorId ?? majorOptions.value[0].majorId

  await loadGradeYearOptions(filters.majorId)
  if (!gradeYearOptions.value.length) return

  const queryGradeYear = Number(route.query.gradeYear)
  filters.gradeYear = Number.isFinite(queryGradeYear) && gradeYearOptions.value.includes(queryGradeYear)
    ? queryGradeYear
    : gradeYearOptions.value[0]

  await loadMatrix()
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

function setMatrixHover(courseId, ipId = null) {
  hoveredCourseId.value = courseId
  hoveredIndicatorId.value = ipId
}

function clearMatrixHover() {
  hoveredCourseId.value = null
  hoveredIndicatorId.value = null
}

function getMatrixRowClass({ row }) {
  return row?.courseId === hoveredCourseId.value ? 'matrix-row-hover' : ''
}

function getIndicatorColumnClass(ipId) {
  return ipId === hoveredIndicatorId.value ? 'matrix-column-hover' : ''
}

function getMatrixCellClass(courseId, ipId) {
  return courseId === hoveredCourseId.value && ipId === hoveredIndicatorId.value
    ? 'matrix-edit-cell matrix-edit-cell--active'
    : 'matrix-edit-cell'
}

function getSummaryCellClass(ipId) {
  return ipId === hoveredIndicatorId.value
    ? 'matrix-summary-cell matrix-summary-cell--hover'
    : 'matrix-summary-cell'
}

function renderGroupHeader(group) {
  return h('div', { class: 'matrix-group-header' }, [
    h('div', { class: 'matrix-group-header__code' }, group.grCode),
    h('div', { class: 'matrix-group-header__desc' }, group.grDescription),
  ])
}

function renderIndicatorHeader(indicator) {
  return h('div', { class: 'matrix-dynamic-header' }, [
    h('div', { class: 'matrix-dynamic-header__code' }, indicator.ipCode),
    h('div', { class: 'matrix-dynamic-header__desc' }, indicator.ipDescription),
  ])
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

onMounted(() => {
  initializeDefaultMatrix()
})

onBeforeUnmount(() => {
  detachMatrixScrollSync()
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
              :label="formatCourseLabel(course)"
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

        <div v-else class="matrix-table-shell" @mouseleave="clearMatrixHover">
          <div class="matrix-table-meta">
            <span>固定列：课程</span>
            <span>毕业要求：{{ graduationRequirementCount }}</span>
            <span>指标点：{{ indicators.length }}</span>
            <span>课程：{{ courses.length }}</span>
          </div>

          <el-table
            ref="matrixTableRef"
            :data="visibleCourses"
            border
            max-height="620"
            :row-class-name="getMatrixRowClass"
            class="matrix-el-table"
            @header-dragend="handleHeaderDragEnd"
          >
            <el-table-column prop="courseName" label="课程 / 毕业要求" width="220" fixed="left" class-name="matrix-course-column">
              <template #default="{ row }">
                <div class="course-name-cell" @mouseenter="setMatrixHover(row.courseId)">{{ formatCourseLabel(row) }}</div>
              </template>
            </el-table-column>

            <el-table-column
              v-for="group in indicatorGroups"
              :key="`${group.grId}-${group.gradeYear}`"
              :label="group.grCode"
              :render-header="() => renderGroupHeader(group)"
              align="center"
            >
              <el-table-column
                v-for="indicator in group.indicators"
                :key="indicator.ipId"
                :label="indicator.ipCode"
                :render-header="() => renderIndicatorHeader(indicator)"
                min-width="178"
                align="center"
                :class-name="getIndicatorColumnClass(indicator.ipId)"
              >
                <template #default="{ row }">
                  <div
                    :class="getMatrixCellClass(row.courseId, indicator.ipId)"
                    @mouseenter="setMatrixHover(row.courseId, indicator.ipId)"
                  >
                    <el-checkbox
                      :model-value="isChecked(row.courseId, indicator.ipId)"
                      @change="(value) => onCheckChange(row.courseId, indicator.ipId, value)"
                    />
                    <el-input-number
                      :model-value="getWeight(row.courseId, indicator.ipId)"
                      :disabled="!isChecked(row.courseId, indicator.ipId)"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      class="weight-input"
                      @change="(value) => onWeightChange(row.courseId, indicator.ipId, value)"
                    />
                  </div>
                </template>
              </el-table-column>
            </el-table-column>
          </el-table>

          <div ref="summaryScrollRef" class="matrix-summary-scroll">
            <div
              class="matrix-summary-grid"
              :style="getSummaryGridStyle()"
            >
              <div class="matrix-summary-label">列权重合计</div>
              <div
                v-for="indicator in indicators"
                :key="indicator.ipId"
                :class="getSummaryCellClass(indicator.ipId)"
              >
                <span :class="isColumnValid(indicator.ipId) ? 'summary-valid' : 'summary-invalid'">
                  <span class="summary-value">{{ getColumnSum(indicator.ipId).toFixed(2) }}</span>
                  <span
                    :class="isColumnValid(indicator.ipId)
                      ? 'summary-icon summary-icon--valid'
                      : 'summary-icon summary-icon--invalid'"
                  />
                </span>
              </div>
            </div>
          </div>
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

.matrix-table-shell {
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.matrix-table-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  align-items: center;
  padding: 12px 16px;
  color: #6b7280;
  font-size: 13px;
  background: #f5f7fa;
  border-bottom: 1px solid #e5e7eb;
}

.matrix-el-table {
  width: 100%;
}

.matrix-el-table :deep(.el-table__header th) {
  color: #111827;
  background: #fff;
}

.matrix-el-table :deep(.el-table__header .is-group th) {
  background: #fff;
}

.matrix-el-table :deep(.el-table__body tr),
.matrix-el-table :deep(.el-table__body tr td),
.matrix-el-table :deep(.el-table__body tr.el-table__row--striped td) {
  background: #fff;
}

.matrix-el-table :deep(.el-table__body tr.matrix-row-hover > td) {
  background: #f8fbff !important;
}

.matrix-el-table :deep(.el-table__body td.matrix-column-hover) {
  background: #eef6ff !important;
}

.matrix-el-table :deep(.el-table__body tr.matrix-row-hover > td.matrix-column-hover) {
  background: #dbeafe !important;
}

.matrix-el-table :deep(.el-table__body td.matrix-course-column) {
  transition: background-color 0.12s ease;
}

.matrix-el-table :deep(.el-table__body td.matrix-column-hover),
.matrix-el-table :deep(.el-table__body tr.matrix-row-hover > td) {
  transition: background-color 0.12s ease;
}

.course-name-cell {
  color: #1f2937;
  font-weight: 500;
  line-height: 1.5;
}

.matrix-group-header {
  display: flex;
  min-height: 88px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  white-space: normal;
  line-height: 1.45;
}

.matrix-group-header__code {
  color: #111827;
  font-size: 18px;
  font-weight: 800;
}

.matrix-group-header__desc {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  word-break: break-word;
}

.matrix-dynamic-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-height: 78px;
  justify-content: center;
  white-space: normal;
  line-height: 1.45;
}

.matrix-dynamic-header__code {
  color: #1f2937;
  font-size: 18px;
  font-weight: 800;
}

.matrix-dynamic-header__desc {
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  word-break: break-word;
}

.matrix-edit-cell {
  display: flex;
  min-height: 72px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  transition: background-color 0.12s ease;
}

.matrix-edit-cell--active {
  background: #dbeafe;
}

.weight-input {
  width: 78px;
}

.matrix-summary-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  background: #f8fafc;
  border-top: 1px solid #dbe2ea;
  scrollbar-width: none;
}

.matrix-summary-scroll::-webkit-scrollbar {
  display: none;
}

.matrix-summary-grid {
  display: grid;
  width: max-content;
  min-height: 48px;
  color: #334155;
  background: #f8fafc;
}

.matrix-summary-label,
.matrix-summary-cell {
  display: flex;
  min-height: 48px;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e5e7eb;
  transition: background-color 0.12s ease;
}

.matrix-summary-label {
  position: sticky;
  left: 0;
  z-index: 2;
  color: #334155;
  font-weight: 800;
  background: #f8fafc;
}

.matrix-summary-cell--hover {
  background: #eef6ff;
}

.summary-valid,
.summary-invalid {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 18px;
  font-weight: 800;
}

.summary-valid {
  color: #16a34a;
}

.summary-invalid {
  color: #dc2626;
}

.summary-icon {
  position: relative;
  display: inline-flex;
  width: 14px;
  height: 14px;
  align-items: center;
  justify-content: center;
  border: 1.5px solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}

.summary-icon--valid {
  color: #22c55e;
}

.summary-icon--valid::after {
  position: absolute;
  width: 6px;
  height: 3px;
  border-bottom: 1.5px solid currentColor;
  border-left: 1.5px solid currentColor;
  content: '';
  transform: translateY(-1px) rotate(-45deg);
}

.summary-icon--invalid {
  color: #dc2626;
}

.summary-icon--invalid::before,
.summary-icon--invalid::after {
  position: absolute;
  width: 7px;
  height: 1.5px;
  background: currentColor;
  border-radius: 999px;
  content: '';
}

.summary-icon--invalid::before {
  transform: rotate(45deg);
}

.summary-icon--invalid::after {
  transform: rotate(-45deg);
}

.bottom-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
</style>
