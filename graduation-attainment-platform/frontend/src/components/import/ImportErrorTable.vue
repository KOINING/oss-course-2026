<script setup>
const props = defineProps({
  failedItems: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const ERROR_CLASSIFIERS = [
  {
    type: 'template',
    label: '模板字段错误',
    tagType: 'danger',
    keywords: [
      '不能为空',
      '必须为合法',
      '必须为数字',
      '必须为 0 或 1',
      '必须为合法数值',
      '必须为合法年份',
      '模板',
      '表头',
      '列名',
      '字段缺失',
      '字段类型',
      '字段格式',
    ],
  },
  {
    type: 'student_not_found',
    label: '学号或学生不在当前教学班',
    tagType: 'warning',
    keywords: ['学号', '学生不在', '不在当前教学班', '不属于该教学班', '学生不存在', '未找到该学生'],
  },
  {
    type: 'score_out_of_range',
    label: '成绩超出满分或小于 0',
    tagType: 'warning',
    keywords: ['超出满分', '小于 0', '超出范围', '成绩范围', '大于满分', '负数', '分数不合法', '成绩不合法'],
  },
  {
    type: 'duplicate',
    label: '重复记录',
    tagType: 'warning',
    keywords: ['重复', '已存在', 'duplicate'],
  },
  {
    type: 'context_mismatch',
    label: '当前课程/教学班与年级上下文不匹配',
    tagType: 'danger',
    keywords: ['不匹配', '年级', '上下文', '学期', '不属于当前课程', '课程不一致', '教学班不一致'],
  },
  {
    type: 'unknown',
    label: '未知错误',
    tagType: 'info',
    keywords: [],
  },
]

function classifyError(reason) {
  const message = reason || ''
  for (const cls of ERROR_CLASSIFIERS) {
    if (cls.keywords.length === 0) continue
    if (cls.keywords.some((kw) => message.includes(kw))) return cls.type
  }
  return 'unknown'
}

function getErrorConfig(type) {
  return ERROR_CLASSIFIERS.find((c) => c.type === type) || ERROR_CLASSIFIERS[ERROR_CLASSIFIERS.length - 1]
}

const errorTypeConfig = Object.fromEntries(
  ERROR_CLASSIFIERS.map((c) => [c.type, { label: c.label, type: c.tagType }]),
)
</script>

<template>
  <div v-loading="loading" class="import-error-table">
    <el-table v-if="failedItems.length > 0" :data="failedItems" border stripe size="small">
      <el-table-column label="行号" width="96" align="center">
        <template #default="{ row }">
          第 {{ row.rowNumber }} 行
        </template>
      </el-table-column>
      <el-table-column label="错误类型" width="140">
        <template #default="{ row }">
          <el-tag :type="getErrorConfig(classifyError(row.reason)).tagType" effect="plain" size="small">
            {{ getErrorConfig(classifyError(row.reason)).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" min-width="320">
        <template #default="{ row }">
          <span class="cell-message">{{ row.reason }}</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="暂无导入错误" :image-size="80" />
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
