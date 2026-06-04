<template>
  <div class="macro-dashboard">
    <div class="filter-bar">
      <el-select
        v-model="filters.majorId"
        placeholder="选择专业"
        style="width: 200px"
        @change="handleMajorChange"
      >
        <el-option
          v-for="m in filterOptions.majors"
          :key="m.majorId"
          :label="m.majorName"
          :value="m.majorId"
        />
      </el-select>
      <el-select
        v-model="filters.gradeYear"
        placeholder="选择年级"
        style="width: 160px"
        @change="loadDashboard"
      >
        <el-option
          v-for="y in filterOptions.gradeYears"
          :key="y"
          :label="`${y}级`"
          :value="y"
        />
      </el-select>
      <el-select
        v-model="filters.termId"
        placeholder="选择学期"
        style="width: 200px"
        @change="loadDashboard"
      >
        <el-option
          v-for="t in filterOptions.terms"
          :key="t.termId"
          :label="t.termCode"
          :value="t.termId"
        />
      </el-select>
      <el-button @click="loadDashboard" :loading="loading">查询</el-button>
    </div>

    <ErrorState v-if="loadError" :message="loadError" @retry="loadAll" />

    <EmptyState
      v-else-if="!hasFilters"
      description="请选择专业、年级和学期以查看宏观看板"
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
          <strong>无法执行专业级全局达成度计算</strong>
          &mdash;
          存在 <strong>{{ unlockedCount }}</strong> 门课程未锁定，
          请确保所有支撑课程均已完成计算并锁定后再执行专业级汇总。
        </template>
      </el-alert>

      <el-alert
        v-else-if="!loading && dashboardData.aggregationAllowed"
        type="success"
        :closable="false"
        show-icon
        class="aggregation-warning"
      >
        <template #title>
          所有支撑课程均已锁定，可以进行专业级汇总计算。
        </template>
      </el-alert>

      <EmptyState
        v-if="!loading && courses.length === 0"
        description="未找到符合条件的支撑课程数据"
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
        <el-table-column prop="classCode" label="班级代码" width="120" />
        <el-table-column prop="teacherName" label="任课教师" width="110" />
        <el-table-column label="学生数" width="80" align="center">
          <template #default="{ row }">{{ row.studentCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="成绩提交数" width="110" align="center">
          <template #default="{ row }">{{ row.scoreCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="计算状态" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.calcStatus)" effect="light" size="small">
              {{ statusLabel(row.calcStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否可汇总" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.calcStatus === 'locked' ? 'success' : 'info'" effect="plain" size="small">
              {{ row.calcStatus === 'locked' ? '可汇总' : '不可汇总' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import { getMacroDashboardDataApi, listMajorGradeYearTermsApi } from '@/api/assessment'

const loading = ref(false)
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
})

const hasFilters = computed(() => filters.majorId || filters.gradeYear || filters.termId)

const courses = computed(() => dashboardData.courses || [])
const lockedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'locked').length)
const computedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'calculating').length)
const submittedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'score_imported').length)
const unsubmittedCount = computed(() => courses.value.filter((c) => c.calcStatus === 'unsubmitted').length)
const unlockedCount = computed(() => courses.value.filter((c) => c.calcStatus !== 'locked').length)

function statusType(status) {
  const map = { unsubmitted: 'info', score_imported: 'warning', calculating: 'primary', locked: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { unsubmitted: '未提交', score_imported: '已提交未计算', calculating: '已计算未锁定', locked: '已锁定' }
  return map[status] || status || '-'
}

async function loadFilterOptions() {
  try {
    const data = await listMajorGradeYearTermsApi()
    if (data) {
      filterOptions.majors = data.majors || []
      filterOptions.gradeYears = data.gradeYears || []
      filterOptions.terms = data.terms || []
    }
  } catch {
    // silently handle
  }
}

function handleMajorChange() {
  filters.gradeYear = null
  filters.termId = null
  loadDashboard()
}

async function loadDashboard() {
  if (!filters.majorId && !filters.gradeYear && !filters.termId) {
    dashboardData.courses = []
    dashboardData.aggregationAllowed = false
    dashboardData.unlockedWarning = false
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const data = await getMacroDashboardDataApi({
      majorId: filters.majorId || undefined,
      gradeYear: filters.gradeYear || undefined,
      termId: filters.termId || undefined,
    })
    dashboardData.courses = data.courses || []
    dashboardData.aggregationAllowed = data.aggregationAllowed ?? true
    dashboardData.unlockedWarning = data.unlockedWarning ?? false

    if (data.filterOptions) {
      filterOptions.majors = data.filterOptions.majors || filterOptions.majors
      filterOptions.gradeYears = data.filterOptions.gradeYears || filterOptions.gradeYears
      filterOptions.terms = data.filterOptions.terms || filterOptions.terms
    }
  } catch (e) {
    loadError.value = e.message || '加载宏观看板数据失败'
    dashboardData.courses = []
  } finally {
    loading.value = false
  }
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

.status-summary {
  margin-bottom: 0;
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
  line-height: 1.2;
}

.summary-card--total .summary-card__num { color: #2563eb; }
.summary-card--locked .summary-card__num { color: #16a34a; }
.summary-card--computed .summary-card__num { color: #ca8a04; }
.summary-card--submitted .summary-card__num { color: #ea580c; }
.summary-card--unsubmitted .summary-card__num { color: #6b7280; }

.summary-card__label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.aggregation-warning {
  margin: 0;
}
</style>
