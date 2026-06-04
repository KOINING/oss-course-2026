# 成绩计算模块实现总结

## 实现概述

根据第四周任务分配，本人（member/wzr，后端1）完成了 **模板生成、成绩导入、课程级计算、专业级汇总与课程计算状态汇总接口第一版**，包括：

- 动态成绩模板预览接口
- 成绩导入预校验接口
- 成绩保存接口
- 课程级达成度计算接口
- 专业级达成度汇总接口
- 课程计算状态汇总接口

## 目录结构

### 1. 实体类（7个）
- `AssessmentPoint.java` - 考核点实体
- `StudentAssessmentScore.java` - 学生考核成绩实体
- `CourseObjective.java` - 课程目标实体
- `ObjectiveIndicatorContribution.java` - 目标-指标点内部贡献实体
- `CourseObjectiveAchievement.java` - 课程目标达成度实体
- `CourseIndicatorAchievement.java` - 课程级指标点达成度实体
- `MajorIndicatorAchievement.java` - 专业级指标点达成度实体

### 2. Mapper 接口（7个）
- `AssessmentPointMapper.java`
- `StudentAssessmentScoreMapper.java`
- `CourseObjectiveMapper.java`
- `ObjectiveIndicatorContributionMapper.java`
- `CourseObjectiveAchievementMapper.java`
- `CourseIndicatorAchievementMapper.java`
- `MajorIndicatorAchievementMapper.java`

### 3. DTO/VO 对象（8个）

**成绩相关：**
- `ScoreTemplatePreviewResponse.java` - 成绩模板预览响应
- `ScoreImportRequest.java` - 成绩导入请求
- `ScoreImportPreviewResponse.java` - 成绩导入预览响应
- `ScoreSaveRequest.java` - 成绩保存请求

**达成度相关：**
- `CourseCalcRequest.java` - 课程级计算请求
- `CourseCalcResponse.java` - 课程级计算响应
- `MajorCalcRequest.java` - 专业级汇总请求
- `MajorCalcResponse.java` - 专业级汇总响应
- `CourseCalcStatusResponse.java` - 课程计算状态汇总响应

### 4. Service 接口和实现（2个）
- `ScoreCalcService.java` - 接口
- `ScoreCalcServiceImpl.java` - 实现

### 5. Controller（1个）
- `ScoreCalcController.java`

### 6. 文档（2个）
- `docs/score-calc-api-test.md` - API 自测样例文档
- `docs/score-calc-implementation.md` - 本文档

---

## API 接口说明

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/teacher/previewTemplate` | 预览成绩模板 | 课程主讲教师 |
| GET | `/api/teacher/downloadTemplate` | 下载成绩模板 Excel | 课程主讲教师 |
| POST | `/api/teacher/importScorePreview` | 成绩导入预校验 | 课程主讲教师 |
| POST | `/api/teacher/saveScores` | 保存成绩 | 课程主讲教师 |
| POST | `/api/teacher/calcCourseAchievement` | 课程级达成度计算 | 课程主讲教师 |
| POST | `/api/teacher/calcMajorAchievement` | 专业级达成度汇总 | 专业负责人 |
| POST | `/api/teacher/getCourseCalcStatus` | 查询课程计算状态 | 专业负责人/教务管理员 |

---

## 计算公式实现

### 课程级计算

1. **学生-课程目标达成度 Cij**
   ```
   Cij = Σ(支撑目标j的考核点实际得分) / Σ(支撑目标j的考核点满分)
   ```

2. **班级课程目标平均达成度 C̄j**
   ```
   C̄j = 全班 Cij 的算术平均
   ```

3. **课程级指标点达成度 Ek**
   ```
   Ek = Σ(C̄j × wjk)
   其中 wjk 是课程目标j对指标点k的内部贡献权重（来自 objective_indicator_contribution 表）
   ```

### 专业级汇总

4. **专业级指标点达成度 Gk**
   ```
   Gk = Σ(Eck × Wck)
   其中 Eck 是课程c对指标点k的课程级达成度
   其中 Wck 是课程c对指标点k的宏观支撑权重（来自 course_indicator_support 表）
   ```

---

## 校验规则

### 成绩保存校验
- 学生必须属于当前教学班
- 考核点必须存在
- 成绩必须在 0 到满分之间

### 课程级计算校验
- 教学班必须存在
- 必须有成绩数据
- 必须配置课程目标和考核点

### 专业级汇总校验
- 专业和学期必须存在
- 所有支撑课程的教学班必须已完成计算（状态为 locked）
- 同一指标点下 ΣW = 1.0（由第三周支撑矩阵配置保证）

---

## 技术特性

1. **MyBatis-Plus**: 使用 LambdaQueryWrapper 构建查询条件
2. **参数校验**: 使用 Jakarta Validation 注解
3. **Swagger/OpenAPI**: 完整的接口文档注解
4. **事务管理**: 写操作使用 @Transactional 注解
5. **异常处理**: 统一使用 BusinessException 抛出业务异常
6. **返回值封装**: 使用 Result 统一返回格式

---

## 自测样例

详见 `docs/score-calc-api-test.md`，包含：
- 完整的 API 调用示例
- 预期响应格式
- 计算公式说明
- 校验规则说明

---

## 与其他模块的关系

### 依赖的模块
- **TeachingClass（教学班）**：成绩和计算都基于教学班
- **Student（学生）**：成绩属于学生
- **CourseObjective（课程目标）**：由 member/mxb 负责
- **AssessmentPoint（考核点）**：由 member/mxb 负责
- **ObjectiveIndicatorContribution（内部权重 w）**：由 member/mxb 负责
- **CourseIndicatorSupport（宏观权重 W）**：由第三周 member/ygp 负责

### 被依赖的模块
- **报表导出**：第五周模块 D 将使用课程级和专业级结果

---

## 待优化项

1. 完善成绩导入的 Excel 解析功能
2. 添加批量保存成绩的事务优化
3. 添加计算结果的锁定/解锁机制
4. 添加计算审计日志
5. 优化大量数据的计算性能

---

## 编译验证

代码已通过 Maven 编译验证，无语法错误。

```bash
cd graduation-attainment-platform/backend
./mvnw compile -q
```
