<template>
  <div class="macro-dashboard">
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

      <el-button :loading="loading" @click="loadDashboard">查询</el-button>
      <el-button
        type="primary"
        :loading="aggregating"
        :disabled="!dashboardData.aggregationAllowed || !hasFilters"
        @click="calculateMajor"
      >
        执行专业级计算
      </el-button>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadAll" />

    <EmptyState
      v-else-if="!hasFilters"
      description="请先按专业、年级查看当前届学生涉及的全部支撑课程状态，再执行专业级全局达成度计算。"
    />

    <template v-else>
      <div class="status-summary" v-if="!loading">
        <div class="summary-cards">
          <div class="summary-card summary-card--total">
            <div class="summary-card__num">{{ courses.length }}</div>
            <div class="summary-card__label">支撑教学班总数</div>
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
          <div class="summary-card summary-card--pending">
            <div class="summary-card__num">{{ pendingUnlockCount }}</div>
            <div class="summary-card__label">待处理解锁申请</div>
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
        <template #title>当前不能执行专业级计算</template>
        <template #default>
          {{ dashboardData.blockReason || '仍有支撑课程未完成计算并锁定。' }}
        </template>
      </el-alert>

      <el-alert
        v-else-if="!loading && dashboardData.aggregationAllowed"
        type="success"
        :closable="false"
        show-icon
        class="aggregation-warning"
      >
        <template #title>当前年级涉及的全部支撑课程均已锁定，可以执行专业级全局达成度计算。</template>
      </el-alert>

      <EmptyState
        v-if="!loading && courses.length === 0"
        description="当前筛选条件下没有支撑课程数据。"
      />

      <el-table
        v-else
        v-loading="loading"
        :data="courses"
        border
        stripe
        max-height="520"
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
        <el-table-column label="解锁申请" min-width="220">
          <template #default="{ row }">
            <div v-if="row.unlockRequested" class="unlock-cell">
              <el-tag type="warning" effect="plain" size="small">待审批</el-tag>
              <div>{{ row.unlockRequestedBy || '课程主讲教师' }}</div>
              <div class="unlock-reason">{{ row.unlockReason }}</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="阻断原因" min-width="220">
          <template #default="{ row }">{{ row.blockReason || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="canApproveUnlock" label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="!row.unlockRequested || row.calcStatus !== 'locked' || unlockingClassId === row.classId"
              @click="approveUnlock(row)"
            >
              {{ unlockingClassId === row.classId ? '解锁中...' : '执行解锁' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-card v-if="majorResult.resultReady" class="major-result-card" shadow="never">
        <template #header>
          <div class="major-result-header">
            <span>专业级全局达成度计算结果</span>
            <el-tag type="success" effect="light">已生成</el-tag>
          </div>
        </template>
        <el-table :data="majorResult.indicatorAchievements || []" border size="small">
          <el-table-column prop="ipCode" label="指标点" width="120" />
          <el-table-column prop="ipDescription" label="指标点描述" min-width="260" />
          <el-table-column prop="finalAchievement" label="专业级达成度 Gk" width="180">
            <template #default="{ row }">{{ formatDecimal(row.finalAchievement) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import { useUserStore } from '@/stores/user'
import {
  approveUnlockApi,
  calculateMajorLevelApi,
  getMacroDashboardDataApi,
  getMajorCalcResultApi,
  listMajorGradeYearTermsApi,
} from '@/api/assessment'

const userStore = useUserStore()
const loading = ref(false)
const aggregating = ref(false)
const unlockingClassId = ref(null)
const loadError = ref('')

const filterOptions = reactive({ majors: [] })
const filters = reactive({ majorId: null, gradeYear: null })

const dashboardData = reactive({
  courses: [],
  aggregationAllowed: false,
  unlockedWarning: false,
  blockReason: '',
  majorResultExists: false,
  termId: null,
  termCode: '',
})

const majorResult = reactive({
  resultReady: false,
  indicatorAchievements: [],
})

const hasFilters = computed(() => !!(filters.majorId && filters.gradeYear))
const canApproveUnlock = computed(() => userStore.roleCodes.includes('academic_affairs'))
const selectedMajor = computed(() => filterOptions.majors.find((item) => item.majorId === filters.majorId) || null)
const currentGradeYears = computed(() => (selectedMajor.value?.gradeYearScopes || []).map((item) => item.gradeYear))
const courses = computed(() => dashboardData.courses || [])
const lockedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'locked').length)
const computedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'calculating').length)
const submittedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'score_imported').length)
const pendingUnlockCount = computed(() => courses.value.filter((c) => c.unlockRequested).length)

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

function buildPayload() {
  return {
    majorId: filters.majorId,
    gradeYear: filters.gradeYear,
    termId: dashboardData.termId,
  }
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
  dashboardData.courses = []
  dashboardData.aggregationAllowed = false
  dashboardData.unlockedWarning = false
  dashboardData.blockReason = ''
  dashboardData.majorResultExists = false
  dashboardData.termId = null
  dashboardData.termCode = ''
  majorResult.resultReady = false
  majorResult.indicatorAchievements = []
}

function handleMajorChange() {
  hydrateDependentFilters()
  clearResults()
  loadDashboard()
}

function handleGradeYearChange() {
  clearResults()
  loadDashboard()
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

async function loadDashboard() {
  if (!hasFilters.value) {
    clearResults()
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
    dashboardData.termId = data?.termId ?? null
    dashboardData.termCode = data?.termCode || ''
    await loadMajorResult()
  } catch (error) {
    loadError.value = error.message || '加载专业级计算看板失败'
    clearResults()
  } finally {
    loading.value = false
  }
}

async function calculateMajor() {
  if (!hasFilters.value || !dashboardData.aggregationAllowed) return
  aggregating.value = true
  try {
    await calculateMajorLevelApi(buildPayload())
    ElMessage.success('专业级全局达成度计算完成')
    await loadDashboard()
  } catch (error) {
    ElMessage.error(error.message || '专业级全局达成度计算失败')
  } finally {
    aggregating.value = false
  }
}

async function approveUnlock(row) {
  try {
    await ElMessageBox.confirm(
      `将解锁教学班 ${row.classCode}，并清空该班已生成的课程级结果，后续需修改成绩重新计算。是否继续？`,
      '执行解锁',
      {
        confirmButtonText: '确认解锁',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    unlockingClassId.value = row.classId
    await approveUnlockApi({ classId: row.classId })
    ElMessage.success('教学班已解锁')
    await loadDashboard()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error.message || '执行解锁失败')
  } finally {
    unlockingClassId.value = null
  }
}

function formatDecimal(value) {
  if (value === undefined || value === null) return '-'
  return Number(value).toFixed(4)
}

async function loadAll() {
  await loadFilterOptions()
  await loadDashboard()
}

onMounted(() => {
  loadAll()
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

.summary-card--pending {
  background: #faf5ff;
  border: 1px solid #e9d5ff;
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

.aggregation-warning,
.major-result-card {
  border-radius: 10px;
}

.major-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.unlock-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.unlock-reason {
  color: #475569;
  line-height: 1.5;
}
</style>
