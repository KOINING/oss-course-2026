<!--
  组件预览页面 — 页面组 C 开发调试用
  上线前移除此文件及对应路由/导航配置
-->
<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorState from '@/components/common/ErrorState.vue'
import NoPermission from '@/components/common/NoPermission.vue'
import ImportResultPreview from '@/components/import/ImportResultPreview.vue'
import MatrixWeightValidation from '@/components/matrix/MatrixWeightValidation.vue'

const activeTab = ref('import-course')

// ---- Mock: 课程清单导入错误（与后端 CourseImportResult.failedItems 格式一致） ----
const mockCourseFailedItems = [
  { rowNumber: 3, reason: '课程代码在导入模板中重复: CS201' },
  { rowNumber: 7, reason: '所属专业代码不存在: BIO' },
  { rowNumber: 12, reason: '学分必须为合法数值: abc' },
  { rowNumber: 15, reason: '课程代码不能为空' },
  { rowNumber: 18, reason: '状态值必须为0或1: 2' },
  { rowNumber: 21, reason: '所属专业代码不能为空' },
  { rowNumber: 24, reason: '课程名称不能为空' },
  { rowNumber: 27, reason: '学分不能为负数: -2' },
]

// ---- Mock: 学生名单导入错误（与后端 StudentClassImportResult.failedItems 格式一致） ----
const mockStudentFailedItems = [
  { rowNumber: 2, reason: '学号不能为空' },
  { rowNumber: 5, reason: '学生 2024001 在同一批次中重复导入到教学班 TC2024CS01' },
  { rowNumber: 8, reason: '教学班编号不存在: TC999' },
  { rowNumber: 11, reason: '姓名不能为空' },
  { rowNumber: 14, reason: '专业代码不存在: BIO' },
  { rowNumber: 17, reason: '入学年份不合法: 1899' },
  { rowNumber: 20, reason: '学号不存在: 2021999' },
  { rowNumber: 23, reason: '专业代码不能为空' },
  { rowNumber: 26, reason: '教学班编号不能为空' },
  { rowNumber: 29, reason: '学生 2021005 已存在于教学班 TC2024CS01 中' },
]

// ---- Mock: 矩阵校验错误 ----
const mockMatrixErrors = reactive([
  { indicatorCode: 'IP-1-1', indicatorDescription: '能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题', currentSum: 0.75, expectedSum: 1.0 },
  { indicatorCode: 'IP-2-3', indicatorDescription: '能够对复杂工程问题的解决方案进行评价', currentSum: 1.25, expectedSum: 1.0 },
  { indicatorCode: 'IP-3-2', indicatorDescription: '能够设计满足特定需求的系统或工艺流程', currentSum: 0.0, expectedSum: 1.0 },
])

// ---- 课程导入结果状态切换 ----
const courseResultMode = ref('partial')
const courseSummaryMap = {
  empty: { totalCount: 0, successCount: 0, failureCount: 0 },
  allSuccess: { totalCount: 50, successCount: 50, failureCount: 0 },
  partial: { totalCount: 50, successCount: 42, failureCount: 8 },
  allFailure: { totalCount: 50, successCount: 0, failureCount: 50 },
}

// ---- 学生导入结果状态切换 ----
const studentResultMode = ref('partial')
const studentSummaryMap = {
  empty: { totalCount: 0, successCount: 0, failureCount: 0 },
  allSuccess: { totalCount: 80, successCount: 80, failureCount: 0 },
  partial: { totalCount: 80, successCount: 70, failureCount: 10 },
  allFailure: { totalCount: 80, successCount: 0, failureCount: 80 },
}

// ---- 矩阵校验开关 ----
const matrixErrorsVisible = ref(true)

function handleRetry() {
  ElMessage.info('重试操作已触发（演示）')
}
</script>

<template>
  <div class="demo-page">
    <el-card class="demo-card">
      <template #header>
        <div class="demo-header">
          <div>
            <p class="demo-section">开发工具</p>
            <h1>页面组 C 组件预览</h1>
            <p class="demo-summary">
              全专业课程清单导入结果预览、教学班学生名单导入结果预览、错误提示、宏观支撑矩阵权重校验提示、页面空状态。
            </p>
          </div>
          <el-tag type="warning" effect="plain">开发期间临时页面</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- ===== 导入结果预览 ===== -->
        <el-tab-pane label="导入结果预览" name="import-course">
          <div class="demo-section-block">
            <div class="demo-controls">
              <span class="demo-controls__label">课程导入状态：</span>
              <el-radio-group v-model="courseResultMode" size="small">
                <el-radio-button value="empty">空数据</el-radio-button>
                <el-radio-button value="allSuccess">全部成功</el-radio-button>
                <el-radio-button value="partial">部分失败</el-radio-button>
                <el-radio-button value="allFailure">全部失败</el-radio-button>
              </el-radio-group>
            </div>

            <ImportResultPreview
              title="全专业课程清单导入结果"
              :summary="courseSummaryMap[courseResultMode]"
              :failed-items="courseResultMode === 'partial' || courseResultMode === 'allFailure' ? mockCourseFailedItems : []"
            />
          </div>

          <el-divider />

          <div class="demo-section-block">
            <div class="demo-controls">
              <span class="demo-controls__label">学生名单导入状态：</span>
              <el-radio-group v-model="studentResultMode" size="small">
                <el-radio-button value="empty">空数据</el-radio-button>
                <el-radio-button value="allSuccess">全部成功</el-radio-button>
                <el-radio-button value="partial">部分失败</el-radio-button>
                <el-radio-button value="allFailure">全部失败</el-radio-button>
              </el-radio-group>
            </div>

            <ImportResultPreview
              title="教学班学生名单导入结果"
              :summary="studentSummaryMap[studentResultMode]"
              :failed-items="studentResultMode === 'partial' || studentResultMode === 'allFailure' ? mockStudentFailedItems : []"
            />
          </div>
        </el-tab-pane>

        <!-- ===== 矩阵校验提示 ===== -->
        <el-tab-pane label="矩阵权重校验" name="matrix">
          <div class="demo-section-block">
            <div class="demo-controls">
              <el-switch v-model="matrixErrorsVisible" active-text="显示校验错误" inactive-text="隐藏校验错误" />
            </div>

            <MatrixWeightValidation
              :visible="matrixErrorsVisible"
              :validation-errors="mockMatrixErrors"
            />

            <div v-if="!matrixErrorsVisible" class="demo-hint">
              <el-alert type="success" :closable="false" show-icon>
                <template #title>所有毕业要求指标点权重均已配平（W = 1.0），可以提交。</template>
              </el-alert>
            </div>
          </div>
        </el-tab-pane>

        <!-- ===== 通用状态组件 ===== -->
        <el-tab-pane label="空状态 / 错误 / 无权限" name="states">
          <el-row :gutter="24">
            <el-col :span="8">
              <h3 class="state-label">空状态</h3>
              <el-card class="state-card">
                <EmptyState description="暂无课程数据" />
              </el-card>
              <el-card class="state-card" style="margin-top: 16px">
                <EmptyState description="暂无学生名单">
                  <template #actions>
                    <el-button type="primary">导入名单</el-button>
                  </template>
                </EmptyState>
              </el-card>
            </el-col>

            <el-col :span="8">
              <h3 class="state-label">错误状态</h3>
              <el-card class="state-card">
                <ErrorState message="网络请求失败，请检查网络连接后重试" @retry="handleRetry" />
              </el-card>
              <el-card class="state-card" style="margin-top: 16px">
                <ErrorState message="服务器内部错误" :show-retry="false" />
              </el-card>
            </el-col>

            <el-col :span="8">
              <h3 class="state-label">无权限</h3>
              <el-card class="state-card">
                <NoPermission required-role="专业负责人" />
              </el-card>
              <el-card class="state-card" style="margin-top: 16px">
                <NoPermission />
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.demo-page {
  padding: 20px;
}

.demo-card {
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.demo-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.demo-header h1 {
  margin: 4px 0 8px;
  color: #1f2937;
  font-size: 26px;
}

.demo-section {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.demo-summary {
  margin: 0;
  max-width: 720px;
  color: #64748b;
  line-height: 1.75;
}

.demo-section-block {
  margin-bottom: 8px;
}

.demo-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.demo-controls__label {
  font-weight: 600;
  color: #475569;
  font-size: 14px;
}

.demo-hint {
  margin-top: 20px;
}

.state-label {
  margin: 0 0 12px;
  color: #1f2937;
  font-size: 16px;
}

.state-card {
  border-radius: 12px;
}
</style>
