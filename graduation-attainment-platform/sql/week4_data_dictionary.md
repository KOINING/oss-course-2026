# 第四周数据字典（完整版）— 含字段分类/导入口径/前端展示

> 来源：`GRA_db.sql`（29表体系中第6~8组）
> 第四周聚焦 OBE 三层计算引擎的 Level 1 和 Level 2：课程目标达成度 → 课程级指标点达成度 → 专业级指标点达成度

---

## 一、字段分类标签说明

每个字段标注以下标签之一：

| 标签 | 含义 |
|------|------|
| 🔵 **定义字段** | 教师/教务手动录入的配置数据 |
| 🟢 **原始成绩** | 教师录入的学生考核得分，计算引擎的输入 |
| 🟡 **中间计算结果** | 计算引擎自动生成，可能被后续计算引用 |
| 🔴 **课程级结果** | Level 1/2 最终输出，课程级别的达成度 |
| 🟣 **专业级结果** | Level 3 最终输出，专业级别的达成度 |
| ⚪ **系统字段** | 自动维护的时间戳/审计字段 |

---

## 二、第四周核心表总览

```
                    course (课程定义)
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
  course_objective   course_indicator_support   teaching_class
  (课程目标,🔵定义)   (宏观W权重,第三周已有)     (教学班,第三周已有)
         │                                        │
    ┌────┴────┐                                   │
    │         │                                   │
    ▼         ▼                                   │
  objective_indicator    assessment_point          │
  _contribution          (考核点,🔵定义)            │
  (内部权重w,🔵定义)         │                    │
         │                   ▼                    │
         │         student_assessment_score ──────┘
         │              (🟢原始成绩)                │
         │                   │                      │
         │         ┌─────────┴──────────┐           │
         │         │                    │           │
         │         ▼                    ▼           │
         │  course_objective     course_indicator    │
         │  _achievement         _achievement        │
         │  (🔴L1:课程目标达成度)  (🔴L2:课程级达成度) │
         │                                          │
         └────(通过 w 权重加权)──────────────────────┘
                              │
                    按 W 权重汇总(跨课程)
                              │
                              ▼
                  major_indicator_achievement
                  (🟣L3: 专业级最终达成度)
```

---

## 三、7张核心表数据字典（含字段分类）

### 3.1 `course_objective` — 课程目标表

| 标签 | 字段 | 类型 | 约束 | 说明 | 导入/计算/展示 |
|------|------|------|------|------|--------------|
| ⚪ | `co_id` | BIGINT | PK AUTO | 课程目标主键 | 系统主键 |
| 🔵 | `objective_code` | VARCHAR(16) | NOT NULL | 课程目标编码，如 `CO1` | **导入字段** / 前端列表展示 |
| 🔵 | `co_description` | TEXT | NOT NULL | 课程目标详细描述 | **导入字段** / 前端详情展示 |
| 🔵 | `course_id` | BIGINT | NOT NULL FK→course | 所属课程 | **导入字段**（课程下拉选择） |
| ⚪ | `created_at` | DATETIME | NOT NULL | | 系统自动 |
| ⚪ | `updated_at` | DATETIME | NOT NULL ON UPDATE | | 系统自动 |

**约束：** UNIQUE(course_id, objective_code) | INDEX idx_obj_course(course_id)

---

### 3.2 `objective_indicator_contribution` — 内部权重 w 表

| 标签 | 字段 | 类型 | 约束 | 说明 | 导入/计算/展示 |
|------|------|------|------|------|--------------|
| ⚪ | `oic_id` | BIGINT | PK AUTO | 贡献关系主键 | 系统主键 |
| 🔵 | `co_id` | BIGINT | NOT NULL FK→course_objective | 课程目标 | **导入字段**（矩阵中选择） |
| 🔵 | `ip_id` | BIGINT | NOT NULL FK→indicator_point | 指标点 | **导入字段**（矩阵中选择） |
| 🔵 | `internal_weight` | FLOAT | NOT NULL CHECK(≥0≤1) | 内部权重 w | **导入字段**（前端矩阵输入） |
| ⚪ | `created_at` | DATETIME | NOT NULL | | 系统自动 |
| ⚪ | `updated_at` | DATETIME | NOT NULL ON UPDATE | | 系统自动 |

**约束：** UNIQUE(co_id, ip_id) | INDEX idx_oic_objective(co_id), idx_oic_indicator(ip_id), idx_oic_co_ip(co_id, ip_id)

**业务规则：** 同一课程内所有目标对同一 IP 的 w 之和 = 1.0（应用层校验，前端矩阵实时提示）

---

### 3.3 `assessment_point` — 考核点表

| 标签 | 字段 | 类型 | 约束 | 说明 | 导入/计算/展示 |
|------|------|------|------|------|--------------|
| ⚪ | `ap_id` | BIGINT | PK AUTO | 考核点主键 | 系统主键 |
| 🔵 | `ap_name` | VARCHAR(100) | NOT NULL | 考核点名称 | **导入字段** / 前端列表展示 |
| 🔵 | `full_score` | FLOAT | NOT NULL | 满分分值 | **导入字段** / 前端分数列展示 |
| 🔵 | `co_id` | BIGINT | NOT NULL FK→course_objective | 所属课程目标 | **导入字段**（目标下拉选择） |
| ⚪ | `created_at` | DATETIME | NOT NULL | | 系统自动 |
| ⚪ | `updated_at` | DATETIME | NOT NULL ON UPDATE | | 系统自动 |

**约束：** INDEX idx_ap_objective(co_id)

---

### 3.4 `student_assessment_score` — 学生考核成绩表（🟢 原始成绩）

| 标签 | 字段 | 类型 | 约束 | 说明 | 导入/计算/展示 |
|------|------|------|------|------|--------------|
| ⚪ | `sas_id` | BIGINT | PK AUTO | 成绩记录主键 | 系统主键 |
| 🟢 | `student_id` | BIGINT | NOT NULL FK→student | 学生 | **导入字段**（Excel学号匹配） |
| 🟢 | `ap_id` | BIGINT | NOT NULL FK→assessment_point | 考核点 | **导入字段**（Excel列匹配） |
| 🟢 | `class_id` | BIGINT | NOT NULL FK→teaching_class | 教学班 | **导入字段**（班级选择） |
| 🟢 | `actual_score` | FLOAT | NOT NULL | 实际得分 | **导入字段** / **前端展示列** |
| ⚪ | `created_at` | DATETIME | NOT NULL | | 系统自动 |
| ⚪ | `updated_at` | DATETIME | NOT NULL ON UPDATE | | 系统自动 |

**约束：** UNIQUE(student_id, ap_id, class_id) | INDEX idx_sas_class, idx_sas_student, idx_sas_ap_student, idx_sas_class_ap

**前端展示口径：**
- 成绩列表：student_no + student_name + ap_name + actual_score + full_score
- 导出模板字段：学号、学生姓名、考核点1得分、考核点2得分、...（按考核点展开为列）

---

### 3.5 `course_objective_achievement` — 课程目标达成度（🔴 课程级结果 L1）

| 标签 | 字段 | 类型 | 约束 | 说明 | 来源 |
|------|------|------|------|------|------|
| ⚪ | `coa_id` | BIGINT | PK AUTO | 达成度记录主键 | 系统主键 |
| 🔴 | `class_id` | BIGINT | NOT NULL FK→teaching_class | 教学班 | 计算主键 |
| 🔴 | `co_id` | BIGINT | NOT NULL FK→course_objective | 课程目标 | 计算主键 |
| 🔴 | `average_achievement` | FLOAT | NOT NULL CHECK(≥0≤1) | 班级课程目标达成度 | **计算字段** |

**约束：** UNIQUE(class_id, co_id) | INDEX idx_coa_class, idx_coa_class_co

**计算来源：** `student_assessment_score.actual_score` / `assessment_point.full_score`（按课程目标分组平均）

**前端展示口径：**
- 结果展示：课程目标编码 + 达成度值 + 条形图
- 公式：`AVG( SUM(actual_score) / SUM(full_score) )`（每个学生先算，再全班平均）

---

### 3.6 `course_indicator_achievement` — 课程级指标点达成度（🔴 课程级结果 L2）

| 标签 | 字段 | 类型 | 约束 | 说明 | 来源 |
|------|------|------|------|------|------|
| ⚪ | `cia_id` | BIGINT | PK AUTO | 达成度记录主键 | 系统主键 |
| 🔴 | `class_id` | BIGINT | NOT NULL FK→teaching_class | 教学班 | 计算主键 |
| 🔴 | `ip_id` | BIGINT | NOT NULL FK→indicator_point | 指标点 | 计算主键 |
| 🔴 | `achievement` | FLOAT | NOT NULL CHECK(≥0≤1) | 课程级指标点达成度 | **计算字段** |
| 🔴 | `is_locked` | BOOLEAN | NOT NULL DEFAULT FALSE | 锁定状态 | **计算触发**（锁定后禁止重算） |

**约束：** UNIQUE(class_id, ip_id) | INDEX idx_cia_class, idx_cia_class_ip

**计算来源：** `course_objective_achievement.average_achievement` × `objective_indicator_contribution.internal_weight`

**前端展示口径：**
- 结果展示：指标点编码(ip_code) + 达成度 + 是否锁定 + 锁定/解锁按钮
- 公式：`SUM(L1达成度[目标] × w[目标→指标点])`

---

### 3.7 `major_indicator_achievement` — 专业级指标点达成度（🟣 专业级结果 L3）

| 标签 | 字段 | 类型 | 约束 | 说明 | 来源 |
|------|------|------|------|------|------|
| ⚪ | `mia_id` | BIGINT | PK AUTO | 达成度记录主键 | 系统主键 |
| 🟣 | `major_id` | BIGINT | NOT NULL FK→major | 专业 | 计算主键 |
| 🟣 | `term_id` | BIGINT | NOT NULL FK→academic_term | 学期 | 计算主键 |
| 🟣 | `ip_id` | BIGINT | NOT NULL FK→indicator_point | 指标点 | 计算主键 |
| 🟣 | `final_achievement` | FLOAT | NOT NULL CHECK(≥0≤1) | 专业级最终达成度 | **计算字段** |

**约束：** UNIQUE(major_id, term_id, ip_id) | INDEX idx_mia_major, idx_mia_term

**计算来源：** `course_indicator_achievement.achievement` × `course_indicator_support.total_weight`

**前端展示口径：**
- 结果展示：指标点编码 + 达成度 + 合格/不合格 + 进度条
- 合格阈值：0.60（`v_major_achievement_dashboard` 自动判定）
- 公式：`SUM(L2达成度[课程] × W[课程→指标点])`

---

## 四、字段分类汇总（与后端/前端对齐）

### 4.1 导入字段清单（后端接口组需处理的字段）

| 表 | 导入字段 | 导入方式 | 说明 |
|----|---------|---------|------|
| `course_objective` | objective_code, co_description, course_id | 表单输入 | 教师逐条录入 |
| `objective_indicator_contribution` | co_id, ip_id, internal_weight | 矩阵批量保存 | 教师配置权重矩阵 |
| `assessment_point` | ap_name, full_score, co_id | 表单输入 | 教师逐条录入考核点 |
| `student_assessment_score` | student_id, ap_id, class_id, actual_score | **Excel 批量导入** | 教师上传成绩表 |

### 4.2 计算字段清单（后端计算引擎输出的字段）

| 表 | 计算字段 | 输入数据 | 计算逻辑 |
|----|---------|---------|---------|
| `course_objective_achievement` | average_achievement | student_assessment_score + assessment_point | AVG(student's SUM(actual)/SUM(full)) |
| `course_indicator_achievement` | achievement | course_objective_achievement + objective_indicator_contribution | SUM(L1 × w) |
| `major_indicator_achievement` | final_achievement | course_indicator_achievement + course_indicator_support | SUM(L2 × W) |

### 4.3 前端展示字段清单

| 页面 | 所在表 | 展示字段 |
|------|--------|---------|
| 课程目标管理 | course_objective | objective_code, co_description, course_name |
| 内部权重矩阵 | objective_indicator_contribution | co_code × ip_code 矩阵, internal_weight, w求和校验提示 |
| 考核点管理 | assessment_point | ap_name, full_score, objective_code |
| 成绩录入/预览 | student_assessment_score | student_no, student_name, ap_name, actual_score, full_score |
| 课程目标达成度 | course_objective_achievement | objective_code, average_achievement, 条形图 |
| 课程级达成度 | course_indicator_achievement | ip_code, achievement, is_locked, 锁定/解锁按钮 |
| 专业级达成度 | major_indicator_achievement | ip_code, final_achievement, pass_status, 进度条 |

---

## 五、gradeYear 链路（模块B + 模块C）

### 5.1 年级推导路径

```
teaching_class.class_id
  → student_class.student_id
    → student.student_id (PK)
      → student.enrollment_year  ← 入学年份，如 2022

teaching_class.term_id
  → academic_term.term_id (PK)
    → academic_term.academic_year  ← 学年起始年，如 2024

年级 = academic_year - enrollment_year + 1  (如 2024-2022+1 = 大三)
```

### 5.2 毕业要求年级版本化

```
graduation_requirement
  UNIQUE(major_id, enrollment_year, gr_code)
  ← enrollment_year 区分不同年级的毕业要求版本

示例：
  major_id=1, enrollment_year=2022, gr_code='1' → 2022级毕业要求1
  major_id=1, enrollment_year=2023, gr_code='1' → 2023级毕业要求1
```

### 5.3 模块B（课程大纲/内部权重）与年级的关系

| 表 | 与年级的关系 | 说明 |
|----|------------|------|
| `course_objective` | 无年级字段 | 课程目标属于课程定义，与年级无关 |
| `objective_indicator_contribution` | 通过 ip_id 间接关联年级 | ip_id 指向某个年级版本的指标点 |

### 5.4 模块C（成绩录入）与年级的关系

| 表 | 与年级的关系 | 说明 |
|----|------------|------|
| `student_assessment_score` | 通过 student_id → enrollment_year | 成绩属于特定年级的学生 |
| `teaching_class` | 无年级字段 | 教学班可包含不同年级学生（重修等），年级是学生属性 |

---

## 六、已引用的已有表变更

`graduation_requirement` 新增 `enrollment_year INT NOT NULL`，UNIQUE KEY 改为 `uk_major_grade_gr(major_id, enrollment_year, gr_code)`。后续表（`indicator_point`、`course_indicator_support`、`objective_indicator_contribution`、结果表）通过 `ip_id` / `gr_id` 间接引用，无需修改。

---

## 七、三层计算引擎总结

```
┌──────────────────────────────────────────────────────────────────────┐
│ Level 1: 课程目标达成度 (🔴 课程级结果)                               │
│ 输入:  student_assessment_score (🟢 原始成绩)                        │
│       assessment_point (🔵 定义)                                     │
│ 输出:  course_objective_achievement (🔴 L1)                          │
│ 粒度:  教学班 × 课程目标                                              │
│ 公式:  AVG( SUM(actual_score) / SUM(full_score) ) 按目标分组         │
│ 前端:  课程目标编码 + 达成度 + 条形图                                 │
├──────────────────────────────────────────────────────────────────────┤
│ Level 2: 课程级指标点达成度 (🔴 课程级结果)                           │
│ 输入:  course_objective_achievement (🟡 中间结果)                    │
│       objective_indicator_contribution (🔵 w权重)                    │
│ 输出:  course_indicator_achievement (🔴 L2)                          │
│ 粒度:  教学班 × 指标点                                                │
│ 公式:  SUM(L1达成度[目标] × w[目标→指标点])                           │
│ 前端:  指标点编码 + 达成度 + 锁定状态 + 锁定/解锁按钮                 │
├──────────────────────────────────────────────────────────────────────┤
│ Level 3: 专业级指标点达成度 (🟣 专业级结果)                           │
│ 输入:  course_indicator_achievement (🟡 中间结果)                    │
│       course_indicator_support (🔵 W权重, 第三周)                    │
│ 输出:  major_indicator_achievement (🟣 L3)                           │
│ 粒度:  专业 × 学期 × 指标点                                           │
│ 公式:  SUM(L2达成度[课程] × W[课程→指标点])                           │
│ 前端:  指标点编码 + 达成度 + 合格/不合格 + 进度条 (阈值0.60)          │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 八、样例数据覆盖情况

| 要求 | 覆盖方式 | 数据文件 |
|------|---------|---------|
| 1个专业 | 计算机科学与技术 major_id=1 | GRA_db.sql |
| 2个年级 | enrollment_year=2022 + 2023 | GRA_db.sql + week4_sample_data.sql |
| 2套毕业要求版本 | 2022级 GR 1~6(id=1~6) + 2023级 GR 1~6(id=7~12) | week4_sample_data.sql |
| 1门跨年级课程 | 数据结构 course_id=1 | GRA_db.sql |
| 1个完整教学班 | 数据结构2025-2026-1班 class_id=11 | week4_sample_data.sql |
| 课程级成绩样例 | 10名学生 × 7个考核点 = 70条 | week4_sample_data.sql |

---

## 九、视图与审计日志（第四周相关）

| 视图/表 | 说明 |
|---------|------|
| `v_score_drilldown` | 穿透式追溯：指标点→考核点→得分，10表联查，用于认证专家查阅 |
| `v_course_calc_progress` | 计算进度：每个教学班的学生数、成绩条数、calc_status |
| `v_major_achievement_dashboard` | 专业级看板：含合格/不合格判定（阈值 0.60） |
| `calc_audit_log` | 计算审计：记录每次计算的触发人、操作类型、结果快照 |

---

## 十、索引汇总（第四周相关，15条）

| 索引名 | 表 | 字段 |
|--------|-----|------|
| idx_obj_course | course_objective | (course_id) |
| idx_oic_objective | objective_indicator_contribution | (co_id) |
| idx_oic_indicator | objective_indicator_contribution | (ip_id) |
| idx_oic_co_ip | objective_indicator_contribution | (co_id, ip_id) |
| idx_ap_objective | assessment_point | (co_id) |
| idx_sas_class | student_assessment_score | (class_id) |
| idx_sas_student | student_assessment_score | (student_id) |
| idx_sas_ap_student | student_assessment_score | (ap_id, student_id) |
| idx_sas_class_ap | student_assessment_score | (class_id, ap_id) |
| idx_coa_class | course_objective_achievement | (class_id) |
| idx_coa_class_co | course_objective_achievement | (class_id, co_id) |
| idx_cia_class | course_indicator_achievement | (class_id) |
| idx_cia_class_ip | course_indicator_achievement | (class_id, ip_id) |
| idx_mia_major | major_indicator_achievement | (major_id) |
| idx_mia_term | major_indicator_achievement | (term_id) |