<script setup>
import { watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
  width: {
    type: [String, Number],
    default: '560px',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  confirmText: {
    type: String,
    default: '提交',
  },
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel', 'closed'])

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      emit('closed')
    }
  },
)

function handleCancel() {
  emit('update:modelValue', false)
  emit('cancel')
}

function handleConfirm() {
  emit('confirm')
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    destroy-on-close
    @update:model-value="(val) => $emit('update:modelValue', val)"
    @closed="$emit('closed')"
  >
    <slot />

    <template #footer>
      <div class="form-dialog__footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
