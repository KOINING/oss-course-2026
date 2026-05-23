# 模块A：基础与宏观数据管理 — 完整数据字典

> 覆盖 A-1 基础数据、A-2 毕业要求、A-3 课程导入、A-4 支撑矩阵 四大功能模块，共 12 张表。

---

## 表清单

| 序号 | 功能 | 表名 | 中文名 | 操作类型 |
|------|------|------|--------|----------|
| A-1 | 基础数据 | `college` | 学院表 | CRUD |
| A-2 | 基础数据 | `major` | 专业表 | CRUD |
| A-3 | 基础数据 | `academic_term` | 学年学期表 | CRUD |
| A-4 | 毕业要求 | `graduation_requirement` | 毕业要求表 | CRUD |
| A-5 | 毕业要求 | `indicator_point` | 二级指标点表 | CRUD |
| A-6 | 课程导入 | `course` | 课程表 | 批量 INSERT |
| A-7 | 课程导入 | `course_major` | 课程-专业关联表 | 批量 INSERT |
| A-8 | 课程导入 | `teacher` | 教师信息表 | 批量 INSERT/UPDATE |
| A-9 | 课程导入 | `teaching_class` | 教学班级表 | 批量 INSERT |
| A-10 | 课程导入 | `student` | 学生表 | 批量 INSERT/UPDATE |
| A-11 | 课程导入 | `student_class` | 学生班级关联表 | 批量 INSERT |
| A-12 | 支撑矩阵 | `course_indicator_support` | 课程-指标点宏观支撑表 | UPSERT（含校验） |

---

## ER 关系图

```
college ────► major ────► graduation_requirement ────► indicator_point
                │                                              │
                │                    course_indicator_support ◄─┤
                │                           │                  │
                ├── course_major ────► course                  │
                │                        │                     │
                │                        ▼                     │
                │                  teaching_class ◄── teacher   │
                │                        │                     │
                │                        ▼                     │
                └── student ───► student_class                  │
                                                               │
academic_term ────────────────────┘                            │
```

---

## A-1 基础数据表（3张）

### A01：`college`（学院表）

> 存储学校的二级教学单位信息，是专业数据的上级节点。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `college_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 学院唯一标识 |
| `college_code` | `VARCHAR(20)` | 是 | — | UNIQUE | 学院编码，如 `"CS"`、`"EE"` |
| `college_name` | `VARCHAR(100)` | 是 | — | — | 学院名称，如"计算机科学与技术学院" |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：`UNIQUE(college_code)` — 学院编码全局唯一

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS college (
    college_id   BIGINT       PRIMARY KEY AUTO_INCREMENT,
    college_code VARCHAR(20)  NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

---

### A02：`major`（专业表）

> 存储学校开设的本科专业信息，每个专业隶属于一个学院。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `major_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 专业唯一标识 |
| `major_code` | `VARCHAR(20)` | 是 | — | UNIQUE | 专业编码，如 `"080901"` |
| `major_name` | `VARCHAR(100)` | 是 | — | — | 专业名称，如"计算机科学与技术" |
| `college_id` | `BIGINT` | 是 | — | FK → college(college_id) | 所属学院ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(major_code)` — 专业编码全局唯一
- `FK college_id → college(college_id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS major (
    major_id    BIGINT       PRIMARY KEY AUTO_INCREMENT,
    major_code  VARCHAR(20)  NOT NULL UNIQUE,
    major_name  VARCHAR(100) NOT NULL,
    college_id  BIGINT       NOT NULL,
    FOREIGN KEY (college_id) REFERENCES college(college_id) ON DELETE RESTRICT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

---

### A03：`academic_term`（学年学期表）

> 存储学校的学年学期划分，是教学班级的时间维度锚点。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `term_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 学期唯一标识 |
| `term_code` | `VARCHAR(20)` | 是 | — | UNIQUE | 学期编码，如 `"2024-2025-1"` |
| `academic_year` | `INT` | 是 | — | — | 学年（起始年），如 `2024` |
| `semester` | `INT` | 是 | — | CHECK(1,2,3) | 学期序号：1=第一学期，2=第二学期，3=夏季短学期 |
| `start_date` | `DATE` | 是 | — | — | 学期开始日期 |
| `end_date` | `DATE` | 是 | — | — | 学期结束日期 |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(term_code)` — 学期编码全局唯一
- `CHECK (semester IN (1, 2, 3))` — 学期序号仅限 1/2/3

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS academic_term (
    term_id       BIGINT      PRIMARY KEY AUTO_INCREMENT,
    term_code     VARCHAR(20) NOT NULL UNIQUE,
    academic_year INT         NOT NULL,
    semester      INT         NOT NULL CHECK (semester IN (1, 2, 3)),
    start_date    DATE        NOT NULL,
    end_date      DATE        NOT NULL,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

---

## A-2 毕业要求表（2张）

### A04：`graduation_requirement`（毕业要求表）

> 存储各专业对应的工程教育认证毕业要求条目，是整个 OBE 体系的顶层入口。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `gr_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 毕业要求唯一标识 |
| `gr_code` | `VARCHAR(10)` | 是 | — | UNIQUE(与major_id组合) | 毕业要求编码，如 `"1"`=工程知识 |
| `gr_description` | `TEXT` | 是 | — | — | 毕业要求的完整文字描述 |
| `major_id` | `BIGINT` | 是 | — | UNIQUE(与gr_code组合)，FK → major(major_id) | 所属专业ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(major_id, gr_code)` — 同一专业下毕业要求编码不可重复
- `FK major_id → major(major_id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS graduation_requirement (
    gr_id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    gr_code        VARCHAR(10) NOT NULL,
    gr_description TEXT        NOT NULL,
    major_id       BIGINT      NOT NULL,
    UNIQUE KEY uk_major_gr_code(major_id, gr_code),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_req_major ON graduation_requirement(major_id);
```

**业务规则**：
- 一个专业通常对应 6~12 条毕业要求
- `gr_code` 在同一专业内唯一，建议使用纯数字 `1~12`
- 删除毕业要求前必须确保无下级指标点引用

---

### A05：`indicator_point`（二级指标点表）

> 将每条毕业要求拆解为可衡量、可考核的子指标点，是达成度计算的最小分析单元。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `ip_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 指标点唯一标识 |
| `ip_code` | `VARCHAR(10)` | 是 | — | UNIQUE(与gr_id组合) | 指标点编码，如 `"1.1"` |
| `ip_description` | `TEXT` | 是 | — | — | 指标点详细描述，含可观测行为动词 |
| `gr_id` | `BIGINT` | 是 | — | UNIQUE(与ip_code组合)，FK → graduation_requirement(gr_id) | 所属毕业要求ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(gr_id, ip_code)` — 同一毕业要求下指标点编码不可重复
- `FK gr_id → graduation_requirement(gr_id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS indicator_point (
    ip_id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    ip_code        VARCHAR(10) NOT NULL,
    ip_description TEXT        NOT NULL,
    gr_id          BIGINT      NOT NULL,
    UNIQUE KEY uk_gr_ip_code(gr_id, ip_code),
    FOREIGN KEY (gr_id) REFERENCES graduation_requirement(gr_id) ON DELETE RESTRICT,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_ind_req ON indicator_point(gr_id);
```

**业务规则**：
- 每条毕业要求通常拆分为 2~5 个指标点
- 编码规则：`"{gr_code}.{序号}"`，如 `1.1`、`1.2`
- 描述须包含可衡量的行为动词："能运用"、"能设计"、"能分析"

---

## A-3 课程导入表（6张）

### A06：`course`（课程表）

> 存储全校课程基础信息，课程与专业通过 `course_major` 多对多关联。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `course_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 课程唯一标识 |
| `course_code` | `VARCHAR(20)` | 是 | — | UNIQUE | 课程编码，如 `"CS201"` |
| `course_name` | `VARCHAR(100)` | 是 | — | — | 课程名称，如"数据结构" |
| `credit` | `FLOAT` | 是 | — | — | 学分 |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：`UNIQUE(course_code)` — 课程编码全局唯一

**导入策略**：根据 `course_code` 判断，已存在则 UPDATE（学分、名称），不存在则 INSERT。

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS course (
    course_id   BIGINT       PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20)  NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credit      FLOAT        NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

---

### A07：`course_major`（课程-专业关联表）

> 建立课程与专业的 N:N 关系，避免同一门公共课在不同专业下重复创建课程记录。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `cm_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 关联记录唯一标识 |
| `course_id` | `BIGINT` | 是 | — | UNIQUE(与major_id组合)，FK → course(course_id) CASCADE | 课程ID |
| `major_id` | `BIGINT` | 是 | — | UNIQUE(与course_id组合)，FK → major(major_id) RESTRICT | 专业ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 关联关系创建时间 |

**约束**：
- `UNIQUE(course_id, major_id)` — 同一课程不能重复绑定同一专业
- `FK course_id → course(course_id) ON DELETE CASCADE` — 删课时自动清理关联
- `FK major_id → major(major_id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS course_major (
    cm_id      BIGINT   PRIMARY KEY AUTO_INCREMENT,
    course_id  BIGINT   NOT NULL,
    major_id   BIGINT   NOT NULL,
    UNIQUE KEY uk_course_major(course_id, major_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
    FOREIGN KEY (major_id)  REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_cm_course ON course_major(course_id);
CREATE INDEX idx_cm_major  ON course_major(major_id);
```

---

### A08：`teacher`（教师信息表）

> 存储教师档案信息，可通过 `user_id` 关联系统登录账号。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 教师唯一标识 |
| `teacher_no` | `VARCHAR(32)` | 是 | — | UNIQUE | 教师编号/工号，如 `"T2024001"` |
| `teacher_name` | `VARCHAR(64)` | 是 | — | — | 教师姓名 |
| `title` | `VARCHAR(64)` | 否 | NULL | — | 职称，如"教授"、"副教授" |
| `major_id` | `BIGINT` | 否 | NULL | FK → major(major_id) SET NULL | 所属专业ID |
| `user_id` | `BIGINT` | 否 | NULL | UNIQUE，FK → sys_user(id) SET NULL | 关联系统用户ID，用于登录定位 |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(teacher_no)` — 教师编号全局唯一
- `UNIQUE(user_id)` — 一个系统用户只能对应一个教师身份
- `FK major_id → major(major_id) ON DELETE SET NULL`
- `FK user_id → sys_user(id) ON DELETE SET NULL`

**导入策略**：根据 `teacher_no` 判断，已存在则 UPDATE（姓名、职称、专业），不存在则 INSERT。

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS teacher (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    teacher_no   VARCHAR(32)  NOT NULL UNIQUE,
    teacher_name VARCHAR(64)  NOT NULL,
    title        VARCHAR(64)  DEFAULT NULL,
    major_id     BIGINT       DEFAULT NULL,
    user_id      BIGINT       DEFAULT NULL,
    UNIQUE KEY uk_teacher_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id)  REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_teacher_major ON teacher(major_id);
```

---

### A09：`teaching_class`（教学班级表）

> 存储每学期每个课程开设的教学班，绑定主讲教师。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `class_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 教学班级唯一标识 |
| `class_name` | `VARCHAR(50)` | 是 | — | UNIQUE(与course_id,term_id组合) | 班级名称，如"数据结构2024-2025-1班" |
| `course_id` | `BIGINT` | 是 | — | UNIQUE(与term_id,class_name组合)，FK → course(course_id) | 所属课程ID |
| `term_id` | `BIGINT` | 是 | — | UNIQUE(与course_id,class_name组合)，FK → academic_term(term_id) | 所属学期ID |
| `teacher_id` | `BIGINT` | 是 | — | FK → teacher(id) | 主讲教师ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(course_id, term_id, class_name)` — 同一课程同一学期内班级名称唯一
- `FK course_id → course(course_id) ON DELETE RESTRICT`
- `FK term_id → academic_term(term_id) ON DELETE RESTRICT`
- `FK teacher_id → teacher(id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS teaching_class (
    class_id   BIGINT      PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL,
    course_id  BIGINT      NOT NULL,
    term_id    BIGINT      NOT NULL,
    teacher_id BIGINT      NOT NULL,
    UNIQUE KEY uk_course_term_class(course_id, term_id, class_name),
    FOREIGN KEY (course_id)  REFERENCES course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (term_id)    REFERENCES academic_term(term_id) ON DELETE RESTRICT,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_class_course  ON teaching_class(course_id);
CREATE INDEX idx_class_term    ON teaching_class(term_id);
CREATE INDEX idx_class_teacher ON teaching_class(teacher_id);
```

---

### A10：`student`（学生表）

> 存储学生基本信息，可通过 `user_id` 关联系统登录账号。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `student_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 学生唯一标识 |
| `student_no` | `VARCHAR(20)` | 是 | — | UNIQUE | 学号，如 `"20220101001"` |
| `student_name` | `VARCHAR(50)` | 是 | — | — | 学生姓名 |
| `major_id` | `BIGINT` | 是 | — | FK → major(major_id) | 所属专业ID |
| `enrollment_year` | `INT` | 是 | — | — | 入学年份，如 `2022` |
| `user_id` | `BIGINT` | 否 | NULL | UNIQUE，FK → sys_user(id) SET NULL | 关联系统用户ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(student_no)` — 学号全局唯一
- `UNIQUE(user_id)` — 一个系统用户只能对应一个学生身份
- `FK major_id → major(major_id) ON DELETE RESTRICT`
- `FK user_id → sys_user(id) ON DELETE SET NULL`

**导入策略**：根据 `student_no` 判断，已存在则 UPDATE（姓名、专业、入学年份），不存在则 INSERT。

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS student (
    student_id      BIGINT      PRIMARY KEY AUTO_INCREMENT,
    student_no      VARCHAR(20) NOT NULL UNIQUE,
    student_name    VARCHAR(50) NOT NULL,
    major_id        BIGINT      NOT NULL,
    enrollment_year INT         NOT NULL,
    user_id         BIGINT      DEFAULT NULL,
    UNIQUE KEY uk_student_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id)  REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_student_major ON student(major_id);
```

---

### A11：`student_class`（学生班级关联表）

> 建立学生与教学班级的 N:N 选课关系。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `sc_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 关联记录唯一标识 |
| `student_id` | `BIGINT` | 是 | — | UNIQUE(与class_id组合)，FK → student(student_id) | 学生ID |
| `class_id` | `BIGINT` | 是 | — | UNIQUE(与student_id组合)，FK → teaching_class(class_id) | 班级ID |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 关联关系创建时间 |

**约束**：
- `UNIQUE(student_id, class_id)` — 同一学生不能重复加入同一班级
- `FK student_id → student(student_id) ON DELETE RESTRICT`
- `FK class_id → teaching_class(class_id) ON DELETE RESTRICT`

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS student_class (
    sc_id      BIGINT   PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT   NOT NULL,
    class_id   BIGINT   NOT NULL,
    UNIQUE KEY uk_student_class(student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id)   REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

---

## A-4 支撑矩阵表（1张）

### A12：`course_indicator_support`（课程-指标点宏观支撑表）

> 以二维矩阵形式记录课程与指标点的支撑关系及权重，是专业级达成度计算的权重来源。

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `cis_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 支撑关系唯一标识 |
| `course_id` | `BIGINT` | 是 | — | UNIQUE(与ip_id组合)，FK → course(course_id) | 支撑课程ID |
| `ip_id` | `BIGINT` | 是 | — | UNIQUE(与course_id组合)，FK → indicator_point(ip_id) | 被支撑的指标点ID |
| `total_weight` | `FLOAT` | 是 | — | CHECK ≥0 ≤1 | 总支撑权重 W |
| `created_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | CURRENT_TIMESTAMP ON UPDATE | — | 记录最后修改时间 |

**约束**：
- `UNIQUE(course_id, ip_id)` — 同一课程对同一指标点只能有一条支撑记录
- `CHECK(total_weight >= 0 AND total_weight <= 1)` — 权重范围校验

**DDL**：
```sql
CREATE TABLE IF NOT EXISTS course_indicator_support (
    cis_id       BIGINT   PRIMARY KEY AUTO_INCREMENT,
    course_id    BIGINT   NOT NULL,
    ip_id        BIGINT   NOT NULL,
    total_weight FLOAT    NOT NULL,
    UNIQUE KEY uk_course_ip(course_id, ip_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id)     REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (total_weight >= 0 AND total_weight <= 1),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_cis_course    ON course_indicator_support(course_id);
CREATE INDEX idx_cis_indicator ON course_indicator_support(ip_id);
```

**业务规则（关键）**：
1. **列求和 = 1.0**：同一 `ip_id` 下所有课程 `total_weight` 之和必须等于 1.0（应用层校验）
2. 一个指标点可由多门课程共同支撑（体现课程体系协同性）
3. 一门课程可支撑多个指标点（体现课程综合性）
4. 删除支撑记录后需重新校验权重和

**提交前校验 SQL**：
```sql
-- 检查某专业下所有指标点的权重和是否均为 1.0
SELECT ip.ip_id, ip.ip_code, COALESCE(SUM(cis.total_weight), 0) AS weight_sum
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
WHERE gr.major_id = ?
GROUP BY ip.ip_id, ip.ip_code
HAVING ABS(COALESCE(SUM(cis.total_weight), 0) - 1.0) > 0.001;
```

**矩阵界面样例**（专业：计算机科学与技术）：

```
                IP1.1  IP1.2  IP2.1  IP2.2  IP3.1  ...
数据结构 (C1)   0.40    —      —      —     0.30    ...
操作系统 (C2)    —    0.40   0.25     —      —      ...
计算机网络 (C3)   —    0.30    —     0.30     —      ...
══════════════════════════════════════════════════════
列求和           0.40  0.70   0.25   0.30   0.30    ...
校验状态          ❌     ❌     ❌     ❌     ❌     ← 标红禁止提交
```

---

## 模块A Excel 导入顺序

按外键依赖关系，导入顺序必须为：

```
① college ──► major
② academic_term
③ course ──► course_major
④ teacher
⑤ teaching_class (依赖 course, academic_term, teacher)
⑥ student ──► student_class (依赖 student, teaching_class)
```

---

## 模块A 业务索引汇总

```sql
-- 基础数据
CREATE INDEX idx_req_major         ON graduation_requirement(major_id);
CREATE INDEX idx_ind_req           ON indicator_point(gr_id);
-- 课程导入
CREATE INDEX idx_class_course      ON teaching_class(course_id);
CREATE INDEX idx_class_term        ON teaching_class(term_id);
CREATE INDEX idx_class_teacher     ON teaching_class(teacher_id);
CREATE INDEX idx_student_major     ON student(major_id);
CREATE INDEX idx_teacher_major     ON teacher(major_id);
CREATE INDEX idx_cm_course         ON course_major(course_id);
CREATE INDEX idx_cm_major          ON course_major(major_id);
-- 支撑矩阵
CREATE INDEX idx_cis_course        ON course_indicator_support(course_id);
CREATE INDEX idx_cis_indicator     ON course_indicator_support(ip_id);
```