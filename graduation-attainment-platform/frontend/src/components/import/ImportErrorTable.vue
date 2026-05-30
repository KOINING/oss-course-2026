<script setup>
import { computed } from 'vue'

const props = defineProps({
  failedItems: {
    type: Array,
    default: () => [],
    // Each: { rowNumber: Number, reason: String }
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

// Auto-classify error type from reason text
const TEMPLATE_KEYWORDS = [
  '不能为空', '必须为合法', '必须为数字', '不能为负数',
  '必须为0或1', '必须为合法数值', '必须为合法年份',
]
const BUSINESS_KEYWORDS = [
  '不存在', '已存在', '重复', '冲突', '不唯一', '不合法',
]

function classifyError(reason) {
  const r = reason || ''
  if (TEMPLATE_KEYWORDS.some((kw) => r.includes(kw))) return 'template'
  if (BUSINESS_KEYWORDS.some((kw) => r.includes(kw))) return 'business'
  return 'unknown'
}

const errorTypeConfig = {
  template: { label: '模板字段错误', type: 'danger' },
  business: { label: '业务校验错误', type: 'warning' },
  unknown: { label: '未知错误', type: 'info' },
}
</script>

<template>
  <div v-loading="loading" class="import-error-table">
    <el-table
      v-if="failedItems.length > 0"
      :data="failedItems"
      border
      stripe
      size="small"
    >
      <el-table-column label="行号" width="80" align="center">
        <template #default="{ row }">
          第 {{ row.rowNumber }} 行
        </template>
      </el-table-column>
      <el-table-column label="错误类型" width="140">
        <template #default="{ row }">
          <el-tag
            :type="errorTypeConfig[classifyError(row.reason)]?.type || 'info'"
            effect="plain"
            size="small"
          >
            {{ errorTypeConfig[classifyError(row.reason)]?.label || '错误' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" min-width="300">
        <template #default="{ row }">
          <span class="cell-message">{{ row.reason }}</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="无导入错误" :image-size="80" />
  </div>
</template>

<style scoped>
.import-error-table {
  min-height: 120px;
}

.cell-message {
  color: #dc2626;
  line-height: 1.6;
  word-break: break-word;
}

:deep(.el-table__header-wrapper th .cell) {
  font-weight: 700;
}
</style>
