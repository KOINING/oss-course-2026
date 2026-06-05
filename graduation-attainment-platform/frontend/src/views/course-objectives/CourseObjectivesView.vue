<template>
  <div class="course-objectives-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 B：课程大纲与微观映射管理</p>
            <h1>课程目标与考核点</h1>
            <p class="page-summary">维护课程目标、课程目标对指标点的内部贡献权重，以及与考核点之间的映射关系。</p>
          </div>
        </div>
      </template>

      <div class="page-content">
        <section class="context-section">
          <h2>课程上下文</h2>
          <div class="context-info">
            <p class="context-hint">当前配置版本：<span class="version-badge">{{ context ? `${context.majorName} - ${context.enrollmentYear}年级` : '未选择' }}</span></p>
          </div>
          <el-form :inline="true" :model="filters">
            <el-form-item label="课程">
              <el-select v-model="filters.courseId" @change="onCourseChange" placeholder="请选择课程" style="width: 200px">
                <el-option v-for="c in courses" :key="c.courseId" :label="`${c.courseCode}-${c.courseName}`" :value="c.courseId" />
              </el-select>
            </el-form-item>
            <el-form-item label="教学班">
              <el-select v-model="filters.teachingClassId" @change="onContextChange" placeholder="请选择教学班" style="width: 200px">
                <el-option v-for="tc in teachingClasses" :key="tc.teachingClassId" :label="tc.teachingClassCode" :value="tc.teachingClassId" />
              </el-select>
            </el-form-item>
          </el-form>

          <div v-if="context" class="context-display">
            <el-row :gutter="20">
              <el-col :span="4"><span class="label">专业:</span> {{ context.majorName }}</el-col>
              <el-col :span="4"><span class="label">年级:</span> {{ context.enrollmentYear }}</el-col>
              <el-col :span="4"><span class="label">课程:</span> {{ context.courseName }}</el-col>
              <el-col :span="4"><span class="label">教学班:</span> {{ context.teachingClassCode }}</el-col>
              <el-col :span="4"><span class="label">学期:</span> {{ context.semesterName }}</el-col>
            </el-row>
          </div>

          <el-alert v-if="contextWarning" :title="contextWarning" type="warning" :closable="false" style="margin-top: 12px" />
        </section>

        <section class="objectives-section">
          <div class="section-header">
            <h2>课程目标定义</h2>
            <el-button type="primary" @click="openDialog('create')">新增课程目标</el-button>
          </div>

          <el-alert v-if="!context" type="info" title="请先选择课程和教学班" :closable="false" />

          <el-table v-else v-loading="loading" :data="objectives" border style="margin-top: 16px">
            <el-table-column prop="objectiveCode" label="编号" width="100" />
            <el-table-column prop="objectiveName" label="名称" width="150" />
            <el-table-column label="描述" min-width="200">
              <template #default="{ row }">
                <div v-html="row.description" class="desc-preview" />
              </template>
            </el-table-column>
            <el-table-column label="引用状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.isReferencedByAssessmentPoint" type="warning">已引用</el-tag>
                <el-tag v-else type="info">未引用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="权重配置" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.isInWeightConfiguration" type="success">已配置</el-tag>
                <el-tag v-else type="info">未配置</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
                <el-popconfirm 
                  :title="getDeleteConfirmTitle(row)" 
                  @confirm="deleteObjective(row)"
                  confirm-button-text="确认删除"
                  cancel-button-text="取消"
                >
                  <template #reference>
                    <el-button link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无数据" />
            </template>
          </el-table>
        </section>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增课程目标' : '编辑课程目标'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="目标编号" prop="objectiveCode">
          <el-input v-model.trim="form.objectiveCode" placeholder="请输入目标编号" />
        </el-form-item>
        <el-form-item label="目标名称" prop="objectiveName">
          <el-input v-model.trim="form.objectiveName" placeholder="请输入目标名称" />
        </el-form-item>
        <el-form-item label="描述类型" prop="descriptionType">
          <el-radio-group v-model="form.descriptionType">
            <el-radio label="text">纯文本</el-radio>
            <el-radio label="html">富文本</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-if="form.descriptionType === 'text'" v-model="form.description" type="textarea" rows="4" />
          <div v-else class="editor">
            <div class="toolbar">
              <el-button-group>
                <el-button size="small" @click="insertTag('b')">粗体</el-button>
                <el-button size="small" @click="insertTag('i')">斜体</el-button>
                <el-button size="small" @click="insertTag('u')">下划线</el-button>
              </el-button-group>
            </div>
            <textarea v-model="form.description" class="textarea" />
          </div>
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
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import {
  listCoursesForInstructorApi,
  listTeachingClassesApi,
  getContextApi,
  listCourseObjectivesApi,
  addCourseObjectiveApi,
  updateCourseObjectiveApi,
  deleteCourseObjectiveApi,
  checkAssessmentPointReferencesApi,
  checkWeightConfigurationApi,
} from '@/api/courseObjectives'

const router = useRouter()
const userStore = useUserStore()

const isInstructor = computed(() => userStore.roleCodes.includes('instructor'))

const courses = ref([])
const teachingClasses = ref([])
const objectives = ref([])
const context = ref(null)
const contextWarning = ref('')

const filters = reactive({ courseId: null, teachingClassId: null })
const form = reactive({
  objectiveId: null,
  objectiveCode: '',
  objectiveName: '',
  description: '',
  descriptionType: 'text',
})

const rules = {
  objectiveCode: [{ required: true, message: '请输入目标编号', trigger: 'blur' }],
  objectiveName: [{ required: true, message: '请输入目标名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入目标描述', trigger: 'blur' }],
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

async function loadCourses() {
  try {
    courses.value = await listCoursesForInstructorApi()
  } catch {
    ElMessage.error('加载课程列表失败')
  }
}

async function onCourseChange() {
  filters.teachingClassId = null
  teachingClasses.value = []
  context.value = null
  contextWarning.value = ''
  if (!filters.courseId) return
  try {
    teachingClasses.value = await listTeachingClassesApi({ courseId: filters.courseId })
  } catch {
    ElMessage.error('加载教学班列表失败')
  }
}

async function onContextChange() {
  if (!filters.courseId || !filters.teachingClassId) return
  try {
    const ctx = await getContextApi({
      courseId: filters.courseId,
      teachingClassId: filters.teachingClassId,
    })
    context.value = ctx
    contextWarning.value = !ctx.hasSupportIndicatorPoints
      ? '当前课程在该专业+年级下尚未配置支撑指标点，无法配置课程目标的权重关系。'
      : ''
    await loadObjectives()
  } catch {
    ElMessage.error('加载上下文失败')
  }
}

async function loadObjectives() {
  if (!context.value) return
  loading.value = true
  try {
    const data = await listCourseObjectivesApi({
      courseId: filters.courseId,
      teachingClassId: filters.teachingClassId,
    })
    for (const obj of data) {
      const [ref, weight] = await Promise.all([
        checkAssessmentPointReferencesApi({ objectiveId: obj.objectiveId })
          .then((r) => r.isReferenced)
          .catch(() => false),
        checkWeightConfigurationApi({ objectiveId: obj.objectiveId })
          .then((r) => r.isInConfiguration)
          .catch(() => false),
      ])
      obj.isReferencedByAssessmentPoint = ref
      obj.isInWeightConfiguration = weight
    }
    objectives.value = data
  } catch {
    ElMessage.error('加载课程目标失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.objectiveId = null
  form.objectiveCode = ''
  form.objectiveName = ''
  form.description = ''
  form.descriptionType = 'text'
}

function getDeleteConfirmTitle(row) {
  if (row.isReferencedByAssessmentPoint || row.isInWeightConfiguration) {
    const reasons = []
    if (row.isReferencedByAssessmentPoint) reasons.push('已被考核点引用')
    if (row.isInWeightConfiguration) reasons.push('已参与权重配置')
    return `确认删除该课程目标吗？（${reasons.join('、')}）`
  }
  return '确认删除该课程目标吗？'
}

function openDialog(mode, row = null) {
  dialogMode.value = mode
  resetForm()
  if (mode === 'edit' && row) {
    form.objectiveId = row.objectiveId
    form.objectiveCode = row.objectiveCode
    form.objectiveName = row.objectiveName
    form.description = row.description
    form.descriptionType = row.descriptionType || 'text'
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function insertTag(tag) {
  const textarea = document.querySelector('.editor textarea')
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = form.description.substring(start, end)
  form.description = `${form.description.substring(0, start)}<${tag}>${text}</${tag}>${form.description.substring(end)}`
}

async function submitForm() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      objectiveCode: form.objectiveCode,
      objectiveName: form.objectiveName,
      description: form.description,
      descriptionType: form.descriptionType,
      courseId: filters.courseId,
      teachingClassId: filters.teachingClassId,
    }
    if (dialogMode.value === 'create') {
      await addCourseObjectiveApi(payload)
      ElMessage.success('创建成功')
    } else {
      await updateCourseObjectiveApi({ objectiveId: form.objectiveId, ...payload })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await loadObjectives()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function deleteObjective(row) {
  try {
    await deleteCourseObjectiveApi({ objectiveId: row.objectiveId })
    ElMessage.success('删除成功')
    await loadObjectives()
  } catch {
    ElMessage.error('删除失败')
  }
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
.course-objectives-page {
  padding: 20px;
}

.page-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header {
  display: flex;
  gap: 20px;
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
  max-width: 600px;
}

.page-content {
  display: grid;
  gap: 28px;
}

.context-section,
.objectives-section {
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.context-section h2,
.objectives-section h2 {
  margin: 0 0 16px;
  color: #1f2937;
  font-size: 18px;
}

.context-display {
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-top: 12px;
  line-height: 2;
}

.context-display .label {
  font-weight: 600;
  color: #475569;
  margin-right: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.desc-preview {
  max-height: 60px;
  overflow: hidden;
  line-height: 1.5;
}

.editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.toolbar {
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.textarea {
  width: 100%;
  min-height: 120px;
  padding: 8px;
  border: none;
  font-family: monospace;
  font-size: 14px;
  resize: vertical;
}

.context-info {
  margin-bottom: 16px;
}

.context-hint {
  margin: 0;
  padding: 8px 12px;
  background: #fef3c7;
  border-left: 4px solid #f59e0b;
  border-radius: 4px;
  font-size: 14px;
  color: #92400e;
}

.version-badge {
  font-weight: 600;
  color: #d97706;
  background: #fef3c7;
  padding: 2px 8px;
  border-radius: 3px;
}
</style>
