# 第四周全链路测试数据设计清单

## 1. 设计目标

本清单对应第四周模块 C 全链路测试数据，目标是给
`graduation-attainment-platform/sql/upgrade_week4_e2e_test_data.sql`
提供一份可执行、可解释、可回归的数据设计说明。

执行本清单对应 SQL 前，默认数据库已先完成以下结构升级：

- `upgrade_grade_year_20260603.sql`
- `upgrade_week4_score_calc.sql`

这套数据覆盖 4 个测试上下文：

| 上下文编码 | 专业 | 年级 | 班级状态 | 作用 |
| --- | --- | --- | --- | --- |
| `CS2022_MAIN` | 计算机科学与技术 | 2022 | `5 locked + 1 unsubmitted` | 教师端模板、成绩导入、课程级计算、专业级人工触发 |
| `CS2023_BLOCK` | 计算机科学与技术 | 2023 | `5 locked + 1 score_imported` | 宏观看板阻断、结果页阻断 |
| `SE2022_UNLOCK` | 软件工程 | 2022 | `4 locked` | 专业级结果展示、解锁审批回退 |
| `SE2023_BLOCK` | 软件工程 | 2023 | `3 locked + 1 unsubmitted` | 第二套阻断态样例 |

## 2. 业务编码约定

为避免误删现有数据，本方案统一使用 `E2E` 前缀业务编码。

### 2.1 专业与教师

- 学院代码：`CS`
- 专业代码：
  - `080901` 计算机科学与技术
  - `080902` 软件工程
- 复用教师工号：
  - `T2024001` -> 计科主链路教师
  - `T2024002` -> 软工链路教师
  - `T2024003` -> 软工链路教师

### 2.2 课程编码

| 课程编码 | 专业 | 课程名称 |
| --- | --- | --- |
| `E2E-CS-DS` | 计科 | E2E-数据结构 |
| `E2E-CS-OS` | 计科 | E2E-操作系统 |
| `E2E-CS-NET` | 计科 | E2E-计算机网络 |
| `E2E-CS-DB` | 计科 | E2E-数据库原理 |
| `E2E-CS-SE` | 计科 | E2E-软件工程基础 |
| `E2E-CS-PRA` | 计科 | E2E-工程实践 |
| `E2E-SE-REQ` | 软工 | E2E-需求分析 |
| `E2E-SE-DES` | 软工 | E2E-软件设计 |
| `E2E-SE-TEST` | 软工 | E2E-软件测试 |
| `E2E-SE-PM` | 软工 | E2E-项目管理 |

### 2.3 毕业要求与指标点编码

- 毕业要求编码：`EGR01` ~ `EGR08`
- 指标点编码：`E1-1`、`E1-2` ... `E8-1`、`E8-2`

### 2.4 教学班编码

- 统一格式：`E2E-<MAJOR><GRADE>-<SEQ>`
- 示例：
  - `E2E-CS22-01`
  - `E2E-CS23-06`
  - `E2E-SE22-03`

### 2.5 学号编码

- 统一格式：`<gradeYear><majorCodeTail><seq>`
- 示例：
  - `2022901001` -> 计科 2022 第 1 名学生
  - `2023902008` -> 软工 2023 第 8 名学生

## 3. 按表分段的数据设计

### 3.1 基础主数据

| 表名 | 建议写入行数 | 设计说明 | 业务关联键 |
| --- | ---: | --- | --- |
| `academic_term` | 1 | 确保主测试学期 `2025-2026-1` 存在 | `term_code` |
| `college` | 1 | 复用或补齐 `CS` 学院 | `college_code` |
| `major` | 2 | 复用或补齐计科、软工专业并置为启用 | `major_code` |
| `teacher` | 0 新增 / 3 更新 | 不新增账号，只复用既有教师映射并调整 `major_id` | `teacher_no` |

### 3.2 培养方案层

| 表名 | 建议写入行数 | 设计说明 | 业务关联键 |
| --- | ---: | --- | --- |
| `course` | 10 | 10 门 E2E 测试课程 | `course_code` |
| `course_major` | 20 | 10 门课程分别绑定到各自专业的 2022、2023 年级 | `course_code + major_code + grade_year` |
| `graduation_requirement` | 32 | 4 个上下文各 8 条毕业要求 | `major_code + grade_year + gr_code` |
| `indicator_point` | 64 | 每条毕业要求 2 个指标点 | `major_code + grade_year + gr_code + ip_code` |
| `course_indicator_support` | 64 | 每个上下文 16 条宏观支撑关系，按指标点列配平 | `course_code + ip_code + grade_year` |

说明：

- `course_indicator_support` 虽然不存 `major_id`、`grade_year`，但必须通过
  `course_major(course_id, major_id, grade_year)` 和
  `indicator_point -> graduation_requirement(major_id, grade_year)` 间接对齐。
- 每个指标点在当前上下文下只由 1 门课程支撑，`ΣW = 1.0`，便于解释与回归。

### 3.3 课程大纲与微观配置层

| 表名 | 建议写入行数 | 设计说明 | 业务关联键 |
| --- | ---: | --- | --- |
| `course_objective` | 30 | 每门课程 3 个课程目标 | `course_code + objective_code` |
| `assessment_point` | 40 | 每门课程 4 个考核点，固定绑定到课程目标 | `course_code + ap_name` |
| `objective_indicator_contribution` | 128 | 每个指标点由 2 个课程目标共同支撑，`Σw = 1.0` | `course_code + co_code + ip_code + grade_year` |

说明：

- 每门课程固定 `CO1 / CO2 / CO3`
- 每门课程固定 4 个考核点：
  - `平时作业`
  - `阶段测验`
  - `实验报告`
  - `期末考核`
- 考核点绑定关系：
  - `平时作业` -> `CO1`
  - `阶段测验` -> `CO1`
  - `实验报告` -> `CO2`
  - `期末考核` -> `CO3`

### 3.4 教学执行层

| 表名 | 建议写入行数 | 设计说明 | 业务关联键 |
| --- | ---: | --- | --- |
| `teaching_class` | 20 | 每个课程-专业-年级绑定产生 1 个教学班 | `class_code` |
| `student` | 40 | 4 个上下文各 10 名学生 | `student_no` |
| `student_class` | 200 | 每个上下文的 10 名学生覆盖本上下文全部教学班 | `student_no + class_code` |
| `student_assessment_score` | 720 | 18 个已录入成绩班级，10 学生 * 4 考核点/班 | `student_no + class_code + ap_name` |

说明：

- `unsubmitted` 班：仅有学生名单，没有成绩。
- `score_imported` 班：有完整成绩，但没有课程级结果。
- `locked` 班：有完整成绩，并生成课程级结果。

### 3.5 结果层与回退层

| 表名 | 建议写入行数 | 设计说明 | 业务关联键 |
| --- | ---: | --- | --- |
| `student_objective_achievement` | 510 | 仅为 17 个 `locked` 班生成学生课程目标达成度 | `student_no + class_code + objective_code` |
| `course_objective_achievement` | 51 | 17 个 `locked` 班 * 3 课程目标 | `class_code + objective_code` |
| `course_indicator_achievement` | 56 | 仅为 `locked` 班生成课程级指标点结果 | `class_code + ip_code` |
| `major_indicator_achievement` | 16 | 只为 `SE2022_UNLOCK` 预置专业级结果 | `major_code + grade_year + term_code + ip_code` |
| `unlock_audit_log` | 1 | 为 `SE2022_UNLOCK` 中 1 个班级预置待审批解锁记录 | `class_code` |
| `calc_audit_log` | 0 | 当前代码未实际消费，可留空 | - |
| `temp_import_staging` | 0 | 当前代码未实际消费，可留空 | - |

## 4. 各表之间如何用业务编码关联

### 4.1 专业与课程

- `major.major_code` -> `080901` / `080902`
- `course.course_code` -> `E2E-*`
- `course_major` 用 `course_code + major_code + grade_year` 关联课程与培养方案年级

### 4.2 专业与毕业要求

- `graduation_requirement` 用 `major_code + grade_year + gr_code`
- `indicator_point` 用 `gr_code + ip_code`

### 4.3 课程与指标点

- `course_indicator_support` 通过
  `course.course_code -> course_major.grade_year`
  和
  `indicator_point.ip_code -> graduation_requirement.grade_year`
  形成同一 `专业 + 年级` 上下文

### 4.4 课程目标与考核点

- `course_objective` 用 `course_code + objective_code`
- `assessment_point` 通过 `co_id` 挂接课程目标

### 4.5 课程目标与指标点

- `objective_indicator_contribution` 用
  `course_code + objective_code + ip_code + grade_year`
  形成微观映射

### 4.6 教学班与学生

- `teaching_class.class_code` 唯一标识教学班
- `student.student_no` 唯一标识学生
- `student_class` 用 `student_no + class_code`

### 4.7 成绩与结果

- `student_assessment_score` 用 `student_no + class_code + ap_name`
- `student_objective_achievement` 用 `student_no + class_code + objective_code`
- `course_objective_achievement` 用 `class_code + objective_code`
- `course_indicator_achievement` 用 `class_code + ip_code`
- `major_indicator_achievement` 用 `major_code + grade_year + term_code + ip_code`

## 5. 推荐验证重点

- `080901`、`080902` 两个专业在 `2022`、`2023` 年级均存在 `8` 个毕业要求、`16` 个指标点。
- `course_major.grade_year`、`graduation_requirement.grade_year`、`teaching_class.grade_year` 三者一致。
- 各上下文下 `course_indicator_support` 对每个指标点满足 `ΣW = 1.0`。
- 各课程在每个年级上下文下 `objective_indicator_contribution` 对每个指标点满足 `Σw = 1.0`。
- `CS2022_MAIN` 可执行教师端模板预览、成绩保存、课程级计算。
- `CS2023_BLOCK`、`SE2023_BLOCK` 在专业级看板中被阻断。
- `SE2022_UNLOCK` 可直接展示专业级结果，并可触发审批解锁回退。
