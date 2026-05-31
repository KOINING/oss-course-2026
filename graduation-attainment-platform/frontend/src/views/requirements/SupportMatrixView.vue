<template>
  <div class="support-matrix-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础数据与宏观支撑配置</p>
            <h1>支撑矩阵配置</h1>
            <p class="page-summary">
              配置课程与毕业要求指标点之间的宏观支撑关系和支撑权重，确保专业级达成度汇总链路完整可追踪。
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
          说明：请为每门课程选择支撑的毕业要求指标点，并填写支撑权重；课程筛选仅影响当前展示行，
          每列权重之和仍按当前专业下全部课程计算，并且必须等于 1.00。
        </template>
      </el-alert>

      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty
          v-if="!tableLoading && indicators.length === 0"
          description="暂无指标点数据，请先选择专业并查询"
        />

        <div v-else class="matrix-scroll">
          <table class="matrix-table" border="0" cellspacing="0" cellpadding="0">
            <thead>
              <tr>
                <th class="th-corner" rowspan="2">
                  <div class="corner-inner">
                    <div class="corner-diagonal"></div>

                    <div class="corner-title corner-title-top">毕业要求</div>
                    <el-tooltip placement="top" :show-after="200">
                      <template #content>
                        <div class="corner-tip-content">
                          <span>共 {{ graduationRequirementCount }} 条毕业要求</span>
                          <span>共 {{ indicators.length }} 个指标点</span>
                        </div>
                      </template>
                      <button
                        type="button"
                        class="corner-info-trigger corner-info-trigger-top"
                        aria-label="毕业要求统计"
                      >
                        <el-icon><InfoFilled /></el-icon>
                      </button>
                    </el-tooltip>

                    <div class="corner-title corner-title-bottom">课程要求</div>
                    <el-tooltip placement="top" :show-after="200">
                      <template #content>
                        <div class="corner-tip-content">
                          <span>共 {{ courses.length }} 门</span>
                        </div>
                      </template>
                      <button
                        type="button"
                        class="corner-info-trigger corner-info-trigger-bottom"
                        aria-label="课程统计"
                      >
                        <el-icon><InfoFilled /></el-icon>
                      </button>
                    </el-tooltip>
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
              <tr v-for="course in visibleCourses" :key="course.courseId">
                <td class="td-course">{{ course.courseName }}</td>
                <td
                  v-for="indicator in indicators"
                  :key="indicator.ipId"
                  class="td-cell"
                >
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
                      style="width: 96px"
                      @change="(value) => onWeightChange(course.courseId, indicator.ipId, value)"
                    />
                  </div>
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
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存矩阵</el-button>
        <el-button @click="handleFormReset">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, InfoFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getSupportMatrixApi,
  listCoursesApi,
  listIndicatorPointsForMatrixApi,
  listMajorsForMatrixApi,
  saveSupportMatrixApi,
} from '@/api/supportmatrix.js'

const router = useRouter()

const filters = reactive({
  majorId: null,
  courseId: null,
})

const majorOptions = ref([])
const courseOptions = ref([])
const tableLoading = ref(false)
const saveLoading = ref(false)
const courses = ref([])
const indicators = ref([])
const matrixData = ref({})

let matrixSnapshot = {}

async function loadOptions() {
  majorOptions.value = await listMajorsForMatrixApi()
}

async function onMajorChange(majorId) {
  filters.courseId = null
  courseOptions.value = []

  if (!majorId) return

  courseOptions.value = await listCoursesApi({ majorId })
}

function resetFilters() {
  filters.majorId = null
  filters.courseId = null
  courseOptions.value = []
  courses.value = []
  indicators.value = []
  matrixData.value = {}
  matrixSnapshot = {}
}

const visibleCourses = computed(() => {
  if (!filters.courseId) {
    return courses.value
  }
  return courses.value.filter((course) => course.courseId === filters.courseId)
})

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

function cellKey(courseId, ipId) {
  return `${courseId}_${ipId}`
}

function isChecked(courseId, ipId) {
  return matrixData.value[cellKey(courseId, ipId)]?.checked ?? false
}

function getWeight(courseId, ipId) {
  return matrixData.value[cellKey(courseId, ipId)]?.weight ?? 0
}

function onCheckChange(courseId, ipId, value) {
  const key = cellKey(courseId, ipId)

  if (!matrixData.value[key]) {
    matrixData.value[key] = { checked: false, weight: 0 }
  }

  matrixData.value[key].checked = value
  if (!value) {
    matrixData.value[key].weight = 0
  }
}

function onWeightChange(courseId, ipId, value) {
  const key = cellKey(courseId, ipId)
  if (!matrixData.value[key]) return
  matrixData.value[key].weight = value ?? 0
}

function getColumnSum(ipId) {
  let total = 0
  courses.value.forEach((course) => {
    const cell = matrixData.value[cellKey(course.courseId, ipId)]
    if (cell?.checked) {
      total += cell.weight ?? 0
    }
  })
  return Math.round(total * 100) / 100
}

function isColumnValid(ipId) {
  return getColumnSum(ipId) === 1.0
}

function buildMatrixData(serverRows) {
  const data = {}

  courses.value.forEach((course) => {
    indicators.value.forEach((indicator) => {
      data[cellKey(course.courseId, indicator.ipId)] = { checked: false, weight: 0 }
    })
  })

  serverRows.forEach((row) => {
    const key = cellKey(row.courseId, row.ipId)
    if (data[key] !== undefined) {
      data[key] = { checked: true, weight: row.weight ?? 0 }
    }
  })

  return data
}

async function loadMatrix() {
  if (!filters.majorId) {
    ElMessage.warning('请先选择专业')
    return
  }

  tableLoading.value = true
  try {
    const [courseList, indicatorList, matrixRows] = await Promise.all([
      listCoursesApi({ majorId: filters.majorId }),
      listIndicatorPointsForMatrixApi({ majorId: filters.majorId }),
      getSupportMatrixApi({ majorId: filters.majorId }),
    ])

    courses.value = courseList
    indicators.value = indicatorList

    const built = buildMatrixData(matrixRows)
    matrixData.value = built
    matrixSnapshot = JSON.parse(JSON.stringify(built))
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

  const rows = []
  courses.value.forEach((course) => {
    indicators.value.forEach((indicator) => {
      const cell = matrixData.value[cellKey(course.courseId, indicator.ipId)]
      if (cell?.checked) {
        rows.push({
          courseId: course.courseId,
          ipId: indicator.ipId,
          weight: cell.weight,
        })
      }
    })
  })

  saveLoading.value = true
  try {
    await saveSupportMatrixApi({
      majorId: filters.majorId,
      rows,
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
.support-matrix-page {
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
  font-size: 14px;
  white-space: nowrap;
  min-width: max-content;
}

.matrix-table th,
.matrix-table td {
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.th-corner {
  min-width: 160px;
  background: #eef2ff;
  position: sticky;
  top: 0;
  left: 0;
  z-index: 10;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.1);
  transform: translateZ(0);
}

.corner-inner {
  position: relative;
  min-height: 148px;
  padding: 0;
  background: linear-gradient(180deg, #eef2ff 0%, #edf2ff 100%);
}

.corner-diagonal {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    36deg,
    transparent calc(50% - 1px),
    #64748b calc(50% - 1px),
    #64748b calc(50% + 1px),
    transparent calc(50% + 1px)
  );
  pointer-events: none;
}

.corner-title {
  position: absolute;
  z-index: 1;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.02em;
}

.corner-title-top {
  top: 18px;
  right: 18px;
  font-size: 18px;
}

.corner-title-bottom {
  left: 18px;
  bottom: 24px;
  font-size: 18px;
}

.corner-tip-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.5;
}

.corner-info-trigger {
  position: absolute;
  z-index: 1;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  background: transparent;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.corner-info-trigger:hover {
  color: #2563eb;
}

.corner-info-trigger-top {
  top: 14px;
  right: 0;
}

.corner-info-trigger-bottom {
  left: 0;
  bottom: 32px;
}

.th-gr-group {
  position: sticky;
  top: 0;
  z-index: 8;
  text-align: center;
  padding: 8px 18px;
  height: 96px;
  background: #e8edff;
  font-weight: 700;
  font-size: 13px;
  color: #1d4ed8;
  border-left: 2px solid #c7d7ff;
  letter-spacing: 0.02em;
  vertical-align: top;
  border-top: none;
  border-bottom: 1px solid #d6def7;
}

.th-gr-group:first-of-type {
  border-left: none;
}

.gr-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px dashed #dbe4ff;
  text-align: center;
}

.gr-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  white-space: normal;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: default;
  min-height: 36px;
  text-align: center;
  font-weight: 400;
}

.th-indicator {
  position: sticky;
  top: 96px;
  z-index: 7;
  min-width: 220px;
  padding: 10px 18px 12px;
  background: #f8f9ff;
  vertical-align: top;
  text-align: center;
  border-top: none;
}

.indicator-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 13px;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px dashed #dbe4ff;
  text-align: center;
}

.indicator-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  white-space: normal;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: default;
  min-height: 36px;
  text-align: center;
}

.td-course {
  padding: 0 24px;
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
  background: #fff;
  position: sticky;
  left: 0;
  z-index: 2;
  white-space: nowrap;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.08);
  height: 60px;
  vertical-align: middle;
  text-align: center;
  min-width: 160px;
}

.td-cell {
  padding: 12px 16px;
  text-align: center;
  background: #fff;
  vertical-align: middle;
  position: relative;
  z-index: 0;
  overflow: hidden;
}

.cell-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.tr-sum-spacer td {
  height: 62px;
  padding: 0;
  border: none;
  background: transparent;
}

.tr-sum td {
  position: sticky;
  bottom: 0;
  z-index: 4;
  background: #f0f4ff !important;
  font-weight: 700;
  border-top: 2px solid #dbe4ff;
  box-shadow: 0 -6px 16px rgba(15, 23, 42, 0.08);
}

.td-sum-label {
  padding: 0 24px;
  height: 60px;
  vertical-align: middle;
  color: #374151;
  font-size: 13px;
  position: sticky;
  bottom: 0;
  left: 0;
  z-index: 5;
  white-space: nowrap;
  background: #e8edff !important;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.td-sum {
  padding: 0 16px;
  height: 60px;
  vertical-align: middle;
  text-align: center;
}

.sum-inner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 700;
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
