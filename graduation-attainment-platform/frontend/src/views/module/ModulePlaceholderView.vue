<template>
  <div class="module-page">
    <el-card class="module-card">
      <template #header>
        <div class="module-header">
          <div>
            <p class="module-section">{{ route.meta.moduleTitle }}</p>
            <h1>{{ route.meta.title }}</h1>
          </div>
          <el-tag type="warning" effect="plain">骨架页</el-tag>
        </div>
      </template>

      <div class="module-content">
        <section class="module-block">
          <h2>模块职责</h2>
          <p>{{ route.meta.summary }}</p>
        </section>

        <section class="module-block">
          <h2>对应核心数据对象</h2>
          <div class="entity-list">
            <el-tag
              v-for="entity in entities"
              :key="entity"
              type="info"
              effect="plain"
            >
              {{ entity }}
            </el-tag>
          </div>
        </section>

        <section class="module-block module-note">
          <h2>当前状态</h2>
          <p>
            当前页面用于承接导航结构与模块边界，后续会在此基础上逐步补充真实业务表单、表格、导入导出和计算流程。
          </p>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const entities = computed(() =>
  Array.isArray(route.meta.entities) ? route.meta.entities : [],
)
</script>

<style scoped>
.module-page {
  padding: 20px;
}

.module-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.module-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.module-header h1 {
  margin: 4px 0 0;
  color: #1f2937;
  font-size: 26px;
}

.module-section {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.module-content {
  display: grid;
  gap: 18px;
}

.module-block {
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.module-block h2 {
  margin: 0 0 10px;
  color: #1f2937;
  font-size: 16px;
}

.module-block p {
  margin: 0;
  color: #475569;
  line-height: 1.8;
}

.entity-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.module-note {
  background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%);
  border-color: #fed7aa;
}
</style>
