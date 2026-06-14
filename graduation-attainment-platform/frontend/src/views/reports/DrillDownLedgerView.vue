<template>
  <div class="drill-down-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <h1>穿透式台账下钻查询</h1>
            <p class="page-summary">
              从专业级毕业要求指标点逐层下钻至课程级指标点、课程目标、考核点，最终追溯到原始成绩，支持穿透式 Excel 台账导出。
            </p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <!-- 查询区 -->
        <section class="filter-section">
          <h2>查询区</h2>
          <el-form :inline="true" :model="filters" class="filter-form">
            <el-form-item label="专业">
              <el-select
                v-model="filters.majorId"
                placeholder="请选择专业"
                style="width: 240px"
                @change="onMajorChange"
              >
                <el-option
                  v-for="m in filterOptions.majors"
                  :key="m.majorId"
                  :label="m.majorName"
                  :value="m.majorId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="年级">
              <el-select
                v-model="filters.gradeYear"
                placeholder="请选择年级"
                style="width: 160px"
                :disabled="!filters.majorId"
                @change="onGradeYearChange"
              >
                <el-option
                  v-for="y in currentGradeYears"
                  :key="y"
                  :label="`${y} 级`"
                  :value="y"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" :disabled="!canQuery" @click="loadMajorReport">
                查询
              </el-button>
            </el-form-item>
          </el-form>
        </section>

        <ErrorState v-if="loadError" :message="loadError" @retry="loadMajorReport" />

        <EmptyState
          v-else-if="!hasQueried"
          description="请选择专业和年级后查询穿透式台账，按指标点逐层下钻查看课程级、课程目标、考核点及原始成绩数据。"
        />

        <template v-else>
          <!-- 追溯上下文 -->
          <section class="context-section">
            <h2>追溯上下文</h2>
            <div class="context-display">
              <div class="context-breadcrumb">
                <span class="breadcrumb-item">
                  <span class="label">专业：</span>
                  <el-tag type="primary" effect="plain" size="small">{{ context.majorName || '-' }}</el-tag>
                </span>
                <span class="breadcrumb-sep">&gt;</span>
                <span class="breadcrumb-item">
                  <span class="label">年级：</span>
                  <el-tag type="primary" effect="plain" size="small">{{ context.gradeYear ? `${context.gradeYear} 级` : '-' }}</el-tag>
                </span>
                <span class="breadcrumb-sep">&gt;</span>
                <span class="breadcrumb-item">
                  <span class="label">统计学期：</span>
                  <el-tag type="primary" effect="plain" size="small">{{ context.termCode || '-' }}</el-tag>
                </span>
                <template v-if="context.ipCode">
                  <span class="breadcrumb-sep">&gt;</span>
                  <span class="breadcrumb-item">
                    <span class="label">指标点：</span>
                    <el-tag effect="plain" size="small">{{ context.ipCode }}</el-tag>
                  </span>
                </template>
                <template v-if="context.courseName">
                  <span class="breadcrumb-sep">&gt;</span>
                  <span class="breadcrumb-item">
                    <span class="label">课程：</span>
                    <el-tag effect="plain" size="small">{{ context.courseName }}</el-tag>
                  </span>
                </template>
                <template v-if="context.className">
                  <span class="breadcrumb-sep">&gt;</span>
                  <span class="breadcrumb-item">
                    <span class="label">教学班：</span>
                    <el-tag effect="plain" size="small">{{ context.className }}</el-tag>
                  </span>
                </template>
                <template v-if="context.objectiveCode">
                  <span class="breadcrumb-sep">&gt;</span>
                  <span class="breadcrumb-item">
                    <span class="label">课程目标：</span>
                    <el-tag effect="plain" size="small">{{ context.objectiveCode }}</el-tag>
                  </span>
                </template>
                <template v-if="context.apName">
                  <span class="breadcrumb-sep">&gt;</span>
                  <span class="breadcrumb-item">
                    <span class="label">考核点：</span>
                    <el-tag effect="plain" size="small">{{ context.apName }}</el-tag>
                  </span>
                </template>
              </div>
              <el-button
                v-if="drillLevel > 0"
                :icon="Back"
                size="small"
                @click="goBack"
              >
                返回上一级
              </el-button>
            </div>
          </section>

          <!-- 导出区 -->
          <section class="export-section">
            <div class="section-header">
              <h2>穿透式台账导出</h2>
              <el-button
                type="success"
                :loading="exportStatus === 'exporting'"
                :disabled="exportStatus === 'exporting'"
                @click="exportLedger"
              >
                <el-icon v-if="exportStatus === 'idle'"><Download /></el-icon>
                <el-icon v-if="exportStatus === 'exporting'" class="is-loading"><Loading /></el-icon>
                <el-icon v-if="exportStatus === 'success'"><CircleCheck /></el-icon>
                <el-icon v-if="exportStatus === 'failure'"><CircleClose /></el-icon>
                {{ exportLabel }}
              </el-button>
            </div>
            <p v-if="exportStatus === 'success'" class="export-hint export-hint--success">
              穿透式台账 Excel 导出成功，文件已自动下载。
            </p>
            <p v-else-if="exportStatus === 'failure'" class="export-hint export-hint--failure">
              导出失败：{{ exportMessage }}
            </p>
            <p v-else class="export-hint">
              将当前可追溯的全部层级数据（含指标点、课程、课程目标、考核点及原始成绩）导出为一份结构化 Excel 台账。
            </p>
          </section>

          <!-- Level 0: 专业级指标点 -->
          <section class="drill-section">
            <div class="drill-section-header">
              <h3>
                <el-tag type="primary" effect="dark" size="small">L0</el-tag>
                专业级指标点达成度
              </h3>
              <span class="drill-hint">点击指标点行可下钻查看课程级明细</span>
            </div>
            <el-table
              :data="majorReport.indicatorAchievements"
              border
              stripe
              highlight-current-row
              @row-click="drillToIndicator"
            >
              <el-table-column prop="ipCode" label="指标点编码" width="120" />
              <el-table-column prop="ipDescription" label="指标点描述" min-width="240" show-overflow-tooltip />
              <el-table-column prop="grCode" label="毕业要求" width="120" align="center" />
              <el-table-column label="专业级达成度 Gk" width="160" align="center">
                <template #default="{ row }">
                  <span :class="getAchievementClass(row.finalAchievement)">
                    {{ formatAchievement(row.finalAchievement) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="contributingCourseCount" label="支撑课程数" width="120" align="center" />
              <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">下钻 &gt;</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无专业级指标点数据，请先执行专业级计算" :image-size="80" />
              </template>
            </el-table>
          </section>

          <!-- Level 1: 课程级指标点 -->
          <section v-if="drillLevel >= 1 && selectedIndicator" class="drill-section">
            <div class="drill-section-header">
              <h3>
                <el-tag type="success" effect="dark" size="small">L1</el-tag>
                课程级指标点达成度
                <span class="drill-context">（指标点：{{ selectedIndicator.ipCode }} {{ selectedIndicator.ipDescription }}）</span>
              </h3>
              <span class="drill-hint">点击课程行可下钻查看课程目标明细</span>
            </div>
            <el-table
              :data="selectedIndicator.contributingCourses"
              border
              stripe
              highlight-current-row
              @row-click="drillToCourse"
            >
              <el-table-column prop="courseCode" label="课程代码" width="140" />
              <el-table-column prop="courseName" label="课程名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="className" label="教学班" min-width="160" show-overflow-tooltip />
              <el-table-column label="课程级达成度 Ek" width="160" align="center">
                <template #default="{ row }">
                  <span :class="getAchievementClass(row.courseAchievement)">
                    {{ formatAchievement(row.courseAchievement) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="宏观权重 W" width="140" align="center">
                <template #default="{ row }">
                  {{ formatWeight(row.totalWeight) }}
                </template>
              </el-table-column>
              <el-table-column label="加权贡献 Ek×W" width="160" align="center">
                <template #default="{ row }">
                  {{ formatWeight(row.weightedContribution) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">下钻 &gt;</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="该指标点下暂无支撑课程数据" :image-size="80" />
              </template>
            </el-table>
          </section>

          <!-- Level 2: 课程目标 -->
          <section v-if="drillLevel >= 2 && courseReportData" class="drill-section">
            <div class="drill-section-header">
              <h3>
                <el-tag type="warning" effect="dark" size="small">L2</el-tag>
                课程目标达成度
                <span class="drill-context">（课程：{{ context.courseName }} | 教学班：{{ context.className }}）</span>
              </h3>
              <span class="drill-hint">点击课程目标行可下钻查看考核点明细</span>
            </div>
            <el-table
              v-if="currentClassDetail"
              :data="currentClassDetail.objectiveAchievementDetails"
              border
              stripe
              highlight-current-row
              @row-click="drillToObjective"
            >
              <el-table-column prop="objectiveCode" label="目标编码" width="120" align="center" />
              <el-table-column prop="description" label="目标描述" min-width="260" show-overflow-tooltip />
              <el-table-column label="班级平均达成度" width="160" align="center">
                <template #default="{ row }">
                  <span :class="getAchievementClass(row.averageAchievement)">
                    {{ formatAchievement(row.averageAchievement) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">下钻 &gt;</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="该课程下暂无课程目标数据" :image-size="80" />
              </template>
            </el-table>
            <EmptyState v-else description="该课程下暂无教学班明细数据" />
          </section>

          <!-- Level 3: 考核点 -->
          <section v-if="drillLevel >= 3 && selectedObjective && currentClassDetail" class="drill-section">
            <div class="drill-section-header">
              <h3>
                <el-tag type="danger" effect="dark" size="small">L3</el-tag>
                考核点得分概况
                <span class="drill-context">（课程目标：{{ selectedObjective.objectiveCode }} {{ selectedObjective.description }}）</span>
              </h3>
              <span class="drill-hint">点击考核点行可下钻查看原始成绩</span>
            </div>
            <el-table
              :data="currentClassDetail.assessmentPointAverages"
              border
              stripe
              highlight-current-row
              @row-click="drillToAssessmentPoint"
            >
              <el-table-column prop="apName" label="考核点名称" min-width="220" show-overflow-tooltip />
              <el-table-column prop="fullScore" label="满分" width="100" align="center" />
              <el-table-column label="平均分" width="120" align="center">
                <template #default="{ row }">
                  {{ formatScore(row.averageScore) }}
                </template>
              </el-table-column>
              <el-table-column label="得分率" width="120" align="center">
                <template #default="{ row }">
                  <span :class="getAchievementClass(row.scoreRate)">
                    {{ formatAchievement(row.scoreRate) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">下钻 &gt;</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无考核点数据" :image-size="80" />
              </template>
            </el-table>
          </section>

          <!-- Level 4: 原始成绩 -->
          <section v-if="drillLevel >= 4 && rawScores.rows && rawScores.rows.length" class="drill-section">
            <div class="drill-section-header">
              <h3>
                <el-tag class="level-tag--l4" size="small">L4</el-tag>
                原始成绩明细
                <span class="drill-context">
                  （考核点：{{ selectedAssessmentPoint.apName }} | 满分：{{ selectedAssessmentPoint.fullScore }}）
                </span>
              </h3>
              <span class="drill-count">共 {{ rawScores.rows.length }} 名学生</span>
            </div>
            <el-table :data="rawScores.rows" border stripe max-height="400">
              <el-table-column prop="studentNo" label="学号" width="140" />
              <el-table-column prop="studentName" label="姓名" width="120" />
              <el-table-column label="成绩" width="120" align="center">
                <template #default="{ row }">
                  {{ formatScore(row.actualScore) }}
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无原始成绩数据" :image-size="80" />
              </template>
            </el-table>
          </section>

          <div v-if="!majorReport.indicatorAchievements?.length && !loading" class="no-result">
            <el-empty description="当前筛选条件下无专业级指标点数据，请先确认已执行专业级计算" :image-size="120" />
          </div>
        </template>

        <div v-if="loading" class="loading-mask">
          <el-skeleton :rows="6" animated />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Back, CircleCheck, CircleClose, Download, Loading } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import { listMajorGradeYearTermsApi } from '@/api/assessment'
import {
  exportAchievementLedgerApi,
  getCourseToObjectiveTraceApi,
  getObjectiveToScoreTraceApi,
} from '@/api/achievementTrace'
import { getMajorReportApi, triggerDownload as triggerLedgerDownload } from '@/api/report'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const hasAccess = computed(() =>
  userStore.roleCodes.some((r) => ['program_director', 'academic_affairs'].includes(r)),
)

// Filters
const filterOptions = reactive({ majors: [] })
const filters = reactive({ majorId: null, gradeYear: null })
const canQuery = computed(() => !!(filters.majorId && filters.gradeYear))
const hasQueried = ref(false)
const loading = ref(false)
const loadError = ref('')

const selectedMajor = computed(() =>
  filterOptions.majors.find((m) => m.majorId === filters.majorId) || null,
)
const currentGradeYears = computed(() =>
  (selectedMajor.value?.gradeYearScopes || []).map((item) => item.gradeYear),
)

// Context
const context = reactive({
  majorName: '',
  gradeYear: null,
  termCode: '',
  ipCode: '',
  courseName: '',
  className: '',
  objectiveCode: '',
  apName: '',
})

// Drill-down state
const drillLevel = ref(0)
const selectedIndicator = ref(null)
const selectedCourseIdx = ref(-1)
const selectedObjective = ref(null)
const selectedAssessmentPoint = ref(null)
const selectedApIndex = ref(-1)

// Data
const majorReport = reactive({ indicatorAchievements: [], termId: null, termCode: '' })
const courseReportData = ref(null)
const courseLoading = ref(false)
const rawScores = reactive({ rows: [], headers: [] })
const rawScoreLoading = ref(false)

const currentClassDetail = computed(() => {
  if (!courseReportData.value) return null
  const classes = courseReportData.value.teachingClasses || []
  return classes.length > 0 ? classes[0] : null
})

// Export
const exportStatus = ref('idle') // 'idle' | 'exporting' | 'success' | 'failure'
const exportMessage = ref('')

const exportLabel = computed(() => {
  const map = { idle: '导出穿透式台账 Excel', exporting: '导出中...', success: '导出成功', failure: '导出失败' }
  return map[exportStatus] || '导出穿透式台账 Excel'
})

// Format helpers
function formatAchievement(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(4)
}

function formatScore(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

function formatWeight(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(4)
}

function getAchievementClass(val) {
  if (val === null || val === undefined) return ''
  return Number(val) >= 0.6 ? 'achievement-pass' : 'achievement-fail'
}

// Load filter options
async function loadFilterOptions() {
  try {
    const data = await listMajorGradeYearTermsApi()
    filterOptions.majors = data?.majors || []
    if (filterOptions.majors.length && !filters.majorId) {
      filters.majorId = filterOptions.majors[0].majorId
      hydrateGradeYears()
    }
  } catch {
    ElMessage.error('加载筛选项失败')
  }
}

function hydrateGradeYears() {
  const years = currentGradeYears.value
  if (!years.includes(filters.gradeYear)) {
    filters.gradeYear = years[0] || null
  }
}

function onMajorChange() {
  hydrateGradeYears()
  resetAll()
}

function onGradeYearChange() {
  resetAll()
}

function resetAll() {
  drillLevel.value = 0
  selectedIndicator.value = null
  selectedCourseIdx.value = -1
  selectedObjective.value = null
  selectedAssessmentPoint.value = null
  selectedApIndex.value = -1
  majorReport.indicatorAchievements = []
  majorReport.termId = null
  majorReport.termCode = ''
  courseReportData.value = null
  rawScores.rows = []
  rawScores.headers = []
  context.majorName = ''
  context.gradeYear = null
  context.termCode = ''
  context.ipCode = ''
  context.courseName = ''
  context.className = ''
  context.objectiveCode = ''
  context.apName = ''
  hasQueried.value = false
  loadError.value = ''
  exportStatus.value = 'idle'
  exportMessage.value = ''
}

// Load major report
async function loadMajorReport() {
  if (!canQuery.value) return
  loading.value = true
  loadError.value = ''
  resetAll()
  hasQueried.value = true

  try {
    const data = await getMajorReportApi({
      majorId: filters.majorId,
      gradeYear: filters.gradeYear,
    })
    majorReport.indicatorAchievements = data?.indicatorAchievements || []
    majorReport.termId = data?.termId
    majorReport.termCode = data?.termCode || ''

    context.majorName = data?.majorName || selectedMajor.value?.majorName || ''
    context.gradeYear = data?.gradeYear || filters.gradeYear
    context.termCode = data?.termCode || ''

    if (!majorReport.indicatorAchievements.length) {
      ElMessage.info('当前筛选条件下暂无指标点数据')
    }
  } catch (error) {
    loadError.value = error.message || '加载专业级指标点数据失败'
  } finally {
    loading.value = false
  }
}

// Drill to Level 1: Course-level indicators
function drillToIndicator(row) {
  if (!row || !row.contributingCourses?.length) {
    ElMessage.info('该指标点下暂无支撑课程数据')
    return
  }
  selectedIndicator.value = row
  selectedCourseIdx.value = -1
  selectedObjective.value = null
  selectedAssessmentPoint.value = null
  selectedApIndex.value = -1
  courseReportData.value = null
  rawScores.rows = []
  drillLevel.value = 1
  context.ipCode = row.ipCode
  context.courseName = ''
  context.className = ''
  context.objectiveCode = ''
  context.apName = ''
}

// Drill to Level 2: Course objectives
async function drillToCourse(row) {
  if (!row) return
  selectedCourseIdx.value = selectedIndicator.value?.contributingCourses?.indexOf(row) ?? -1
  selectedObjective.value = null
  selectedAssessmentPoint.value = null
  selectedApIndex.value = -1
  rawScores.rows = []

  context.courseName = row.courseName || row.courseCode || ''
  context.className = row.className || ''
  context.objectiveCode = ''
  context.apName = ''

  courseLoading.value = true
  courseReportData.value = null
  drillLevel.value = 2

  try {
    const data = await getCourseToObjectiveTraceApi({
      classId: row.classId,
      ipId: selectedIndicator.value?.ipId,
    })
    courseReportData.value = {
      courseId: data?.courseId,
      courseCode: data?.courseCode,
      courseName: data?.courseName,
      teachingClasses: [
        {
          classId: data?.classId,
          classCode: data?.classCode,
          className: data?.className,
          objectiveAchievementDetails: (data?.objectiveContributions || []).map((item) => ({
            coId: item.coId,
            objectiveCode: item.objectiveCode,
            description: item.coDescription,
            averageAchievement: item.objectiveAchievement,
            internalWeight: item.internalWeight,
            weightedContribution: item.weightedContribution,
          })),
          assessmentPointAverages: [],
        },
      ],
    }
    if (!currentClassDetail.value?.objectiveAchievementDetails?.length) {
      ElMessage.info('该课程下暂无课程目标数据')
    }
  } catch {
    drillLevel.value = 1
  } finally {
    courseLoading.value = false
  }
}

// Drill to Level 3: Assessment points
async function drillToObjective(row) {
  if (!row) return
  selectedObjective.value = row
  selectedAssessmentPoint.value = null
  selectedApIndex.value = -1
  rawScores.rows = []
  drillLevel.value = 3
  context.objectiveCode = row.objectiveCode
  context.apName = ''

  if (!currentClassDetail.value?.classId || !row.coId) {
    ElMessage.info('无法获取课程目标追溯信息')
    return
  }

  try {
    const data = await getObjectiveToScoreTraceApi({
      classId: currentClassDetail.value.classId,
      coId: row.coId,
    })
    currentClassDetail.value.assessmentPointAverages = (data?.assessmentPoints || []).map((item) => ({
      apId: item.apId,
      apName: item.apName,
      fullScore: item.fullScore,
      averageScore: item.averageScore,
      scoreRate:
        item.fullScore && item.averageScore !== null && item.averageScore !== undefined
          ? Number(item.averageScore) / Number(item.fullScore)
          : null,
      studentScores: item.studentScores || [],
    }))

    if (!currentClassDetail.value.assessmentPointAverages.length) {
      ElMessage.info('暂无考核点数据')
    }
  } catch {
    drillLevel.value = 2
  }
}

// Drill to Level 4: Raw scores
function drillToAssessmentPoint(row) {
  if (!row) return
  selectedAssessmentPoint.value = row
  rawScores.rows = []
  drillLevel.value = 4
  context.apName = row.apName

  if (!currentClassDetail.value?.classId) {
    ElMessage.info('无法获取教学班信息')
    return
  }

  const apIndex = currentClassDetail.value.assessmentPointAverages?.indexOf(row) ?? -1
  selectedApIndex.value = apIndex
  rawScores.rows = (row.studentScores || []).map((item) => ({
    studentNo: item.studentNo,
    studentName: item.studentName,
    actualScore: item.actualScore,
  }))
  rawScores.headers = []
}

// Go back one level
function goBack() {
  if (drillLevel.value <= 1) {
    drillLevel.value = 0
    selectedIndicator.value = null
    selectedCourseIdx.value = -1
    selectedObjective.value = null
    selectedAssessmentPoint.value = null
    selectedApIndex.value = -1
    courseReportData.value = null
    rawScores.rows = []
    rawScores.headers = []
    context.ipCode = ''
    context.courseName = ''
    context.className = ''
    context.objectiveCode = ''
    context.apName = ''
  } else if (drillLevel.value === 2) {
    drillLevel.value = 1
    selectedObjective.value = null
    selectedAssessmentPoint.value = null
    selectedApIndex.value = -1
    courseReportData.value = null
    rawScores.rows = []
    rawScores.headers = []
    context.objectiveCode = ''
    context.apName = ''
  } else if (drillLevel.value === 3) {
    drillLevel.value = 2
    selectedAssessmentPoint.value = null
    selectedApIndex.value = -1
    rawScores.rows = []
    rawScores.headers = []
    context.apName = ''
  } else if (drillLevel.value === 4) {
    drillLevel.value = 3
    rawScores.rows = []
    rawScores.headers = []
    context.apName = ''
  }
}

// Export ledger
async function exportLedger() {
  if (exportStatus.value === 'exporting') return
  if (!canQuery.value) {
    ElMessage.info('请先选择专业和年级')
    return
  }
  exportStatus.value = 'exporting'
  exportMessage.value = ''

  const majorName = context.majorName || selectedMajor.value?.majorName || '专业'
  const gradeYear = context.gradeYear || filters.gradeYear || ''

  try {
    const blob = await exportAchievementLedgerApi({
      majorId: filters.majorId,
      gradeYear: filters.gradeYear,
      termId: majorReport.termId || undefined,
    })
    triggerLedgerDownload(blob, `穿透式台账_${majorName}_${gradeYear}级.xlsx`)
    exportStatus.value = 'success'
    exportMessage.value = ''
    setTimeout(() => {
      if (exportStatus.value === 'success') exportStatus.value = 'idle'
    }, 3000)
    return
  } catch (error) {
    exportStatus.value = 'failure'
    exportMessage.value = error.message || '导出失败，请重试'
    setTimeout(() => {
      if (exportStatus.value === 'failure') exportStatus.value = 'idle'
    }, 4000)
    return
  }

  try {
    const htmlParts = []
    htmlParts.push('<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">')
    htmlParts.push('<head><meta charset="UTF-8"><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>穿透式台账</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head>')
    htmlParts.push('<body>')

    // Title
    const majorName = context.majorName || selectedMajor.value?.majorName || ''
    const gradeYear = context.gradeYear || filters.gradeYear || ''
    htmlParts.push(`<h2>穿透式台账 — ${majorName} ${gradeYear}级</h2>`)
    htmlParts.push(`<p>统计学期：${context.termCode || '-'} | 导出时间：${new Date().toLocaleString()}</p>`)

    // Level 0: Major indicators
    if (majorReport.indicatorAchievements?.length) {
      htmlParts.push('<h3>专业级指标点达成度</h3>')
      htmlParts.push('<table border="1"><tr><th>指标点编码</th><th>指标点描述</th><th>毕业要求</th><th>专业级达成度 Gk</th><th>支撑课程数</th></tr>')
      for (const ip of majorReport.indicatorAchievements) {
        htmlParts.push(`<tr><td>${esc(ip.ipCode)}</td><td>${esc(ip.ipDescription)}</td><td>${esc(ip.grCode)}</td><td>${formatAchievement(ip.finalAchievement)}</td><td>${ip.contributingCourseCount ?? 0}</td></tr>`)
      }
      htmlParts.push('</table>')

      // Level 1: Course indicators for each IP
      for (const ip of majorReport.indicatorAchievements) {
        if (!ip.contributingCourses?.length) continue
        htmlParts.push(`<h3>指标点 ${esc(ip.ipCode)} — 支撑课程明细</h3>`)
        htmlParts.push('<table border="1"><tr><th>课程代码</th><th>课程名称</th><th>教学班</th><th>课程级达成度 Ek</th><th>宏观权重 W</th><th>加权贡献</th></tr>')
        for (const cc of ip.contributingCourses) {
          htmlParts.push(`<tr><td>${esc(cc.courseCode)}</td><td>${esc(cc.courseName)}</td><td>${esc(cc.className)}</td><td>${formatAchievement(cc.courseAchievement)}</td><td>${formatWeight(cc.totalWeight)}</td><td>${formatWeight(cc.weightedContribution)}</td></tr>`)
        }
        htmlParts.push('</table>')
      }
    }

    // Level 2: Course objectives (if data loaded)
    if (courseReportData.value) {
      htmlParts.push(`<h3>课程目标达成度 — ${esc(context.courseName)}</h3>`)

      // Objectives
      const detail = currentClassDetail.value
      if (detail?.objectiveAchievementDetails?.length) {
        htmlParts.push('<table border="1"><tr><th>目标编码</th><th>目标描述</th><th>班级平均达成度</th></tr>')
        for (const obj of detail.objectiveAchievementDetails) {
          htmlParts.push(`<tr><td>${esc(obj.objectiveCode)}</td><td>${esc(obj.description)}</td><td>${formatAchievement(obj.averageAchievement)}</td></tr>`)
        }
        htmlParts.push('</table>')
      }

      // Assessment points
      if (detail?.assessmentPointAverages?.length) {
        htmlParts.push('<h3>考核点得分概况</h3>')
        htmlParts.push('<table border="1"><tr><th>考核点名称</th><th>满分</th><th>平均分</th><th>得分率</th></tr>')
        for (const ap of detail.assessmentPointAverages) {
          htmlParts.push(`<tr><td>${esc(ap.apName)}</td><td>${ap.fullScore ?? '-'}</td><td>${formatScore(ap.averageScore)}</td><td>${formatAchievement(ap.scoreRate)}</td></tr>`)
        }
        htmlParts.push('</table>')
      }
    }

    // Level 4: Raw scores
    if (rawScores.rows?.length) {
      htmlParts.push(`<h3>原始成绩明细 — ${esc(context.courseName)} | ${esc(context.apName || '')}</h3>`)
      htmlParts.push('<table border="1"><tr><th>学号</th><th>姓名</th><th>成绩</th></tr>')
      for (const row of rawScores.rows) {
        htmlParts.push(`<tr><td>${esc(row.studentNo)}</td><td>${esc(row.studentName)}</td><td>${formatScore(row.scores?.[selectedApIndex.value])}</td></tr>`)
      }
      htmlParts.push('</table>')
    }

    // Course objectives for all courses in the current indicator (if available)
    if (selectedIndicator.value?.contributingCourses?.length && drillLevel.value >= 1) {
      htmlParts.push('<h3>全部支撑课程课程级指标点达成度</h3>')
      htmlParts.push('<table border="1"><tr><th>课程代码</th><th>课程名称</th><th>教学班</th><th>课程级达成度 Ek</th><th>宏观权重 W</th><th>加权贡献</th></tr>')
      for (const cc of selectedIndicator.value.contributingCourses) {
        htmlParts.push(`<tr><td>${esc(cc.courseCode)}</td><td>${esc(cc.courseName)}</td><td>${esc(cc.className)}</td><td>${formatAchievement(cc.courseAchievement)}</td><td>${formatWeight(cc.totalWeight)}</td><td>${formatWeight(cc.weightedContribution)}</td></tr>`)
      }
      htmlParts.push('</table>')
    }

    htmlParts.push('</body></html>')

    const blob = new Blob([htmlParts.join('')], { type: 'application/vnd.ms-excel;charset=utf-8' })
    const fileName = `穿透式台账_${majorName}_${gradeYear}级.xls`
    triggerLedgerDownload(blob, fileName)
    exportStatus.value = 'success'
    exportMessage.value = ''
    setTimeout(() => {
      if (exportStatus.value === 'success') exportStatus.value = 'idle'
    }, 3000)
  } catch (error) {
    exportStatus.value = 'failure'
    exportMessage.value = error.message || '导出失败，请重试'
    setTimeout(() => {
      if (exportStatus.value === 'failure') exportStatus.value = 'idle'
    }, 4000)
  }
}

function esc(val) {
  if (val === null || val === undefined) return '-'
  return String(val).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

onMounted(async () => {
  if (!hasAccess.value) {
    ElMessage.error('无权访问此页面')
    router.replace(DEFAULT_HOME_PATH)
    return
  }
  await loadFilterOptions()
})
</script>

<style scoped>
.drill-down-page {
  padding: 20px;
}

.page-card {
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
}

.page-summary {
  margin: 0;
  color: #64748b;
  line-height: 1.75;
  max-width: none;
}

.page-content {
  display: grid;
  gap: 24px;
}

.filter-section,
.context-section,
.export-section,
.drill-section {
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.filter-section h2,
.context-section h2,
.export-section h2 {
  margin: 0 0 14px;
  color: #1f2937;
  font-size: 18px;
}

.filter-form {
  margin-bottom: 0;
}

.context-display {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.context-breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  line-height: 2;
}

.breadcrumb-item {
  white-space: nowrap;
}

.breadcrumb-item .label {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
}

.breadcrumb-sep {
  color: #94a3b8;
  font-weight: 600;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}

.export-hint {
  margin: 8px 0 0;
  font-size: 13px;
  color: #64748b;
}

.export-hint--success {
  color: #16a34a;
  font-weight: 500;
}

.export-hint--failure {
  color: #dc2626;
  font-weight: 500;
}

.drill-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 8px;
}

.drill-section-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #1f2937;
}

.drill-context {
  font-size: 13px;
  font-weight: 400;
  color: #64748b;
}

.drill-section-header :deep(.level-tag--l4) {
  background-color: #0f766e;
  border-color: #0f766e;
  color: #fff;
}

.drill-hint {
  font-size: 12px;
  color: #94a3b8;
}

.drill-count {
  font-size: 13px;
  color: #64748b;
}

.loading-mask {
  padding: 16px 0;
}

.no-result {
  padding: 40px 0;
}

.achievement-pass {
  font-weight: 600;
  color: #16a34a;
}

.achievement-fail {
  font-weight: 600;
  color: #dc2626;
}
</style>
