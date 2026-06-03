<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addGraduationRequirementApi,
  addIndicatorPointApi,
  deleteGraduationRequirementApi,
  deleteIndicatorPointApi,
  listGraduationRequirementsApi,
  listIndicatorPointsApi,
  listMajorsApi,
  listRequirementGradeYearsApi,
  updateGraduationRequirementApi,
  updateIndicatorPointApi,
} from '@/api/requirements'

const grLoading = ref(false)
const ipLoading = ref(false)

const grs = ref([])
const ips = ref([])
const majorOptions = ref([])
const gradeYearOptions = ref([])

const currentRequirement = ref(null)

const grDialogVisible = ref(false)
const grDialogMode = ref('create')
const grFormRef = ref(null)
const grSubmitLoading = ref(false)

const ipDialogVisible = ref(false)
const ipDialogMode = ref('create')
const ipFormRef = ref(null)
const ipSubmitLoading = ref(false)

const grFilters = reactive({
  grCode: '',
  majorId: null,
  gradeYear: null,
})

const ipFilters = reactive({
  ipCode: '',
  grId: null,
  gradeYear: null,
})

const grForm = reactive({
  grId: null,
  grCode: '',
  grDescription: '',
  majorId: null,
  gradeYear: 2022,
})

const ipForm = reactive({
  ipId: null,
  ipCode: '',
  ipDescription: '',
  grId: null,
})

const grFormRules = {
  grCode: [{ required: true, message: '请输入毕业要求编号。', trigger: 'blur' }],
  grDescription: [{ required: true, message: '请输入毕业要求描述。', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业。', trigger: 'change' }],
  gradeYear: [{ required: true, message: '请选择年级。', trigger: 'change' }],
}

const ipFormRules = {
  ipCode: [{ required: true, message: '请输入指标点编号。', trigger: 'blur' }],
  ipDescription: [{ required: true, message: '请输入指标点描述。', trigger: 'blur' }],
  grId: [{ required: true, message: '请选择毕业要求。', trigger: 'change' }],
}

function formatGradeYear(gradeYear) {
  return gradeYear ? `${gradeYear}级` : '-'
}

function formatRequirementLabel(gr) {
  return `${formatGradeYear(gr.gradeYear)} / ${gr.grCode} - ${gr.grDescription}`
}

const filteredRequirementOptions = computed(() => {
  if (!ipFilters.gradeYear) {
    return grs.value
  }
  return grs.value.filter((item) => item.gradeYear === ipFilters.gradeYear)
})

function normalizeRequirementFilters() {
  return {
    grCode: grFilters.grCode || undefined,
    majorId: grFilters.majorId || undefined,
    gradeYear: grFilters.gradeYear || undefined,
  }
}

function normalizeIndicatorFilters() {
  return {
    ipCode: ipFilters.ipCode || undefined,
    grId: ipFilters.grId || undefined,
    gradeYear: ipFilters.gradeYear || undefined,
  }
}

async function loadMajorOptions() {
  majorOptions.value = (await listMajorsApi()) || []
}

async function loadGradeYearOptions() {
  gradeYearOptions.value = (await listRequirementGradeYearsApi({ majorId: grFilters.majorId || undefined })) || [2022]
}

async function loadGrs() {
  grLoading.value = true
  try {
    grs.value = (await listGraduationRequirementsApi(normalizeRequirementFilters())) || []
  } finally {
    grLoading.value = false
  }
}

async function loadIps() {
  ipLoading.value = true
  try {
    ips.value = (await listIndicatorPointsApi(normalizeIndicatorFilters())) || []
  } finally {
    ipLoading.value = false
  }
}

function resetGrFilters() {
  grFilters.grCode = ''
  grFilters.majorId = null
  grFilters.gradeYear = null
  loadGradeYearOptions()
  loadGrs()
}

function resetIpFilters() {
  ipFilters.ipCode = ''
  ipFilters.grId = null
  ipFilters.gradeYear = null
  loadIps()
}

function resetGrForm() {
  grForm.grId = null
  grForm.grCode = ''
  grForm.grDescription = ''
  grForm.majorId = null
  grForm.gradeYear = gradeYearOptions.value[0] || 2022
}

function resetIpForm() {
  ipForm.ipId = null
  ipForm.ipCode = ''
  ipForm.ipDescription = ''
  ipForm.grId = currentRequirement.value?.grId || null
}

function openGrCreateDialog() {
  grDialogMode.value = 'create'
  resetGrForm()
  grDialogVisible.value = true
}

function openGrEditDialog(row) {
  grDialogMode.value = 'edit'
  Object.assign(grForm, {
    grId: row.grId,
    grCode: row.grCode,
    grDescription: row.grDescription,
    majorId: row.majorId,
    gradeYear: row.gradeYear,
  })
  grDialogVisible.value = true
}

function openIpCreateDialog() {
  ipDialogMode.value = 'create'
  resetIpForm()
  if (!ipForm.grId && filteredRequirementOptions.value.length > 0) {
    ipForm.grId = filteredRequirementOptions.value[0].grId
  }
  ipDialogVisible.value = true
}

function openIpEditDialog(row) {
  ipDialogMode.value = 'edit'
  Object.assign(ipForm, {
    ipId: row.ipId,
    ipCode: row.ipCode,
    ipDescription: row.ipDescription,
    grId: row.grId,
  })
  ipDialogVisible.value = true
}

function onGrRowChange(row) {
  currentRequirement.value = row
  if (row) {
    ipFilters.grId = row.grId
    ipFilters.gradeYear = row.gradeYear
    loadIps()
  }
}

async function handleGrSubmit() {
  await grFormRef.value?.validate()
  const payload = {
    grId: grForm.grId,
    grCode: grForm.grCode.trim(),
    grDescription: grForm.grDescription.trim(),
    majorId: grForm.majorId,
    gradeYear: grForm.gradeYear,
  }

  grSubmitLoading.value = true
  try {
    if (grDialogMode.value === 'create') {
      await addGraduationRequirementApi(payload)
      ElMessage.success('毕业要求新增成功。')
    } else {
      await updateGraduationRequirementApi(payload)
      ElMessage.success('毕业要求更新成功。')
    }
    grDialogVisible.value = false
    await loadGradeYearOptions()
    await loadGrs()
  } finally {
    grSubmitLoading.value = false
  }
}

async function handleIpSubmit() {
  await ipFormRef.value?.validate()
  const payload = {
    ipId: ipForm.ipId,
    ipCode: ipForm.ipCode.trim(),
    ipDescription: ipForm.ipDescription.trim(),
    grId: ipForm.grId,
  }

  ipSubmitLoading.value = true
  try {
    if (ipDialogMode.value === 'create') {
      await addIndicatorPointApi(payload)
      ElMessage.success('指标点新增成功。')
    } else {
      await updateIndicatorPointApi(payload)
      ElMessage.success('指标点更新成功。')
    }
    ipDialogVisible.value = false
    await loadIps()
  } finally {
    ipSubmitLoading.value = false
  }
}

async function handleGrDelete(row) {
  await ElMessageBox.confirm(`确定删除毕业要求“${row.grCode}”吗？`, '提示', { type: 'warning' })
  await deleteGraduationRequirementApi({ grId: row.grId })
  ElMessage.success('毕业要求删除成功。')
  await loadGradeYearOptions()
  await loadGrs()
  await loadIps()
}

async function handleIpDelete(row) {
  await ElMessageBox.confirm(`确定删除指标点“${row.ipCode}”吗？`, '提示', { type: 'warning' })
  await deleteIndicatorPointApi({ ipId: row.ipId })
  ElMessage.success('指标点删除成功。')
  await loadIps()
}

function indicatorSpanMethod({ row, columnIndex, rowIndex }) {
  if (columnIndex !== 0) {
    return { rowspan: 1, colspan: 1 }
  }
  if (rowIndex > 0) {
    const prev = ips.value[rowIndex - 1]
    if (prev?.grId === row.grId && prev?.gradeYear === row.gradeYear) {
      return { rowspan: 0, colspan: 0 }
    }
  }
  let rowspan = 1
  for (let index = rowIndex + 1; index < ips.value.length; index += 1) {
    const next = ips.value[index]
    if (next?.grId === row.grId && next?.gradeYear === row.gradeYear) {
      rowspan += 1
    } else {
      break
    }
  }
  return { rowspan, colspan: 1 }
}

function requirementGradeYearSpanMethod({ row, columnIndex, rowIndex }) {
  if (columnIndex !== 0) {
    return { rowspan: 1, colspan: 1 }
  }
  if (rowIndex > 0) {
    const prev = grs.value[rowIndex - 1]
    if (prev?.gradeYear === row.gradeYear) {
      return { rowspan: 0, colspan: 0 }
    }
  }
  let rowspan = 1
  for (let index = rowIndex + 1; index < grs.value.length; index += 1) {
    const next = grs.value[index]
    if (next?.gradeYear === row.gradeYear) {
      rowspan += 1
    } else {
      break
    }
  }
  return { rowspan, colspan: 1 }
}

onMounted(async () => {
  await loadMajorOptions()
  await loadGradeYearOptions()
  await loadGrs()
  await loadIps()
})
</script>

<template>
  <div class="requirements-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">模块 A</p>
            <h1>毕业要求与指标点</h1>
            <p class="page-summary">按专业和年级维护毕业要求及指标点版本。</p>
          </div>
        </div>
      </template>

      <section class="entity-section">
        <div class="section-header">
          <h2>毕业要求</h2>
          <el-button type="primary" @click="openGrCreateDialog">新增毕业要求</el-button>
        </div>

        <el-form :inline="true" :model="grFilters" class="filter-form">
          <el-form-item label="编号">
            <el-input v-model.trim="grFilters.grCode" placeholder="请输入毕业要求编号" clearable />
          </el-form-item>
          <el-form-item label="专业">
            <el-select v-model="grFilters.majorId" placeholder="全部专业" clearable filterable style="width: 200px">
              <el-option
                v-for="major in majorOptions"
                :key="major.majorId"
                :label="major.majorName"
                :value="major.majorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="grFilters.gradeYear" placeholder="全部年级" clearable filterable style="width: 160px">
              <el-option
                v-for="gradeYear in gradeYearOptions"
                :key="gradeYear"
                :label="formatGradeYear(gradeYear)"
                :value="gradeYear"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadGrs">查询</el-button>
            <el-button @click="resetGrFilters">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table
          v-loading="grLoading"
          :data="grs"
          :span-method="requirementGradeYearSpanMethod"
          border
          highlight-current-row
          @current-change="onGrRowChange"
        >
          <el-table-column label="年级" width="110">
            <template #default="{ row }">{{ formatGradeYear(row.gradeYear) }}</template>
          </el-table-column>
          <el-table-column prop="grCode" label="编号" width="100" />
          <el-table-column prop="grDescription" label="描述" min-width="320" show-overflow-tooltip />
          <el-table-column prop="majorName" label="专业" width="180" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click.stop="openGrEditDialog(row)">编辑</el-button>
                <el-button link type="danger" @click.stop="handleGrDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <div class="section-divider"></div>

      <section class="entity-section">
        <div class="section-header">
          <h2>指标点</h2>
          <el-button type="primary" @click="openIpCreateDialog">新增指标点</el-button>
        </div>

        <el-form :inline="true" :model="ipFilters" class="filter-form">
          <el-form-item label="指标点编号">
            <el-input v-model.trim="ipFilters.ipCode" placeholder="请输入指标点编号" clearable />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="ipFilters.gradeYear" placeholder="全部年级" clearable filterable style="width: 160px">
              <el-option
                v-for="gradeYear in gradeYearOptions"
                :key="gradeYear"
                :label="formatGradeYear(gradeYear)"
                :value="gradeYear"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="毕业要求">
            <el-select v-model="ipFilters.grId" placeholder="全部毕业要求" clearable filterable style="width: 340px">
              <el-option
                v-for="gr in filteredRequirementOptions"
                :key="gr.grId"
                :label="formatRequirementLabel(gr)"
                :value="gr.grId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadIps">查询</el-button>
            <el-button @click="resetIpFilters">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="ipLoading" :data="ips" :span-method="indicatorSpanMethod" border>
          <el-table-column label="毕业要求" min-width="340" show-overflow-tooltip>
            <template #default="{ row }">{{ formatRequirementLabel(row) }}</template>
          </el-table-column>
          <el-table-column prop="ipCode" label="指标点编号" width="140" />
          <el-table-column prop="ipDescription" label="描述" min-width="320" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="openIpEditDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="handleIpDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-card>

    <el-dialog v-model="grDialogVisible" :title="grDialogMode === 'create' ? '新增毕业要求' : '编辑毕业要求'" width="560px">
      <el-form ref="grFormRef" :model="grForm" :rules="grFormRules" label-width="120px">
        <el-form-item label="毕业要求编号" prop="grCode">
          <el-input v-model.trim="grForm.grCode" placeholder="请输入毕业要求编号" />
        </el-form-item>
        <el-form-item label="描述" prop="grDescription">
          <el-input v-model.trim="grForm.grDescription" type="textarea" :rows="4" placeholder="请输入毕业要求描述" />
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="grForm.majorId" placeholder="请选择专业" style="width: 100%">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="gradeYear">
          <el-select v-model="grForm.gradeYear" placeholder="请选择年级" style="width: 100%">
            <el-option
              v-for="gradeYear in gradeYearOptions"
              :key="gradeYear"
              :label="formatGradeYear(gradeYear)"
              :value="gradeYear"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="grSubmitLoading" @click="handleGrSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ipDialogVisible" :title="ipDialogMode === 'create' ? '新增指标点' : '编辑指标点'" width="560px">
      <el-form ref="ipFormRef" :model="ipForm" :rules="ipFormRules" label-width="120px">
        <el-form-item label="指标点编号" prop="ipCode">
          <el-input v-model.trim="ipForm.ipCode" placeholder="请输入指标点编号" />
        </el-form-item>
        <el-form-item label="描述" prop="ipDescription">
          <el-input v-model.trim="ipForm.ipDescription" type="textarea" :rows="4" placeholder="请输入指标点描述" />
        </el-form-item>
        <el-form-item label="毕业要求" prop="grId">
          <el-select v-model="ipForm.grId" placeholder="请选择毕业要求" filterable style="width: 100%">
            <el-option
              v-for="gr in filteredRequirementOptions"
              :key="gr.grId"
              :label="formatRequirementLabel(gr)"
              :value="gr.grId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ipSubmitLoading" @click="handleIpSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.requirements-page {
  padding: 16px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.page-section {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.page-summary {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.entity-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
}

.section-divider {
  height: 1px;
  margin: 24px 0;
  background: #e2e8f0;
}

.table-actions {
  display: flex;
  gap: 8px;
}
</style>
