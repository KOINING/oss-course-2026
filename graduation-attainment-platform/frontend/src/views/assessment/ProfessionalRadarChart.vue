<template>
  <div class="radar-chart-wrapper">
    <div v-if="title" class="radar-chart-title">{{ title }}</div>
    <div ref="containerRef" class="radar-chart-container">
      <div ref="chartRef" class="radar-chart-box"></div>
      <div
        v-if="tooltip.visible"
        class="custom-tooltip"
        :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
      >
        <strong>{{ tooltip.ipCode }}</strong>
        <div class="custom-tooltip__desc">{{ tooltip.ipDescription }}</div>
        <div class="custom-tooltip__value">达成度 Gk：{{ tooltip.value }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  indicatorAchievements: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: '',
  },
})

const containerRef = ref(null)
const chartRef = ref(null)
let chart = null

const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  ipCode: '',
  ipDescription: '',
  value: '',
})

function formatDecimal(value) {
  if (value === undefined || value === null) return '-'
  return Number(value).toFixed(4)
}

function bindMouseEvents() {
  if (!chart) return
  const zr = chart.getZr()
  zr.off('mousemove')
  zr.off('mouseout')
  zr.on('mousemove', (event) => {
    const data = props.indicatorAchievements || []
    const n = data.length
    if (n === 0) {
      tooltip.visible = false
      return
    }

    const w = chart.getWidth()
    const h = chart.getHeight()
    const cx = w * 0.5
    const cy = h * 0.54
    const dx = event.offsetX - cx
    const dy = event.offsetY - cy
    const dist = Math.sqrt(dx * dx + dy * dy)
    const outerR = (Math.min(w, h) / 2) * 0.68

    // only show tooltip when mouse is near the radar data ring
    if (dist < outerR * 0.25 || dist > outerR * 1.15) {
      tooltip.visible = false
      return
    }

    // atan2(dy,dx) gives angle from +x axis CCW; negate to go CCW in screen
    // then rotate so 0 = top
    let angle = -Math.atan2(dy, dx) - Math.PI / 2
    if (angle < 0) angle += 2 * Math.PI

    const sector = (2 * Math.PI) / n
    const idx = Math.round(angle / sector) % n

    const item = data[idx]
    tooltip.ipCode = item.ipCode || ''
    tooltip.ipDescription = item.ipDescription || ''
    tooltip.value = formatDecimal(item.finalAchievement)

    const containerRect = containerRef.value.getBoundingClientRect()
    tooltip.x = event.offsetX + 12
    tooltip.y = event.offsetY - 10
    tooltip.visible = true
  })
  zr.on('mouseout', () => {
    tooltip.visible = false
  })
}

function renderChart() {
  const data = props.indicatorAchievements || []
  if (!chartRef.value || data.length === 0) return

  chart ??= echarts.init(chartRef.value)

  const indicatorNames = data.map((item) => item.ipCode || '')
  const values = data.map((item) => Number(item.finalAchievement ?? 0))

  chart.setOption({
    animation: false,
    tooltip: { show: false },
    radar: {
      center: ['50%', '54%'],
      radius: '68%',
      indicator: indicatorNames.map((name) => ({
        name,
        max: 1,
      })),
      axisName: {
        fontSize: 13,
        color: '#374151',
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(37, 99, 235, 0.04)', 'rgba(37, 99, 235, 0.02)'],
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(37, 99, 235, 0.15)',
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(37, 99, 235, 0.25)',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            name: '专业级达成度 Gk',
            areaStyle: {
              color: 'rgba(37, 99, 235, 0.15)',
            },
            lineStyle: {
              color: '#2563eb',
              width: 2,
            },
            itemStyle: {
              color: '#2563eb',
            },
          },
        ],
        symbol: 'circle',
        symbolSize: 6,
        label: {
          show: true,
          formatter: ({ value }) => formatDecimal(value),
          fontSize: 11,
          color: '#1f2937',
        },
      },
    ],
  })

  bindMouseEvents()
}

function resizeChart() {
  chart?.resize()
}

watch(
  () => props.indicatorAchievements,
  async () => {
    await nextTick()
    renderChart()
    resizeChart()
  },
  { deep: true, immediate: true },
)

window.addEventListener('resize', resizeChart)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<style scoped>
.radar-chart-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.radar-chart-title {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
  margin-bottom: 12px;
}

.radar-chart-container {
  position: relative;
  width: 100%;
}

.radar-chart-box {
  width: 100%;
  max-width: 640px;
  height: 480px;
  margin: 0 auto;
}

.custom-tooltip {
  position: absolute;
  z-index: 100;
  pointer-events: none;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-size: 13px;
  line-height: 1.6;
  max-width: 280px;
  white-space: normal;
}

.custom-tooltip__desc {
  color: #64748b;
  font-size: 12px;
  margin-top: 2px;
}

.custom-tooltip__value {
  color: #2563eb;
  font-weight: 500;
  margin-top: 4px;
}
</style>
