<template>
  <div class="course-report-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 D：报表生成与底稿导出</p>
            <h1>课程级评价报表</h1>
            <p class="page-summary">输出《课程目标达成情况评价表》，覆盖该课程该年级下全部相关教学班，供课程目标与指标点达成情况说明使用。</p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <!-- 上下文选择区 -->
        <section class="context-section">
          <h2>报表主体：课程 / 年级</h2>
          <div class="context-note">
            报表以「课程 + 年级」为主体，教学班为明细范围。选择后可预览或导出完整评价报表。
          </div>
          <el-form :inline="true" :model="filters" class="filter-form">
            <el-form-item label="课程">
              <el-select
                v-model="filters.courseId"
                placeholder="请选择课程"
                style="width: 220px"
                @change="onCourseChange"
              >
                <el-option
                  v-for="c in courses"
                  :key="c.courseId"
                  :label="`${c.courseCode} - ${c.courseName}`"
                  :value="c.courseId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="年级">
              <el-select
                v-model="filters.gradeYear"
                placeholder="请选择年级"
                style="width: 160px"
                :disabled="!filters.courseId"
                @change="onGradeYearChange"
              >
                <el-option
                  v-for="y in gradeYears"
                  :key="y"
                  :label="`${y} 级`"
                  :value="y"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" :disabled="!canQuery" @click="loadReport">
                查询报表
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 当前上下文展示 -->
          <div v-if="reportData" class="context-display">
            <el-row :gutter="16">
              <el-col :span="6"><span class="label">课程：</span>{{ reportData.courseName }}</el-col>
              <el-col :span="4"><span class="label">年级：</span>{{ reportData.gradeYear }} 级</el-col>
              <el-col :span="6"><span class="label">专业：</span>{{ reportData.majorName }}</el-col>
              <el-col :span="4"><span class="label">涉及教学班：</span>{{ reportData.classCount }} 个</el-col>
            </el-row>
          </div>
        </section>

        <template v-if="reportData">
          <!-- 导出入口 -->
          <section class="export-section">
            <div class="section-header">
              <h2>导出课程级评价报表</h2>
              <div class="export-actions">
                <el-button
                  type="success"
                  :loading="exportingExcel"
                  :icon="Download"
                  @click="exportExcel"
                >
                  导出 Excel
                </el-button>
                <el-button
                  type="danger"
                  :loading="exportingPdf"
                  :icon="DocumentIcon"
                  @click="exportPdf"
                >
                  导出 PDF
                </el-button>
              </div>
            </div>
            <p class="export-hint">
              报表主体为「{{ reportData.courseName }} / {{ reportData.gradeYear }} 级」，教学班为明细范围，所有达成度保留 4 位小数。
            </p>
          </section>

          <!-- 各教学班单项平均分 -->
          <section class="report-section">
            <h2>各教学班单项平均分</h2>
            <el-table :data="reportData.classScoreSummaries" border stripe>
              <el-table-column prop="className" label="教学班" min-width="160" />
              <el-table-column prop="studentCount" label="人数" width="80" align="center" />
              <el-table-column
                v-for="ap in reportData.assessmentPoints"
                :key="ap.apId"
                :label="`${ap.apName}（满分${ap.fullScore}）`"
                min-width="140"
                align="center"
              >
                <template #default="{ row }">
                  {{ formatScore(row.apAverages?.[ap.apId]) }}
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无数据" />
              </template>
            </el-table>
          </section>

          <!-- 各课程目标达成度明细 -->
          <section class="report-section">
            <h2>各课程目标达成度明细</h2>
            <el-table :data="reportData.objectiveAchievements" border stripe>
              <el-table-column prop="objectiveCode" label="目标编号" width="100" align="center" />
              <el-table-column prop="objectiveName" label="目标名称" min-width="160" />
              <el-table-column
                v-for="cls in reportData.classSummaries"
                :key="cls.classId"
                :label="cls.className"
                min-width="130"
                align="center"
              >
                <template #default="{ row }">
                  {{ formatAchievement(row.classAchievements?.[cls.classId]) }}
                </template>
              </el-table-column>
              <el-table-column label="课程级均值" width="120" align="center">
                <template #default="{ row }">
                  <span class="highlight">{{ formatAchievement(row.courseAverage) }}</span>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无数据" />
              </template>
            </el-table>
          </section>

          <!-- 课程级指标点达成度 -->
          <section class="report-section">
            <h2>课程级指标点达成度</h2>
            <el-table :data="reportData.indicatorAchievements" border stripe>
              <el-table-column prop="ipCode" label="指标点编号" width="120" align="center" />
              <el-table-column prop="ipDescription" label="指标点描述" min-width="200" />
              <el-table-column
                v-for="cls in reportData.classSummaries"
                :key="cls.classId"
                :label="cls.className"
                min-width="130"
                align="center"
              >
                <template #default="{ row }">
                  {{ formatAchievement(row.classAchievements?.[cls.classId]) }}
                </template>
              </el-table-column>
              <el-table-column label="课程级达成度" width="130" align="center">
                <template #default="{ row }">
                  <span :class="getAchievementClass(row.courseAchievement)">
                    {{ formatAchievement(row.courseAchievement) }}
                  </span>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无数据" />
              </template>
            </el-table>
          </section>
        </template>

        <el-empty
          v-else-if="!loading"
          description="请先选择课程和年级，再查询课程级评价报表"
          :image-size="120"
        />

        <div v-if="loading" class="loading-mask">
          <el-skeleton :rows="8" animated />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Document as DocumentIcon } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import { listMyTeachingClassesApi } from '@/api/teacherContext'
import { getCourseReportApi, exportCourseReportExcelApi, exportCourseReportPdfApi } from '@/api/courseReport'

const router = useRouter()
const userStore = useUserStore()

const isInstructor = computed(() => userStore.roleCodes.includes('instructor'))

const filters = reactive({ courseId: null, gradeYear: null })
const courses = ref([])
const gradeYears = ref([])
const reportData = ref(null)
const loading = ref(false)
const exportingExcel = ref(false)
const exportingPdf = ref(false)

const canQuery = computed(() => filters.courseId && filters.gradeYear)

async function loadCourses() {
  try {
    const list = await listMyTeachingClassesApi({})
    const seen = new Set()
    courses.value = list.filter((item) => {
      if (seen.has(item.courseId)) return false
      seen.add(item.courseId)
      return true
    })
  } catch {
    ElMessage.error('加载课程列表失败')
  }
}

function onCourseChange() {
  filters.gradeYear = null
  gradeYears.value = []
  reportData.value = null
  if (!filters.courseId) return
  const matched = courses.value.filter((c) => c.courseId === filters.courseId)
  const years = [...new Set(matched.map((c) => c.gradeYear).filter(Boolean))]
  gradeYears.value = years.sort((a, b) => b - a)
}

function onGradeYearChange() {
  reportData.value = null
}

async function loadReport() {
  if (!canQuery.value) return
  loading.value = true
  reportData.value = null
  try {
    const data = await getCourseReportApi({
      courseId: filters.courseId,
      gradeYear: filters.gradeYear,
    })
    reportData.value = data
  } catch {
    ElMessage.error('加载报表数据失败')
  } finally {
    loading.value = false
  }
}

function formatAchievement(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(4)
}

function formatScore(val) {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

function getAchievementClass(val) {
  if (val === null || val === undefined) return ''
  return Number(val) >= 0.7 ? 'achievement-pass' : 'achievement-fail'
}

async function exportExcel() {
  exportingExcel.value = true
  try {
    const blob = await exportCourseReportExcelApi({
      courseId: filters.courseId,
      gradeYear: filters.gradeYear,
    })
    triggerDownload(blob, `课程评价报表_${reportData.value?.courseName}_${filters.gradeYear}级.xlsx`)
    ElMessage.success('Excel 导出成功')
  } catch {
    ElMessage.error('Excel 导出失败')
  } finally {
    exportingExcel.value = false
  }
}

async function exportPdf() {
  exportingPdf.value = true
  try {
    const blob = await exportCourseReportPdfApi({
      courseId: filters.courseId,
      gradeYear: filters.gradeYear,
    })
    triggerDownload(blob, `课程评价报表_${reportData.value?.courseName}_${filters.gradeYear}级.pdf`)
    ElMessage.success('PDF 导出成功')
  } catch {
    ElMessage.error('PDF 导出失败')
  } finally {
    exportingPdf.value = false
  }
}

function triggerDownload(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  if (!isInstructor.value) {
    ElMessage.error('无权访问此页面')
    router.replace(DEFAULT_HOME_PATH)
    return
  }
  await loadCourses()
})
</script>

<style scoped>
.course-report-page {
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
  max-width: 640px;
}

.page-content {
  display: grid;
  gap: 24px;
}

.context-section,
.export-section,
.report-section {
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.context-section h2,
.export-section h2,
.report-section h2 {
  margin: 0 0 14px;
  color: #1f2937;
  font-size: 18px;
}

.context-note {
  margin-bottom: 16px;
  padding: 10px 14px;
  background: #dbeafe;
  border-left: 4px solid #3b82f6;
  border-radius: 4px;
  font-size: 13px;
  color: #1e40af;
  line-height: 1.6;
}

.filter-form {
  margin-bottom: 4px;
}

.context-display {
  margin-top: 16px;
  padding: 14px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  line-height: 2.2;
}

.context-display .label {
  font-weight: 600;
  color: #475569;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.export-actions {
  display: flex;
  gap: 10px;
}

.export-hint {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.loading-mask {
  padding: 16px 0;
}

.highlight {
  font-weight: 600;
  color: #1d4ed8;
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
