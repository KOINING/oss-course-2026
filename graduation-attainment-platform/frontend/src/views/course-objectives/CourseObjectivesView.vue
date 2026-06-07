<template>
  <div class="course-objectives-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：课程目标与考核点</p>
            <h1>课程目标配置</h1>
            <p class="page-summary">
              课程目标属于课程级配置，但教师端需要基于当前教学班确认专业、年级和学期口径。
              进入页面后默认加载一个课程与教学班，不再显示空白页。
            </p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <section class="context-section">
          <div class="section-header">
            <h2>课程上下文</h2>
          </div>

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
                style="width: 260px"
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

          <el-table v-loading="loading" :data="objectives" border>
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
            <el-table-column label="操作" min-width="320" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
                  <el-button link type="primary" @click="goToTab('weights', row)">进入权重配置</el-button>
                  <el-button link type="primary" @click="goToTab('assessment-points', row)">进入考核点配置</el-button>
                  <el-popconfirm
                    :title="buildDeleteTitle(row)"
                    popper-class="objective-delete-popconfirm"
                    confirm-button-text="确认删除"
                    cancel-button-text="取消"
                    @confirm="deleteObjective(row)"
                  >
                    <template #reference>
                      <el-button link type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </div>
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
import { useRoute, useRouter } from 'vue-router'
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
const route = useRoute()
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
    if (!courses.value.length) {
      return
    }

    const preferredCourseId = Number(route.query.courseId) || courses.value[0].courseId
    filters.courseId = courses.value.some((item) => item.courseId === preferredCourseId)
      ? preferredCourseId
      : courses.value[0].courseId

    await handleCourseChange(filters.courseId)
  } catch {
    courses.value = []
    ElMessage.error('加载课程列表失败')
  }
}

async function handleCourseChange(courseId = filters.courseId) {
  filters.courseId = courseId || null
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
    if (!teachingClasses.value.length) {
      await loadObjectives()
      return
    }

    const preferredClassId = Number(route.query.teachingClassId) || teachingClasses.value[0].classId
    filters.teachingClassId = teachingClasses.value.some((item) => item.classId === preferredClassId)
      ? preferredClassId
      : teachingClasses.value[0].classId

    await handleTeachingClassChange()
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
    syncQuery()
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
      listCourseObjectivesApi({ courseId: filters.courseId }),
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
  } catch {
    objectives.value = []
    ElMessage.error('加载课程目标失败')
  } finally {
    loading.value = false
  }
}

function syncQuery(extra = {}) {
  router.replace({
    query: {
      ...route.query,
      courseId: filters.courseId || undefined,
      teachingClassId: filters.teachingClassId || undefined,
      ...extra,
    },
  })
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
  if (row.isReferencedByAssessmentPoint) reasons.push('已被考核点引用')
  if (row.isInWeightConfiguration) reasons.push('已参与内部权重配置')
  return reasons.length
    ? `确认删除该课程目标吗？（${reasons.join('，')}）`
    : '确认删除该课程目标吗？'
}

function goToTab(tab, row) {
  syncQuery({
    tab,
    highlightCoId: row?.coId || undefined,
  })
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

.context-panel {
  margin-top: 16px;
  background: #fff;
}

.context-alert {
  margin-top: 16px;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 10px;
}

:deep(.objective-delete-popconfirm .el-popconfirm__action) {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: flex-end;
}

:deep(.objective-delete-popconfirm .el-popconfirm__action .el-button + .el-button) {
  margin-left: 8px;
}
</style>
