<template>
  <div class="ap-config">
    <div class="filter-bar">
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
    </div>

    <div v-if="contextInfo" class="context-info">
      <el-tag type="info" effect="plain">{{ contextInfo.majorName || '-' }}</el-tag>
      <el-tag type="info" effect="plain">{{ contextInfo.gradeYear ? `${contextInfo.gradeYear}级` : '-' }}</el-tag>
      <el-tag type="info" effect="plain">{{ contextInfo.termCode || '-' }}</el-tag>
      <el-tag type="success" effect="plain">{{ contextInfo.classCode || '-' }}</el-tag>
    </div>

    <div class="table-toolbar">
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
      description="暂无考核点数据，请先新增考核点"
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
            <el-tag type="info" effect="plain" size="small">{{ row.objectiveCode }}</el-tag>
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
              title="确认删除该考核点吗？若已产生成绩数据则无法删除"
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
.ap-config {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.context-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
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
