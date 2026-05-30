<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

defineProps({
  modelValue: Boolean,
  title: String,
  loading: Boolean,
  templateFields: Array,
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const file = ref(null)

function handleFileSelect(uploadFile) {
  if (uploadFile && uploadFile.raw) {
    file.value = uploadFile.raw
  }
}

function handleRemoveFile() {
  file.value = null
}

function handleConfirm() {
  if (!file.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  emit('confirm', file.value)
}

function handleClose() {
  emit('update:modelValue', false)
  handleRemoveFile()
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="500px"
    @close="handleClose"
  >
    <div class="import-dialog">
      <div class="import-section">
        <h4>选择文件</h4>
        <el-upload
          ref="uploadRef"
          drag
          action="#"
          :auto-upload="false"
          accept=".csv,.xlsx,.xls"
          @change="handleFileSelect"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此或<em>点击选择</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 CSV、Excel 格式，单个文件不超过 10MB
            </div>
          </template>
        </el-upload>

        <div v-if="file" class="file-info">
          <el-tag>{{ file.name }}</el-tag>
          <el-button link type="danger" @click="handleRemoveFile">移除</el-button>
        </div>
      </div>

      <div class="template-section">
        <h4>模板说明</h4>
        <el-table :data="templateFields" size="small" border>
          <el-table-column prop="label" label="字段名" min-width="120" />
          <el-table-column label="必填" width="60">
            <template #default="{ row }">
              <el-tag v-if="row.required" type="danger">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">
        确认导入
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.import-dialog {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.import-section h4,
.template-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.file-info {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-upload-dragger) {
  border-radius: 8px;
}
</style>
