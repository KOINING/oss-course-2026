<template>
  <div class="objective-dashboard">
    <EmptyState
      v-if="!dashboard?.resultReady"
      description="当前教学班尚未生成课程目标达成结果"
    />

    <template v-else>
      <div class="dashboard-grid">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">课程目标班级平均达成度 C̄j</div>
          </template>
          <div ref="summaryChartRef" class="chart-box"></div>
        </el-card>

        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">学生-课程目标达成热力图 Cij</div>
          </template>
          <div ref="heatmapChartRef" class="chart-box chart-box--tall"></div>
        </el-card>
      </div>

      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="card-title">学生目标达成明细</div>
        </template>
        <el-table :data="dashboard.studentRows || []" border size="small" max-height="420">
          <el-table-column prop="studentNo" label="学号" width="140" fixed="left" />
          <el-table-column prop="studentName" label="姓名" width="120" fixed="left" />
          <el-table-column
            v-for="(objective, index) in dashboard.objectiveSummaries || []"
            :key="objective.coId"
            :label="objective.objectiveCode"
            min-width="140"
            align="center"
          >
            <template #default="{ row }">
              {{ formatPercent(row.achievements?.[index]) }}
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
const heatmapChartRef = ref(null)

let summaryChart = null
let heatmapChart = null

const objectiveLabels = computed(() => (props.dashboard?.objectiveSummaries || []).map((item) => item.objectiveCode))
const objectiveAverages = computed(() => (props.dashboard?.objectiveSummaries || []).map((item) => Number(item.averageAchievement ?? 0)))
const studentLabels = computed(() =>
  (props.dashboard?.studentRows || []).map((item) => `${item.studentName} (${item.studentNo})`),
)
const heatmapData = computed(() => {
  const rows = props.dashboard?.studentRows || []
  return rows.flatMap((row, rowIndex) =>
    (row.achievements || []).map((value, colIndex) => [colIndex, rowIndex, value == null ? '-' : Number(value)]),
  )
})

function formatPercent(value) {
  if (value === undefined || value === null || value === '') return '-'
  return `${(Number(value) * 100).toFixed(2)}%`
}

function renderSummaryChart() {
  if (!summaryChartRef.value || !props.dashboard?.resultReady) return
  summaryChart ??= echarts.init(summaryChartRef.value)
  summaryChart.setOption({
    animation: false,
    grid: { left: 56, right: 24, top: 32, bottom: 56 },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const item = params?.[0]
        if (!item) return ''
        return `${item.axisValue}<br/>班级平均达成度：${formatPercent(item.value)}`
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
      axisLabel: {
        formatter: (value) => `${Math.round(value * 100)}%`,
      },
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
          formatter: ({ value }) => formatPercent(value),
        },
      },
    ],
  })
}

function renderHeatmapChart() {
  if (!heatmapChartRef.value || !props.dashboard?.resultReady) return
  heatmapChart ??= echarts.init(heatmapChartRef.value)
  heatmapChart.setOption({
    animation: false,
    grid: {
      left: 140,
      right: 60,
      top: 24,
      bottom: 36,
    },
    tooltip: {
      position: 'top',
      formatter: (params) => {
        const [objectiveIndex, studentIndex, value] = params.data
        return `${studentLabels.value[studentIndex]}<br/>${objectiveLabels.value[objectiveIndex]}：${formatPercent(value === '-' ? null : value)}`
      },
    },
    xAxis: {
      type: 'category',
      data: objectiveLabels.value,
      splitArea: { show: true },
      axisLabel: { interval: 0 },
    },
    yAxis: {
      type: 'category',
      data: studentLabels.value,
      splitArea: { show: true },
    },
    visualMap: {
      min: 0,
      max: 1,
      calculable: false,
      orient: 'vertical',
      right: 8,
      top: 'middle',
      inRange: {
        color: ['#fef3c7', '#f59e0b', '#2563eb'],
      },
      formatter: (value) => `${Math.round(Number(value) * 100)}%`,
    },
    series: [
      {
        type: 'heatmap',
        data: heatmapData.value,
        label: {
          show: true,
          formatter: ({ data }) => formatPercent(data[2] === '-' ? null : data[2]),
          color: '#111827',
          fontSize: 11,
        },
      },
    ],
  })
}

function resizeCharts() {
  summaryChart?.resize()
  heatmapChart?.resize()
}

watch(
  () => props.dashboard,
  async () => {
    await nextTick()
    renderSummaryChart()
    renderHeatmapChart()
    resizeCharts()
  },
  { deep: true, immediate: true },
)

window.addEventListener('resize', resizeCharts)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  summaryChart?.dispose()
  heatmapChart?.dispose()
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

@media (max-width: 1100px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card,
.detail-card {
  border-radius: 8px;
}

.card-title {
  font-weight: 600;
  color: #111827;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box--tall {
  height: 420px;
}
</style>
