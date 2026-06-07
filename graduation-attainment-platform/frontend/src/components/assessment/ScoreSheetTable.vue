<template>
  <div class="score-sheet-table">
    <el-table
      :data="rows"
      border
      stripe
      :max-height="maxHeight"
      show-summary
      :summary-method="getSummary"
    >
      <el-table-column prop="studentNo" label="学号" width="140" fixed="left" />
      <el-table-column prop="studentName" label="姓名" width="120" fixed="left" />

      <el-table-column
        v-for="(ap, index) in headers"
        :key="ap.apId"
        :min-width="editable ? 170 : 150"
        align="center"
      >
        <template #header>
          <div class="dynamic-header">
            <div class="dynamic-header__name">{{ ap.apName }}</div>
            <div class="dynamic-header__meta">
              <span>满分 {{ formatScore(ap.fullScore) }}</span>
              <el-tag size="small" type="primary" effect="plain">{{ ap.objectiveCode }}</el-tag>
            </div>
          </div>
        </template>
        <template #default="{ row, $index }">
          <div v-if="editable" class="editable-cell">
            <el-input-number
              :model-value="normalizeValue(row.scores?.[index])"
              :min="0"
              :max="normalizeValue(ap.fullScore)"
              :step="0.1"
              :precision="2"
              controls-position="right"
              class="score-input"
              @update:model-value="(value) => emitUpdate($index, index, value)"
            />
          </div>
          <span v-else :class="row.scores?.[index] == null ? 'score-empty' : 'score-value'">
            {{ row.scores?.[index] == null ? '-' : formatScore(row.scores[index]) }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
const props = defineProps({
  rows: {
    type: Array,
    default: () => [],
  },
  headers: {
    type: Array,
    default: () => [],
  },
  maxHeight: {
    type: Number,
    default: 620,
  },
  editable: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update-score'])

function normalizeValue(value) {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const number = Number(value)
  return Number.isNaN(number) ? null : number
}

function formatScore(value) {
  const number = normalizeValue(value)
  if (number === null) {
    return '-'
  }
  return Number.isInteger(number) ? `${number}` : number.toFixed(1)
}

function emitUpdate(rowIndex, columnIndex, value) {
  emit('update-score', {
    rowIndex,
    columnIndex,
    value: value === undefined || value === '' ? null : value,
  })
}

function getSummary() {
  const sums = [`共 ${props.rows.length} 名学生`, '']
  props.headers.forEach(() => sums.push(''))
  return sums
}
</script>

<style scoped>
.dynamic-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.dynamic-header__name {
  font-weight: 600;
  color: #1f2937;
}

.dynamic-header__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
  flex-wrap: wrap;
  justify-content: center;
}

.editable-cell {
  display: flex;
  justify-content: center;
}

.score-input {
  width: 128px;
}

.score-empty {
  color: #c0c4cc;
}

.score-value {
  color: #111827;
  font-weight: 500;
}
</style>
