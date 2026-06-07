<template>
  <div class="assessment-point-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：课程目标与考核点</p>
            <h1>考核点配置</h1>
            <p class="page-summary">
              基于当前课程和教学班配置考核点，明确每个考核点的满分和唯一绑定的课程目标，
              为成绩模板生成、成绩导入和课程级计算提供稳定输入。
            </p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <section class="context-section">
          <div class="section-header">
            <h2>课程上下文</h2>
          </div>

          <el-form :inline="true">
            <el-form-item label="课程">
              <el-select
                v-model="selectedCourseId"
                placeholder="选择课程"
                style="width: 240px"
                @change="handleCourseChange"
              >
                <el-option
                  v-for="course in courseOptions"
                  :key="course.courseId"
                  :label="`${course.courseCode} - ${course.courseName}`"
                  :value="course.courseId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="教学班">
              <el-select
                v-model="selectedClassId"
                placeholder="选择教学班"
                style="width: 260px"
                :disabled="!selectedCourseId"
                @change="handleClassChange"
              >
                <el-option
                  v-for="item in currentClassOptions"
                  :key="item.classId"
                  :label="`${item.classCode} - ${item.className || item.courseName}`"
                  :value="item.classId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="课程目标筛选">
              <el-select
                v-model="filters.coId"
                placeholder="按课程目标筛选"
                clearable
                style="width: 260px"
                :disabled="!selectedCourseId"
                @change="loadAssessmentPoints"
              >
                <el-option
                  v-for="obj in objectiveOptions"
                  :key="obj.coId"
                  :label="`${obj.objectiveCode}: ${obj.description || obj.coDescription || ''}`"
                  :value="obj.coId"
                />
              </el-select>
            </el-form-item>
          </el-form>

          <el-descriptions v-if="contextInfo" :column="5" border class="context-panel">
            <el-descriptions-item label="专业">{{ contextInfo.majorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ contextInfo.gradeYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ contextInfo.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教学班">{{ contextInfo.classCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ contextInfo.termCode || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="points-section">
          <div class="section-header">
            <div>
              <h2>考核点列表</h2>
              <p class="section-summary">
                每个考核点必须绑定唯一课程目标，并作为成绩模板中的动态列参与后续成绩导入和课程级计算。
              </p>
            </div>
            <el-button type="primary" :disabled="!selectedCourseId" @click="openDialog('create')">
              <el-icon><Plus /></el-icon>
              新增考核点
            </el-button>
          </div>

          <ErrorState v-if="loadError" :message="loadError" @retry="loadAll" />

          <EmptyState
            v-else-if="!loading && !selectedCourseId"
            description="当前没有可用课程"
          />

          <EmptyState
            v-else-if="!loading && points.length === 0"
            description="当前课程暂无考核点，请先新增考核点"
          />

          <el-table
            v-else
            v-loading="loading"
            :data="points"
            border
            stripe
          >
            <el-table-column prop="apId" label="ID" width="70" />
            <el-table-column prop="apName" label="考核点名称" min-width="180" />
            <el-table-column label="所属课程目标" min-width="240">
              <template #default="{ row }">
                <div class="objective-cell">
                  <el-tag type="info" effect="light" size="small">{{ row.objectiveCode }}</el-tag>
                  <span class="objective-desc">{{ row.coDescription || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="满分" width="100" align="center">
              <template #default="{ row }">
                <span class="full-score">{{ row.fullScore }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
                  <el-popconfirm
                    title="确认删除该考核点吗？若已产生关联成绩数据则无法删除。"
                    @confirm="handleDelete(row.apId)"
                  >
                    <template #reference>
                      <el-button link type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </el-card>

    <FormDialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增考核点' : '编辑考核点'"
      width="480px"
      :loading="submitLoading"
      @confirm="handleSubmit"
      @cancel="dialogVisible = false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="110px"
      >
        <el-form-item label="考核点名称" prop="apName">
          <el-input v-model.trim="form.apName" maxlength="100" placeholder="请输入考核点名称" />
        </el-form-item>
        <el-form-item label="满分" prop="fullScore">
          <el-input-number
            v-model="form.fullScore"
            :min="0.5"
            :max="999"
            :step="0.5"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="绑定课程目标" prop="coId">
          <el-select v-model="form.coId" placeholder="请选择课程目标" style="width: 100%">
            <el-option
              v-for="obj in objectiveOptions"
              :key="obj.coId"
              :label="`${obj.objectiveCode}: ${obj.description || obj.coDescription || ''}`"
              :value="obj.coId"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </FormDialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import FormDialog from '@/components/common/FormDialog.vue'
import {
  addAssessmentPointApi,
  deleteAssessmentPointApi,
  listAssessmentPointsApi,
  listCourseObjectivesApi,
  listInstructorTeachingClassesApi,
  updateAssessmentPointApi,
} from '@/api/assessment'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loadError = ref('')
const teachingClassOptions = ref([])
const courseOptions = ref([])
const objectiveOptions = ref([])
const selectedCourseId = ref(null)
const selectedClassId = ref(null)
const contextInfo = ref(null)
const points = ref([])

const filters = reactive({ coId: null })

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  apId: null,
  apName: '',
  fullScore: 60,
  coId: null,
})

const formRules = {
  apName: [{ required: true, message: '请输入考核点名称', trigger: 'blur' }],
  fullScore: [{ required: true, message: '请输入满分', trigger: 'blur' }],
  coId: [{ required: true, message: '请选择课程目标', trigger: 'change' }],
}

const currentClassOptions = computed(() =>
  teachingClassOptions.value.filter((item) => item.courseId === selectedCourseId.value),
)

async function loadCourses() {
  try {
    teachingClassOptions.value = (await listInstructorTeachingClassesApi()) || []
    const seen = new Set()
    courseOptions.value = teachingClassOptions.value.filter((row) => {
      if (!row?.courseId || seen.has(row.courseId)) {
        return false
      }
      seen.add(row.courseId)
      return true
    })

    if (!courseOptions.value.length) {
      return
    }

    const preferredCourseId = Number(route.query.courseId) || courseOptions.value[0].courseId
    selectedCourseId.value = courseOptions.value.some((item) => item.courseId === preferredCourseId)
      ? preferredCourseId
      : courseOptions.value[0].courseId

    await handleCourseChange(selectedCourseId.value)
  } catch {
    teachingClassOptions.value = []
    courseOptions.value = []
  }
}

async function loadObjectives(courseId) {
  if (!courseId) {
    objectiveOptions.value = []
    return
  }
  try {
    objectiveOptions.value = (await listCourseObjectivesApi({ courseId })) || []
  } catch {
    objectiveOptions.value = []
  }
}

async function loadAssessmentPoints() {
  if (!selectedCourseId.value) {
    points.value = []
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const params = { courseId: selectedCourseId.value }
    if (filters.coId) params.coId = filters.coId
    points.value = (await listAssessmentPointsApi(params)) || []
  } catch (error) {
    loadError.value = error.message || '加载考核点失败'
    points.value = []
  } finally {
    loading.value = false
  }
}

async function handleCourseChange(courseId = selectedCourseId.value) {
  selectedCourseId.value = courseId || null
  filters.coId = null
  contextInfo.value = null
  points.value = []
  await loadObjectives(selectedCourseId.value)

  const classes = currentClassOptions.value
  if (!classes.length) {
    selectedClassId.value = null
    await loadAssessmentPoints()
    return
  }

  const preferredClassId = Number(route.query.teachingClassId) || classes[0].classId
  selectedClassId.value = classes.some((item) => item.classId === preferredClassId)
    ? preferredClassId
    : classes[0].classId
  handleClassChange(selectedClassId.value)
  await loadAssessmentPoints()
}

function handleClassChange(classId = selectedClassId.value) {
  selectedClassId.value = classId || null
  contextInfo.value = currentClassOptions.value.find((item) => item.classId === selectedClassId.value) || null
  syncQuery()
}

function syncQuery() {
  router.replace({
    query: {
      ...route.query,
      tab: 'assessment-points',
      courseId: selectedCourseId.value || undefined,
      teachingClassId: selectedClassId.value || undefined,
    },
  })
}

function resetForm() {
  form.apId = null
  form.apName = ''
  form.fullScore = 60
  form.coId = null
}

function openDialog(mode, row = null) {
  dialogMode.value = mode
  resetForm()
  if (mode === 'edit' && row) {
    form.apId = row.apId
    form.apName = row.apName
    form.fullScore = row.fullScore
    form.coId = row.coId
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = {
      apName: form.apName,
      fullScore: form.fullScore,
      coId: form.coId,
    }
    if (dialogMode.value === 'edit') {
      payload.apId = form.apId
      await updateAssessmentPointApi(payload)
      ElMessage.success('考核点更新成功')
    } else {
      await addAssessmentPointApi(payload)
      ElMessage.success('考核点创建成功')
    }
    dialogVisible.value = false
    await loadAssessmentPoints()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(apId) {
  try {
    await deleteAssessmentPointApi({ apId })
    ElMessage.success('考核点已删除')
    await loadAssessmentPoints()
  } catch {
    // handled by interceptor
  }
}

async function loadAll() {
  await loadCourses()
  if (selectedCourseId.value) {
    await loadObjectives(selectedCourseId.value)
    await loadAssessmentPoints()
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.assessment-point-page {
  padding: 20px;
}

.page-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header h1 {
  margin: 6px 0 8px;
  font-size: 28px;
  color: #0f172a;
}

.page-section {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-summary {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.context-section,
.points-section {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #f8fafc;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.section-summary {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.context-panel {
  margin-top: 16px;
  background: #fff;
}

.objective-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.objective-desc {
  color: #4b5563;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 220px;
}

.full-score {
  font-weight: 600;
  color: #1f2937;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
