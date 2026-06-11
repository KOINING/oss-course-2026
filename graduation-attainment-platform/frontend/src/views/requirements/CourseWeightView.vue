<template>
  <div class="course-weight-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：内部权重配置</p>
            <h1>课程目标对指标点的内部权重</h1>
            <p class="page-summary">
              根据《软件需求规格说明书》B-2，教师端只展示当前课程在当前专业、年级版本下负责支撑的指标点，
              并配置课程目标对这些指标点的内部贡献权重。
            </p>
          </div>
        </div>
      </template>

      <section class="context-section">
        <el-form :inline="true" :model="filters">
          <el-form-item label="课程">
            <el-select
              v-model="filters.courseId"
              placeholder="请选择课程"
              style="width: 240px"
              @change="handleCourseChange"
            >
              <el-option
                v-for="course in courses"
                :key="course.courseId"
                :label="`${course.courseCode} - ${course.courseName}`"
                :value="course.courseId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教学班">
            <el-select
              v-model="filters.teachingClassId"
              placeholder="请选择教学班"
              style="width: 260px"
              :disabled="!filters.courseId"
              @change="handleTeachingClassChange"
            >
              <el-option
                v-for="item in teachingClasses"
                :key="item.classId"
                :label="`${item.classCode} - ${item.className || item.courseName}`"
                :value="item.classId"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :disabled="!canQuery" :loading="tableLoading" @click="loadMatrix">
              刷新
            </el-button>
            <el-button @click="resetAll">重置</el-button>
          </el-form-item>
        </el-form>

        <el-descriptions v-if="context" :column="5" border class="context-panel">
          <el-descriptions-item label="专业">{{ context.majorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ context.gradeYear || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ context.courseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="教学班">{{ context.classCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ context.termCode || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="contextWarning"
          :title="contextWarning"
          type="warning"
          :closable="false"
          class="context-alert"
        />
      </section>

      <el-alert type="info" :closable="false" show-icon class="notice-alert">
        <template #default>
          页面只展示当前课程负责支撑的指标点。每个指标点列下所有课程目标的权重和必须等于 1.00；
          未参与该指标点的课程目标保持 0 即可，保存时不会提交 0 值单元格。
        </template>
      </el-alert>

      <div v-loading="tableLoading" class="matrix-wrap">
        <el-empty
          v-if="!canQuery"
          description="请先选择课程和教学班"
        />

        <el-empty
          v-else-if="!tableLoading && courseObjectives.length === 0"
          description="当前课程暂无课程目标，请先到课程目标页完成配置"
        />

        <el-empty
          v-else-if="!tableLoading && indicators.length === 0"
          description="当前课程在该专业和年级下没有可配置的支撑指标点，请先检查宏观支撑矩阵"
        />

        <div v-else class="matrix-scroll">
          <table class="matrix-table" cellspacing="0" cellpadding="0">
            <thead>
              <tr>
                <th class="th-corner" rowspan="2">
                  <div class="corner-inner">
                    <div class="corner-title-main">课程目标 / 指标点</div>
                    <div class="corner-stats">
                      <span>课程目标 {{ courseObjectives.length }}</span>
                      <span class="corner-stat-sep">|</span>
                      <span>指标点 {{ indicators.length }}</span>
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
                  <div class="gr-desc">{{ group.grDescription || '-' }}</div>
                </th>
              </tr>
              <tr>
                <th v-for="indicator in indicators" :key="indicator.ipId" class="th-indicator">
                  <div class="indicator-name">{{ indicator.ipCode }}</div>
                  <div class="indicator-desc">{{ indicator.ipDescription || '-' }}</div>
                </th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="objective in courseObjectives" :key="objective.coId">
                <td class="td-objective">
                  <div class="objective-code">{{ objective.objectiveCode }}</div>
                  <div class="objective-desc">{{ objective.description }}</div>
                </td>

                <td
                  v-for="indicator in indicators"
                  :key="indicator.ipId"
                  class="td-cell"
                  :class="{ 'td-cell-disabled': !isSupported(objective.coId, indicator.ipId) }"
                >
                  <div v-if="isSupported(objective.coId, indicator.ipId)" class="cell-inner">
                    <el-input-number
                      :model-value="getWeight(objective.coId, indicator.ipId)"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      controls-position="right"
                      size="small"
                      style="width: 110px"
                      @change="(value) => onWeightChange(objective.coId, indicator.ipId, value)"
                    />
                  </div>
                  <div v-else class="cell-disabled-inner">-</div>
                </td>
              </tr>

              <tr class="tr-sum">
                <td class="td-sum-label">列权重合计</td>
                <td v-for="indicator in indicators" :key="indicator.ipId" class="td-sum">
                  <div class="sum-inner">
                    <span :class="isColumnValid(indicator.ipId) ? 'sum-valid' : 'sum-invalid'">
                      {{ getColumnSum(indicator.ipId).toFixed(2) }}
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bottom-actions">
        <el-button type="primary" :loading="saveLoading" :disabled="!canSave" @click="handleSave">保存配置</el-button>
        <el-button :disabled="!hasSnapshot" @click="resetMatrix">恢复上次加载结果</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getContextApi,
  listCourseObjectivesApi,
  listCoursesForInstructorApi,
  listTeachingClassesApi,
} from '@/api/courseObjectives'
import { getCourseWeightApi, saveCourseWeightApi } from '@/api/courseWeight'

const route = useRoute()
const router = useRouter()

const filters = reactive({
  courseId: null,
  teachingClassId: null,
})

const courses = ref([])
const teachingClasses = ref([])
const context = ref(null)
const contextWarning = ref('')

const tableLoading = ref(false)
const saveLoading = ref(false)
const courseObjectives = ref([])
const indicators = ref([])
const supportSet = ref(new Set())
const matrixData = ref({})
let matrixSnapshot = {}

const canQuery = computed(() => Boolean(filters.courseId && filters.teachingClassId && context.value?.gradeYear))
const hasSnapshot = computed(() => Object.keys(matrixSnapshot).length > 0)
const canSave = computed(() => canQuery.value && courseObjectives.value.length > 0 && indicators.value.length > 0)

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
  courseObjectives.value.forEach((objective) => {
    if (isSupported(objective.coId, ipId)) {
      total += Number(matrixData.value[cellKey(objective.coId, ipId)] ?? 0)
    }
  })
  return Math.round(total * 100) / 100
}

function isColumnValid(ipId) {
  const hasSupport = courseObjectives.value.some((objective) => isSupported(objective.coId, ipId))
  if (!hasSupport) return true
  return Math.abs(getColumnSum(ipId) - 1) < 0.001
}

const indicatorGroups = computed(() => {
  const groups = new Map()
  indicators.value.forEach((indicator) => {
    const key = indicator.grId ?? 'unknown'
    if (!groups.has(key)) {
      groups.set(key, {
        grId: indicator.grId,
        grCode: indicator.grCode || '未分组',
        grDescription: indicator.grDescription || '',
        indicators: [],
      })
    }
    groups.get(key).indicators.push(indicator)
  })
  return Array.from(groups.values())
})

async function loadCourses() {
  try {
    courses.value = (await listCoursesForInstructorApi()) || []
    if (!courses.value.length) {
      return
    }

    const preferredCourseId = Number(route.query.courseId) || courses.value[0].courseId
    filters.courseId = courses.value.some((item) => item.courseId === preferredCourseId)
      ? preferredCourseId
      : courses.value[0].courseId
    await handleCourseChange(filters.courseId)
  } catch {
    courses.value = []
    ElMessage.error('加载课程列表失败')
  }
}

async function handleCourseChange(courseId = filters.courseId) {
  filters.courseId = courseId || null
  filters.teachingClassId = null
  teachingClasses.value = []
  context.value = null
  contextWarning.value = ''
  clearMatrix()

  if (!filters.courseId) return

  try {
    teachingClasses.value = (await listTeachingClassesApi({ courseId: filters.courseId })) || []
    if (!teachingClasses.value.length) {
      return
    }

    const preferredClassId = Number(route.query.teachingClassId) || teachingClasses.value[0].classId
    filters.teachingClassId = teachingClasses.value.some((item) => item.classId === preferredClassId)
      ? preferredClassId
      : teachingClasses.value[0].classId
    await handleTeachingClassChange()
  } catch {
    ElMessage.error('加载教学班失败')
  }
}

async function handleTeachingClassChange() {
  clearMatrix()
  if (!filters.courseId || !filters.teachingClassId) {
    context.value = null
    contextWarning.value = ''
    return
  }

  try {
    context.value = await getContextApi({
      courseId: filters.courseId,
      teachingClassId: filters.teachingClassId,
    })
    contextWarning.value = context.value?.blockReason || ''
    await loadMatrix()
    syncQuery()
  } catch {
    ElMessage.error('加载教学班上下文失败')
  }
}

function buildMatrix(weightRows) {
  const nextMatrix = {}
  const nextSupportSet = new Set()
  const indicatorMap = new Map()

  ;(weightRows || []).forEach((row) => {
    nextSupportSet.add(cellKey(row.coId, row.ipId))
    if (!indicatorMap.has(row.ipId)) {
      indicatorMap.set(row.ipId, {
        ipId: row.ipId,
        ipCode: row.ipCode,
        ipDescription: row.ipDescription,
        grId: row.grId,
        grCode: row.grCode,
        grDescription: row.grDescription,
      })
    }
    if (row.internalWeight !== null && row.internalWeight !== undefined) {
      nextMatrix[cellKey(row.coId, row.ipId)] = Number(row.internalWeight)
    }
  })

  return {
    matrix: nextMatrix,
    supported: nextSupportSet,
    indicatorList: Array.from(indicatorMap.values()),
  }
}

async function loadMatrix() {
  if (!canQuery.value) return

  tableLoading.value = true
  try {
    const [objectiveRows, weightRows] = await Promise.all([
      listCourseObjectivesApi({ courseId: filters.courseId }),
      getCourseWeightApi({
        courseId: filters.courseId,
        gradeYear: Number(context.value.gradeYear),
      }),
    ])

    courseObjectives.value = objectiveRows || []
    const { matrix, supported, indicatorList } = buildMatrix(weightRows || [])
    matrixData.value = matrix
    supportSet.value = supported
    indicators.value = indicatorList
    matrixSnapshot = JSON.parse(JSON.stringify(matrix))

    if (!indicatorList.length) {
      contextWarning.value = '当前课程在该专业和年级下没有可配置的支撑指标点，请先检查宏观支撑矩阵。'
    } else if (!context.value?.blockReason) {
      contextWarning.value = ''
    }
  } catch {
    ElMessage.error('加载内部权重矩阵失败')
  } finally {
    tableLoading.value = false
  }
}

function syncQuery() {
  router.replace({
    query: {
      ...route.query,
      courseId: filters.courseId || undefined,
      teachingClassId: filters.teachingClassId || undefined,
      tab: 'weights',
    },
  })
}

async function handleSave() {
  const invalidColumns = indicators.value.filter((indicator) => !isColumnValid(indicator.ipId))
  if (invalidColumns.length) {
    ElMessage.warning(`以下指标点列的权重和不等于 1.00：${invalidColumns.map((item) => item.ipCode).join('、')}`)
    return
  }

  const contributions = []
  courseObjectives.value.forEach((objective) => {
    indicators.value.forEach((indicator) => {
      if (!isSupported(objective.coId, indicator.ipId)) return
      const internalWeight = Number(matrixData.value[cellKey(objective.coId, indicator.ipId)] ?? 0)
      if (internalWeight > 0) {
        contributions.push({
          coId: objective.coId,
          ipId: indicator.ipId,
          internalWeight,
        })
      }
    })
  })

  if (!contributions.length) {
    ElMessage.warning('当前没有可保存的内部权重配置')
    return
  }

  saveLoading.value = true
  try {
    await saveCourseWeightApi({
      courseId: filters.courseId,
      gradeYear: Number(context.value.gradeYear),
      contributions,
    })
    ElMessage.success('内部权重保存成功')
    matrixSnapshot = JSON.parse(JSON.stringify(matrixData.value))
    await loadMatrix()
  } finally {
    saveLoading.value = false
  }
}

function resetMatrix() {
  matrixData.value = JSON.parse(JSON.stringify(matrixSnapshot))
  ElMessage.info('已恢复到上次加载结果')
}

function clearMatrix() {
  courseObjectives.value = []
  indicators.value = []
  supportSet.value = new Set()
  matrixData.value = {}
  matrixSnapshot = {}
}

function resetAll() {
  filters.courseId = null
  filters.teachingClassId = null
  teachingClasses.value = []
  context.value = null
  contextWarning.value = ''
  clearMatrix()
}

onMounted(async () => {
  await loadCourses()
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
  margin: 6px 0 8px;
  font-size: 28px;
  color: #0f172a;
}

.page-section {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-summary {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.context-section {
  margin-bottom: 20px;
}

.context-panel {
  margin-top: 16px;
  background: #fff;
}

.context-alert {
  margin-top: 16px;
}

.notice-alert {
  margin-bottom: 20px;
}

.matrix-wrap {
  min-height: 160px;
  margin-bottom: 24px;
}

.matrix-scroll {
  overflow-x: auto;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.matrix-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.matrix-table th,
.matrix-table td {
  border: 1px solid #e5e7eb;
  padding: 0;
}

.th-corner,
.td-objective,
.td-sum-label {
  width: 240px;
  min-width: 240px;
  background: #fff;
}

.corner-inner,
.td-objective,
.td-sum-label {
  padding: 16px;
}

.corner-title-main,
.objective-code {
  font-weight: 600;
  color: #111827;
}

.corner-stats,
.objective-desc,
.gr-desc,
.indicator-desc {
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.th-gr-group,
.th-indicator,
.td-cell,
.td-sum {
  text-align: center;
  background: #fff;
}

.th-gr-group,
.th-indicator {
  padding: 12px;
}

.td-cell {
  padding: 12px;
}

.td-cell-disabled {
  background: #f8fafc;
}

.cell-inner {
  display: flex;
  justify-content: center;
}

.cell-disabled-inner {
  color: #9ca3af;
}

.tr-sum td {
  background: #f8fafc;
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
  justify-content: center;
  gap: 12px;
}
</style>
