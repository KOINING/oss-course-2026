<template>
  <div class="course-weight-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：课程内部权重配置</p>
            <h1>课程内部权重配置</h1>
            <p class="page-summary">
              为当前课程的各课程目标配置对毕业要求指标点的内部支撑权重，每个指标点列下所有课程目标的权重之和须等于 1.00。
            </p>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="专业">
          <el-select
              v-model="filters.majorId"
              placeholder="请选择专业"
              clearable
              style="width: 200px"
              @change="onMajorChange"
          >
            <el-option
                v-for="major in majorOptions"
                :key="major.majorId"
                :label="major.majorName"
                :value="major.majorId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="年级">
          <el-select
              v-model="filters.gradeYear"
              placeholder="请选择年级"
              clearable
              style="width: 160px"
              @change="onGradeChange"
          >
            <el-option
                v-for="grade in gradeOptions"
                :key="grade.value"
                :label="grade.label"
                :value="grade.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="课程">
          <el-select
              v-model="filters.courseId"
              placeholder="请选择课程"
              clearable
              style="width: 200px"
          >
            <el-option
                v-for="course in courseOptions"
                :key="course.courseId"
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
        <template #default>
          说明：灰色单元格表示该课程目标与指标点之间无支撑关系，不可录入权重；每列权重之和须等于 1.00 方可保存。
        </template>
      </el-alert>

      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty
            v-if="!tableLoading && indicators.length === 0"
            description="暂无数据，请先选择专业、年级和课程并查询"
        />

        <div v-else class="matrix-scroll">
          <table class="matrix-table" border="0" cellspacing="0" cellpadding="0">
            <thead>
            <tr>
              <th class="th-corner" rowspan="2">
                <div class="corner-inner">
                  <div class="corner-title-main">课程目标 / 指标点</div>
                  <div class="corner-stats">
                    <span>毕业要求: {{ graduationRequirementCount }}</span>
                    <span class="corner-stat-sep">|</span>
                    <span>指标点: {{ indicators.length }}</span>
                    <span class="corner-stat-sep">|</span>
                    <span>课程目标: {{ courseObjectives.length }}</span>
                  </div>
                </div>
              </th>

              <th
                  v-for="group in indicatorGroups"
                  :key="group.grId"
                  :colspan="group.indicators.length"
                  class="th-gr-group"
              >
                <div class="gr-name">{{ group.grCode }}</div>
                <el-tooltip :content="group.grDescription" placement="top" :show-after="300">
                  <div class="gr-desc">{{ group.grDescription }}</div>
                </el-tooltip>
              </th>
            </tr>

            <tr>
              <th
                  v-for="indicator in indicators"
                  :key="indicator.ipId"
                  class="th-indicator"
              >
                <div class="indicator-name">{{ indicator.ipCode }}</div>
                <el-tooltip :content="indicator.ipDescription" placement="top" :show-after="300">
                  <div class="indicator-desc">{{ indicator.ipDescription }}</div>
                </el-tooltip>
              </th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="obj in courseObjectives" :key="obj.coId">
              <td class="td-objective">
                <div class="objective-code">{{ obj.objectiveCode }}</div>
                <div class="objective-desc">{{ obj.description }}</div>
              </td>
              <td
                  v-for="indicator in indicators"
                  :key="indicator.ipId"
                  class="td-cell"
                  :class="{ 'td-cell-disabled': !isSupported(obj.coId, indicator.ipId) }"
              >
                <div v-if="isSupported(obj.coId, indicator.ipId)" class="cell-inner">
                  <el-input-number
                      :model-value="getWeight(obj.coId, indicator.ipId)"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      style="width: 96px"
                      @change="(value) => onWeightChange(obj.coId, indicator.ipId, value)"
                  />
                </div>
                <div v-else class="cell-disabled-inner">—</div>
              </td>
            </tr>

            <tr class="tr-sum-spacer" aria-hidden="true">
              <td :colspan="indicators.length + 1"></td>
            </tr>

            <tr class="tr-sum">
              <td class="td-sum-label">列权重合计（Σ）</td>
              <td
                  v-for="indicator in indicators"
                  :key="indicator.ipId"
                  class="td-sum"
              >
                <div class="sum-inner">
                    <span :class="isColumnValid(indicator.ipId) ? 'sum-valid' : 'sum-invalid'">
                      {{ getColumnSum(indicator.ipId).toFixed(2) }}
                    </span>
                  <el-icon v-if="isColumnValid(indicator.ipId)" color="#67c23a" size="15">
                    <CircleCheck />
                  </el-icon>
                  <el-icon v-else color="#f56c6c" size="15">
                    <CircleClose />
                  </el-icon>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bottom-actions">
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存配置</el-button>
        <el-button @click="handleFormReset">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import {
  getCourseWeightApi,
  listCourseObjectivesApi,
  listCoursesForWeightApi,
  listGradesApi,
  listIndicatorPointsForWeightApi,
  listMajorsForWeightApi,
  saveCourseWeightApi,
} from '@/api/courseWeight.js'

const filters = reactive({
  majorId: null,
  gradeYear: null,
  courseId: null,
})

const majorOptions = ref([])
const gradeOptions = ref([])
const courseOptions = ref([])
const tableLoading = ref(false)
const saveLoading = ref(false)

const courseObjectives = ref([])
const indicators = ref([])
const supportSet = ref(new Set())
const matrixData = ref({})

let matrixSnapshot = {}

async function loadOptions() {
  majorOptions.value = await listMajorsForWeightApi()
  const gradeYears = await listGradesApi()
  gradeOptions.value = gradeYears.map((year) => ({
    value: String(year),
    label: year + '级',
  }))
}

async function onMajorChange(majorId) {
  filters.gradeYear = null
  filters.courseId = null
  courseOptions.value = []
  if (!majorId) return
}

async function onGradeChange() {
  filters.courseId = null
  courseOptions.value = []
  if (!filters.majorId || !filters.gradeYear) return
  courseOptions.value = await listCoursesForWeightApi({
    majorId: filters.majorId,
    gradeYear: Number(filters.gradeYear),
  })
}

function resetFilters() {
  filters.majorId = null
  filters.gradeYear = null
  filters.courseId = null
  courseOptions.value = []
  courseObjectives.value = []
  indicators.value = []
  supportSet.value = new Set()
  matrixData.value = {}
  matrixSnapshot = {}
}

const indicatorGroups = computed(() => {
  const map = new Map()
  indicators.value.forEach((indicator) => {
    const key = indicator.grId ?? 'other'
    if (!map.has(key)) {
      map.set(key, {
        grId: indicator.grId,
        grCode: indicator.grCode ?? '其他',
        grDescription: indicator.grDescription ?? '',
        indicators: [],
      })
    }
    map.get(key).indicators.push(indicator)
  })
  return Array.from(map.values())
})

const graduationRequirementCount = computed(() => indicatorGroups.value.length)

function cellKey(coId, ipId) {
  return `${coId}_${ipId}`
}

function isSupported(coId, ipId) {
  return supportSet.value.has(cellKey(coId, ipId))
}

function getWeight(coId, ipId) {
  return matrixData.value[cellKey(coId, ipId)] ?? 0
}

function onWeightChange(coId, ipId, value) {
  matrixData.value[cellKey(coId, ipId)] = value ?? 0
}

function getColumnSum(ipId) {
  let total = 0
  courseObjectives.value.forEach((obj) => {
    if (isSupported(obj.coId, ipId)) {
      total += matrixData.value[cellKey(obj.coId, ipId)] ?? 0
    }
  })
  return Math.round(total * 100) / 100
}

function isColumnValid(ipId) {
  const hasAny = courseObjectives.value.some((obj) => isSupported(obj.coId, ipId))
  if (!hasAny) return true
  return getColumnSum(ipId) === 1.0
}

function buildMatrixData(serverRows) {
  const data = {}
  const supported = new Set()

  serverRows.forEach((row) => {
    const key = cellKey(row.coId, row.ipId)
    supported.add(key)
    data[key] = row.internalWeight ?? 0
  })

  return { data, supported }
}

async function loadMatrix() {
  if (!filters.majorId) {
    ElMessage.warning('请先选择专业')
    return
  }
  if (!filters.gradeYear) {
    ElMessage.warning('请先选择年级')
    return
  }
  if (!filters.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  tableLoading.value = true
  try {
    const [objectiveList, indicatorList, weightRows] = await Promise.all([
      listCourseObjectivesApi({ courseId: filters.courseId }),
      listIndicatorPointsForWeightApi({
        majorId: filters.majorId,
        gradeYear: Number(filters.gradeYear),
      }),
      getCourseWeightApi({
        courseId: filters.courseId,
        gradeYear: Number(filters.gradeYear),
      }),
    ])

    courseObjectives.value = objectiveList
    indicators.value = indicatorList

    const { data, supported } = buildMatrixData(weightRows)
    matrixData.value = data
    supportSet.value = supported
    matrixSnapshot = JSON.parse(JSON.stringify(data))
  } finally {
    tableLoading.value = false
  }
}

async function handleSave() {
  const invalidColumns = indicators.value.filter((indicator) => !isColumnValid(indicator.ipId))
  if (invalidColumns.length) {
    ElMessage.warning(
        `以下指标点列权重之和不等于 1.00：${invalidColumns.map((item) => item.ipCode).join('、')}`,
    )
    return
  }

  const contributions = []
  courseObjectives.value.forEach((obj) => {
    indicators.value.forEach((indicator) => {
      if (isSupported(obj.coId, indicator.ipId)) {
        contributions.push({
          coId: obj.coId,
          ipId: indicator.ipId,
          internalWeight: matrixData.value[cellKey(obj.coId, indicator.ipId)] ?? 0,
        })
      }
    })
  })

  saveLoading.value = true
  try {
    await saveCourseWeightApi({
      courseId: filters.courseId,
      gradeYear: Number(filters.gradeYear),
      contributions,
    })
    ElMessage.success('保存成功')
    matrixSnapshot = JSON.parse(JSON.stringify(matrixData.value))
  } finally {
    saveLoading.value = false
  }
}

function handleFormReset() {
  if (!Object.keys(matrixSnapshot).length) return
  matrixData.value = JSON.parse(JSON.stringify(matrixSnapshot))
  ElMessage.info('已重置为上次保存状态')
}

onMounted(async () => {
  await loadOptions()
})
</script>

<style scoped>
.course-weight-page {
  padding: 20px;
}

.page-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
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
  margin-bottom: 16px;
}

.notice-alert {
  margin-bottom: 24px;
}

.matrix-wrap {
  min-height: 160px;
  margin-bottom: 28px;
}

.matrix-scroll {
  overflow-x: auto;
  overflow-y: auto;
  max-height: 860px;
  width: 100%;
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.matrix-scroll::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.matrix-scroll::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

.matrix-scroll::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.matrix-scroll::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.matrix-scroll::-webkit-scrollbar-corner {
  background: #f5f7fa;
}

.matrix-table {
  border-collapse: separate;
  border-spacing: 0;
  font-size: 15px;
  table-layout: fixed;
  width: 100%;
}

.matrix-table th,
.matrix-table td {
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.th-corner {
  width: 220px;
  min-width: 180px;
  background: #fff;
  position: sticky;
  top: 0;
  left: 0;
  z-index: 10;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.1);
  transform: translateZ(0);
}

.corner-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 14px;
  background: #fff;
}

.corner-title-main {
  font-weight: 700;
  font-size: 16px;
  color: #1f2937;
  text-align: center;
  margin-bottom: 10px;
  letter-spacing: 0.02em;
}

.corner-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.corner-stat-sep {
  color: #c0c4cc;
}

.th-gr-group {
  position: sticky;
  top: 0;
  z-index: 8;
  text-align: center;
  padding: 14px 18px 14px;
  background: #fff;
  font-weight: 700;
  font-size: 14px;
  color: #1d4ed8;
  border-left: 2px solid #e4e7ed;
  letter-spacing: 0.02em;
  vertical-align: top;
  border-top: none;
  border-bottom: 1px solid #e4e7ed;
  white-space: normal;
}

.th-gr-group:first-of-type {
  border-left: none;
}

.gr-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 15px;
  margin-bottom: 6px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #d4d8e0;
  text-align: center;
}

.gr-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  cursor: default;
  min-height: 18px;
  text-align: center;
  font-weight: 400;
}

.th-indicator {
  position: sticky;
  top: auto;
  z-index: 7;
  padding: 12px 16px 12px;
  background: #fff;
  vertical-align: top;
  text-align: center;
  border-top: none;
  white-space: normal;
  word-break: break-word;
}

.indicator-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 8px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #d4d8e0;
  text-align: center;
}

.indicator-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  white-space: normal !important;
  word-break: break-word;
  overflow-wrap: anywhere;
  display: block;
  overflow: visible;
  cursor: default;
  min-height: 0;
  text-align: center;
  max-width: 100%;
}

.td-objective {
  padding: 14px 20px;
  background: #fff;
  position: sticky;
  left: 0;
  z-index: 2;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.08);
  vertical-align: middle;
  min-width: 180px;
  max-width: 220px;
  border-right: 1px solid #e4e7ed;
}

.objective-code {
  font-weight: 600;
  font-size: 14px;
  color: #1d4ed8;
  margin-bottom: 6px;
}

.objective-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.td-cell {
  padding: 16px 20px;
  text-align: center;
  background: #fff;
  vertical-align: middle;
  position: relative;
  z-index: 0;
}

.td-cell-disabled {
  background: #f5f7fa;
}

.cell-inner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cell-disabled-inner {
  color: #c0c4cc;
  font-size: 18px;
  line-height: 32px;
}

.tr-sum-spacer td {
  height: 0;
  padding: 0;
  border: none;
  background: transparent;
}

.tr-sum td {
  position: sticky;
  bottom: 0;
  z-index: 4;
  background: #fff !important;
  font-weight: 700;
  border-top: 2px solid #e4e7ed;
  box-shadow: 0 -4px 12px rgba(15, 23, 42, 0.06);
}

.td-sum-label {
  padding: 24px 24px;
  vertical-align: middle;
  color: #374151;
  font-size: 13px;
  position: sticky;
  bottom: 0;
  left: 0;
  z-index: 5;
  white-space: nowrap;
  background: #fff !important;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.td-sum {
  padding: 24px 16px;
  vertical-align: middle;
  text-align: center;
}

.sum-inner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 700;
  min-height: 24px;
}

.sum-valid {
  color: #15803d;
}

.sum-invalid {
  color: #dc2626;
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 12px;
}
</style>
