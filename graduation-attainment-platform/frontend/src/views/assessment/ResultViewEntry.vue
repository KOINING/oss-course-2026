<template>
  <div class="result-entry">
    <div class="filter-bar">
      <el-select v-model="filters.majorId" placeholder="选择专业" style="width: 220px" @change="handleMajorChange">
        <el-option
          v-for="major in filterOptions.majors"
          :key="major.majorId"
          :label="major.majorName"
          :value="major.majorId"
        />
      </el-select>

      <el-select
        v-model="filters.gradeYear"
        placeholder="选择年级"
        style="width: 160px"
        :disabled="!filters.majorId"
        @change="handleGradeYearChange"
      >
        <el-option v-for="year in currentGradeYears" :key="year" :label="`${year}级`" :value="year" />
      </el-select>

      <el-button :loading="loading" @click="loadResults">查询结果</el-button>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadAll" />

    <EmptyState
      v-else-if="!hasFilters"
      description="请按专业、年级查询当前届学生的课程级状态和专业级结果。"
    />

    <template v-else>
      <div class="entry-cards">
        <el-card class="entry-card">
          <div class="entry-card__icon">
            <el-icon :size="40" color="#2563eb"><DataAnalysis /></el-icon>
          </div>
          <div class="entry-card__body">
            <h3>课程级状态</h3>
            <p>当前范围内共 {{ dashboard.courses.length }} 个教学班，其中 {{ lockedCount }} 个已锁定。</p>
          </div>
          <div class="entry-card__status">
            <el-tag :type="dashboard.aggregationAllowed ? 'success' : 'warning'" effect="light">
              {{ dashboard.aggregationAllowed ? '全部已锁定' : '仍有未锁定教学班' }}
            </el-tag>
          </div>
        </el-card>

        <el-card class="entry-card">
          <div class="entry-card__icon">
            <el-icon :size="40" color="#16a34a"><TrendCharts /></el-icon>
          </div>
          <div class="entry-card__body">
            <h3>专业级结果</h3>
            <p>{{ majorResult.message || '当前筛选条件下暂无专业级汇总结果。' }}</p>
          </div>
          <div class="entry-card__status">
            <el-tag :type="majorResult.resultReady ? 'success' : 'info'" effect="light">
              {{ majorResult.resultReady ? '已生成' : '未生成' }}
            </el-tag>
          </div>
        </el-card>
      </div>

      <el-card class="entry-info-card">
        <template #header>
          <span class="info-card-title">课程级状态明细</span>
        </template>
        <EmptyState
          v-if="!dashboard.courses.length"
          description="当前筛选条件下没有课程级状态数据。"
        />
        <el-table v-else :data="dashboard.courses" border size="small">
          <el-table-column prop="courseCode" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column prop="classCode" label="教学班代码" width="140" />
          <el-table-column prop="teacherName" label="任课教师" width="120" />
          <el-table-column prop="calcStatus" label="状态" width="140">
            <template #default="{ row }">
              <el-tag :type="statusType(row.calcStatus)" effect="light">{{ statusLabel(row.calcStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="blockReason" label="阻断原因" min-width="220">
            <template #default="{ row }">{{ row.blockReason || '-' }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="entry-info-card">
        <template #header>
          <div class="info-card-header-row">
            <span class="info-card-title">专业级指标点结果</span>
            <el-button
              v-if="majorResult.resultReady"
              type="primary"
              :icon="Download"
              @click="exportReport"
            >
              导出专业级报告
            </el-button>
          </div>
        </template>
        <EmptyState
          v-if="!majorResult.resultReady"
          :description="majorResult.message || '当前筛选条件下暂无专业级汇总结果。'"
        />
        <template v-else>
          <div v-if="majorResult.termCode" class="term-info">
            统计学期：{{ majorResult.termCode }}
          </div>
          <el-table :data="majorResult.indicatorAchievements || []" border size="small">
            <el-table-column prop="ipCode" label="指标点" width="120" />
            <el-table-column prop="ipDescription" label="指标点描述" min-width="260" />
            <el-table-column prop="finalAchievement" label="专业级达成度 Gk" width="180">
              <template #default="{ row }">{{ formatDecimal(row.finalAchievement) }}</template>
            </el-table-column>
          </el-table>
        </template>
      </el-card>

      <el-card v-if="majorResult.resultReady" class="entry-info-card">
        <template #header>
          <span class="info-card-title">毕业要求指标点达成度雷达图</span>
        </template>
        <div class="radar-export-row">
          <el-button
            type="primary"
            plain
            :icon="Download"
            @click="exportRadarChart"
          >
            导出全专业评价雷达图
          </el-button>
        </div>
        <ProfessionalRadarChart
          ref="radarChartRef"
          :indicator-achievements="majorResult.indicatorAchievements"
        />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { DataAnalysis, Download, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import ProfessionalRadarChart from './ProfessionalRadarChart.vue'
import {
  exportMajorReportApi,
  getMacroDashboardDataApi,
  getMajorCalcResultApi,
  listMajorGradeYearTermsApi,
} from '@/api/assessment'

const loading = ref(false)
const loadError = ref('')
const radarChartRef = ref(null)

const filterOptions = reactive({ majors: [] })
const filters = reactive({ majorId: null, gradeYear: null })

const dashboard = reactive({
  courses: [],
  aggregationAllowed: false,
})

const majorResult = reactive({
  resultReady: false,
  message: '',
  indicatorAchievements: [],
  termCode: '',
})

const hasFilters = computed(() => !!(filters.majorId && filters.gradeYear))
const selectedMajor = computed(() => filterOptions.majors.find((item) => item.majorId === filters.majorId) || null)
const currentGradeYears = computed(() => (selectedMajor.value?.gradeYearScopes || []).map((item) => item.gradeYear))
const lockedCount = computed(() => dashboard.courses.filter((item) => item.calcStatus === 'locked').length)

function buildPayload() {
  return {
    majorId: filters.majorId,
    gradeYear: filters.gradeYear,
  }
}

function statusType(status) {
  const map = { unsubmitted: 'info', score_imported: 'warning', calculating: 'primary', locked: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = {
    unsubmitted: '未提交',
    score_imported: '已提交未计算',
    calculating: '已计算未锁定',
    locked: '已锁定',
  }
  return map[status] || status || '-'
}

async function loadFilterOptions() {
  const data = await listMajorGradeYearTermsApi()
  filterOptions.majors = data?.majors || []
  if (!filters.majorId && filterOptions.majors.length) {
    filters.majorId = filterOptions.majors[0].majorId
  }
  hydrateDependentFilters()
}

function hydrateDependentFilters() {
  const years = currentGradeYears.value
  if (!years.includes(filters.gradeYear)) {
    filters.gradeYear = years[0] || null
  }
}

function clearResults() {
  dashboard.courses = []
  dashboard.aggregationAllowed = false
  majorResult.resultReady = false
  majorResult.message = ''
  majorResult.indicatorAchievements = []
  majorResult.termCode = ''
}

function handleMajorChange() {
  hydrateDependentFilters()
  clearResults()
  loadResults()
}

function handleGradeYearChange() {
  clearResults()
  loadResults()
}

async function loadResults() {
  if (!hasFilters.value) {
    clearResults()
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const [dashboardData, majorData] = await Promise.all([
      getMacroDashboardDataApi(buildPayload()),
      getMajorCalcResultApi(buildPayload()),
    ])
    dashboard.courses = dashboardData?.courses || []
    dashboard.aggregationAllowed = dashboardData?.aggregationAllowed ?? false
    majorResult.resultReady = majorData?.resultReady ?? false
    majorResult.message = majorData?.message || ''
    majorResult.indicatorAchievements = majorData?.indicatorAchievements || []
    majorResult.termCode = majorData?.termCode || ''
  } catch (error) {
    loadError.value = error.message || '加载专业级结果失败'
    clearResults()
  } finally {
    loading.value = false
  }
}

async function exportReport() {
  try {
    const response = await exportMajorReportApi(buildPayload())
    const blob = response.data
    const isExcel = blob.type.includes('spreadsheet') || blob.type.includes('excel') || blob.type.includes('ms-excel')
    if (!isExcel) {
      const text = await blob.text()
      let msg = '导出失败，服务器未返回有效文件'
      try {
        const err = JSON.parse(text)
        msg = err.message || msg
      } catch {}
      ElMessage.error(msg)
      return
    }
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const majorName = selectedMajor.value?.majorName || '专业'
    link.download = `${majorName}${filters.gradeYear}级专业级评价报告.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('报告导出成功')
  } catch (error) {
    ElMessage.error(error.message || '报告导出失败')
  }
}

function exportRadarChart() {
  const dataUrl = radarChartRef.value?.exportImage?.()
  if (!dataUrl) {
    ElMessage.error('雷达图尚未生成，暂时无法导出')
    return
  }
  const link = document.createElement('a')
  const majorName = selectedMajor.value?.majorName || '专业'
  link.href = dataUrl
  link.download = `${majorName}${filters.gradeYear}级全专业评价雷达图.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('雷达图导出成功')
}

function formatDecimal(value) {
  if (value === undefined || value === null) return '-'
  return Number(value).toFixed(4)
}

async function loadAll() {
  await loadFilterOptions()
  await loadResults()
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.result-entry {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.entry-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.entry-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.entry-card__icon {
  text-align: center;
  padding-top: 8px;
}

.entry-card__body h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
}

.entry-card__body p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
  font-size: 14px;
}

.entry-card__status {
  margin-top: auto;
}

.entry-info-card {
  border-radius: 14px;
}

.info-card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-card-title {
  font-weight: 600;
  color: #1f2937;
}

.term-info {
  padding: 8px 0 16px;
  font-size: 13px;
  color: #64748b;
}

.radar-export-row {
  display: flex;
  justify-content: flex-end;
  padding-bottom: 12px;
}
</style>
