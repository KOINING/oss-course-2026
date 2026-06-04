<template>
  <div class="result-entry">
    <div class="entry-cards">
      <el-card class="entry-card">
        <div class="entry-card__icon">
          <el-icon :size="40" color="#2563eb"><DataAnalysis /></el-icon>
        </div>
        <div class="entry-card__body">
          <h3>课程级结果查看</h3>
          <p>
            查看各教学班的课程级达成度计算结果，包括学生-课程目标达成度、班级课程目标平均达成度和课程级指标点达成度。
          </p>
        </div>
        <div class="entry-card__status">
          <el-tag v-if="courseResultAvailable" type="success" effect="light">已有计算结果</el-tag>
          <el-tag v-else type="info" effect="light">暂无数据</el-tag>
        </div>
      </el-card>

      <el-card class="entry-card">
        <div class="entry-card__icon">
          <el-icon :size="40" color="#16a34a"><TrendCharts /></el-icon>
        </div>
        <div class="entry-card__body">
          <h3>专业级结果查看</h3>
          <p>
            查看专业级指标点达成度汇总结果。专业级汇总基于全部支撑课程的课程级结果，按专业-年级-学期维度聚合。
          </p>
        </div>
        <div class="entry-card__status">
          <el-tag v-if="majorResultAvailable" type="success" effect="light">已有汇总结果</el-tag>
          <el-tag v-else type="info" effect="light">暂无数据</el-tag>
        </div>
      </el-card>
    </div>

    <el-card class="entry-info-card">
      <template #header>
        <span class="info-card-title">说明</span>
      </template>
      <div class="info-content">
        <p>
          <strong>课程级计算结果</strong>由课程主讲教师在成绩导入后触发计算。
          计算结果包括学生-课程目标达成度 (Cij)、班级课程目标平均达成度 (C̄j) 和课程级指标点达成度 (Ek)。
        </p>
        <p>
          <strong>专业级汇总结果</strong>由专业负责人确认全部支撑课程均已锁定后触发。
          汇总结果为专业级指标点达成度 (Gk)，基于课程级指标点达成度和宏观支撑权重计算。
        </p>
        <p>
          详细报表和底稿导出将在<strong>模块 D：报表与底稿</strong>中提供。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { DataAnalysis, TrendCharts } from '@element-plus/icons-vue'

const courseResultAvailable = ref(false)
const majorResultAvailable = ref(false)
</script>

<style scoped>
.result-entry {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.entry-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.entry-card {
  display: flex;
  flex-direction: column;
  border-radius: 14px;
}

.entry-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.entry-card__icon {
  text-align: center;
  padding: 8px 0;
}

.entry-card__body h3 {
  margin: 0 0 6px;
  color: #1f2937;
  font-size: 16px;
}

.entry-card__body p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
  font-size: 14px;
}

.entry-card__status {
  margin-top: auto;
  text-align: center;
  padding-top: 4px;
}

.entry-info-card {
  border-radius: 14px;
  background: #f8fafc;
}

.info-card-title {
  font-weight: 600;
  color: #1f2937;
}

.info-content p {
  margin: 0 0 8px;
  color: #475569;
  line-height: 1.7;
  font-size: 14px;
}

.info-content p:last-child {
  margin-bottom: 0;
}
</style>
