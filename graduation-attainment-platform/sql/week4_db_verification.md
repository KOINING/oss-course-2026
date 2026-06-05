# 第四周核查报告（最终版）— 含 GR.enrollment_year + student.enrollment_year 双年级字段

> 核查日期：2026-05-23
> 核查范围：`graduation_requirement.enrollment_year` + `student.enrollment_year` 双年级字段，所有计算链路和约束的完整性
> 变更记录：`graduation_requirement` 新增 `enrollment_year INT NOT NULL`，UNIQUE KEY 改为 `uk_major_grade_gr(major_id, enrollment_year, gr_code)`

---

## 零、graduation_requirement.enrollment_year 字段核查

| 核查项 | 值 | 结论 |
|--------|-----|------|
| 字段名 | `enrollment_year` | ✅ 语义清晰 |
| 类型 | `INT NOT NULL` | ✅ 不可为空 |
| UNIQUE | `uk_major_grade_gr(major_id, enrollment_year, gr_code)` | ✅ 同一专业同一年级下GR编码唯一 |
| FK | `major_id → major(major_id) RESTRICT` | ✅ |
| 下游影响 | `indicator_point.gr_id` 间接引用 | ✅ 无需修改下游表 |

> 此变更使同一专业可以拥有多个年级版本的毕业要求，通过 `indicator_point.gr_id` 实现版本隔离。

---

## 一、student.enrollment_year 字段核查

| 核查项 | 值 | 结论 |
|--------|-----|------|
| 字段名 | `enrollment_year` | ✅ 语义清晰 |
| 类型 | `INT NOT NULL` | ✅ 不可为空 |
| 默认值 | 无 | ✅ 必须显式指定 |
| UNIQUE | 无 | ✅ 不需要（同一年级有多名学生） |
| 索引 | 无独立索引 | ⚠️ 见下文 |
| 外键 | 无 | ✅ 不需要 |

**索引分析：**

| 查询场景 | 使用的索引 | 是否高效 |
|---------|-----------|---------|
| `WHERE major_id=1 AND enrollment_year=2022` | `idx_student_major(major_id)` + 回表过滤 | ✅ 正常 |
| `WHERE student_id=123` | PRIMARY KEY | ✅ 高效 |
| `WHERE enrollment_year=2022`（跨专业） | 无索引，全表扫描 | ⚠️ 低频场景，可接受 |

> 结论：`enrollment_year` 不需要单独索引。主要查询场景（按专业+年级筛选）已通过 `idx_student_major` 覆盖。

---

## 二、年级推导链路核查

```
teaching_class.class_id
  → student_class.student_id
    → student.student_id (PK)
      → student.enrollment_year  ← 入学年份，如 2022

teaching_class.term_id
  → academic_term.term_id (PK)
    → academic_term.academic_year  ← 学年起始年，如 2024

年级 = academic_year - enrollment_year  (如 2024-2022 = 大三)
```

| 链路节点 | FK | 索引 | 结论 |
|---------|-----|------|------|
| student_class.student_id → student | ✅ RESTRICT | ✅ PK | ✅ |
| student_class.class_id → teaching_class | ✅ RESTRICT | ✅ idx_sc_class | ✅ |
| teaching_class.term_id → academic_term | ✅ RESTRICT | ✅ idx_class_term | ✅ |

---

## 三、L3 专业级达成度与年级的关系

```
major_indicator_achievement
  UNIQUE(major_id, term_id, ip_id)
  ← 没有 enrollment_year 字段
```

**这是否正确？**

| 场景 | 分析 |
|------|------|
| OBE 认证评估 | 按**专业整体**评估，不区分年级。✅ 正确 |
| 辅导员/教务按年级查看 | 需 JOIN `student` 表按 `enrollment_year` 分组，衍生于计算数据 |
| 按年级重新计算 | 通过 `student_assessment_score → student.enrollment_year` 过滤，所有中间表粒度都支持 |

> 结论：`major_indicator_achievement` 不存 `enrollment_year` 是正确的。年级是**展示维度**，不是**计算主键**。

---

## 四、5条计算链路核查（含年级维度）

### 链路1：考核点 → 课程目标
```
assessment_point.co_id → course_objective.co_id → course.course_id
```
✅ 不受年级影响，课程目标和考核点属于课程定义。

### 链路2：课程目标 → 指标点（w）
```
course_objective → objective_indicator_contribution → indicator_point
```
✅ 不受年级影响，w 权重是课程大纲配置。

### 链路3：课程 → 指标点（W）
```
course → course_indicator_support → indicator_point
```
✅ 不受年级影响，W 权重是专业级配置。

### 链路4：教学班 → 学生（含年级）
```
teaching_class → student_class → student → enrollment_year
```
✅ 通过 `student.enrollment_year` 可获取年级。`student_class` 的 `idx_sc_class(class_id)` 已补充。

### 链路5：教学班 → 课程 → 专业 + 年级 + 学期
```
teaching_class → course → course_major → major
teaching_class → term → academic_year
teaching_class → student_class → student → enrollment_year
```
✅ 三条路径均可独立查询。

---

## 五、全字段/外键/UNIQUE/索引汇总（第四周相关）

| 表 | 字段 | NULL | 默认值 | FK | UNIQUE | CHECK | 索引 | 结论 |
|----|------|------|--------|-----|--------|-------|------|------|
| **student** | enrollment_year | NOT NULL | — | — | — | — | idx_student_major | ✅ |
| course_objective | co_id | PK | AUTO | — | — | — | — | ✅ |
| | objective_code | NOT NULL | — | — | (course_id,code) | — | — | ✅ |
| | course_id | NOT NULL | — | →course RESTRICT | — | — | idx_obj_course | ✅ |
| obj_indicator_contrib | co_id | NOT NULL | — | →co RESTRICT | (co_id,ip_id) | — | idx_oic_objective | ✅ |
| | ip_id | NOT NULL | — | →ip RESTRICT | — | — | idx_oic_indicator | ✅ |
| | internal_weight | NOT NULL | — | — | — | ≥0≤1 | idx_oic_co_ip | ✅ |
| assessment_point | co_id | NOT NULL | — | →co RESTRICT | — | — | idx_ap_objective | ✅ |
| student_assess_score | student_id | NOT NULL | — | →student RESTRICT | (stu,ap,cls) | — | idx_sas_student | ✅ |
| | ap_id | NOT NULL | — | →ap RESTRICT | — | — | idx_sas_ap_student | ✅ |
| | class_id | NOT NULL | — | →tc RESTRICT | — | — | idx_sas_class, idx_sas_class_ap | ✅ |
| course_obj_achievement | class_id | NOT NULL | — | →tc RESTRICT | (class_id,co_id) | ≥0≤1 | idx_coa_class, idx_coa_class_co | ✅ |
| course_ind_achievement | class_id | NOT NULL | — | →tc RESTRICT | (class_id,ip_id) | ≥0≤1 | idx_cia_class, idx_cia_class_ip | ✅ |
| major_ind_achievement | major_id | NOT NULL | — | →major RESTRICT | (major,term,ip) | ≥0≤1 | idx_mia_major | ✅ |
| | term_id | NOT NULL | — | →term RESTRICT | — | — | idx_mia_term | ✅ |
| student_class | class_id | NOT NULL | — | →tc RESTRICT | (stu,cls) | — | idx_sc_class ✅ | ✅ |

---

## 六、最终结论

| 维度 | 结论 |
|------|------|
| enrollment_year 字段 | ✅ 类型/约束正确，索引覆盖充分 |
| 年级推导链路 | ✅ 完整，teaching_class → student_class → student.enrollment_year |
| 5条计算链路 | ✅ 全部通过，含年级维度 |
| 外键 | ✅ 7张表 FK 全部就位，RESTRICT 策略一致 |
| UNIQUE 约束 | ✅ 所有组合键均有 UNIQUE |
| 索引 | ✅ 15条索引覆盖高频路径，上次缺失的 `idx_sc_class` 已补充 |
| 结果表与年级 | ✅ major_indicator_achievement 不存 enrollment_year 是正确的（专业整体评价） |

**无新增问题。**