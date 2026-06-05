# 第三周数据库任务（支佳璇 / member/zjx）

## 一、任务说明

第三周聚焦模块A第二阶段落地，所有工作**基于已有28张表 + 4个视图**，不需要修改任何表结构。

核心任务只有两项：

| 任务 | 说明 | 交付物 |
|------|------|--------|
| 核查确认 | 确认第三周涉及的6张主表 + 1张暂存表 + 1个视图的约束和索引正确 | 约束确认清单 |
| 初始化数据 | 补充足量测试数据，支撑前后端并行开发和联调 | `week3_init_data.sql` |

---

## 二、第三周涉及的表结构详情

> 以下 DDL 来自 `GRA_db.sql`，本节按业务分组列出第三周涉及的每张表及其完整字段、约束和索引。

### 2.1 `major` — 专业表

> 课程和教学班都归属于专业，第三周所有数据以"计算机科学与技术（major_id=1）"为核心展开。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `major_id` | BIGINT | PK AUTO_INCREMENT | 专业主键 |
| `major_code` | VARCHAR(20) | NOT NULL UNIQUE | 专业编码，如 `080901` |
| `major_name` | VARCHAR(100) | NOT NULL | 专业名称 |
| `college_id` | BIGINT | NOT NULL FK→college RESTRICT | 所属学院 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=招生中 0=停招 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

### 2.2 `teacher` — 教师表

> 教学班需要关联教师（teaching_class.teacher_id）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 教师主键 |
| `teacher_no` | VARCHAR(32) | NOT NULL UNIQUE | 工号，如 `T2024001` |
| `teacher_name` | VARCHAR(64) | NOT NULL | 姓名 |
| `title` | VARCHAR(64) | NULL | 职称 |
| `major_id` | BIGINT | NULL FK→major SET NULL | 所属专业 |
| `user_id` | BIGINT | NULL UNIQUE FK→sys_user SET NULL | 关联系统用户 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=在职 0=离职 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

索引：`uk_teacher_user(user_id)`、`idx_teacher_major(major_id)`

### 2.3 `academic_term` — 学期表

> 教学班需要关联学期（teaching_class.term_id）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `term_id` | BIGINT | PK AUTO_INCREMENT | 学期主键 |
| `term_code` | VARCHAR(20) | NOT NULL UNIQUE | 如 `2024-2025-1` |
| `academic_year` | INT | NOT NULL | 学年起始年 |
| `semester` | INT | NOT NULL CHECK(1,2,3) | 1/2/3 |
| `start_date` | DATE | NOT NULL | 开始日期 |
| `end_date` | DATE | NOT NULL | 结束日期 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=当前学期 0=历史学期 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

### 2.4 `graduation_requirement` — 毕业要求表

> 支撑矩阵通过 `indicator_point.gr_id` 间接关联到此表，用于矩阵总览视图。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `gr_id` | BIGINT | PK AUTO_INCREMENT | 毕业要求主键 |
| `gr_code` | VARCHAR(10) | NOT NULL | 编号如 `1` |
| `gr_description` | TEXT | NOT NULL | 完整描述 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 所属专业 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=启用 0=停用 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

约束：`UNIQUE(major_id, gr_code)`、`idx_req_major(major_id)`

### 2.5 `indicator_point` — 指标点表

> 支撑矩阵直接关联此表（course_indicator_support.ip_id），是矩阵的"列"维度。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `ip_id` | BIGINT | PK AUTO_INCREMENT | 指标点主键 |
| `ip_code` | VARCHAR(10) | NOT NULL | 如 `1.1` |
| `ip_description` | TEXT | NOT NULL | 详细描述 |
| `gr_id` | BIGINT | NOT NULL FK→graduation_requirement RESTRICT | 所属毕业要求 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=启用 0=停用 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

约束：`UNIQUE(gr_id, ip_code)`、`idx_ind_req(gr_id)`

---

### 2.6 `course` — 课程表（★ 主表，全专业课程清单存储目标）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `course_id` | BIGINT | PK AUTO_INCREMENT | 课程主键 |
| `course_code` | VARCHAR(20) | NOT NULL UNIQUE | 课程编码，如 `CS201` |
| `course_name` | VARCHAR(100) | NOT NULL | 课程名称 |
| `credit` | FLOAT | NOT NULL | 学分 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=开课中 0=停开 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

### 2.7 `course_major` — 课程-专业关联表（★ 主表，课程归属专业的绑定）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `cm_id` | BIGINT | PK AUTO_INCREMENT | 关联主键 |
| `course_id` | BIGINT | NOT NULL FK→course CASCADE | 课程ID |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 专业ID |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |

约束：`UNIQUE(course_id, major_id)`、`idx_cm_course(course_id)`、`idx_cm_major(major_id)`

### 2.8 `teaching_class` — 教学班级表（★ 主表，后端接口组A核心操作目标）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `class_id` | BIGINT | PK AUTO_INCREMENT | 班级主键 |
| `class_name` | VARCHAR(50) | NOT NULL | 班级名称 |
| `course_id` | BIGINT | NOT NULL FK→course RESTRICT | 所属课程 |
| `term_id` | BIGINT | NOT NULL FK→academic_term RESTRICT | 所属学期 |
| `teacher_id` | BIGINT | NOT NULL FK→teacher RESTRICT | 主讲教师 |
| `calc_status` | ENUM | NOT NULL DEFAULT 'unsubmitted' | unsubmitted / score_imported / calculating / locked |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

约束：`UNIQUE(course_id, term_id, class_name)`、`idx_class_course(course_id)`、`idx_class_term(term_id)`、`idx_class_teacher(teacher_id)`、`idx_tc_calc_status(calc_status, term_id)`

### 2.9 `student` — 学生表（★ 主表，后端接口组A核心操作目标）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `student_id` | BIGINT | PK AUTO_INCREMENT | 学生主键 |
| `student_no` | VARCHAR(20) | NOT NULL UNIQUE | 学号，如 `20220101001` |
| `student_name` | VARCHAR(50) | NOT NULL | 姓名 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 所属专业 |
| `enrollment_year` | INT | NOT NULL | 入学年份 |
| `user_id` | BIGINT | NULL UNIQUE FK→sys_user SET NULL | 关联系统用户 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=在读 2=毕业 3=休学 0=退学 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

约束：`UNIQUE(student_no)`、`uk_student_user(user_id)`、`idx_student_major(major_id)`

### 2.10 `student_class` — 学生-教学班关联表（★ 主表，名单导入目标）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `sc_id` | BIGINT | PK AUTO_INCREMENT | 关联主键 |
| `student_id` | BIGINT | NOT NULL FK→student RESTRICT | 学生ID |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 班级ID |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |

约束：`UNIQUE(student_id, class_id)` — 同一学生不能重复加入同一教学班

### 2.11 `course_indicator_support` — 宏观支撑矩阵表（★ 主表，后端接口组C核心操作目标）

> 这是第三周最核心的表：存"课程→指标点"的宏观支撑关系及总支撑权重 W。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `cis_id` | BIGINT | PK AUTO_INCREMENT | 支撑关系主键 |
| `course_id` | BIGINT | NOT NULL FK→course RESTRICT | 支撑课程 |
| `ip_id` | BIGINT | NOT NULL FK→indicator_point RESTRICT | 被支撑指标点 |
| `total_weight` | FLOAT | NOT NULL CHECK(≥0 AND ≤1) | 总支撑权重 W |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | |

约束：`UNIQUE(course_id, ip_id)` — 同一课程对同一指标点仅一条支撑记录、`CHECK(total_weight >= 0 AND total_weight <= 1)`、`idx_cis_course(course_id)`、`idx_cis_indicator(ip_id)`

> **注意：** 单表 CHECK 只能保证每个 W 值在 0~1 范围，不能保证同一指标点下所有 W 之和 = 1.0。列和校验通过 `v_weight_validation` 视图完成。

### 2.12 `temp_import_staging` — Excel导入暂存表（★ 辅助表，后端接口组B导入依赖）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `staging_id` | BIGINT | PK AUTO_INCREMENT | 暂存记录ID |
| `batch_id` | VARCHAR(36) | NOT NULL | 导入批次UUID |
| `table_name` | VARCHAR(64) | NOT NULL | 目标表名：course / student / student_class |
| `row_index` | INT | NOT NULL | Excel行号（从2开始） |
| `row_data` | JSON | NOT NULL | 行原始数据JSON |
| `status` | ENUM | DEFAULT 'pending' | pending / validated / imported / error |
| `error_msg` | VARCHAR(512) | NULL | 校验失败原因 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | |

索引：`idx_staging_batch(batch_id)`、`idx_staging_status(batch_id, status)`

### 2.13 `v_weight_validation` — 宏观支撑矩阵权重校验视图（★ 视图，后端接口组C核心依赖）

> 查询每个指标点的当前 W 总和，以及是否满足 W=1.0。

| 输出列 | 来源 | 说明 |
|--------|------|------|
| `ip_id` | indicator_point | 指标点ID |
| `ip_code` | indicator_point | 指标点编码 |
| `gr_code` | graduation_requirement | 毕业要求编码 |
| `major_id` | major | 专业ID |
| `major_name` | major | 专业名称 |
| `support_course_count` | COUNT(cis.course_id) | 支撑该指标点的课程数 |
| `weight_sum` | COALESCE(SUM(total_weight), 0) | 当前W总和 |
| `is_valid` | 计算列 | `ABS(weight_sum - 1.0) < 0.001` → OK，否则 FAIL |

完整 DDL：

```sql
CREATE OR REPLACE VIEW v_weight_validation AS
SELECT 
    ip.ip_id,
    ip.ip_code,
    gr.gr_code,
    m.major_id,
    m.major_name,
    COUNT(cis.course_id)                                               AS support_course_count,
    COALESCE(SUM(cis.total_weight), 0)                                 AS weight_sum,
    CASE WHEN ABS(COALESCE(SUM(cis.total_weight), 0) - 1.0) < 0.001 
         THEN 'OK' ELSE 'FAIL' END                                     AS is_valid
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
GROUP BY ip.ip_id, ip.ip_code, gr.gr_code, m.major_id, m.major_name;
```

---

## 三、表关系总览

```
major ──(FK)── college
  │
  ├──(FK)── teacher
  ├──(FK)── student
  ├──(FK)── graduation_requirement ──(FK)── indicator_point
  │                                             │
  │                     (course_indicator_support.ip_id)
  │                              │
  ├── course_major ── course ────┘
  │       │             │
  │       │    teaching_class ──(FK)── academic_term
  │       │         │
  │       │    student_class ──(FK)── student
  │       │
  └───────┘  (course_major connects major ↔ course)
```

---

## 四、与前后端对应关系

| 后端负责人 | 接口组 | 依赖表 | 前端负责人 | 页面组 |
|-----------|--------|--------|-----------|--------|
| 王子儒 | 接口A：TeachingClass/Student CRUD | teaching_class, student | 王子嘉 | 页面A：课程清单/教学班/名单 |
| 毛小斌 | 接口B：课程导入、名单导入 | course, course_major, student_class, temp_import_staging | 支梦林 | 页面B：宏观支撑矩阵 |
| 叶高平 | 接口C：支撑矩阵接口、权重校验 | course_indicator_support, v_weight_validation | 陈思远 | 页面C：导入结果/校验提示 |

---

## 五、初始化数据规模

| 数据项 | 已有 | 补充 | 补充后总计 |
|--------|------|------|-----------|
| 课程（计算机科学与技术） | 3门 | +10门 | 13门 |
| 教师 | 3人 | +2人 | 5人 |
| 教学班级 | 3个 | +7个 | 10个 |
| 学生 | 10人 | +50人 | 60人 |
| 学生-教学班关联 | 22条 | ≈100条 | ≈122条 |
| 宏观支撑矩阵 | 9条 | +21条 | 30条 |

**支撑矩阵权重校验双场景：**

| 场景 | 指标点 | W总和 | v_weight_validation 结果 |
|------|--------|-------|-------------------------|
| 校验通过 | IP 1.1 / IP 3.1 / IP 5.1 | 1.00 | OK |
| 校验失败 | IP 1.2 / IP 2.2 | 0.85 / 0.50 | FAIL |
| 待完善 | 其余7个指标点 | 0~0.75 | FAIL |

> 校验失败的数据是**故意保留的**，用于前端测试错误提示和修正入口。

---

## 六、交付物

| 序号 | 文件 | 说明 |
|------|------|------|
| 1 | `week3_init_data.sql` | 补充测试数据，在 `GRA_db.sql` 之后执行 |
| 2 | 本文件 | 第三周任务说明 |

---

## 七、执行方式

```bash
mysql> source GRA_db.sql;
mysql> source week3_init_data.sql;
```

执行后可验证：

```sql
-- 查询"计算机科学与技术"专业的权重校验结果
SELECT * FROM v_weight_validation WHERE major_id = 1 ORDER BY ip_id;

-- 预期输出：
-- IP 1.1: support_course_count=3, weight_sum=1.00, is_valid='OK'
-- IP 1.2: support_course_count=3, weight_sum=0.85, is_valid='FAIL'
-- IP 3.1: support_course_count=4, weight_sum=1.00, is_valid='OK'
-- IP 5.1: support_course_count=3, weight_sum=1.00, is_valid='OK'
-- IP 2.2: support_course_count=2, weight_sum=0.50, is_valid='FAIL'
```