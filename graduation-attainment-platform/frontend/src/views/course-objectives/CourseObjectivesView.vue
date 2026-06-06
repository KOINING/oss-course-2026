<template>
  <div class="course-objectives-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：课程大纲与微观映射管理</p>
            <h1>课程目标</h1>
            <p class="page-summary">
              先选择课程和教学班上下文，再维护课程目标。课程目标属于课程级配置，但教师端页面需要基于当前教学班确认专业、年级和学期口径。
            </p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <section class="context-section">
          <div class="section-header">
            <h2>课程上下文</h2>
          </div>

          <div class="selectors">
            <el-form :inline="true" :model="filters">
              <el-form-item label="课程">
                <el-select
                  v-model="filters.courseId"
                  placeholder="请选择课程"
                  style="width: 240px"
                  @change="handleCourseChange"
                >
                  <el-option
                    v-for="course in courses"
                    :key="course.courseId"
                    :label="`${course.courseCode} - ${course.courseName}`"
                    :value="course.courseId"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="教学班">
                <el-select
                  v-model="filters.teachingClassId"
                  placeholder="请选择教学班"
                  style="width: 240px"
                  :disabled="!filters.courseId"
                  @change="handleTeachingClassChange"
                >
                  <el-option
                    v-for="item in teachingClasses"
                    :key="item.classId"
                    :label="`${item.classCode} - ${item.className || item.courseName}`"
                    :value="item.classId"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <el-descriptions v-if="context" :column="5" border class="context-panel">
            <el-descriptions-item label="专业">{{ context.majorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ context.gradeYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ context.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教学班">{{ context.classCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ context.termCode || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-alert
            v-if="contextWarning"
            :title="contextWarning"
            type="warning"
            :closable="false"
            class="context-alert"
          />
        </section>

        <section class="objectives-section">
          <div class="section-header">
            <h2>课程目标列表</h2>
            <el-button type="primary" :disabled="!filters.courseId" @click="openDialog('create')">
              新增课程目标
            </el-button>
          </div>

          <el-alert
            v-if="!filters.courseId"
            title="请先选择课程"
            type="info"
            :closable="false"
          />

          <el-table v-else v-loading="loading" :data="objectives" border>
            <el-table-column prop="objectiveCode" label="目标编号" width="120" />
            <el-table-column prop="description" label="目标描述" min-width="320" show-overflow-tooltip />
            <el-table-column label="考核点引用" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isReferencedByAssessmentPoint ? 'warning' : 'info'" effect="light">
                  {{ row.isReferencedByAssessmentPoint ? '已引用' : '未引用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内部权重配置" width="140" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isInWeightConfiguration ? 'success' : 'info'" effect="light">
                  {{ row.isInWeightConfiguration ? '已配置' : '未配置' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
                <el-popconfirm
                  :title="buildDeleteTitle(row)"
                  confirm-button-text="确认删除"
                  cancel-button-text="取消"
                  @confirm="deleteObjective(row)"
                >
                  <template #reference>
                    <el-button link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="当前课程暂无课程目标" />
            </template>
          </el-table>
        </section>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增课程目标' : '编辑课程目标'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="目标编号" prop="objectiveCode">
          <el-input v-model.trim="form.objectiveCode" placeholder="例如 CO1" />
        </el-form-item>
        <el-form-item label="描述类型" prop="descriptionType">
          <el-radio-group v-model="form.descriptionType">
            <el-radio label="text">纯文本</el-radio>
            <el-radio label="html">富文本源码</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标描述" prop="description">
          <el-input
            v-if="form.descriptionType === 'text'"
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请输入课程目标描述"
          />
          <el-input
            v-else
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="请输入 HTML 内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import {
  addCourseObjectiveApi,
  deleteCourseObjectiveApi,
  getContextApi,
  listCourseObjectivesApi,
  listCoursesForInstructorApi,
  listTeachingClassesApi,
  updateCourseObjectiveApi,
} from '@/api/courseObjectives'
import { listAssessmentPointsApi } from '@/api/assessment'
import { getCourseWeightApi } from '@/api/courseWeight'

const router = useRouter()
const userStore = useUserStore()

const isInstructor = computed(() => userStore.roleCodes.includes('instructor'))

const filters = reactive({
  courseId: null,
  teachingClassId: null,
})

const courses = ref([])
const teachingClasses = ref([])
const context = ref(null)
const contextWarning = ref('')
const objectives = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  coId: null,
  objectiveCode: '',
  description: '',
  descriptionType: 'text',
})

const rules = {
  objectiveCode: [{ required: true, message: '请输入目标编号', trigger: 'blur' }],
  description: [{ required: true, message: '请输入目标描述', trigger: 'blur' }],
}

async function loadCourses() {
  try {
    courses.value = (await listCoursesForInstructorApi()) || []
  } catch {
    courses.value = []
    ElMessage.error('加载课程列表失败')
  }
}

async function handleCourseChange() {
  filters.teachingClassId = null
  context.value = null
  contextWarning.value = ''
  objectives.value = []
  teachingClasses.value = []

  if (!filters.courseId) {
    return
  }

  try {
    teachingClasses.value = (await listTeachingClassesApi({ courseId: filters.courseId })) || []
    await loadObjectives()
  } catch {
    ElMessage.error('加载教学班失败')
  }
}

async function handleTeachingClassChange() {
  if (!filters.courseId || !filters.teachingClassId) {
    context.value = null
    contextWarning.value = ''
    await loadObjectives()
    return
  }

  try {
    context.value = await getContextApi({
      courseId: filters.courseId,
      teachingClassId: filters.teachingClassId,
    })
    contextWarning.value = context.value?.blockReason || ''
    await loadObjectives()
  } catch {
    ElMessage.error('加载课程上下文失败')
  }
}

async function loadObjectives() {
  if (!filters.courseId) {
    objectives.value = []
    return
  }

  loading.value = true
  try {
    const [objectiveRows, assessmentPoints, weightRows] = await Promise.all([
      listCourseObjectivesApi({
        courseId: filters.courseId,
        teachingClassId: filters.teachingClassId || undefined,
      }),
      listAssessmentPointsApi({ courseId: filters.courseId }).catch(() => []),
      context.value?.gradeYear
        ? getCourseWeightApi({
            courseId: filters.courseId,
            gradeYear: Number(context.value.gradeYear),
          }).catch(() => [])
        : Promise.resolve([]),
    ])

    const referencedObjectiveIds = new Set((assessmentPoints || []).map((item) => item.coId))
    const configuredObjectiveIds = new Set(
      (weightRows || [])
        .filter((item) => Number(item.internalWeight) > 0)
        .map((item) => item.coId),
    )

    objectives.value = (objectiveRows || []).map((item) => ({
      ...item,
      isReferencedByAssessmentPoint: referencedObjectiveIds.has(item.coId),
      isInWeightConfiguration: configuredObjectiveIds.has(item.coId),
    }))

    if (context.value && !context.value.blockReason && context.value.gradeYear && (weightRows || []).length === 0) {
      contextWarning.value = '当前课程在该专业和年级下尚未形成可配置的内部权重矩阵，请先确认宏观支撑矩阵已配置。'
    }
  } catch {
    objectives.value = []
    ElMessage.error('加载课程目标失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.coId = null
  form.objectiveCode = ''
  form.description = ''
  form.descriptionType = 'text'
}

function openDialog(mode, row = null) {
  dialogMode.value = mode
  resetForm()

  if (mode === 'edit' && row) {
    form.coId = row.coId
    form.objectiveCode = row.objectiveCode
    form.description = row.descriptionRich || row.description || ''
    form.descriptionType = row.descriptionRich ? 'html' : 'text'
  }

  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function stripHtml(html) {
  return String(html || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function buildDeleteTitle(row) {
  const reasons = []
  if (row.isReferencedByAssessmentPoint) {
    reasons.push('已被考核点引用')
  }
  if (row.isInWeightConfiguration) {
    reasons.push('已参与内部权重配置')
  }
  if (!reasons.length) {
    return '确认删除该课程目标吗？'
  }
  return `确认删除该课程目标吗？（${reasons.join('，')}）`
}

async function submitForm() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      courseId: filters.courseId,
      objectiveCode: form.objectiveCode,
      description: form.descriptionType === 'html' ? stripHtml(form.description) : form.description,
      descriptionRich: form.descriptionType === 'html' ? form.description : undefined,
    }

    if (dialogMode.value === 'create') {
      await addCourseObjectiveApi(payload)
      ElMessage.success('课程目标创建成功')
    } else {
      await updateCourseObjectiveApi({
        coId: form.coId,
        ...payload,
      })
      ElMessage.success('课程目标更新成功')
    }

    dialogVisible.value = false
    await loadObjectives()
  } catch {
    ElMessage.error('课程目标保存失败')
  } finally {
    submitting.value = false
  }
}

async function deleteObjective(row) {
  try {
    await deleteCourseObjectiveApi({ coId: row.coId })
    ElMessage.success('课程目标删除成功')
    await loadObjectives()
  } catch {
    ElMessage.error('课程目标删除失败')
  }
}

onMounted(async () => {
  if (!isInstructor.value) {
    ElMessage.error('无权访问该页面')
    router.replace(DEFAULT_HOME_PATH)
    return
  }

  await loadCourses()
})
</script>

<style scoped>
.course-objectives-page {
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
  max-width: 760px;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.context-section,
.objectives-section {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #f8fafc;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.selectors {
  margin-bottom: 16px;
}

.context-panel {
  background: #fff;
}

.context-alert {
  margin-top: 16px;
}
</style>
