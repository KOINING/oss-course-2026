# 模块A：毕业要求/指标点 — 数据字典

> 本模块包含毕业要求体系的核心表，覆盖从顶层培养目标到课程支撑关系的完整层级结构。

---

## 表清单

| 序号 | 表名 | 说明 |
|------|------|------|
| A-1 | `graduation_requirement` | 毕业要求主数据表 |
| A-2 | `indicator_point` | 二级指标点表 |
| A-3 | `course_indicator_support` | 课程-指标点宏观支撑表 |

---

## ER 关系图

```
major (专业)
  │
  │ 1 : N
  ▼
graduation_requirement (毕业要求)        course (课程)
  │                                       │
  │ 1 : N                                 │
  ▼                                       │
indicator_point (二级指标点) ◄─────────────┘
                              N : M
                    course_indicator_support
                      (宏观支撑矩阵)
```

---

## A-1：`graduation_requirement`（毕业要求表）

> 存储各专业对应的工程教育认证毕业要求条目，是整个 OBE 体系的顶层入口。

### 字段定义

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `gr_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 毕业要求唯一标识，系统内部ID |
| `gr_code` | `VARCHAR(10)` | 是 | — | 联合唯一（与 `major_id`） | 毕业要求编码，如 `"1"`=工程知识、`"2"`=问题分析 |
| `gr_description` | `TEXT` | 是 | — | — | 毕业要求的完整文字描述 |
| `major_id` | `BIGINT` | 是 | — | 联合唯一（与 `gr_code`），外键 | 所属专业ID，关联 `major.major_id` |
| `created_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP` | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP ON UPDATE` | — | 记录最后修改时间，修改时自动更新 |

### 约束清单

| 约束类型 | 约束内容 | 业务规则 |
|----------|----------|----------|
| 主键 | `PRIMARY KEY (gr_id)` | 每条毕业要求有唯一ID |
| 联合唯一 | `UNIQUE (major_id, gr_code)` | 同一专业下毕业要求编码不可重复 |
| 外键-RESTRICT | `major_id → major(major_id) ON DELETE RESTRICT` | 有毕业要求引用的专业不允许删除 |

### 业务规则

1. 一个专业通常对应 6~12 条毕业要求（如工程教育认证通用标准）
2. `gr_code` 在同一专业内唯一，不同专业可重复使用相同编码
3. 删除毕业要求前必须确保无下级指标点引用

---

## A-2：`indicator_point`（二级指标点表）

> 将每条毕业要求拆解为可衡量、可考核的子指标点，是达成度计算的最小分析单元。

### 字段定义

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `ip_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 指标点唯一标识，系统内部ID |
| `ip_code` | `VARCHAR(10)` | 是 | — | 联合唯一（与 `gr_id`） | 指标点编码，如 `"1.1"`="毕业要求1的第1个子指标" |
| `ip_description` | `TEXT` | 是 | — | — | 指标点详细描述，含可观测的行为动词 |
| `gr_id` | `BIGINT` | 是 | — | 联合唯一（与 `ip_code`），外键 | 所属毕业要求ID，关联 `graduation_requirement.gr_id` |
| `created_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP` | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP ON UPDATE` | — | 记录最后修改时间 |

### 约束清单

| 约束类型 | 约束内容 | 业务规则 |
|----------|----------|----------|
| 主键 | `PRIMARY KEY (ip_id)` | 每条指标点有唯一ID |
| 联合唯一 | `UNIQUE (gr_id, ip_code)` | 同一毕业要求下指标点编码不可重复 |
| 外键-RESTRICT | `gr_id → graduation_requirement(gr_id) ON DELETE RESTRICT` | 有指标点引用的毕业要求不允许删除 |

### 业务规则

1. 每条毕业要求通常拆分为 2~5 个指标点
2. `ip_code` 命名规则："{gr_code}.{序号}"，如 `1.1`、`1.2`
3. `ip_description` 须包含可衡量的行为动词，如"能运用"、"能设计"、"能分析"
4. 指标点是所有后续计算（内部权重、考核点、达成度）的锚定节点

---

## A-3：`course_indicator_support`（课程-指标点宏观支撑表）

> 建立课程与指标点之间的宏观支撑关系，定义某门课程对某个指标点的支撑权重，是达成度加权计算的权重来源。

### 字段定义

| 字段名 | 数据类型 | 必填 | 默认值 | 键/约束 | 业务含义 |
|--------|----------|------|--------|---------|----------|
| `cis_id` | `BIGINT` | 是 | AUTO_INCREMENT | **主键** | 支撑关系唯一标识 |
| `course_id` | `BIGINT` | 是 | — | 联合唯一（与 `ip_id`），外键 | 支撑课程ID，关联 `course.course_id` |
| `ip_id` | `BIGINT` | 是 | — | 联合唯一（与 `course_id`），外键 | 被支撑的指标点ID，关联 `indicator_point.ip_id` |
| `total_weight` | `FLOAT` | 是 | — | CHECK ≥0 ≤1 | 总支撑权重 W，同一指标点所有课程权重之和为 1.0 |
| `created_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP` | — | 记录创建时间 |
| `updated_at` | `DATETIME` | 是 | `CURRENT_TIMESTAMP ON UPDATE` | — | 记录最后修改时间 |

### 约束清单

| 约束类型 | 约束内容 | 业务规则 |
|----------|----------|----------|
| 主键 | `PRIMARY KEY (cis_id)` | 每条支撑关系有唯一ID |
| 联合唯一 | `UNIQUE (course_id, ip_id)` | 同一课程对同一指标点只能有一条支撑记录 |
| 取值范围 | `CHECK (total_weight >= 0 AND total_weight <= 1)` | 权重必须在0~1之间 |
| 外键-RESTRICT | `course_id → course(course_id) ON DELETE RESTRICT` | 有关联的课程不允许删除 |
| 外键-RESTRICT | `ip_id → indicator_point(ip_id) ON DELETE RESTRICT` | 有关联的指标点不允许删除 |

### 业务规则

1. **权重求和约束**：同一 `ip_id` 下所有课程的 `total_weight` 之和应等于 1.0（需应用层校验）
2. 一个指标点可以由多门课程共同支撑（体现课程体系的协同性）
3. 一门课程可以支撑多个指标点（体现课程的综合性）
4. 删除支撑关系后需重新校验该指标点下权重之和

---

## 数据流转路径

```
┌──────────────────────────────────────────────────────────────────┐
│                       模块A：毕业要求体系                          │
│                                                                  │
│  major ──► graduation_requirement ──► indicator_point             │
│  专业         毕业要求                   二级指标点                  │
│                                            │                     │
│   │                                        │ 1 : N               │
│   │ 课程-指标点关联                          ▼                     │
│   │ 支撑权重                            ┌─────────────┐           │
│   │                                    │ 达成度计算    │           │
│   └── course ─────────────────────────►│  (模块C)     │           │
│             course_indicator_support   └─────────────┘           │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## 模块A 建表 DDL（参考）

```sql
-- 毕业要求表
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

-- 二级指标点表
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

-- 课程-指标点宏观支撑表
CREATE TABLE IF NOT EXISTS course_indicator_support (
    cis_id       BIGINT   PRIMARY KEY AUTO_INCREMENT,
    course_id    BIGINT   NOT NULL,
    ip_id        BIGINT   NOT NULL,
    total_weight FLOAT    NOT NULL,
    UNIQUE KEY uk_course_ip(course_id, ip_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (total_weight >= 0 AND total_weight <= 1),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 业务索引
CREATE INDEX idx_req_major ON graduation_requirement(major_id);
CREATE INDEX idx_ind_req   ON indicator_point(gr_id);
CREATE INDEX idx_cis_course    ON course_indicator_support(course_id);
CREATE INDEX idx_cis_indicator ON course_indicator_support(ip_id);
```
