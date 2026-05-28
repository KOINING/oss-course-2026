<template>
  <div class="support-matrix-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础与宏观数据管理</p>
            <h1>支撑矩阵配置</h1>
            <p class="page-summary">
              配置课程与指标点之间的宏观支撑关系及总支撑权重，保证专业级达成度汇总链路完整可追踪。
            </p>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
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
                v-for="m in majorOptions"
                :key="m.majorId"
                :label="m.majorName"
                :value="m.majorId"
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
                v-for="c in courseOptions"
                :key="c.courseId"
                :label="c.courseName"
                :value="c.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学年学期">
          <el-select
              v-model="filters.termId"
              placeholder="请选择学年学期"
              style="width: 220px"
          >
            <el-option
                v-for="t in termOptions"
                :key="t.termId"
                :label="t.termName"
                :value="t.termId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="tableLoading" @click="loadMatrix">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 说明提示 -->
      <el-alert
          type="info"
          :closable="false"
          show-icon
          class="notice-alert"
      >
        <template #default>
          说明：请为每个课程目标选择支撑的毕业要求指标点，并填写支撑权重，每列权重之和须等于 1.00。
        </template>
      </el-alert>

      <!-- 矩阵表格 -->
      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty v-if="!tableLoading && indicators.length === 0" description="暂无指标点数据，请先选择专业并查询" />

        <div v-else class="matrix-scroll">
          <table class="matrix-table" border="0" cellspacing="0" cellpadding="0">
            <thead>
            <!-- 第一行：左上角（rowspan=3）+ 毕业要求指标点总标题 -->
            <tr>
              <th class="th-corner" rowspan="3">
                <div class="corner-inner">
                  <div class="corner-label">
                    <span>课程</span>
                    <span>共 {{ courses.length }} 门</span>
                  </div>
                </div>
              </th>
              <th :colspan="indicators.length" class="th-group">
                毕业要求指标点（共 {{ indicators.length }} 个）
              </th>
            </tr>
            <!-- 第二行：毕业要求分组，按 grCode 合并列 -->
            <tr>
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
            <!-- 第三行：各指标点 -->
            <tr>
              <th
                  v-for="ind in indicators"
                  :key="ind.ipId"
                  class="th-indicator"
              >
                <div class="indicator-name">{{ ind.ipCode }}</div>
                <el-tooltip :content="ind.ipDescription" placement="top" :show-after="300">
                  <div class="indicator-desc">{{ ind.ipDescription }}</div>
                </el-tooltip>
              </th>
            </tr>
            </thead>

            <tbody>
            <!-- 每门课程一行 -->
            <tr v-for="course in courses" :key="course.courseId">
              <td class="td-course">{{ course.courseName }}</td>
              <td
                  v-for="ind in indicators"
                  :key="ind.ipId"
                  class="td-cell"
              >
                <div class="cell-inner">
                  <el-checkbox
                      :model-value="isChecked(course.courseId, ind.ipId)"
                      @change="(val) => onCheckChange(course.courseId, ind.ipId, val)"
                  />
                  <el-input-number
                      :model-value="getWeight(course.courseId, ind.ipId)"
                      :disabled="!isChecked(course.courseId, ind.ipId)"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      style="width: 96px"
                      @change="(val) => onWeightChange(course.courseId, ind.ipId, val)"
                  />
                </div>
              </td>
            </tr>

            <!-- 列权重合计行 -->
            <tr class="tr-sum">
              <td class="td-sum-label">列权重合计（Σ）</td>
              <td
                  v-for="ind in indicators"
                  :key="ind.ipId"
                  class="td-sum"
              >
                <div class="sum-inner">
                    <span :class="isColumnValid(ind.ipId) ? 'sum-valid' : 'sum-invalid'">
                      {{ getColumnSum(ind.ipId).toFixed(2) }}
                    </span>
                  <el-icon v-if="isColumnValid(ind.ipId)" color="#67c23a" size="15">
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

      <!-- 底部操作 -->
      <div class="bottom-actions">
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存矩阵</el-button>
        <el-button @click="handleFormReset">重置</el-button>
        <el-button @click="router.go(-1)">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getSupportMatrixApi,
  listAcademicTermsApi,
  listCoursesApi,
  listIndicatorPointsForMatrixApi,
  listMajorsForMatrixApi,
  saveSupportMatrixApi,
} from '@/api/supportMatrix'

const router = useRouter()

// ========== 筛选 ==========
const filters = reactive({
  majorId: null,
  courseId: null,
  termId: null,
})

const majorOptions = ref([])
const courseOptions = ref([])
const termOptions = ref([])

async function loadOptions() {
  const [majors, terms] = await Promise.all([listMajorsForMatrixApi(), listAcademicTermsApi()])
  majorOptions.value = majors
  termOptions.value = terms
  if (terms.length) filters.termId = terms[0].termId
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
  filters.termId = termOptions.value[0]?.termId ?? null
  courseOptions.value = []
  courses.value = []
  indicators.value = []
  matrixData.value = {}
}

// ========== 矩阵数据 ==========
const tableLoading = ref(false)
const saveLoading = ref(false)

const courses = ref([])       // [{ courseId, courseName }]
const indicators = ref([])    // [{ ipId, ipCode, ipDescription }]
// matrixData: { `${courseId}_${ipId}`: { checked: bool, weight: number } }
const matrixData = ref({})
// 初始快照，用于重置
let matrixSnapshot = {}

// 按毕业要求分组指标点，用于表头渲染
// 返回 [{ grId, grCode, indicators: [...] }, ...]
const indicatorGroups = computed(() => {
  const map = new Map()
  indicators.value.forEach((ind) => {
    const key = ind.grId ?? 'other'
    if (!map.has(key)) {
      map.set(key, { grId: ind.grId, grCode: ind.grCode ?? '其他', grDescription: ind.grDescription ?? '', indicators: [] })
    }
    map.get(key).indicators.push(ind)
  })
  return Array.from(map.values())
})

function cellKey(courseId, ipId) {
  return `${courseId}_${ipId}`
}

function isChecked(courseId, ipId) {
  return matrixData.value[cellKey(courseId, ipId)]?.checked ?? false
}

function getWeight(courseId, ipId) {
  return matrixData.value[cellKey(courseId, ipId)]?.weight ?? 0
}

function onCheckChange(courseId, ipId, val) {
  const k = cellKey(courseId, ipId)
  if (!matrixData.value[k]) {
    matrixData.value[k] = { checked: false, weight: 0 }
  }
  matrixData.value[k].checked = val
  if (!val) matrixData.value[k].weight = 0
}

function onWeightChange(courseId, ipId, val) {
  const k = cellKey(courseId, ipId)
  if (!matrixData.value[k]) return
  matrixData.value[k].weight = val ?? 0
}

function getColumnSum(ipId) {
  let total = 0
  courses.value.forEach((c) => {
    const cell = matrixData.value[cellKey(c.courseId, ipId)]
    if (cell?.checked) total += cell.weight ?? 0
  })
  return Math.round(total * 100) / 100
}

function isColumnValid(ipId) {
  return getColumnSum(ipId) === 1.0
}

// 从后端数据构建 matrixData
function buildMatrixData(serverRows) {
  // serverRows: [{ courseId, ipId, weight }]
  const data = {}
  courses.value.forEach((c) => {
    indicators.value.forEach((ind) => {
      data[cellKey(c.courseId, ind.ipId)] = { checked: false, weight: 0 }
    })
  })
  serverRows.forEach((row) => {
    const k = cellKey(row.courseId, row.ipId)
    if (data[k] !== undefined) {
      data[k] = { checked: true, weight: row.weight ?? 0 }
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
      getSupportMatrixApi({ majorId: filters.majorId, termId: filters.termId }),
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

// ========== 保存 / 重置 ==========
async function handleSave() {
  const invalidCols = indicators.value.filter((ind) => !isColumnValid(ind.ipId))
  if (invalidCols.length) {
    ElMessage.warning(
        `以下指标点列权重之和不等于 1.00：${invalidCols.map((i) => i.ipCode).join('、')}`,
    )
    return
  }

  // 整理成后端需要的格式：只提交 checked=true 的单元格
  const rows = []
  courses.value.forEach((c) => {
    indicators.value.forEach((ind) => {
      const cell = matrixData.value[cellKey(c.courseId, ind.ipId)]
      if (cell?.checked) {
        rows.push({ courseId: c.courseId, ipId: ind.ipId, weight: cell.weight })
      }
    })
  })

  saveLoading.value = true
  try {
    await saveSupportMatrixApi({
      majorId: filters.majorId,
      termId: filters.termId,
      rows,
    })
    ElMessage.success('保存成功')
    matrixSnapshot = JSON.parse(JSON.stringify(matrixData.value))
  } finally {
    saveLoading.value = false
  }
}

async function handleFormReset() {
  if (!Object.keys(matrixSnapshot).length) return
  matrixData.value = JSON.parse(JSON.stringify(matrixSnapshot))
  ElMessage.info('已重置为上次保存状态')
}

// ========== 初始化 ==========
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

/* 筛选栏 */
.filter-form {
  margin-bottom: 16px;
}

/* 提示 */
.notice-alert {
  margin-bottom: 24px;
}

/* 矩阵容器 */
.matrix-wrap {
  min-height: 160px;
  margin-bottom: 28px;
}

/* 滚动容器：overflow-x + 确定宽度，sticky 才生效 */
.matrix-scroll {
  overflow-x: auto;
  overflow-y: auto;
  max-height: 560px;
  width: 100%;
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 自定义滚动条样式（横向 + 纵向） */
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

/* 矩阵表格 */
.matrix-table {
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
  white-space: nowrap;
}

.matrix-table thead th {
  position: sticky;
  top: 0;
  z-index: 3;
}

/* 毕业要求分组行（第二行）—— 低于 corner，高于 body */
.th-gr-group {
  z-index: 2 !important;
}

/* 指标点列头（第三行）—— 低于 corner，高于 body */
.th-indicator {
  z-index: 2 !important;
}

/* 第一行合并表头 —— 低于 corner */
.th-group {
  z-index: 3 !important;
}

.matrix-table th,
.matrix-table td {
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

/* ===== 左上角：横向纵向双固定，层级最高 ===== */
.th-corner {
  min-width: 160px;
  background: #eef2ff;
  position: sticky;
  top: 0;
  left: 0;
  z-index: 10;
  box-shadow: 3px 0 8px -2px rgba(0, 0, 0, 0.10);
  transform: translateZ(0);
}

.corner-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 20px;
  gap: 6px;
  min-height: 140px;
}

.corner-label {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  text-align: center;
  width: 100%;
}

.corner-label span {
  display: block;
}

/* ===== 合并表头（第一行）===== */
.th-group {
  text-align: center;
  padding: 14px 20px;
  background: #eef2ff;
  font-weight: 700;
  font-size: 14px;
  color: #2563eb;
  letter-spacing: 0.02em;
}

/* ===== 毕业要求分组行（第二行）===== */
.th-gr-group {
  text-align: center;
  padding: 10px 20px;
  background: #e8edff;
  font-weight: 700;
  font-size: 13px;
  color: #1d4ed8;
  border-left: 2px solid #c7d7ff;
  letter-spacing: 0.02em;
  vertical-align: top;
}
.th-gr-group:first-of-type {
  border-left: none;
}

.gr-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #dbe4ff;
  text-align: center;
}

.gr-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.65;
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

/* ===== 指标点列头（第三行）===== */
.th-indicator {
  min-width: 220px;
  padding: 14px 20px 16px;
  background: #f8f9ff;
  vertical-align: top;
  text-align: center;
  border-top: 2px solid #dbe4ff;
}

.indicator-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 13px;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #dbe4ff;
  text-align: center;
}

.indicator-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.65;
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

/* ===== 课程名列（左侧固定）===== */
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

/* 数据行斑马纹 */
.matrix-table tbody tr:nth-child(even) .td-course,
.matrix-table tbody tr:nth-child(even) .td-cell {
  background: #fafbff;
}

.matrix-table tbody tr:hover .td-course,
.matrix-table tbody tr:hover .td-cell {
  background: #f0f4ff;
  transition: background 0.15s;
}

/* ===== 数据单元格 ===== */
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

/* ===== 列权重合计行 ===== */
.tr-sum td {
  background: #f0f4ff !important;
  font-weight: 700;
  border-top: 2px solid #dbe4ff;
}

.td-sum-label {
  padding: 0 24px;
  height: 60px;
  vertical-align: middle;
  color: #374151;
  font-size: 13px;
  position: sticky;
  left: 0;
  z-index: 2;
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

/* ===== 底部操作 ===== */
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 12px;
}
</style>