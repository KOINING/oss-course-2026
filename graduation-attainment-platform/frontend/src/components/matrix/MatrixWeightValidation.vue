<script setup>
defineProps({
  validationErrors: {
    type: Array,
    default: () => [],
  },
  visible: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <div v-if="visible && validationErrors.length > 0" class="matrix-validation">
    <el-alert
      type="error"
      :closable="false"
      show-icon
      class="matrix-validation__banner"
    >
      <template #title>
        <span class="banner-title">当前毕业要求指标点权重未配平，禁止提交</span>
      </template>
      <template #default>
        <p class="banner-desc">
          以下指标点的课程支撑权重合计不等于 1.0，请先在支撑矩阵中修正后再提交。
        </p>
      </template>
    </el-alert>

    <el-card class="matrix-validation__list">
      <template #header>
        <div class="list-header">
          <span class="list-header__title">
            权重未配平的指标点（{{ validationErrors.length }}）
          </span>
        </div>
      </template>

      <div class="validation-items">
        <div
          v-for="error in validationErrors"
          :key="error.indicatorCode"
          class="validation-item"
        >
          <div class="validation-item__info">
            <span class="indicator-code">{{ error.indicatorCode }}</span>
            <span class="indicator-desc">{{ error.indicatorDescription }}</span>
          </div>
          <div class="validation-item__weight">
            <span class="weight-label">当前合计：</span>
            <span class="weight-value weight-value--error">
              {{ error.currentSum?.toFixed(3) }}
            </span>
            <span class="weight-separator">/</span>
            <span class="weight-value weight-value--expected">
              {{ (error.expectedSum ?? 1.0).toFixed(1) }}
            </span>
            <el-tag type="danger" effect="plain" size="small" class="weight-deviation">
              {{ ((error.currentSum ?? 0) - (error.expectedSum ?? 1.0)).toFixed(3) > 0 ? '+' : '' }}
              {{ ((error.currentSum ?? 0) - (error.expectedSum ?? 1.0)).toFixed(3) }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.matrix-validation {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.matrix-validation__banner {
  margin: 0;
}

.banner-title {
  font-weight: 700;
  font-size: 15px;
}

.banner-desc {
  margin: 4px 0 0;
  color: #475569;
  line-height: 1.6;
}

.matrix-validation__list {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-header__title {
  font-weight: 600;
  color: #1f2937;
}

.validation-items {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.validation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.validation-item:last-child {
  border-bottom: none;
}

.validation-item__info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.indicator-code {
  font-weight: 700;
  color: #1f2937;
  font-size: 14px;
  white-space: nowrap;
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 4px;
}

.indicator-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.validation-item__weight {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}

.weight-label {
  color: #64748b;
  font-size: 13px;
}

.weight-value {
  font-weight: 700;
  font-size: 14px;
}

.weight-value--error {
  color: #dc2626;
}

.weight-value--expected {
  color: #64748b;
}

.weight-separator {
  color: #94a3b8;
  margin: 0 2px;
}

.weight-deviation {
  margin-left: 8px;
}
</style>
