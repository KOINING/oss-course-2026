<template>
  <div class="objective-dashboard">
    <EmptyState
      v-if="!dashboard?.resultReady"
      description="当前教学班评价单元尚未生成课程目标达成结果"
    />

    <template v-else>
      <el-card shadow="never" class="status-card">
        <template #header>
          <div class="card-title">课程级计算状态</div>
        </template>
        <div class="status-grid">
          <div class="status-main">
            <el-tag :type="calcStatusType" effect="light" size="large">
              {{ calcStatusLabel }}
            </el-tag>
            <span class="status-hint">
              {{ lockHint }}
            </span>
          </div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="课程">
              {{ dashboard.courseName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="教学班">
              {{ dashboard.className || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="计算状态">
              {{ calcStatusLabel }}
            </el-descriptions-item>
            <el-descriptions-item label="是否锁定">
              {{ dashboard.locked ? '已锁定' : '未锁定' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <div class="dashboard-grid">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">
              课程目标班级平均达成度
              <span class="math-symbol text-math-symbol"><span class="math-overline">C</span><sub>j</sub></span>
            </div>
          </template>
          <div ref="summaryChartRef" class="chart-box"></div>
        </el-card>

        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">课程目标达成区间人数分布</div>
          </template>
          <div ref="bandChartRef" class="chart-box"></div>
        </el-card>
      </div>

      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="card-title">
            学生目标达成明细
            <span class="math-symbol text-math-symbol">C<sub>ij</sub></span>
          </div>
        </template>
        <el-table :data="studentAchievementRows" border size="small" max-height="420">
          <el-table-column prop="studentNo" label="学号" width="140" fixed="left" />
          <el-table-column prop="studentName" label="姓名" width="120" fixed="left" />
          <el-table-column
            v-for="(objective, index) in dashboard.objectiveSummaries || []"
            :key="objective.coId"
            :label="objective.objectiveCode"
            min-width="140"
            align="center"
            sortable
            :sort-method="(left, right) => compareObjectiveAchievement(left, right, index)"
          >
            <template #default="{ row }">
              {{ formatDecimal(row._objectiveAchievements?.[index]) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="card-title">
            课程级毕业要求指标点达成度
            <span class="math-symbol text-math-symbol">E<sub>k</sub></span>
          </div>
        </template>
        <el-table :data="dashboard.indicatorAchievements || []" border size="small">
          <el-table-column prop="ipCode" label="指标点" width="140" />
          <el-table-column prop="ipDescription" label="指标点描述" min-width="260" />
          <el-table-column label="达成度 Ek" width="160" align="center">
            <template #default="{ row }">
              {{ formatDecimal(row.achievement) }}
            </template>
          </el-table-column>
          <el-table-column label="锁定状态" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="row.locked ? 'success' : 'warning'" effect="plain" size="small">
                {{ row.locked ? '已锁定' : '未锁定' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'
import EmptyState from '@/components/common/EmptyState.vue'

const props = defineProps({
  dashboard: {
    type: Object,
    default: null,
  },
})

const summaryChartRef = ref(null)
const bandChartRef = ref(null)

let summaryChart = null
let bandChart = null

const objectiveLabels = computed(() => (props.dashboard?.objectiveSummaries || []).map((item) => item.objectiveCode))
const objectiveAverages = computed(() => (props.dashboard?.objectiveSummaries || []).map((item) => Number(item.averageAchievement ?? 0)))
const objectiveValues = computed(() => objectiveLabels.value.map((_, objectiveIndex) =>
  (props.dashboard?.studentRows || [])
    .map((row) => normalizeAchievement(row.achievements?.[objectiveIndex]))
    .filter((value) => value !== null),
))
const studentAchievementRows = computed(() => (props.dashboard?.studentRows || []).map((row) => ({
  ...row,
  _objectiveAchievements: (props.dashboard?.objectiveSummaries || []).map((_, index) =>
    normalizeAchievement(row.achievements?.[index]),
  ),
})))
const achievementBands = computed(() => {
  const bands = [
    { key: 'notReached', name: '<0.60 未达成', color: '#dc2626' },
    { key: 'basic', name: '0.60-0.70 基本达成', color: '#f59e0b' },
    { key: 'good', name: '0.70-0.85 良好', color: '#3b82f6' },
    { key: 'excellent', name: '>=0.85 优秀', color: '#16a34a' },
  ]
  const counts = bands.reduce((map, band) => ({ ...map, [band.key]: [] }), {})
  objectiveValues.value.forEach((values) => {
    counts.notReached.push(values.filter((value) => value < 0.6).length)
    counts.basic.push(values.filter((value) => value >= 0.6 && value < 0.7).length)
    counts.good.push(values.filter((value) => value >= 0.7 && value < 0.85).length)
    counts.excellent.push(values.filter((value) => value >= 0.85).length)
  })
  return bands.map((band) => ({ ...band, data: counts[band.key] }))
})
const calcStatusLabel = computed(() => {
  const map = {
    unsubmitted: '未提交',
    score_imported: '已提交未计算',
    calculating: '已计算未锁定',
    locked: '已锁定',
  }
  return map[props.dashboard?.calcStatus] || props.dashboard?.calcStatus || '未提交'
})
const calcStatusType = computed(() => {
  const map = {
    unsubmitted: 'info',
    score_imported: 'warning',
    calculating: 'primary',
    locked: 'success',
  }
  return map[props.dashboard?.calcStatus] || 'info'
})
const lockHint = computed(() => (
  props.dashboard?.locked
    ? '当前教学班评价单元成绩与课程级结果已锁定。教师可提交解锁申请，待专业负责人或教务管理员审批后再重新导入和计算。'
    : '当前教学班评价单元尚未锁定。教师可继续核对成绩，确认完整后再执行计算并锁定。'
))

function normalizeAchievement(value) {
  if (value === undefined || value === null || value === '') return null
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

function formatDecimal(value) {
  const numberValue = normalizeAchievement(value)
  if (numberValue === null) return '-'
  return numberValue.toFixed(4)
}

function compareObjectiveAchievement(left, right, index) {
  const leftValue = normalizeAchievement(left._objectiveAchievements?.[index])
  const rightValue = normalizeAchievement(right._objectiveAchievements?.[index])
  if (leftValue === null && rightValue === null) return 0
  if (leftValue === null) return 1
  if (rightValue === null) return -1
  return leftValue - rightValue
}

function renderSummaryChart() {
  if (!summaryChartRef.value || !props.dashboard?.resultReady) return
  summaryChart ??= echarts.init(summaryChartRef.value)
  summaryChart.setOption({
    animation: false,
    grid: { left: 56, right: 24, top: 36, bottom: 56 },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const item = params?.[0]
        if (!item) return ''
        return `${item.axisValue}<br/>班级平均达成度：${formatDecimal(item.value)}`
      },
    },
    xAxis: {
      type: 'category',
      data: objectiveLabels.value,
      axisLabel: { interval: 0 },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLabel: { formatter: (value) => Number(value).toFixed(2) },
    },
    series: [
      {
        type: 'bar',
        data: objectiveAverages.value,
        barWidth: 36,
        itemStyle: {
          color: '#3b82f6',
          borderRadius: [6, 6, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          formatter: ({ value }) => formatDecimal(value),
        },
      },
    ],
  }, true)
}

function renderBandChart() {
  if (!bandChartRef.value || !props.dashboard?.resultReady) return
  bandChart ??= echarts.init(bandChartRef.value)
  bandChart.setOption({
    animation: false,
    grid: { left: 48, right: 24, top: 58, bottom: 56 },
    legend: {
      top: 0,
      type: 'scroll',
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    xAxis: {
      type: 'category',
      data: objectiveLabels.value,
      axisLabel: { interval: 0 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: '人数',
    },
    series: achievementBands.value.map((band) => ({
      name: band.name,
      type: 'bar',
      stack: 'achievement',
      data: band.data,
      itemStyle: { color: band.color },
      label: {
        show: true,
        formatter: ({ value }) => (value > 0 ? value : ''),
      },
    })),
  }, true)
}

async function renderAllCharts() {
  await nextTick()
  renderSummaryChart()
  renderBandChart()
  resizeCharts()
}

function resizeCharts() {
  summaryChart?.resize()
  bandChart?.resize()
}

watch(
  () => props.dashboard,
  renderAllCharts,
  { deep: true, immediate: true },
)

window.addEventListener('resize', resizeCharts)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  summaryChart?.dispose()
  bandChart?.dispose()
})
</script>

<style scoped>
.objective-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 16px;
}

.status-card,
.chart-card,
.detail-card {
  border-radius: 8px;
}

.status-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-hint {
  color: #475569;
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

.card-title {
  font-weight: 600;
  color: #111827;
}

.math-symbol {
  margin-left: 6px;
  font-family: "Times New Roman", Times, serif;
  font-size: 18px;
  font-style: italic;
  font-weight: 500;
}

.math-symbol sub {
  font-size: 0.7em;
}

.text-math-symbol {
  font-family: inherit;
  font-size: inherit;
  font-style: normal;
  font-weight: inherit;
}

.math-overline {
  text-decoration: overline;
}

.chart-box {
  width: 100%;
  height: 320px;
}
</style>
