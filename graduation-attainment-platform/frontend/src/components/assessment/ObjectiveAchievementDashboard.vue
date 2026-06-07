<template>
  <div class="objective-dashboard">
    <EmptyState
      v-if="!dashboard?.resultReady"
      description="当前教学班尚未生成课程目标达成结果"
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
          <div class="card-title">课程级毕业要求指标点达成度 Ek</div>
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
              {{ formatDecimal(row.achievements?.[index]) }}
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
    ? '当前教学班成绩与课程级结果已锁定。教师可提交解锁申请，待专业负责人或教务管理员审批后再重新导入和计算。'
    : '当前教学班尚未锁定。教师可继续核对成绩，确认完整后再执行计算并锁定。'
))

function formatDecimal(value) {
  if (value === undefined || value === null || value === '') return '-'
  return Number(value).toFixed(4)
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
        return `${studentLabels.value[studentIndex]}<br/>${objectiveLabels.value[objectiveIndex]}：${formatDecimal(value === '-' ? null : value)}`
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
      formatter: (value) => Number(value).toFixed(4),
    },
    series: [
      {
        type: 'heatmap',
        data: heatmapData.value,
        label: {
          show: true,
          formatter: ({ data }) => formatDecimal(data[2] === '-' ? null : data[2]),
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

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box--tall {
  height: 420px;
}
</style>
