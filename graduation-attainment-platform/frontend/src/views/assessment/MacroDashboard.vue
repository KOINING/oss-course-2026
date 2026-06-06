<template>
  <div class="macro-dashboard">
    <div class="filter-bar">
      <el-select v-model="filters.majorId" placeholder="选择专业" style="width: 200px" @change="resetAndLoad">
        <el-option v-for="m in filterOptions.majors" :key="m.majorId" :label="m.majorName" :value="m.majorId" />
      </el-select>
      <el-select v-model="filters.gradeYear" placeholder="选择年级" style="width: 160px" @change="loadDashboard">
        <el-option v-for="y in filterOptions.gradeYears" :key="y" :label="`${y}级`" :value="y" />
      </el-select>
      <el-select v-model="filters.termId" placeholder="选择学期" style="width: 200px" @change="loadDashboard">
        <el-option v-for="t in filterOptions.terms" :key="t.termId" :label="t.termCode" :value="t.termId" />
      </el-select>
      <el-button :loading="loading" @click="loadDashboard">查询</el-button>
      <el-button
        type="primary"
        :loading="aggregating"
        :disabled="!dashboardData.aggregationAllowed || !hasFilters"
        @click="calculateMajor"
      >
        执行专业级汇总
      </el-button>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadAll" />

    <EmptyState
      v-else-if="!hasFilters"
      description="请按专业、年级、学期三个维度查询宏观看板"
    />

    <template v-else>
      <div class="status-summary" v-if="!loading">
        <div class="summary-cards">
          <div class="summary-card summary-card--total">
            <div class="summary-card__num">{{ courses.length }}</div>
            <div class="summary-card__label">支撑课程总数</div>
          </div>
          <div class="summary-card summary-card--locked">
            <div class="summary-card__num">{{ lockedCount }}</div>
            <div class="summary-card__label">已锁定</div>
          </div>
          <div class="summary-card summary-card--computed">
            <div class="summary-card__num">{{ computedCount }}</div>
            <div class="summary-card__label">已计算未锁定</div>
          </div>
          <div class="summary-card summary-card--submitted">
            <div class="summary-card__num">{{ submittedCount }}</div>
            <div class="summary-card__label">已提交未计算</div>
          </div>
          <div class="summary-card summary-card--unsubmitted">
            <div class="summary-card__num">{{ unsubmittedCount }}</div>
            <div class="summary-card__label">未提交</div>
          </div>
        </div>
      </div>

      <el-alert
        v-if="!loading && !dashboardData.aggregationAllowed"
        type="warning"
        :closable="false"
        show-icon
        class="aggregation-warning"
      >
        <template #title>
          当前不能执行专业级汇总
        </template>
        <template #default>
          {{ dashboardData.blockReason || '仍有教学班未锁定。' }}
        </template>
      </el-alert>

      <el-alert
        v-else-if="!loading && dashboardData.aggregationAllowed"
        type="success"
        :closable="false"
        show-icon
        class="aggregation-warning"
      >
        <template #title>当前筛选范围内全部教学班已锁定，可以执行专业级汇总</template>
      </el-alert>

      <EmptyState
        v-if="!loading && courses.length === 0"
        description="当前筛选条件下没有教学班数据"
      />

      <el-table
        v-else
        v-loading="loading"
        :data="courses"
        border
        stripe
        max-height="480"
      >
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="classCode" label="教学班代码" width="140" />
        <el-table-column prop="teacherName" label="任课教师" width="120" />
        <el-table-column label="学生数" width="90" align="center">
          <template #default="{ row }">{{ row.studentCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="成绩条数" width="100" align="center">
          <template #default="{ row }">{{ row.scoreCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="计算状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.calcStatus)" effect="light" size="small">
              {{ statusLabel(row.calcStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阻断原因" min-width="220">
          <template #default="{ row }">{{ row.blockReason || '-' }}</template>
        </el-table-column>
      </el-table>

      <el-card
        v-if="majorResult.resultReady"
        class="major-result-card"
        shadow="never"
      >
        <template #header>
          <div class="major-result-header">
            <span>专业级汇总结果</span>
            <el-tag type="success" effect="light">已生成</el-tag>
          </div>
        </template>
        <el-table :data="majorResult.indicatorAchievements || []" border size="small">
          <el-table-column prop="ipCode" label="指标点" width="120" />
          <el-table-column prop="ipDescription" label="指标点描述" min-width="260" />
          <el-table-column prop="finalAchievement" label="专业级达成度 Gk" width="160">
            <template #default="{ row }">{{ formatPercent(row.finalAchievement) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import {
  calculateMajorLevelApi,
  getMacroDashboardDataApi,
  getMajorCalcResultApi,
  listMajorGradeYearTermsApi,
} from '@/api/assessment'

const loading = ref(false)
const aggregating = ref(false)
const loadError = ref('')

const filterOptions = reactive({
  majors: [],
  gradeYears: [],
  terms: [],
})

const filters = reactive({
  majorId: null,
  gradeYear: null,
  termId: null,
})

const dashboardData = reactive({
  courses: [],
  aggregationAllowed: false,
  unlockedWarning: false,
  blockReason: '',
  majorResultExists: false,
})

const majorResult = reactive({
  resultReady: false,
  indicatorAchievements: [],
})

const hasFilters = computed(() => !!(filters.majorId && filters.gradeYear && filters.termId))
const courses = computed(() => dashboardData.courses || [])
const lockedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'locked').length)
const computedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'calculating').length)
const submittedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'score_imported').length)
const unsubmittedCount = computed(() => courses.value.filter((c) => !c.calcStatus || c.calcStatus === 'unsubmitted').length)

function statusType(status) {
  const map = { unsubmitted: 'info', score_imported: 'warning', calculating: 'primary', locked: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { unsubmitted: '未提交', score_imported: '已提交未计算', calculating: '已计算未锁定', locked: '已锁定' }
  return map[status] || status || '-'
}

function buildPayload() {
  return {
    majorId: filters.majorId,
    gradeYear: filters.gradeYear,
    termId: filters.termId,
  }
}

async function loadFilterOptions() {
  const data = await listMajorGradeYearTermsApi()
  filterOptions.majors = data?.majors || []
  filterOptions.gradeYears = data?.gradeYears || []
  filterOptions.terms = data?.terms || []
}

async function loadMajorResult() {
  if (!hasFilters.value) {
    majorResult.resultReady = false
    majorResult.indicatorAchievements = []
    return
  }
  const data = await getMajorCalcResultApi(buildPayload())
  majorResult.resultReady = data?.resultReady ?? false
  majorResult.indicatorAchievements = data?.indicatorAchievements || []
}

function resetAndLoad() {
  filters.gradeYear = null
  filters.termId = null
  dashboardData.courses = []
  dashboardData.aggregationAllowed = false
  dashboardData.blockReason = ''
  majorResult.resultReady = false
  majorResult.indicatorAchievements = []
}

async function loadDashboard() {
  if (!hasFilters.value) {
    resetAndLoad()
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const data = await getMacroDashboardDataApi(buildPayload())
    dashboardData.courses = data?.courses || []
    dashboardData.aggregationAllowed = data?.aggregationAllowed ?? false
    dashboardData.unlockedWarning = data?.unlockedWarning ?? false
    dashboardData.blockReason = data?.blockReason || ''
    dashboardData.majorResultExists = data?.majorResultExists ?? false
    await loadMajorResult()
  } catch (error) {
    loadError.value = error.message || '加载宏观看板失败'
    dashboardData.courses = []
  } finally {
    loading.value = false
  }
}

async function calculateMajor() {
  if (!hasFilters.value || !dashboardData.aggregationAllowed) return
  aggregating.value = true
  try {
    await calculateMajorLevelApi(buildPayload())
    ElMessage.success('专业级汇总完成')
    await loadDashboard()
  } catch (error) {
    ElMessage.error(error.message || '专业级汇总失败')
  } finally {
    aggregating.value = false
  }
}

function formatPercent(value) {
  if (value === undefined || value === null) return '-'
  return `${(Number(value) * 100).toFixed(2)}%`
}

async function loadAll() {
  await loadFilterOptions()
  await loadDashboard()
}

onMounted(() => {
  loadFilterOptions()
})
</script>

<style scoped>
.macro-dashboard {
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

.summary-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.summary-card {
  flex: 1 1 0;
  min-width: 140px;
  padding: 16px 20px;
  border-radius: 12px;
  text-align: center;
}

.summary-card--total {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.summary-card--locked {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.summary-card--computed {
  background: #fefce8;
  border: 1px solid #fef08a;
}

.summary-card--submitted {
  background: #fff7ed;
  border: 1px solid #fed7aa;
}

.summary-card--unsubmitted {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.summary-card__num {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.summary-card__label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.major-result-card {
  border-radius: 10px;
}

.major-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
