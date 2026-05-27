<template>
  <div class="requirements-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <p class="page-section">妯″潡 A锛氬熀纭€涓庡畯瑙傛暟鎹鐞?/p>
            <h1>姣曚笟瑕佹眰涓庢寚鏍囩偣</h1>
            <p class="page-summary">
              缁存姢姣曚笟瑕佹眰涓讳綋鍙婁簩绾ф寚鏍囩偣锛屽舰鎴愪笓涓氳璇佺殑鐩爣浣撶郴锛屼綔涓鸿绋嬫敮鎾戠煩闃甸厤缃殑渚濇嵁銆?            </p>
          </div>
        </div>
      </template>

      <section class="entity-section">
        <div class="section-header">
          <h2>姣曚笟瑕佹眰</h2>
          <el-button type="primary" @click="openGrCreateDialog">鏂板姣曚笟瑕佹眰</el-button>
        </div>

        <el-form :inline="true" :model="grFilters" class="filter-form">
          <el-form-item label="缂栧彿">
            <el-input v-model.trim="grFilters.grCode" placeholder="璇疯緭鍏ョ紪鍙? clearable />
          </el-form-item>
          <el-form-item label="鎵€灞炰笓涓?>
            <el-select
              v-model="grFilters.majorId"
              placeholder="鍏ㄩ儴涓撲笟"
              clearable
              style="width: 200px"
            >
              <el-option
                v-for="major in majorOptions"
                :key="major.majorId"
                :label="major.majorName"
                :value="major.majorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadGrs">鏌ヨ</el-button>
            <el-button @click="resetGrFilters">閲嶇疆</el-button>
          </el-form-item>
        </el-form>

        <el-table
          v-loading="grLoading"
          :data="grs"
          border
          highlight-current-row
          @current-change="onGrRowChange"
        >
          <el-table-column prop="grCode" label="缂栧彿" width="100" />
          <el-table-column prop="grDescription" label="鎻忚堪" min-width="320" class-name="wrap-cell" />
          <el-table-column prop="majorName" label="鎵€灞炰笓涓? width="180" class-name="wrap-cell" />
          <el-table-column label="鎿嶄綔" width="140" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click.stop="openGrEditDialog(row)">缂栬緫</el-button>
                <el-popconfirm
                  title="纭鍒犻櫎璇ユ瘯涓氳姹傚悧锛熻嫢瀛樺湪鍏宠仈鎸囨爣鐐瑰皢鏃犳硶鍒犻櫎銆?
                  @confirm="handleGrDelete(row)"
                >
                  <template #reference>
                    <el-button link type="danger">鍒犻櫎</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="鏆傛棤姣曚笟瑕佹眰鏁版嵁" />
          </template>
        </el-table>
      </section>

      <div class="section-divider" aria-hidden="true"></div>

      <section class="entity-section">
        <div class="section-header">
          <h2>鎸囨爣鐐?/h2>
          <el-button type="primary" @click="openIpCreateDialog">鏂板鎸囨爣鐐?/el-button>
        </div>

        <el-form :inline="true" :model="ipFilters" class="filter-form">
          <el-form-item label="鎸囨爣鐐圭紪鍙?>
            <el-input v-model.trim="ipFilters.ipCode" placeholder="璇疯緭鍏ユ寚鏍囩偣缂栧彿" clearable />
          </el-form-item>
          <el-form-item label="鎵€灞炴瘯涓氳姹?>
            <el-select
              v-model="ipFilters.grId"
              placeholder="鍏ㄩ儴姣曚笟瑕佹眰"
              clearable
              style="width: 260px"
            >
              <el-option
                v-for="gr in grOptions"
                :key="gr.grId"
                :label="`${gr.grCode} - ${gr.grDescription}`"
                :value="gr.grId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadIps">鏌ヨ</el-button>
            <el-button @click="resetIpFilters">閲嶇疆</el-button>
          </el-form-item>
        </el-form>

        <el-table
          v-loading="ipLoading"
          :data="ips"
          :span-method="indicatorSpanMethod"
          border
        >
          <el-table-column label="鎵€灞炴瘯涓氳姹? min-width="280" class-name="wrap-cell">
            <template #default="{ row }">
              <span v-if="row.grCode">{{ row.grCode }} - {{ row.grDescription }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="ipCode" label="鎸囨爣鐐圭紪鍙? width="140" />
          <el-table-column prop="ipDescription" label="鎸囨爣鐐规弿杩? min-width="300" class-name="wrap-cell" />
          <el-table-column label="鎿嶄綔" width="140" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="openIpEditDialog(row)">缂栬緫</el-button>
                <el-popconfirm title="纭鍒犻櫎璇ユ寚鏍囩偣鍚楋紵" @confirm="handleIpDelete(row)">
                  <template #reference>
                    <el-button link type="danger">鍒犻櫎</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="鏆傛棤鎸囨爣鐐规暟鎹? />
          </template>
        </el-table>
      </section>
    </el-card>

    <el-dialog
      v-model="grDialogVisible"
      :title="grDialogMode === 'create' ? '鏂板姣曚笟瑕佹眰' : '缂栬緫姣曚笟瑕佹眰'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="grFormRef" :model="grForm" :rules="grFormRules" label-width="104px">
        <el-form-item label="缂栧彿" prop="grCode">
          <el-input v-model.trim="grForm.grCode" placeholder="璇疯緭鍏ユ瘯涓氳姹傜紪鍙? />
        </el-form-item>
        <el-form-item label="鎻忚堪" prop="grDescription">
          <el-input
            v-model.trim="grForm.grDescription"
            type="textarea"
            :rows="3"
            placeholder="璇疯緭鍏ユ瘯涓氳姹傛弿杩?
          />
        </el-form-item>
        <el-form-item label="鎵€灞炰笓涓? prop="majorId">
          <el-select v-model="grForm.majorId" placeholder="璇烽€夋嫨鎵€灞炰笓涓? style="width: 100%">
            <el-option
              v-for="major in majorOptions"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="grDialogVisible = false">鍙栨秷</el-button>
          <el-button type="primary" :loading="grSubmitLoading" @click="handleGrSubmit">
            淇濆瓨
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="ipDialogVisible"
      :title="ipDialogMode === 'create' ? '鏂板鎸囨爣鐐? : '缂栬緫鎸囨爣鐐?"
      width="560px"
      destroy-on-close
    >
      <el-form ref="ipFormRef" :model="ipForm" :rules="ipFormRules" label-width="120px">
        <el-form-item label="鎸囨爣鐐圭紪鍙? prop="ipCode">
          <el-input v-model.trim="ipForm.ipCode" placeholder="璇疯緭鍏ユ寚鏍囩偣缂栧彿" />
        </el-form-item>
        <el-form-item label="鎸囨爣鐐规弿杩? prop="ipDescription">
          <el-input
            v-model.trim="ipForm.ipDescription"
            type="textarea"
            :rows="3"
            placeholder="璇疯緭鍏ユ寚鏍囩偣鎻忚堪"
          />
        </el-form-item>
        <el-form-item label="鎵€灞炴瘯涓氳姹? prop="grId">
          <el-select v-model="ipForm.grId" placeholder="璇烽€夋嫨鎵€灞炴瘯涓氳姹? style="width: 100%">
            <el-option
              v-for="gr in grOptions"
              :key="gr.grId"
              :label="`${gr.grCode} - ${gr.grDescription}`"
              :value="gr.grId"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="ipDialogVisible = false">鍙栨秷</el-button>
          <el-button type="primary" :loading="ipSubmitLoading" @click="handleIpSubmit">
            淇濆瓨
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  addGraduationRequirementApi,
  addIndicatorPointApi,
  deleteGraduationRequirementApi,
  deleteIndicatorPointApi,
  listGraduationRequirementsApi,
  listIndicatorPointsApi,
  listMajorsApi,
  updateGraduationRequirementApi,
  updateIndicatorPointApi,
} from '@/api/requirements'
import { DEFAULT_HOME_PATH } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isProgramDirector = computed(() => userStore.roleCodes.includes('program_director'))

const majorOptions = ref([])
const grOptions = ref([])

async function loadMajorOptions() {
  majorOptions.value = await listMajorsApi()
}

async function loadGrOptions() {
  grOptions.value = await listGraduationRequirementsApi()
}

const grLoading = ref(false)
const grSubmitLoading = ref(false)
const grDialogVisible = ref(false)
const grDialogMode = ref('create')
const grs = ref([])
const grFormRef = ref(null)
const selectedGrId = ref(null)

const grFilters = reactive({
  grCode: '',
  majorId: null,
})

const grForm = reactive({
  grId: null,
  grCode: '',
  grDescription: '',
  majorId: null,
})

const grFormRules = {
  grCode: [{ required: true, message: '璇疯緭鍏ユ瘯涓氳姹傜紪鍙?, trigger: 'blur' }],
  grDescription: [{ required: true, message: '璇疯緭鍏ユ瘯涓氳姹傛弿杩?, trigger: 'blur' }],
  majorId: [{ required: true, message: '璇烽€夋嫨鎵€灞炰笓涓?, trigger: 'change' }],
}

function normalizeGrFilters() {
  return {
    grCode: grFilters.grCode || undefined,
    majorId: grFilters.majorId || undefined,
  }
}

async function loadGrs() {
  grLoading.value = true
  try {
    grs.value = await listGraduationRequirementsApi(normalizeGrFilters())
    await loadGrOptions()
  } finally {
    grLoading.value = false
  }
}

function resetGrFilters() {
  grFilters.grCode = ''
  grFilters.majorId = null
  loadGrs()
}

function onGrRowChange(row) {
  if (row) {
    selectedGrId.value = row.grId
    ipFilters.grId = row.grId
  } else {
    selectedGrId.value = null
    ipFilters.grId = null
  }
  loadIps()
}

function resetGrForm() {
  grForm.grId = null
  grForm.grCode = ''
  grForm.grDescription = ''
  grForm.majorId = null
}

function openGrCreateDialog() {
  grDialogMode.value = 'create'
  resetGrForm()
  grDialogVisible.value = true
  nextTick(() => grFormRef.value?.clearValidate())
}

function openGrEditDialog(row) {
  grDialogMode.value = 'edit'
  resetGrForm()
  grForm.grId = row.grId
  grForm.grCode = row.grCode
  grForm.grDescription = row.grDescription
  grForm.majorId = row.majorId
  grDialogVisible.value = true
  nextTick(() => grFormRef.value?.clearValidate())
}

async function handleGrSubmit() {
  await grFormRef.value?.validate()

  grSubmitLoading.value = true
  try {
    const payload = {
      grCode: grForm.grCode,
      grDescription: grForm.grDescription,
      majorId: grForm.majorId,
    }
    if (grDialogMode.value === 'create') {
      await addGraduationRequirementApi(payload)
      ElMessage.success('姣曚笟瑕佹眰鍒涘缓鎴愬姛')
    } else {
      await updateGraduationRequirementApi({
        grId: grForm.grId,
        ...payload,
      })
      ElMessage.success('姣曚笟瑕佹眰鏇存柊鎴愬姛')
    }
    grDialogVisible.value = false
    await loadGrs()
  } finally {
    grSubmitLoading.value = false
  }
}

async function handleGrDelete(row) {
  try {
    await deleteGraduationRequirementApi({ grId: row.grId })
    ElMessage.success('姣曚笟瑕佹眰鍒犻櫎鎴愬姛')
    if (selectedGrId.value === row.grId) {
      selectedGrId.value = null
      ipFilters.grId = null
      await loadIps()
    }
    await loadGrs()
  } catch {
    // 閿欒宸茬敱鎷︽埅鍣ㄥ鐞?  }
}

const ipLoading = ref(false)
const ipSubmitLoading = ref(false)
const ipDialogVisible = ref(false)
const ipDialogMode = ref('create')
const ips = ref([])
const ipFormRef = ref(null)
const ipRequirementRowSpans = ref([])

const ipFilters = reactive({
  ipCode: '',
  grId: null,
})

const ipForm = reactive({
  ipId: null,
  ipCode: '',
  ipDescription: '',
  grId: null,
})

const ipFormRules = {
  ipCode: [{ required: true, message: '璇疯緭鍏ユ寚鏍囩偣缂栧彿', trigger: 'blur' }],
  ipDescription: [{ required: true, message: '璇疯緭鍏ユ寚鏍囩偣鎻忚堪', trigger: 'blur' }],
  grId: [{ required: true, message: '璇烽€夋嫨鎵€灞炴瘯涓氳姹?, trigger: 'change' }],
}

function normalizeIpFilters() {
  return {
    ipCode: ipFilters.ipCode || undefined,
    grId: ipFilters.grId || undefined,
  }
}

function buildRequirementRowSpans(rows) {
  const spans = new Array(rows.length).fill(1)
  let index = 0
  while (index < rows.length) {
    const currentGrId = rows[index]?.grId
    let count = 1
    while (index + count < rows.length && rows[index + count]?.grId === currentGrId) {
      count += 1
    }
    spans[index] = count
    for (let offset = 1; offset < count; offset += 1) {
      spans[index + offset] = 0
    }
    index += count
  }
  ipRequirementRowSpans.value = spans
}

function indicatorSpanMethod({ columnIndex, rowIndex }) {
  if (columnIndex !== 0) {
    return [1, 1]
  }
  const rowspan = ipRequirementRowSpans.value[rowIndex] ?? 1
  return [rowspan, rowspan > 0 ? 1 : 0]
}

async function loadIps() {
  ipLoading.value = true
  try {
    ips.value = await listIndicatorPointsApi(normalizeIpFilters())
    buildRequirementRowSpans(ips.value)
  } finally {
    ipLoading.value = false
  }
}

function resetIpFilters() {
  ipFilters.ipCode = ''
  ipFilters.grId = null
  loadIps()
}

function resetIpForm() {
  ipForm.ipId = null
  ipForm.ipCode = ''
  ipForm.ipDescription = ''
  ipForm.grId = selectedGrId.value
}

function openIpCreateDialog() {
  ipDialogMode.value = 'create'
  resetIpForm()
  ipDialogVisible.value = true
  nextTick(() => ipFormRef.value?.clearValidate())
}

function openIpEditDialog(row) {
  ipDialogMode.value = 'edit'
  resetIpForm()
  ipForm.ipId = row.ipId
  ipForm.ipCode = row.ipCode
  ipForm.ipDescription = row.ipDescription
  ipForm.grId = row.grId
  ipDialogVisible.value = true
  nextTick(() => ipFormRef.value?.clearValidate())
}

async function handleIpSubmit() {
  await ipFormRef.value?.validate()

  ipSubmitLoading.value = true
  try {
    const payload = {
      ipCode: ipForm.ipCode,
      ipDescription: ipForm.ipDescription,
      grId: ipForm.grId,
    }
    if (ipDialogMode.value === 'create') {
      await addIndicatorPointApi(payload)
      ElMessage.success('鎸囨爣鐐瑰垱寤烘垚鍔?)
    } else {
      await updateIndicatorPointApi({
        ipId: ipForm.ipId,
        ...payload,
      })
      ElMessage.success('鎸囨爣鐐规洿鏂版垚鍔?)
    }
    ipDialogVisible.value = false
    await loadIps()
  } finally {
    ipSubmitLoading.value = false
  }
}

async function handleIpDelete(row) {
  try {
    await deleteIndicatorPointApi({ ipId: row.ipId })
    ElMessage.success('鎸囨爣鐐瑰垹闄ゆ垚鍔?)
    await loadIps()
  } catch {
    // 閿欒宸茬敱鎷︽埅鍣ㄥ鐞?  }
}

onMounted(async () => {
  if (!isProgramDirector.value) {
    ElMessage.error('褰撳墠璐﹀彿鏃犳潈璁块棶姣曚笟瑕佹眰涓庢寚鏍囩偣椤甸潰')
    router.replace(DEFAULT_HOME_PATH)
    return
  }

  await loadMajorOptions()
  await Promise.all([loadGrs(), loadIps()])
})
</script>

<style scoped>
.requirements-page {
  padding: 20px;
}

.page-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
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
  letter-spacing: 0.04em;
}

.page-summary {
  margin: 0;
  max-width: 720px;
  color: #64748b;
  line-height: 1.75;
}

.entity-section {
  margin-bottom: 28px;
}

.entity-section:last-child {
  margin-bottom: 0;
}

.section-divider {
  height: 1px;
  margin: 4px 0 28px;
  background: rgba(148, 163, 184, 0.35);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.filter-form {
  margin-bottom: 12px;
}

:deep(.el-table__header-wrapper th .cell) {
  font-weight: 700 !important;
}

:deep(.wrap-cell .cell) {
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  line-height: 1.6;
  word-break: break-word;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-muted {
  color: #94a3b8;
}
</style>
