<script setup>
import ImportErrorTable from './ImportErrorTable.vue'

defineProps({
  summary: {
    type: Object,
    default: () => ({ totalCount: 0, successCount: 0, failureCount: 0 }),
  },
  failedItems: {
    type: Array,
    default: () => [],
    // Each: { rowNumber: Number, reason: String }
  },
  loading: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '导入结果',
  },
})
</script>

<template>
  <div class="import-result-preview">
    <!-- Title -->
    <div class="preview-header">
      <h3 class="preview-title">{{ title }}</h3>
      <slot name="header-actions" />
    </div>

    <!-- Summary stat cards -->
    <el-row :gutter="16" class="preview-stats">
      <el-col :span="8">
        <div class="stat-card stat-card--total">
          <div class="stat-card__number">{{ summary.totalCount }}</div>
          <div class="stat-card__label">总记录数</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card stat-card--success">
          <div class="stat-card__number">{{ summary.successCount }}</div>
          <div class="stat-card__label">导入成功</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card stat-card--failure">
          <div class="stat-card__number">{{ summary.failureCount }}</div>
          <div class="stat-card__label">导入失败</div>
        </div>
      </el-col>
    </el-row>

    <!-- Warning alert when there are failures -->
    <el-alert
      v-if="summary.failureCount > 0"
      type="warning"
      :closable="false"
      show-icon
      class="preview-alert"
    >
      <template #title>
        导入完成：成功 {{ summary.successCount }} 条，失败 {{ summary.failureCount }} 条，请修正后重新导入
      </template>
    </el-alert>

    <!-- Success alert when all pass -->
    <el-alert
      v-else-if="summary.totalCount > 0 && summary.failureCount === 0"
      type="success"
      :closable="false"
      show-icon
      class="preview-alert"
    >
      <template #title>
        全部 {{ summary.totalCount }} 条记录导入成功
      </template>
    </el-alert>

    <!-- Error detail table -->
    <div v-if="failedItems.length > 0" class="preview-errors">
      <ImportErrorTable
        :failed-items="failedItems"
        :loading="loading"
      />
    </div>

    <!-- Empty state when no data at all -->
    <el-result
      v-if="!loading && summary.totalCount === 0"
      icon="info"
      sub-title="未检测到任何数据"
    />
  </div>
</template>

<style scoped>
.import-result-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.preview-title {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.preview-stats {
  margin-bottom: 0;
}

.stat-card {
  padding: 20px 16px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid transparent;
}

.stat-card--total {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.stat-card--success {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stat-card--failure {
  background: #fef2f2;
  border-color: #fecaca;
}

.stat-card__number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: #1f2937;
}

.stat-card__label {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
}

.stat-card--total .stat-card__number {
  color: #2563eb;
}

.stat-card--success .stat-card__number {
  color: #16a34a;
}

.stat-card--failure .stat-card__number {
  color: #dc2626;
}

.preview-alert {
  margin: 0;
}

.preview-errors {
  margin-top: 4px;
}
</style>
