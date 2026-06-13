# 第五周报表与台账字段映射说明

> 基准表结构：`gra_db_full.sql`
> 适用数据库：`GraduationDB`
> 配套脚本：先执行 `week5_sample_data.sql`，再执行 `week5_self_check.sql`

---

## 一、核对结论

以 `gra_db_full.sql` 为准复核后，原第五周交付物大方向正确，但有几处会影响直接演示：

| 问题 | 影响 | 已修复位置 |
|---|---|---|
| `teaching_class` 插入缺少必填 `class_code` | `week5_sample_data.sql` 在权威表结构上无法执行 | `week5_sample_data.sql` 改为插入 `W5-CS201-2022-02` |
| 样例数据硬编码 `class_id=12`、`student_id=71~75` | 自增顺序变化后链路漂移 | 改为按 `class_code`、`student_no`、`course_code` 查询 ID |
| 学号 `20220101051~55` 与 `gra_db_full.sql` 已有学生重复 | 执行会触发唯一键冲突 | 新增平行班学生改为 `20220101061~65` |
| 专业级 SQL 使用 `academic_term.term_name` | `academic_term` 没有该字段 | `week5_self_check.sql` 改为 `term_code` |
| 主班 `TC2024CS01` 第三周新增学生没有成绩 | 课程级多班聚合人数与成绩链路不完整 | 已补齐 51~60 号学生原始成绩，并重算 L1/L2 |
| 课程级报表存在收口成单班风险 | 无法证明“课程 + 年级”聚合多教学班 | 自检 SQL 明确按 `course_id + grade_year + ip_id` 分组 |

结论：第五周现在可以完整演示课程级结果、专业级结果/雷达图、穿透式台账，并能用自检 SQL 验证字段链路。

---

## 二、核心字段链路

### 2.1 原始成绩到课程目标

| 层级 | 表 | 关键字段 | 作用 |
|---|---|---|---|
| 原始成绩 | `student_assessment_score` | `student_id`, `ap_id`, `class_id`, `actual_score` | 学生在某教学班、某考核点的实际得分 |
| 考核点 | `assessment_point` | `ap_id`, `co_id`, `full_score` | 把成绩映射到课程目标，并提供满分 |
| 课程目标 | `course_objective` | `co_id`, `course_id`, `objective_code` | 定义课程目标，限定考核点所属课程 |
| 学生目标达成 | `student_objective_achievement` | `student_id`, `class_id`, `co_id`, `achievement` | 每个学生对每个课程目标的达成度 |

计算口径：

```sql
student_objective_achievement.achievement
  = SUM(student_assessment_score.actual_score)
    / SUM(assessment_point.full_score)
  GROUP BY student_id, class_id, co_id
```

必须同时满足：

```sql
student_assessment_score.ap_id = assessment_point.ap_id
assessment_point.co_id = course_objective.co_id
student_assessment_score.class_id = teaching_class.class_id
course_objective.course_id = teaching_class.course_id
```

### 2.2 课程目标到课程级指标点

| 层级 | 表 | 关键字段 | 作用 |
|---|---|---|---|
| 班级目标达成 | `course_objective_achievement` | `class_id`, `co_id`, `average_achievement` | 某教学班内课程目标的平均达成度 |
| 微观权重 | `objective_indicator_contribution` | `co_id`, `ip_id`, `internal_weight` | 课程目标对指标点的贡献权重 `w` |
| 课程级结果 | `course_indicator_achievement` | `class_id`, `ip_id`, `achievement`, `is_locked` | 单教学班对某指标点的课程级结果 |

计算口径：

```sql
course_objective_achievement.average_achievement
  = AVG(student_objective_achievement.achievement)
  GROUP BY class_id, co_id;

course_indicator_achievement.achievement
  = SUM(course_objective_achievement.average_achievement * objective_indicator_contribution.internal_weight)
    / SUM(objective_indicator_contribution.internal_weight)
  GROUP BY class_id, ip_id;
```

说明：当某课程下同一指标点的 `w` 已配平为 1 时，上式与普通加权求和等价；归一化写法可以避免样例中单一目标映射 `w < 1` 时被错误压低。

### 2.3 课程级到专业级

| 层级 | 表 | 关键字段 | 作用 |
|---|---|---|---|
| 宏观支撑 | `course_indicator_support` | `course_id`, `ip_id`, `total_weight` | 课程对指标点的宏观支撑权重 `W` |
| 年级课程关系 | `course_major` | `course_id`, `major_id`, `grade_year` | 限定课程适用专业和年级 |
| 专业级结果 | `major_indicator_achievement` | `major_id`, `grade_year`, `term_id`, `ip_id`, `final_achievement` | 专业、年级、学期、指标点的最终达成度 |

第五周样例中，`major_indicator_achievement` 为计科 `080901`、2022 级、`2024-2025-1` 学期补齐 12 个指标点，保证专业级报告和雷达图能完整展示。

---

## 三、课程级评价报表字段映射

关键要求：课程级报表的结果粒度是“课程 + 年级 + 指标点”，不是“教学班 + 指标点”。

| 报表字段 | 来源 | 映射/计算 |
|---|---|---|
| 课程代码 | `course.course_code` | `teaching_class.course_id -> course.course_id` |
| 课程名称 | `course.course_name` | 同上 |
| 年级 | `teaching_class.grade_year` | 聚合维度 |
| 指标点编码 | `indicator_point.ip_code` | `course_indicator_achievement.ip_id -> indicator_point.ip_id` |
| 指标点描述 | `indicator_point.ip_description` | 同上 |
| 课程级达成度 | `course_indicator_achievement.achievement` | 按教学班参评学生数加权平均 |
| 教学班明细 | `teaching_class.class_code`, `class_name` | `GROUP_CONCAT` 展示每个平行班结果 |
| 参评学生数 | `student_objective_achievement` | `COUNT(DISTINCT student_id)` |
| 选课学生数 | `student_class` | `COUNT(DISTINCT student_id)` |
| 教学班数 | `teaching_class.class_id` | `COUNT(DISTINCT class_id)` |

课程级多班聚合 SQL 口径：

```sql
SELECT
  c.course_code,
  tc.grade_year,
  cia.ip_id,
  SUM(cia.achievement * ec.evaluated_students)
    / SUM(ec.evaluated_students) AS course_grade_achievement
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN (
  SELECT class_id, COUNT(DISTINCT student_id) AS evaluated_students
  FROM student_objective_achievement
  GROUP BY class_id
) ec ON ec.class_id = tc.class_id
GROUP BY c.course_id, tc.grade_year, cia.ip_id;
```

第五周样例场景：

| 课程 | 年级 | 教学班 |
|---|---|---|
| `CS201` 数据结构 | 2022 | `TC2024CS01` 数据结构2024-2025-1班 |
| `CS201` 数据结构 | 2022 | `W5-CS201-2022-02` 数据结构2022级平行2班 |

`week5_self_check.sql` 中的 `1.1`、`1.3`、`1.4` 用于核对该报表是否正确聚合多教学班。

---

## 四、专业级评价报告与雷达图字段映射

| 报表字段 | 来源 | 映射/计算 |
|---|---|---|
| 专业代码 | `major.major_code` | `major_indicator_achievement.major_id -> major.major_id` |
| 专业名称 | `major.major_name` | 同上 |
| 年级 | `major_indicator_achievement.grade_year` | 报告维度 |
| 学期 | `academic_term.term_code` | `term_id -> academic_term.term_id` |
| 毕业要求编码 | `graduation_requirement.gr_code` | `ip_id -> indicator_point -> graduation_requirement` |
| 指标点编码 | `indicator_point.ip_code` | `major_indicator_achievement.ip_id -> indicator_point.ip_id` |
| 达成度 | `major_indicator_achievement.final_achievement` | 专业级最终值 |
| 合格状态 | 计算字段 | `final_achievement >= 0.60` |
| 雷达图颜色 | 计算字段 | `>=0.70` 绿，`>=0.60` 黄，`<0.60` 红 |

雷达图完整性要求：

```sql
COUNT(DISTINCT major_indicator_achievement.ip_id)
  =
COUNT(DISTINCT indicator_point.ip_id)
```

限定条件必须一致：

```sql
major_id + grade_year + term_id
```

`week5_self_check.sql` 中的 `2.1`、`2.2`、`2.4` 用于核对专业级报告和雷达图是否完整。

---

## 五、穿透式台账字段映射

穿透链路：

```text
graduation_requirement
  -> indicator_point
  -> course_indicator_support
  -> course
  -> teaching_class
  -> course_objective
  -> objective_indicator_contribution
  -> assessment_point
  -> student_assessment_score
  -> student
```

| 台账字段 | 来源表 | 来源字段 |
|---|---|---|
| 毕业要求 | `graduation_requirement` | `gr_code`, `gr_description`, `grade_year` |
| 指标点 | `indicator_point` | `ip_code`, `ip_description` |
| 支撑课程 | `course` | `course_code`, `course_name` |
| 教学班 | `teaching_class` | `class_code`, `class_name` |
| 课程目标 | `course_objective` | `objective_code`, `co_description` |
| 考核点 | `assessment_point` | `ap_name`, `full_score` |
| 原始成绩 | `student_assessment_score` | `actual_score` |
| 得分率 | 计算字段 | `actual_score / full_score` |
| 学生 | `student` | `student_no`, `student_name` |
| 宏观权重 | `course_indicator_support` | `total_weight` |
| 微观权重 | `objective_indicator_contribution` | `internal_weight` |

关键防错条件：

```sql
teaching_class.course_id = course_objective.course_id
course_major.course_id = course.course_id
course_major.major_id = graduation_requirement.major_id
course_major.grade_year = graduation_requirement.grade_year
student_class.student_id = student_assessment_score.student_id
student_class.class_id = student_assessment_score.class_id
```

`week5_self_check.sql` 中的 `3.1`、`3.2`、`3.3` 用于展示和核对穿透台账。

---

## 六、年级字段口径

以 `gra_db_full.sql` 为准：

| 表 | 年级字段 | 说明 |
|---|---|---|
| `graduation_requirement` | `grade_year` | 毕业要求适用年级 |
| `course_major` | `grade_year` | 课程适用专业年级 |
| `teaching_class` | `grade_year` | 教学班适用年级 |
| `major_indicator_achievement` | `grade_year` | 专业级结果年级 |
| `student` | `enrollment_year` | 学生入学年份 |

第五周核对规则：

```sql
teaching_class.grade_year = student.enrollment_year
teaching_class.grade_year = graduation_requirement.grade_year
```

`week5_self_check.sql` 的 `4.5` 用于核对年级一致性。

---

## 七、自检 SQL 清单

| 自检目标 | SQL 文件位置 | 期望结果 |
|---|---|---|
| 课程级多教学班场景 | `week5_self_check.sql` 1.1 | `locked_class_count > 1` |
| 课程级加权聚合结果 | `week5_self_check.sql` 1.3 | 按 `课程 + 年级 + 指标点` 输出，教学班数为 2 |
| 简单平均 vs 加权平均 | `week5_self_check.sql` 1.4 | 能看到多班明细，避免误收口为单班 |
| 专业级报告 | `week5_self_check.sql` 2.1 | 计科 2022 级 `2024-2025-1` 有完整结果 |
| 雷达图完整性 | `week5_self_check.sql` 2.4 | `OK: 雷达图完整` |
| 穿透式台账 | `week5_self_check.sql` 3.1 | 可查到原始成绩、考核点、课程目标、W/w |
| 原始成绩到课程目标链路 | `week5_self_check.sql` 4.1、4.2 | 4.1 无结果；4.2 全部 `OK` |
| L1/L2 结果重算 | `week5_self_check.sql` 4.3、4.4 | 全部 `OK` |
| 年级一致性 | `week5_self_check.sql` 4.5 | 无结果 |

