<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import MatrixWeightValidation from '@/components/matrix/MatrixWeightValidation.vue'
import { listSupportMatrixApi, saveSupportMatrixApi, validateMatrixWeightApi } from '@/api/matrix'
import { listMajorsForSelectApi } from '@/api/basic'

// ---- Major selection ----
const majorOptions = ref([])
const selectedMajorId = ref(null)
const majorLoading = ref(false)

// ---- Matrix data ----
const courses = ref([])
const indicatorPoints = ref([])
const supportMatrix = reactive({}) // key: "courseId-ipId" → { cisId, totalWeight }
const matrixLoading = ref(false)
const saving = ref(false)

// ---- Validation ----
const validationErrors = ref([])
const validationVisible = ref(false)
const validating = ref(false)

// ---- Errors ----
const loadError = ref('')

// ---- Computed: build grid ----
const gridRows = computed(() => {
  return courses.value.map((course) => ({
    courseId: course.courseId,
    courseCode: course.courseCode,
    courseName: course.courseName,
    cells: indicatorPoints.value.map((ip) => {
      const key = `${course.courseId}-${ip.ipId}`
      const entry = supportMatrix[key]
      return {
        ipId: ip.ipId,
        ipCode: ip.ipCode,
        supported: !!entry,
        weight: entry ? entry.totalWeight : 0,
      }
    }),
  }))
})

const indicatorHeaders = computed(() => {
  return indicatorPoints.value.map((ip) => ({
    ipId: ip.ipId,
    ipCode: ip.ipCode,
    label: `${ip.ipCode}`,
  }))
})

// ---- Build grouped indicator points for header ----
const groupedHeaders = computed(() => {
  const map = new Map()
  indicatorPoints.value.forEach((ip) => {
    const grCode = ip.grCode || ip.ipCode.split('.')[0]
    if (!map.has(grCode)) map.set(grCode, [])
    map.get(grCode).push(ip)
  })
  return Array.from(map.entries()).map(([grCode, points]) => ({
    grCode,
    points,
  }))
})

async function loadMajors() {
  try {
    majorOptions.value = await listMajorsForSelectApi()
  } catch {
    // ignore
  }
}

async function loadMatrix() {
  if (!selectedMajorId.value) return
  matrixLoading.value = true
  loadError.value = ''
  try {
    const data = await listSupportMatrixApi({ majorId: selectedMajorId.value })
    courses.value = data.courses ?? []
    indicatorPoints.value = data.indicatorPoints ?? []

    // Rebuild support map
    Object.keys(supportMatrix).forEach((k) => delete supportMatrix[k])
    const relations = data.relations ?? []
    relations.forEach((rel) => {
      const key = `${rel.courseId}-${rel.ipId}`
      supportMatrix[key] = {
        cisId: rel.cisId,
        totalWeight: rel.totalWeight ?? 0,
      }
    })
  } catch (e) {
    loadError.value = e.message || '加载矩阵数据失败'
  } finally {
    matrixLoading.value = false
  }
}

function toggleSupport(courseId, ipId) {
  const key = `${courseId}-${ipId}`
  if (supportMatrix[key]) {
    delete supportMatrix[key]
    validationVisible.value = false
  } else {
    supportMatrix[key] = { cisId: null, totalWeight: 0 }
  }
}

function updateWeight(courseId, ipId, value) {
  const key = `${courseId}-${ipId}`
  if (supportMatrix[key]) {
    const num = parseFloat(value)
    supportMatrix[key].totalWeight = isNaN(num) ? 0 : Math.min(1, Math.max(0, num))
  }
}

async function handleValidate() {
  validating.value = true
  try {
    const relations = buildRelationsPayload()
    const data = await validateMatrixWeightApi({ relations })
    validationErrors.value = data.errors ?? []
    validationVisible.value = validationErrors.value.length > 0
    if (validationErrors.value.length === 0) {
      ElMessage.success('所有毕业要求指标点权重均已配平（W = 1.0）')
    }
  } catch (e) {
    ElMessage.error(e.message || '校验请求失败')
  } finally {
    validating.value = false
  }
}

function buildRelationsPayload() {
  const relations = []
  Object.entries(supportMatrix).forEach(([key, value]) => {
    const [courseId, ipId] = key.split('-').map(Number)
    relations.push({
      courseId,
      ipId,
      cisId: value.cisId,
      totalWeight: value.totalWeight,
    })
  })
  return relations
}

async function handleSave() {
  saving.value = true
  try {
    const relations = buildRelationsPayload()
    await saveSupportMatrixApi({ relations })
    ElMessage.success('支撑矩阵保存成功')
    validationVisible.value = false
    await loadMatrix()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

watch(selectedMajorId, () => {
  if (selectedMajorId.value) {
    loadMatrix()
    validationVisible.value = false
  }
})

onMounted(() => {
  loadMajors()
})
</script>

<template>
  <div class="matrix-page">
    <el-card class="matrix-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A：基础与宏观数据管理</p>
            <h1>支撑矩阵配置</h1>
            <p class="page-summary">
              配置课程与毕业要求指标点之间的宏观支撑关系及总支撑权重，保证同一指标点下所有课程的 W 之和为 1.0。
            </p>
          </div>
        </div>
      </template>

      <!-- Major selector -->
      <div class="matrix-controls">
        <div class="control-row">
          <span class="control-label">选择专业：</span>
          <el-select
            v-model="selectedMajorId"
            placeholder="请选择专业"
            style="width: 280px"
            filterable
          >
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="matrixLoading" v-loading="matrixLoading" style="min-height: 200px" />

      <!-- Error -->
      <ErrorState
        v-else-if="loadError"
        :message="loadError"
        @retry="loadMatrix"
      />

      <!-- Empty: no major selected -->
      <EmptyState
        v-else-if="!selectedMajorId"
        description="请先选择一个专业以查看其支撑矩阵"
      />

      <!-- Empty: no data -->
      <EmptyState
        v-else-if="courses.length === 0 || indicatorPoints.length === 0"
        description="当前专业尚未配置课程或指标点数据"
      />

      <!-- Matrix table -->
      <div v-else class="matrix-content">
        <!-- Weight validation banner -->
        <MatrixWeightValidation
          :visible="validationVisible"
          :validation-errors="validationErrors"
        />

        <!-- Action bar -->
        <div class="matrix-toolbar">
          <el-button
            type="warning"
            :loading="validating"
            @click="handleValidate"
          >
            校验权重配平
          </el-button>
          <el-button
            type="primary"
            :loading="saving"
            @click="handleSave"
          >
            保存矩阵
          </el-button>
          <span class="toolbar-hint">
            勾选复选框建立支撑关系，在输入框中填写总支撑权重 W（0~1）
          </span>
        </div>

        <!-- Scrollable matrix -->
        <div class="matrix-scroll">
          <table class="matrix-table">
            <thead>
              <tr>
                <th class="header-course" rowspan="2">课程</th>
                <th
                  v-for="group in groupedHeaders"
                  :key="group.grCode"
                  :colspan="group.points.length"
                  class="header-gr"
                >
                  毕业要求 {{ group.grCode }}
                </th>
              </tr>
              <tr>
                <template v-for="group in groupedHeaders" :key="`ip-${group.grCode}`">
                  <th
                    v-for="ip in group.points"
                    :key="ip.ipId"
                    class="header-ip"
                  >
                    <el-tooltip :content="ip.ipDescription" placement="top">
                      <span>{{ ip.ipCode }}</span>
                    </el-tooltip>
                  </th>
                </template>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in gridRows" :key="row.courseId">
                <td class="cell-course">
                  <div class="course-info">
                    <span class="course-code">{{ row.courseCode }}</span>
                    <span class="course-name">{{ row.courseName }}</span>
                  </div>
                </td>
                <td
                  v-for="cell in row.cells"
                  :key="`${row.courseId}-${cell.ipId}`"
                  class="cell-matrix"
                  :class="{ 'cell-supported': cell.supported }"
                >
                  <div class="cell-inner">
                    <el-checkbox
                      :model-value="cell.supported"
                      @change="toggleSupport(row.courseId, cell.ipId)"
                    />
                    <el-input-number
                      v-if="cell.supported"
                      :model-value="cell.weight"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      :precision="2"
                      size="small"
                      controls-position="right"
                      style="width: 110px"
                      @change="(v) => updateWeight(row.courseId, cell.ipId, v)"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.matrix-page {
  padding: 20px;
}

.matrix-card {
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

.matrix-controls {
  margin-bottom: 20px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-label {
  font-weight: 600;
  color: #475569;
  font-size: 14px;
}

.matrix-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.matrix-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.toolbar-hint {
  margin-left: auto;
  color: #94a3b8;
  font-size: 13px;
}

.matrix-scroll {
  overflow-x: auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.matrix-table {
  border-collapse: collapse;
  min-width: 100%;
  font-size: 13px;
}

.matrix-table th,
.matrix-table td {
  border: 1px solid #e2e8f0;
  padding: 8px 6px;
  text-align: center;
  vertical-align: middle;
}

.header-course {
  background: #f1f5f9;
  color: #1f2937;
  font-weight: 700;
  min-width: 180px;
  position: sticky;
  left: 0;
  z-index: 2;
}

.header-gr {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 700;
  font-size: 14px;
}

.header-ip {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 12px;
  cursor: help;
  min-width: 130px;
}

.cell-course {
  background: #fafafa;
  text-align: left;
  padding: 10px 12px;
  position: sticky;
  left: 0;
  z-index: 1;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.course-code {
  font-weight: 700;
  color: #1f2937;
  font-size: 13px;
}

.course-name {
  color: #64748b;
  font-size: 12px;
}

.cell-matrix {
  background: #fff;
  min-width: 150px;
}

.cell-matrix.cell-supported {
  background: #f0fdf4;
}

.cell-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>
