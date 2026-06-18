# 第六周：数据库全链路结构说明

> 基准脚本：`gra_db_full.sql`（1312行）  
> 增量脚本：`upgrade_week6_20260618.sql`（235行）  
> 数据库：`GraduationDB` / MySQL 8.x  
> 日期：2026-06-18

---

## 一、第六周主链路核心表结构

### 1.1 表结构分层总览（29 张表 + 4 视图）

```
┌──────────────────────────────────────────────────────────────────┐
│ 第1层 基础数据 (3)                                                │
│ college → major (college_id FK) → academic_term                  │
├──────────────────────────────────────────────────────────────────┤
│ 第2层 人员实体 (2)                                                │
│ teacher (major_id FK, user_id FK)                                │
│ student (major_id FK, enrollment_year, user_id FK, status)       │
├──────────────────────────────────────────────────────────────────┤
│ 第3层 毕业要求体系 (2)                                            │
│ graduation_requirement (major_id FK, grade_year, gr_code)        │
│   → indicator_point (gr_id FK, ip_code)                         │
├──────────────────────────────────────────────────────────────────┤
│ 第4层 课程与教学 (4)                                              │
│ course / course_major (course_id FK, major_id FK, grade_year)    │
│ teaching_class (course_id FK, term_id FK, teacher_id FK,        │
│                 grade_year, class_code, calc_status)             │
│ student_class (student_id FK, class_id FK)                       │
├──────────────────────────────────────────────────────────────────┤
│ 第5层 支撑关系 (3)                                                │
│ course_objective (course_id FK)                                  │
│ objective_indicator_contribution (co_id FK, ip_id FK, w)         │
│ course_indicator_support (course_id FK, ip_id FK, W)             │
├──────────────────────────────────────────────────────────────────┤
│ 第6层 考核与成绩 (2)                                              │
│ assessment_point (co_id FK, full_score)                          │
│ student_assessment_score (student_id FK, ap_id FK, class_id FK)  │
├──────────────────────────────────────────────────────────────────┤
│ 第7层 中间结果 (1)                                                │
│ student_objective_achievement (student_id FK, class_id FK,       │
│                                co_id FK, achievement) ← L0.5    │
├──────────────────────────────────────────────────────────────────┤
│ 第8层 最终结果 (3)                                                │
│ course_objective_achievement (class_id FK, co_id FK) ← L1        │
│ course_indicator_achievement (class_id FK, ip_id FK) ← L2        │
│ major_indicator_achievement (major_id FK, grade_year,            │
│                              term_id FK, ip_id FK) ← L3          │
├──────────────────────────────────────────────────────────────────┤
│ 第9层 RBAC (5)                                                    │
│ sys_user / sys_role / sys_permission / sys_user_role /           │
│ sys_role_permission                                               │
├──────────────────────────────────────────────────────────────────┤
│ 第10层 系统支撑 (4)                                                │
│ system_config / calc_audit_log / unlock_audit_log /              │
│ temp_import_staging                                               │
│ + 4 视图: v_course_calc_progress / v_major_achievement_dashboard │
│          v_score_drilldown / v_weight_validation                 │
└──────────────────────────────────────────────────────────────────┘
```

---

### 1.2 第一层：基础数据表（3 张）

#### 1.2.1 `college` — 学院表

```sql
CREATE TABLE IF NOT EXISTS college (
    college_id   BIGINT       PRIMARY KEY AUTO_INCREMENT,
    college_code VARCHAR(20)  NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `college_id` | BIGINT | PK AUTO_INCREMENT | 学院主键 |
| `college_code` | VARCHAR(20) | NOT NULL UNIQUE | 学院编码，如 `CS` |
| `college_name` | VARCHAR(100) | NOT NULL | 学院名称 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=启用 0=禁用 |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：** `college_code`（UNIQUE）、`idx_college_name(college_name)`（第六周新增）

#### 1.2.2 `major` — 专业表

```sql
CREATE TABLE IF NOT EXISTS major (
    major_id    BIGINT       PRIMARY KEY AUTO_INCREMENT,
    major_code  VARCHAR(20)  NOT NULL UNIQUE,
    major_name  VARCHAR(100) NOT NULL,
    college_id  BIGINT       NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=招生中 0=停招',
    FOREIGN KEY (college_id) REFERENCES college(college_id) ON DELETE RESTRICT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `major_id` | BIGINT | PK AUTO_INCREMENT | 专业主键 |
| `major_code` | VARCHAR(20) | NOT NULL UNIQUE | 专业编码，如 `080901` |
| `major_name` | VARCHAR(100) | NOT NULL | 专业名称 |
| `college_id` | BIGINT | NOT NULL FK→college RESTRICT | 所属学院 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=招生中 0=停招 |

**索引：** `major_code`（UNIQUE）、`idx_major_name(major_name)`、`idx_major_college_status(college_id, status)`（第六周新增）

#### 1.2.3 `academic_term` — 学年学期表

```sql
CREATE TABLE IF NOT EXISTS academic_term (
    term_id       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    term_code     VARCHAR(20)  NOT NULL UNIQUE,
    academic_year INT          NOT NULL,
    semester      INT          NOT NULL CHECK (semester IN (1, 2, 3)),
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=当前学期 0=历史学期',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `term_id` | BIGINT | PK AUTO_INCREMENT | 学期主键 |
| `term_code` | VARCHAR(20) | NOT NULL UNIQUE | 如 `2025-2026-1` |
| `academic_year` | INT | NOT NULL | 学年起始年 |
| `semester` | INT | NOT NULL CHECK(1,2,3) | 1=第一学期 2=第二学期 3=夏季短学期 |
| `start_date` | DATE | NOT NULL | 学期开始日期 |
| `end_date` | DATE | NOT NULL | 学期结束日期 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=当前学期 0=历史学期 |

---

### 1.3 第二层：人员实体（2 张）

#### 1.3.1 `teacher` — 教师表

```sql
CREATE TABLE IF NOT EXISTS teacher (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    teacher_no   VARCHAR(32)  NOT NULL UNIQUE,
    teacher_name VARCHAR(64)  NOT NULL,
    title        VARCHAR(64)  DEFAULT NULL,
    major_id     BIGINT       DEFAULT NULL,
    user_id      BIGINT       DEFAULT NULL,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=在职 0=离职',
    UNIQUE KEY uk_teacher_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 教师主键 |
| `teacher_no` | VARCHAR(32) | NOT NULL UNIQUE | 教师编号/工号 |
| `teacher_name` | VARCHAR(64) | NOT NULL | 教师姓名 |
| `title` | VARCHAR(64) | NULL | 职称 |
| `major_id` | BIGINT | FK→major SET NULL | 所属专业 |
| `user_id` | BIGINT | UNIQUE FK→sys_user SET NULL | 关联系统用户 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=在职 0=离职 |

**索引：** `idx_teacher_major(major_id)`、`uk_teacher_user(user_id)` UNIQUE

#### 1.3.2 `student` — 学生表

```sql
CREATE TABLE IF NOT EXISTS student (
    student_id      BIGINT       PRIMARY KEY AUTO_INCREMENT,
    student_no      VARCHAR(20)  NOT NULL UNIQUE,
    student_name    VARCHAR(50)  NOT NULL,
    major_id        BIGINT       NOT NULL,
    enrollment_year INT          NOT NULL,
    user_id         BIGINT       DEFAULT NULL,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=在读 2=毕业 3=休学 0=退学',
    UNIQUE KEY uk_student_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `student_id` | BIGINT | PK AUTO_INCREMENT | 学生主键 |
| `student_no` | VARCHAR(20) | NOT NULL UNIQUE | 学号 |
| `student_name` | VARCHAR(50) | NOT NULL | 姓名 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 专业 |
| `enrollment_year` | INT | NOT NULL | **入学年份（年级）** |
| `user_id` | BIGINT | UNIQUE FK→sys_user SET NULL | 关联系统用户 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=在读 2=毕业 3=休学 0=退学 |

**索引：** `idx_student_major(major_id)`、`idx_student_name(student_name)`（第六周新增）、`idx_student_major_year_status(major_id, enrollment_year, status)`（第六周新增）、`uk_student_user(user_id)` UNIQUE

---

### 1.4 第三层：毕业要求体系（2 张）

#### 1.4.1 `graduation_requirement` — 毕业要求表（含年级版本化）

```sql
CREATE TABLE IF NOT EXISTS graduation_requirement (
    gr_id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    gr_code        VARCHAR(10)  NOT NULL,
    gr_description TEXT         NOT NULL,
    major_id       BIGINT       NOT NULL,
    grade_year     INT          NOT NULL DEFAULT 2022 COMMENT '适用年级',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    UNIQUE KEY uk_major_grade_gr_code(major_id, grade_year, gr_code),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `gr_id` | BIGINT | PK AUTO_INCREMENT | 毕业要求主键 |
| `gr_code` | VARCHAR(10) | NOT NULL | 毕业要求编码，如 `EGR01` |
| `gr_description` | TEXT | NOT NULL | 完整描述 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 所属专业 |
| `grade_year` | INT | NOT NULL DEFAULT 2022 | **年级版本化关键字段** |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=启用 0=停用 |

**UNIQUE KEY：** `uk_major_grade_gr_code(major_id, grade_year, gr_code)` — 同一专业同一年级下编码唯一

**索引：** `idx_req_major(major_id)`、`idx_req_major_grade(major_id, grade_year)`

#### 1.4.2 `indicator_point` — 二级指标点表

```sql
CREATE TABLE IF NOT EXISTS indicator_point (
    ip_id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    ip_code        VARCHAR(10)  NOT NULL,
    ip_description TEXT         NOT NULL,
    gr_id          BIGINT       NOT NULL,
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    UNIQUE KEY uk_gr_ip_code(gr_id, ip_code),
    FOREIGN KEY (gr_id) REFERENCES graduation_requirement(gr_id) ON DELETE RESTRICT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `ip_id` | BIGINT | PK AUTO_INCREMENT | 指标点主键 |
| `ip_code` | VARCHAR(10) | NOT NULL | 如 `E1-1` |
| `ip_description` | TEXT | NOT NULL | 详细描述 |
| `gr_id` | BIGINT | NOT NULL FK→graduation_requirement RESTRICT | 所属毕业要求 |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=启用 0=停用 |

**UNIQUE KEY：** `uk_gr_ip_code(gr_id, ip_code)` — 同一毕业要求下指标点编码唯一

**索引：** `idx_ind_req(gr_id)`

---

### 1.5 第四层：课程与教学（4 张）

#### 1.5.1 `course` — 课程表

```sql
CREATE TABLE IF NOT EXISTS course (
    course_id   BIGINT       PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20)  NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credit      FLOAT        NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=开课中 0=停开',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `course_id` | BIGINT | PK AUTO_INCREMENT | 课程主键 |
| `course_code` | VARCHAR(20) | NOT NULL UNIQUE | 课程编码 |
| `course_name` | VARCHAR(100) | NOT NULL | 课程名称 |
| `credit` | FLOAT | NOT NULL | 学分（应用层校验 >0） |
| `status` | TINYINT | NOT NULL DEFAULT 1 | 1=开课中 0=停开 |

**索引：** `course_code`（UNIQUE）、`idx_course_name(course_name)`（第六周新增）

#### 1.5.2 `course_major` — 课程-专业-年级关联表

```sql
CREATE TABLE IF NOT EXISTS course_major (
    cm_id      BIGINT   PRIMARY KEY AUTO_INCREMENT,
    course_id  BIGINT   NOT NULL,
    major_id   BIGINT   NOT NULL,
    grade_year INT      NOT NULL DEFAULT 2022 COMMENT '适用年级',
    UNIQUE KEY uk_course_major_grade(course_id, major_id, grade_year),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `cm_id` | BIGINT | PK AUTO_INCREMENT | 关联主键 |
| `course_id` | BIGINT | NOT NULL FK→course CASCADE | 课程 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 专业 |
| `grade_year` | INT | NOT NULL DEFAULT 2022 | **适用年级（版本化）** |

**UNIQUE KEY：** `uk_course_major_grade(course_id, major_id, grade_year)`

**索引：** `idx_cm_course(course_id)`、`idx_cm_major(major_id)`、`idx_cm_major_grade(major_id, grade_year)`

#### 1.5.3 `teaching_class` — 教学班级表

```sql
CREATE TABLE IF NOT EXISTS teaching_class (
    class_id    BIGINT       PRIMARY KEY AUTO_INCREMENT,
    class_code  VARCHAR(32)  NOT NULL UNIQUE COMMENT '教学班编号，业务唯一标识',
    class_name  VARCHAR(50)  NOT NULL,
    course_id   BIGINT       NOT NULL,
    term_id     BIGINT       NOT NULL,
    teacher_id  BIGINT       NOT NULL,
    grade_year  INT          NOT NULL DEFAULT 2022 COMMENT '适用年级',
    calc_status ENUM('unsubmitted','score_imported','calculating','locked')
                             NOT NULL DEFAULT 'unsubmitted'
                             COMMENT 'unsubmitted=未提交 / score_imported=已导入 / calculating=计算中 / locked=已锁定',
    UNIQUE KEY uk_course_term_class(course_id, term_id, class_name),
    FOREIGN KEY (course_id)  REFERENCES course(course_id)        ON DELETE RESTRICT,
    FOREIGN KEY (term_id)    REFERENCES academic_term(term_id)    ON DELETE RESTRICT,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id)              ON DELETE RESTRICT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `class_id` | BIGINT | PK AUTO_INCREMENT | 班级主键 |
| `class_code` | VARCHAR(32) | NOT NULL UNIQUE | 教学班编号 |
| `class_name` | VARCHAR(50) | NOT NULL | 班级名称 |
| `course_id` | BIGINT | NOT NULL FK→course RESTRICT | 所属课程 |
| `term_id` | BIGINT | NOT NULL FK→academic_term RESTRICT | 所属学期 |
| `teacher_id` | BIGINT | NOT NULL FK→teacher RESTRICT | 主讲教师 |
| `grade_year` | INT | NOT NULL DEFAULT 2022 | **适用年级** |
| `calc_status` | ENUM | NOT NULL DEFAULT 'unsubmitted' | unsubmitted/score_imported/calculating/locked |

**UNIQUE KEY：** `uk_course_term_class(course_id, term_id, class_name)` — 同课程同学期班级名唯一

**索引：** `idx_class_course(course_id)`、`idx_class_term(term_id)`、`idx_class_teacher(teacher_id)`、`idx_class_grade(grade_year)`、`idx_tc_calc_status(calc_status, term_id)`、`idx_tc_course_grade_status(course_id, grade_year, calc_status)`（第六周新增）、`idx_tc_teacher_term_status(teacher_id, term_id, calc_status)`（第六周新增）

#### 1.5.4 `student_class` — 学生选班表

```sql
CREATE TABLE IF NOT EXISTS student_class (
    sc_id      BIGINT   PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT   NOT NULL,
    class_id   BIGINT   NOT NULL,
    UNIQUE KEY uk_student_class(student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)        ON DELETE RESTRICT,
    FOREIGN KEY (class_id)   REFERENCES teaching_class(class_id)   ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `sc_id` | BIGINT | PK AUTO_INCREMENT | 关联主键 |
| `student_id` | BIGINT | NOT NULL FK→student RESTRICT | 学生 |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 教学班 |

**UNIQUE KEY：** `uk_student_class(student_id, class_id)` — 防止重复选班

---

### 1.6 第五层：支撑关系（3 张）

#### 1.6.1 `course_objective` — 课程目标表

```sql
CREATE TABLE IF NOT EXISTS course_objective (
    co_id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    objective_code  VARCHAR(16)  NOT NULL,
    co_description  TEXT         NOT NULL,
    course_id       BIGINT       NOT NULL,
    UNIQUE KEY uk_course_obj_code(course_id, objective_code),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `co_id` | BIGINT | PK AUTO_INCREMENT | 目标主键 |
| `objective_code` | VARCHAR(16) | NOT NULL | 如 `CO1` |
| `co_description` | TEXT | NOT NULL | 目标描述 |
| `course_id` | BIGINT | NOT NULL FK→course RESTRICT | 所属课程 |

**UNIQUE KEY：** `uk_course_obj_code(course_id, objective_code)`

**索引：** `idx_obj_course(course_id)`

#### 1.6.2 `objective_indicator_contribution` — 内部权重 w 表

```sql
CREATE TABLE IF NOT EXISTS objective_indicator_contribution (
    oic_id          BIGINT   PRIMARY KEY AUTO_INCREMENT,
    co_id           BIGINT   NOT NULL,
    ip_id           BIGINT   NOT NULL,
    internal_weight FLOAT    NOT NULL,
    UNIQUE KEY uk_co_ip(co_id, ip_id),
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id)   ON DELETE RESTRICT,
    CHECK (internal_weight >= 0 AND internal_weight <= 1),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `oic_id` | BIGINT | PK AUTO_INCREMENT | 关联主键 |
| `co_id` | BIGINT | NOT NULL FK→course_objective RESTRICT | 课程目标 |
| `ip_id` | BIGINT | NOT NULL FK→indicator_point RESTRICT | 指标点 |
| `internal_weight` | FLOAT | NOT NULL CHECK(≥0 ≤1) | **内部贡献权重 w** |

**约束：** UNIQUE(co_id, ip_id)、CHECK(≥0 ≤1)

**索引：** `idx_oic_objective(co_id)`、`idx_oic_indicator(ip_id)`、`idx_oic_co_ip(co_id, ip_id)`

#### 1.6.3 `course_indicator_support` — 宏观支撑权重 W 表

```sql
CREATE TABLE IF NOT EXISTS course_indicator_support (
    cis_id       BIGINT   PRIMARY KEY AUTO_INCREMENT,
    course_id    BIGINT   NOT NULL,
    ip_id        BIGINT   NOT NULL,
    total_weight FLOAT    NOT NULL,
    UNIQUE KEY uk_course_ip(course_id, ip_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id)         ON DELETE RESTRICT,
    FOREIGN KEY (ip_id)     REFERENCES indicator_point(ip_id)    ON DELETE RESTRICT,
    CHECK (total_weight >= 0 AND total_weight <= 1),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `cis_id` | BIGINT | PK AUTO_INCREMENT | 支撑关系主键 |
| `course_id` | BIGINT | NOT NULL FK→course RESTRICT | 支撑课程 |
| `ip_id` | BIGINT | NOT NULL FK→indicator_point RESTRICT | 被支撑的指标点 |
| `total_weight` | FLOAT | NOT NULL CHECK(≥0 ≤1) | **宏观支撑权重 W** |

**约束：** UNIQUE(course_id, ip_id)、CHECK(≥0 ≤1)。业务规则：同一 IP 下所有课程 W 之和 = 1.0（`v_weight_validation` 视图校验）

**索引：** `idx_cis_course(course_id)`、`idx_cis_indicator(ip_id)`

---

### 1.7 第六层：考核与成绩（2 张）

#### 1.7.1 `assessment_point` — 考核点表

```sql
CREATE TABLE IF NOT EXISTS assessment_point (
    ap_id      BIGINT       PRIMARY KEY AUTO_INCREMENT,
    ap_name    VARCHAR(100) NOT NULL,
    full_score FLOAT        NOT NULL,
    co_id      BIGINT       NOT NULL,
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `ap_id` | BIGINT | PK AUTO_INCREMENT | 考核点主键 |
| `ap_name` | VARCHAR(100) | NOT NULL | 考核点名称 |
| `full_score` | FLOAT | NOT NULL | 满分分值（应用层校验 >0） |
| `co_id` | BIGINT | NOT NULL FK→course_objective RESTRICT | 所属课程目标 |

**索引：** `idx_ap_objective(co_id)`

#### 1.7.2 `student_assessment_score` — 学生考核成绩表

```sql
CREATE TABLE IF NOT EXISTS student_assessment_score (
    sas_id       BIGINT   PRIMARY KEY AUTO_INCREMENT,
    student_id   BIGINT   NOT NULL,
    ap_id        BIGINT   NOT NULL,
    class_id     BIGINT   NOT NULL,
    actual_score FLOAT    NOT NULL,
    UNIQUE KEY uk_student_ap_class(student_id, ap_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)        ON DELETE RESTRICT,
    FOREIGN KEY (ap_id)      REFERENCES assessment_point(ap_id)    ON DELETE RESTRICT,
    FOREIGN KEY (class_id)   REFERENCES teaching_class(class_id)   ON DELETE RESTRICT,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `sas_id` | BIGINT | PK AUTO_INCREMENT | 成绩主键 |
| `student_id` | BIGINT | NOT NULL FK→student RESTRICT | 学生 |
| `ap_id` | BIGINT | NOT NULL FK→assessment_point RESTRICT | 考核点 |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 教学班 |
| `actual_score` | FLOAT | NOT NULL | 实际得分（应用层校验 0 ≤ score ≤ full_score） |

**UNIQUE KEY：** `uk_student_ap_class(student_id, ap_id, class_id)`

**索引：** `idx_sas_class(class_id)`、`idx_sas_student(student_id)`、`idx_sas_ap_student(ap_id, student_id)`、`idx_sas_class_ap(class_id, ap_id)`

---

### 1.8 第七层：中间结果（1 张）

#### 1.8.1 `student_objective_achievement` — 学生课程目标达成度（L0.5）

```sql
CREATE TABLE IF NOT EXISTS student_objective_achievement (
    soa_id      BIGINT   PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT   NOT NULL COMMENT '学生ID',
    class_id    BIGINT   NOT NULL COMMENT '教学班ID',
    co_id       BIGINT   NOT NULL COMMENT '课程目标ID',
    achievement FLOAT    NOT NULL COMMENT '达成度 0~1',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_soa_student_class_co (student_id, class_id, co_id),
    KEY idx_soa_class (class_id),
    KEY idx_soa_co (co_id),
    KEY idx_soa_class_co (class_id, co_id),
    CONSTRAINT fk_soa_student FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_soa_class   FOREIGN KEY (class_id)   REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    CONSTRAINT fk_soa_co      FOREIGN KEY (co_id)      REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    CONSTRAINT chk_soa_achievement CHECK (achievement >= 0 AND achievement <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='学生课程目标达成度中间结果表';
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `soa_id` | BIGINT | PK AUTO_INCREMENT | 主键 |
| `student_id` | BIGINT | NOT NULL FK→student RESTRICT | 学生 |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 教学班 |
| `co_id` | BIGINT | NOT NULL FK→course_objective RESTRICT | 课程目标 |
| `achievement` | FLOAT | NOT NULL CHECK(≥0 ≤1) | 学生对课程目标的达成度 |

**计算口径：** `achievement = SUM(actual_score) / SUM(full_score)` GROUP BY student_id, class_id, co_id

**UNIQUE KEY：** `uk_soa_student_class_co(student_id, class_id, co_id)`

**索引：** `idx_soa_class(class_id)`、`idx_soa_co(co_id)`、`idx_soa_class_co(class_id, co_id)`

---

### 1.9 第八层：最终结果（3 张）

#### 1.9.1 `course_objective_achievement` — 课程目标达成度（L1）

```sql
CREATE TABLE IF NOT EXISTS course_objective_achievement (
    coa_id              BIGINT   PRIMARY KEY AUTO_INCREMENT,
    class_id            BIGINT   NOT NULL,
    co_id               BIGINT   NOT NULL,
    average_achievement FLOAT    NOT NULL,
    UNIQUE KEY uk_class_co(class_id, co_id),
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id)   ON DELETE RESTRICT,
    FOREIGN KEY (co_id)    REFERENCES course_objective(co_id)    ON DELETE RESTRICT,
    CHECK (average_achievement >= 0 AND average_achievement <= 1),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `coa_id` | BIGINT | PK AUTO_INCREMENT | 主键 |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 教学班 |
| `co_id` | BIGINT | NOT NULL FK→course_objective RESTRICT | 课程目标 |
| `average_achievement` | FLOAT | NOT NULL CHECK(≥0 ≤1) | 班级课程目标平均达成度 |

**计算口径：** `average_achievement = AVG(soa.achievement)` GROUP BY class_id, co_id

**UNIQUE KEY：** `uk_class_co(class_id, co_id)`

**索引：** `idx_coa_class_co(class_id, co_id)`

#### 1.9.2 `course_indicator_achievement` — 课程级指标点达成度（L2）

```sql
CREATE TABLE IF NOT EXISTS course_indicator_achievement (
    cia_id      BIGINT   PRIMARY KEY AUTO_INCREMENT,
    class_id    BIGINT   NOT NULL,
    ip_id       BIGINT   NOT NULL,
    achievement FLOAT    NOT NULL,
    is_locked   BOOLEAN  NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_class_ip(class_id, ip_id),
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id)   ON DELETE RESTRICT,
    FOREIGN KEY (ip_id)    REFERENCES indicator_point(ip_id)     ON DELETE RESTRICT,
    CHECK (achievement >= 0 AND achievement <= 1),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `cia_id` | BIGINT | PK AUTO_INCREMENT | 主键 |
| `class_id` | BIGINT | NOT NULL FK→teaching_class RESTRICT | 教学班 |
| `ip_id` | BIGINT | NOT NULL FK→indicator_point RESTRICT | 指标点 |
| `achievement` | FLOAT | NOT NULL CHECK(≥0 ≤1) | 课程级达成度 |
| `is_locked` | BOOLEAN | NOT NULL DEFAULT FALSE | 锁定标志 |

**计算口径：** `achievement = Σ(L1 × w) / Σ(w)` GROUP BY class_id, ip_id（归一化加权，第六周修复）

**UNIQUE KEY：** `uk_class_ip(class_id, ip_id)`

**索引：** `idx_cia_class_ip(class_id, ip_id)`

#### 1.9.3 `major_indicator_achievement` — 专业级指标点达成度（L3）

```sql
CREATE TABLE IF NOT EXISTS major_indicator_achievement (
    mia_id            BIGINT   PRIMARY KEY AUTO_INCREMENT,
    major_id          BIGINT   NOT NULL,
    grade_year        INT      NOT NULL DEFAULT 2022 COMMENT '适用年级',
    term_id           BIGINT   NOT NULL,
    ip_id             BIGINT   NOT NULL,
    final_achievement FLOAT    NOT NULL,
    UNIQUE KEY uk_major_grade_term_ip(major_id, grade_year, term_id, ip_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id)              ON DELETE RESTRICT,
    FOREIGN KEY (term_id)  REFERENCES academic_term(term_id)        ON DELETE RESTRICT,
    FOREIGN KEY (ip_id)    REFERENCES indicator_point(ip_id)        ON DELETE RESTRICT,
    CHECK (final_achievement >= 0 AND final_achievement <= 1),
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `mia_id` | BIGINT | PK AUTO_INCREMENT | 主键 |
| `major_id` | BIGINT | NOT NULL FK→major RESTRICT | 专业 |
| `grade_year` | INT | NOT NULL DEFAULT 2022 | **适用年级** |
| `term_id` | BIGINT | NOT NULL FK→academic_term RESTRICT | 学期 |
| `ip_id` | BIGINT | NOT NULL FK→indicator_point RESTRICT | 指标点 |
| `final_achievement` | FLOAT | NOT NULL CHECK(≥0 ≤1) | 专业级最终达成度 |

**计算口径：** `final_achievement = Σ(L2 × W) / Σ(W)` GROUP BY major_id, grade_year, term_id, ip_id（归一化加权，第六周修复）

**UNIQUE KEY：** `uk_major_grade_term_ip(major_id, grade_year, term_id, ip_id)`

**索引：** `idx_mia_major(major_id)`、`idx_mia_term(term_id)`、`idx_mia_major_grade(major_id, grade_year)`

---

### 1.10 三层计算链路

```
L0.5: student_objective_achievement.achievement
      = SUM(actual_score) / SUM(full_score)
      GROUP BY student_id, class_id, co_id

L1:   course_objective_achievement.average_achievement
      = AVG(soa.achievement)
      GROUP BY class_id, co_id

L2:   course_indicator_achievement.achievement
      = Σ(L1 × w) / Σ(w)
      GROUP BY class_id, ip_id

L3:   major_indicator_achievement.final_achievement
      = Σ(L2 × W) / Σ(W)
      GROUP BY major_id, grade_year, term_id, ip_id
```

---

## 二、关键字段约束检查

### 2.1 专业 + 年级版本化字段

| 表 | 字段 | 约束 | UNIQUE KEY |
|----|------|------|-----------|
| `graduation_requirement` | `grade_year` INT NOT NULL DEFAULT 2022 | — | `uk_major_grade_gr_code(major_id, grade_year, gr_code)` |
| `course_major` | `grade_year` INT NOT NULL DEFAULT 2022 | — | `uk_course_major_grade(course_id, major_id, grade_year)` |
| `teaching_class` | `grade_year` INT NOT NULL DEFAULT 2022 | — | — |
| `major_indicator_achievement` | `grade_year` INT NOT NULL DEFAULT 2022 | — | `uk_major_grade_term_ip(major_id, grade_year, term_id, ip_id)` |

**年级一致性规则：** `teaching_class.grade_year = student.enrollment_year = graduation_requirement.grade_year`

### 2.2 教学班关联字段

| 关联路径 | 方式 | 约束 |
|---------|------|------|
| `teaching_class.course_id → course.course_id` | 直接 FK | ON DELETE RESTRICT |
| `teaching_class.term_id → academic_term.term_id` | 直接 FK | ON DELETE RESTRICT |
| `teaching_class.teacher_id → teacher.id` | 直接 FK | ON DELETE RESTRICT |
| `teaching_class → major` | 间接（course → course_major） | 无 FK，应用层校验 `course_major` 中存在 `(course_id, grade_year)` |

### 2.3 外键链路完整性

```
course_objective.course_id ──FK→ course                                    ✅
assessment_point.co_id ──FK→ course_objective                              ✅
student_assessment_score.student_id ──FK→ student                          ✅
student_assessment_score.ap_id ──FK→ assessment_point                      ✅
student_assessment_score.class_id ──FK→ teaching_class                     ✅
student_objective_achievement.student_id ──FK→ student                     ✅
student_objective_achievement.class_id ──FK→ teaching_class                ✅
student_objective_achievement.co_id ──FK→ course_objective                 ✅
course_objective_achievement.class_id ──FK→ teaching_class                 ✅
course_objective_achievement.co_id ──FK→ course_objective                  ✅
course_indicator_achievement.class_id ──FK→ teaching_class                 ✅
course_indicator_achievement.ip_id ──FK→ indicator_point                   ✅
major_indicator_achievement.major_id ──FK→ major                           ✅
major_indicator_achievement.term_id ──FK→ academic_term                    ✅
major_indicator_achievement.ip_id ──FK→ indicator_point                    ✅
```

**全部 FK 使用 ON DELETE RESTRICT**，防止误删关联数据。

### 2.4 权重字段约束

| 表.字段 | CHECK | 业务规则 |
|---------|-------|---------|
| `course_indicator_support.total_weight` | ≥0 ≤1 | 同一 IP 下所有课程 W 之和 = 1.0（视图校验） |
| `objective_indicator_contribution.internal_weight` | ≥0 ≤1 | 同一课程同一 IP 下所有目标 w 之和 = 1.0（应用层校验） |

### 2.5 成绩/达成度字段约束

| 表.字段 | CHECK | 应用层补充校验 |
|---------|-------|-------------|
| `assessment_point.full_score` | — | `> 0` |
| `student_assessment_score.actual_score` | — | `0 ≤ actual_score ≤ full_score` |
| `student_objective_achievement.achievement` | ≥0 ≤1 | — |
| `course_objective_achievement.average_achievement` | ≥0 ≤1 | — |
| `course_indicator_achievement.achievement` | ≥0 ≤1 | — |
| `major_indicator_achievement.final_achievement` | ≥0 ≤1 | — |

### 2.6 状态字段汇总

| 表 | 字段 | 类型 | 合法值 | 默认值 |
|----|------|------|--------|--------|
| `sys_user` | `status` | TINYINT | 1=启用 0=禁用 | 1 |
| `college` | `status` | TINYINT | 1=启用 0=禁用 | 1 |
| `major` | `status` | TINYINT | 1=招生中 0=停招 | 1 |
| `academic_term` | `status` | TINYINT | 1=当前学期 0=历史学期 | 1 |
| `teacher` | `status` | TINYINT | 1=在职 0=离职 | 1 |
| `student` | `status` | TINYINT | 1=在读 2=毕业 3=休学 0=退学 | 1 |
| `graduation_requirement` | `status` | TINYINT | 1=启用 0=停用 | 1 |
| `indicator_point` | `status` | TINYINT | 1=启用 0=停用 | 1 |
| `course` | `status` | TINYINT | 1=开课中 0=停开 | 1 |
| `teaching_class` | `calc_status` | ENUM | unsubmitted/score_imported/calculating/locked | unsubmitted |

---

## 三、第六周分页查询索引

### 3.1 索引对照表

| 查询入口 | 查询维度 | 索引 | 来源 |
|---------|---------|------|------|
| **学院** | `college_code` 精确 | `college_code` UNIQUE | 已有 |
| | `college_name` 模糊 | `idx_college_name(college_name)` | **第六周新增** |
| **专业** | `major_code` 精确 | `major_code` UNIQUE | 已有 |
| | `major_name` 模糊 | `idx_major_name(major_name)` | **第六周新增** |
| | `college_id + status` 筛选 | `idx_major_college_status(college_id, status)` | **第六周新增** |
| **用户** | `username` 精确 | `username` UNIQUE | 已有 |
| | `real_name` 模糊 | `idx_user_real_name(real_name)` | **第六周新增** |
| **课程** | `course_code` 精确 | `course_code` UNIQUE | 已有 |
| | `course_name` 模糊 | `idx_course_name(course_name)` | **第六周新增** |
| **教学班** | `class_code` 精确 | `class_code` UNIQUE | 已有 |
| | `course_id + grade_year + calc_status` | `idx_tc_course_grade_status` | **第六周新增** |
| | `teacher_id + term_id + calc_status` | `idx_tc_teacher_term_status` | **第六周新增** |
| | `course_id` / `term_id` / `teacher_id` | 各自单列索引 | 已有 |
| | `grade_year` | `idx_class_grade(grade_year)` | 已有 |
| | `calc_status + term_id` | `idx_tc_calc_status(calc_status, term_id)` | 已有 |
| **学生** | `student_no` 精确 | `student_no` UNIQUE | 已有 |
| | `student_name` 模糊 | `idx_student_name(student_name)` | **第六周新增** |
| | `major_id + enrollment_year + status` | `idx_student_major_year_status` | **第六周新增** |
| | `major_id` | `idx_student_major(major_id)` | 已有 |

### 3.2 已删除冗余索引

| 冗余索引 | 被覆盖方式 |
|---------|-----------|
| `idx_soa_student(student_id)` | `uk_soa_student_class_co(student_id, class_id, co_id)` 左前缀 |
| `idx_coa_class(class_id)` | `idx_coa_class_co(class_id, co_id)` 左前缀 |
| `idx_cia_class(class_id)` | `idx_cia_class_ip(class_id, ip_id)` 左前缀 |

---

## 四、字段链路数据问题排查

### 4.1 毕业要求和指标点到年级

**链路：** `major → graduation_requirement(major_id, grade_year) → indicator_point(gr_id)`

| 排查项 | 状态 | 备注 |
|--------|------|------|
| `grade_year` 是否为 NULL/0 | ✅ | NOT NULL 约束 + DEFAULT 2022 |
| IP 的 `gr_id` 是否有孤儿引用 | ✅ | FK RESTRICT 阻止 |
| 同一专业不同年级是否有独立 GR 版本 | ✅ | UNIQUE KEY `uk_major_grade_gr_code(major_id, grade_year, gr_code)` 保证 |

### 4.2 课程和专业到年级

**链路：** `course → course_major(course_id, major_id, grade_year)`

| 排查项 | 状态 | 备注 |
|--------|------|------|
| `grade_year` 是否为 NULL/0 | ✅ | NOT NULL 约束 |
| 课程是否缺专业关联 | ✅ | E2E 数据完整覆盖 |

### 4.3 教学班到课程、专业、年级、学期

**链路：** `teaching_class(course_id, term_id, grade_year) → course → course_major(major_id)`

| 排查项 | 状态 | 备注 |
|--------|------|------|
| FK 完整性 | ✅ | course_id/term_id/teacher_id 均有 FK RESTRICT |
| `grade_year` 与 `course_major` 一致性 | ✅ | 后端 `validateGradeYear()` 第六周新增（Bug 1 修复） |
| `grade_year` 创建时被设置 | ✅ | `TeachingClassSaveRequest` 第六周新增 `gradeYear` 字段（Bug 1 修复） |

### 4.4 成绩到教学班、学生、考核点

**链路：** `student_assessment_score(student_id, ap_id, class_id)`

| 排查项 | 状态 | 备注 |
|--------|------|------|
| 学生是否属于对应教学班 | ✅ | `saveScores()` 校验 `student_class` |
| 考核点课程与教学班课程一致 | ✅ | `saveScores()` 第六周新增跨课程校验（Bug 3 修复） |
| `actual_score` 在 [0, full_score] 范围内 | ✅ | `saveScores()` 校验 |
| 成绩唯一性 | ✅ | UNIQUE KEY `uk_student_ap_class` |

### 4.5 结果表到课程、指标点、专业、年级

**链路：** COA → CIA → MIA

| 排查项 | 状态 | 备注 |
|--------|------|------|
| L1 可还原（COA = AVG(SOA)） | ✅ | 全链路自检验证通过 |
| L2 可还原（CIA = Σ(L1×w)/Σ(w)） | ✅ | 第六周修复归一化公式（Bug 2 修复） |
| L3 可还原（MIA = Σ(L2×W)/Σ(W)） | ✅ | 第六周修复归一化公式（Bug 2 修复） |
| CS2022_MAIN 的 MIA | ✅ | 第六周补齐（Bug 2 修复） |
| `unlock_audit_log.approved_by` | ✅ | 第六周 0→1 修复 |
| 审计日志写入 | ✅ | 第六周新增 `CalcAuditLog` 实体 + `ScoreCalcServiceImpl` 写入 |

---

## 五、脚本执行顺序

### 5.1 全新部署

```
步骤 1: gra_db_full.sql（1312行）
           ↓  DROP DATABASE → CREATE → DDL 29表 + 40索引 + 4视图
           ↓  RBAC 数据（10用户/5角色/15权限/5教师/6配置）
           ↓  E2E 全量清空 → 基础数据重建 → 模块A/B/C → 计算结果
           ↓
步骤 2: week5_sample_data.sql
           ↓  第五周多教学班演示数据
           ↓
步骤 3: upgrade_week6_20260618.sql（235行）
           ↓  重算 CIA/MIA（归一化）+ 补齐 CS MIA
           ↓  +9 索引 / -3 冗余索引 / 修复脏数据 / schema_version
           ↓
步骤 4: [第六周新功能脚本]
```

### 5.2 演示数据导入顺序（按模块依赖）

```
第1批：基础组织     college → major → academic_term
第2批：RBAC 账号    sys_user → sys_role → sys_permission
                    → sys_user_role → sys_role_permission
                    → teacher
第3批：毕业要求     graduation_requirement → indicator_point
第4批：课程体系     course → course_major → course_objective
                    → course_indicator_support
第5批：教学安排     teaching_class → student → student_class
第6批：考核权重     assessment_point → objective_indicator_contribution
第7批：原始成绩     student_assessment_score
第8批：计算结果     student_objective_achievement
                    → course_objective_achievement
                    → course_indicator_achievement
                    → major_indicator_achievement
第9批：系统支撑     system_config → calc_audit_log → unlock_audit_log
```

### 5.3 文件清单

| 文件 | 行数 | 说明 |
|------|------|------|
| `gra_db_full.sql` | 1312 | 全量建库脚本 |
| `upgrade_week6_20260618.sql` | 235 | 第六周增量脚本 |
| `week5_sample_data.sql` | ~330 | 第五周演示数据 |
| `week5_self_check.sql` | ~530 | 第五周自检 |
| `week6_comprehensive.md` | — | 本文档 |
| `week6_project_analysis.md` | — | 项目全链路可运行性分析 |
