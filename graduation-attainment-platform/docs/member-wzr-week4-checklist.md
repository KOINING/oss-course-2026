# member/wzr 第四周任务核对清单

## 任务概述

根据第四周任务分配，本人（member/wzr，后端1）负责：**模板生成、成绩导入、课程级计算、专业级汇总与课程计算状态汇总接口第一版**

---

## 接口完成情况

| 序号 | 接口 | 状态 | 说明 |
|------|------|------|------|
| 1 | 动态成绩模板预览接口 | ✅ 完成 | `POST /api/teacher/previewTemplate` |
| 2 | `.xlsx` 模板下载接口 | ✅ 完成 | `GET /api/teacher/downloadTemplate` |
| 3 | 原始成绩导入解析与预校验接口 | ✅ 完成 | `POST /api/teacher/importScorePreview` |
| 4 | 成绩保存接口 | ✅ 完成 | `POST /api/teacher/saveScores` |
| 5 | 课程级达成度计算接口 | ✅ 完成 | `POST /api/teacher/calcCourseAchievement` |
| 6 | 专业级汇总接口 | ✅ 完成 | `POST /api/teacher/calcMajorAchievement` |
| 7 | 课程计算状态汇总接口 | ✅ 完成 | `POST /api/teacher/getCourseCalcStatus` |

---

## 计算公式实现情况

### 课程级计算

| 公式 | 状态 | 实现位置 |
|------|------|----------|
| `Cij = Σ(支撑目标j的考核点实际得分) / Σ(支撑目标j的考核点满分)` | ✅ 完成 | `ScoreCalcServiceImpl.calcCourseAchievement()` |
| `C̄j = 全班 Cij 的算术平均` | ✅ 完成 | `ScoreCalcServiceImpl.calcCourseAchievement()` |
| `Ek = Σ(C̄j × wjk)` | ✅ 完成 | `ScoreCalcServiceImpl.calcCourseAchievement()` |

### 专业级汇总

| 公式 | 状态 | 实现位置 |
|------|------|----------|
| `Gk = Σ(Eck × Wck)` | ✅ 完成 | `ScoreCalcServiceImpl.calcMajorAchievement()` |

---

## 中间结果保存情况

| 中间结果 | 状态 | 保存位置 |
|----------|------|----------|
| 学生-课程目标达成度 | ✅ 完成 | `student_objective_achievement` 表 |
| 班级课程目标平均达成度 | ✅ 完成 | `course_objective_achievement` 表 |
| 课程-指标点达成度 | ✅ 完成 | `course_indicator_achievement` 表 |
| 专业-指标点达成度 | ✅ 完成 | `major_indicator_achievement` 表 |

---

## 核心校验实现情况

| 校验项 | 状态 | 说明 |
|--------|------|------|
| 学生属于当前教学班 | ✅ 完成 | 成绩保存时校验 |
| 考核点属于当前课程目标 | ✅ 完成 | 通过课程目标关联查询 |
| 成绩分值合法（0~满分） | ✅ 完成 | 成绩保存时校验 |
| 课程级结果未完成时，专业级汇总不能执行 | ✅ 完成 | 检查教学班 calcStatus 是否为 locked |
| 专业级汇总按 `专业 + 年级 + 学期` 计算 | ✅ 完成 | 通过 majorId + termId 过滤 |

---

## 模板生成前置校验

| 校验项 | 状态 | 说明 |
|--------|------|------|
| 当前教学班存在学生名单 | ✅ 完成 | 检查 student_class 表 |
| 当前课程已配置课程目标 | ✅ 完成 | 检查 course_objective 表 |
| 当前课程已配置内部权重 `w` | ⚠️ 部分完成 | 由 member/mxb 负责的模块实现 |
| 当前课程已配置考核点 | ✅ 完成 | 检查 assessment_point 表 |

---

## 成绩导入预校验

| 校验项 | 状态 | 说明 |
|--------|------|------|
| 模板列头与系统当前考核点配置一致 | ✅ 完成 | 预校验时检查 |
| 固定列 `学号 | 姓名` 存在 | ✅ 完成 | 预校验时检查 |
| 学号属于当前教学班 | ✅ 完成 | 预校验时检查 |
| 各考核点分值在 `0 ~ 满分` 范围内 | ✅ 完成 | 保存时校验 |

---

## 课程级计算触发后完成事项

| 事项 | 状态 | 说明 |
|------|------|------|
| 计算结果落库 | ✅ 完成 | 保存到 4 张达成度表 |
| 当前教学班课程级状态改为已完成或已锁定 | ✅ 完成 | 更新 teaching_class.calc_status |
| 原始成绩不允许被课程主讲教师继续修改 | ✅ 完成 | 检查 calc_status 是否为 locked |

---

## 专业级汇总前校验

| 校验项 | 状态 | 说明 |
|--------|------|------|
| 当前专业当前年级相关支撑课程全部完成阶段一 | ✅ 完成 | 检查所有教学班 calc_status 是否为 locked |
| 同一指标点下 `ΣW = 1.0` | ⚠️ 部分完成 | 由第三周支撑矩阵配置保证 |
| 所有课程级结果属于同一 `专业 + 年级 + 学期` | ✅ 完成 | 通过 majorId + termId 过滤 |

---

## 课程计算状态汇总接口返回内容

| 返回项 | 状态 | 说明 |
|--------|------|------|
| 当前专业 | ✅ 完成 | 返回 majorId, majorName |
| 当前学期 | ✅ 完成 | 返回 termId, termCode |
| 支撑课程列表 | ✅ 完成 | 返回 courseStatuses |
| 每门课程对应教学班或课程级计算状态 | ✅ 完成 | 返回 calcStatus |
| 是否已锁定 | ✅ 完成 | 返回 isLocked |
| 是否满足专业级汇总前置条件 | ✅ 完成 | 返回 canCalcMajor |

---

## 文件清单

### 实体类（8个）
- `AssessmentPoint.java`
- `StudentAssessmentScore.java`
- `CourseObjective.java`
- `ObjectiveIndicatorContribution.java`
- `CourseObjectiveAchievement.java`
- `CourseIndicatorAchievement.java`
- `MajorIndicatorAchievement.java`
- `StudentObjectiveAchievement.java` ✨ 新增

### Mapper 接口（8个）
- `AssessmentPointMapper.java`
- `StudentAssessmentScoreMapper.java`
- `CourseObjectiveMapper.java`
- `ObjectiveIndicatorContributionMapper.java`
- `CourseObjectiveAchievementMapper.java`
- `CourseIndicatorAchievementMapper.java`
- `MajorIndicatorAchievementMapper.java`
- `StudentObjectiveAchievementMapper.java` ✨ 新增

### DTO/VO 对象（9个）
- `ScoreTemplatePreviewResponse.java`
- `ScoreImportRequest.java`
- `ScoreImportPreviewResponse.java`
- `ScoreSaveRequest.java`
- `CourseCalcRequest.java`
- `CourseCalcResponse.java`
- `MajorCalcRequest.java`
- `MajorCalcResponse.java`
- `CourseCalcStatusResponse.java`

### Service 接口和实现（2个）
- `ScoreCalcService.java`
- `ScoreCalcServiceImpl.java`

### Controller（1个）
- `ScoreCalcController.java`

### 文档和脚本（3个）
- `docs/score-calc-api-test.md`
- `docs/score-calc-implementation.md`
- `sql/upgrade_week4_score_calc.sql` ✨ 新增

---

## 总结

所有核心任务已完成，包括：
- ✅ 7 个接口全部实现
- ✅ 计算公式按要求实现
- ✅ 4 层中间结果全部保存到数据库
- ✅ 核心校验逻辑已实现
- ✅ Swagger 文档已添加
- ✅ 自测样例文档已创建
- ✅ 数据库升级脚本已创建

### 待协调事项

1. **内部权重 `w` 校验**：模板生成前置校验中的"当前课程已配置内部权重 `w`"需要与 member/mxb 协调，确认 ObjectiveIndicatorContribution 表的数据完整性
2. **`ΣW = 1.0` 校验**：专业级汇总前的权重校验需要与第三周的支撑矩阵配置对齐

### 编译验证

```bash
cd graduation-attainment-platform/backend
./mvnw compile -q
```

编译通过，无错误。
