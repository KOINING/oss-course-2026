<script setup>
/**
 * 通用查询栏：支持输入框、下拉框、查询、重置
 * @typedef {'input' | 'select'} QueryFieldType
 * @typedef {Object} QueryField
 * @property {string} prop - 绑定字段名
 * @property {string} label - 表单项标签
 * @property {QueryFieldType} [type='input']
 * @property {string} [placeholder]
 * @property {boolean} [clearable=true]
 * @property {number|string} [width] - 控件宽度
 * @property {Array<{label: string, value: string|number}>} [options] - 下拉选项
 */

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
  },
  fields: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

function updateField(prop, value) {
  props.modelValue[prop] = value
  emit('update:modelValue', props.modelValue)
}

function handleSearch() {
  emit('search')
}

function handleReset() {
  emit('reset')
}
</script>

<template>
  <el-form :inline="true" :model="modelValue" class="query-bar" @submit.prevent>
    <el-form-item
      v-for="field in fields"
      :key="field.prop"
      :label="field.label"
    >
      <el-input
        v-if="field.type !== 'select'"
        :model-value="modelValue[field.prop]"
        :placeholder="field.placeholder || `请输入${field.label}`"
        :clearable="field.clearable !== false"
        :style="{ width: field.width ? `${field.width}px` : '200px' }"
        @update:model-value="(val) => updateField(field.prop, val)"
      />
      <el-select
        v-else
        :model-value="modelValue[field.prop]"
        :placeholder="field.placeholder || `请选择${field.label}`"
        :clearable="field.clearable !== false"
        :style="{ width: field.width ? `${field.width}px` : '160px' }"
        @update:model-value="(val) => updateField(field.prop, val)"
      >
        <el-option
          v-for="option in field.options || []"
          :key="`${field.prop}-${option.value}`"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item class="query-bar__actions">
      <el-button type="primary" :loading="loading" @click="handleSearch">
        查询
      </el-button>
      <el-button :disabled="loading" @click="handleReset">重置</el-button>
      <slot name="actions" />
    </el-form-item>
  </el-form>
</template>

<style scoped>
.query-bar {
  margin-bottom: 16px;
}

.query-bar__actions {
  margin-right: 0;
}
</style>
