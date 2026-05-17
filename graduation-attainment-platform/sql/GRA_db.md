# 毕业要求达成度计算平台 — 数据库 22 张核心表字段说明

下面整理的是 `os_sql` 导出的 22 张表，`备注`列中写明字段含义，适合直接放入数据库设计文档。

***

## 第1组：标准 RBAC 权限体系（5张）

### 1. `sys_user` 用户表

| 字段名          | 类型             | 约束                                                    | 备注                 |
| ------------ | -------------- | ----------------------------------------------------- | ------------------ |
| `id`         | `BIGINT`       | 主键，自增                                                 | 用户主键，唯一标识用户        |
| `username`   | `VARCHAR(50)`  | 非空，唯一                                                 | 登录账号               |
| `password`   | `VARCHAR(255)` | 非空                                                    | 密码密文，禁止存储明文        |
| `real_name`  | `VARCHAR(50)`  | 非空                                                    | 用户真实姓名             |
| `status`     | `TINYINT`      | 非空，默认 `1`                                             | 用户状态，`1` 启用，`0` 禁用 |
| `created_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间             |
| `updated_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间           |

补充约束：

- `UNIQUE (username)`：登录账号全局唯一

### 2. `sys_role` 角色表

| 字段名          | 类型             | 约束                                                    | 备注                 |
| ------------ | -------------- | ----------------------------------------------------- | ------------------ |
| `id`         | `BIGINT`       | 主键，自增                                                 | 角色主键，唯一标识角色        |
| `role_code`  | `VARCHAR(50)`  | 非空，唯一                                                 | 角色编码，程序中用于识别角色     |
| `role_name`  | `VARCHAR(100)` | 非空                                                    | 角色名称，如"系统管理员"      |
| `status`     | `TINYINT`      | 非空，默认 `1`                                             | 角色状态，`1` 启用，`0` 停用 |
| `remark`     | `VARCHAR(255)` | 可空                                                    | 角色说明、备注信息          |
| `created_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间             |
| `updated_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间           |

补充约束：

- `UNIQUE (role_code)`：角色编码全局唯一

### 3. `sys_permission` 权限表

| 字段名           | 类型             | 约束                                                    | 备注                      |
| ------------- | -------------- | ----------------------------------------------------- | ----------------------- |
| `id`          | `BIGINT`       | 主键，自增                                                 | 权限主键，唯一标识权限             |
| `perm_code`   | `VARCHAR(100)` | 非空，唯一                                                 | 权限编码，程序中用于校验权限          |
| `perm_name`   | `VARCHAR(100)` | 非空                                                    | 权限名称，如"成绩导入"            |
| `module_name` | `VARCHAR(50)`  | 可空                                                    | 所属模块，如 `system`、`score` |
| `remark`      | `VARCHAR(255)` | 可空                                                    | 权限说明、备注信息               |
| `created_at`  | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                  |
| `updated_at`  | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                |

补充约束：

- `UNIQUE (perm_code)`：权限编码全局唯一

### 4. `sys_user_role` 用户角色关联表

| 字段名          | 类型         | 约束                        | 备注                        |
| ------------ | ---------- | ------------------------- | ------------------------- |
| `id`         | `BIGINT`   | 主键，自增                     | 用户角色关联主键                  |
| `user_id`    | `BIGINT`   | 非空，外键                     | 关联的用户 ID，对应 `sys_user.id` |
| `role_id`    | `BIGINT`   | 非空，外键                     | 关联的角色 ID，对应 `sys_role.id` |
| `created_at` | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP` | 关联关系创建时间                  |

补充约束：

- `UNIQUE (user_id, role_id)`：同一个用户不能重复分配同一个角色
- 外键 `user_id → sys_user(id) ON DELETE RESTRICT`
- 外键 `role_id → sys_role(id) ON DELETE RESTRICT`

### 5. `sys_role_permission` 角色权限关联表

| 字段名             | 类型         | 约束                        | 备注                              |
| --------------- | ---------- | ------------------------- | ------------------------------- |
| `id`            | `BIGINT`   | 主键，自增                     | 角色权限关联主键                        |
| `role_id`       | `BIGINT`   | 非空，外键                     | 关联的角色 ID，对应 `sys_role.id`       |
| `permission_id` | `BIGINT`   | 非空，外键                     | 关联的权限 ID，对应 `sys_permission.id` |
| `created_at`    | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP` | 关联关系创建时间                        |

补充约束：

- `UNIQUE (role_id, permission_id)`：同一个角色不能重复绑定同一个权限
- 外键 `role_id → sys_role(id) ON DELETE RESTRICT`
- 外键 `permission_id → sys_permission(id) ON DELETE RESTRICT`

***

## 第2组：基础组织与时间实体（3张）

### 6. `College` 学院表

| 字段名            | 类型             | 约束                                                    | 备注                 |
| -------------- | -------------- | ----------------------------------------------------- | ------------------ |
| `college_id`   | `BIGINT`       | 主键，自增                                                 | 学院主键，唯一标识学院        |
| `college_code` | `VARCHAR(20)`  | 非空，唯一                                                 | 学院编码，如 `001`       |
| `college_name` | `VARCHAR(100)` | 非空                                                    | 学院名称，如"计算机科学与技术学院" |
| `created_at`   | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间             |
| `updated_at`   | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间           |

补充约束：

- `UNIQUE (college_code)`：学院编码全局唯一

### 7. `Major` 专业表

| 字段名          | 类型             | 约束                                                    | 备注                              |
| ------------ | -------------- | ----------------------------------------------------- | ------------------------------- |
| `major_id`   | `BIGINT`       | 主键，自增                                                 | 专业主键，唯一标识专业                     |
| `major_code` | `VARCHAR(20)`  | 非空，唯一                                                 | 专业编码，如 `080901`                 |
| `major_name` | `VARCHAR(100)` | 非空                                                    | 专业名称，如"计算机科学与技术"                |
| `college_id` | `BIGINT`       | 非空，外键                                                 | 所属学院 ID，对应 `College.college_id` |
| `created_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                          |
| `updated_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                        |

补充约束：

- `UNIQUE (major_code)`：专业编码全局唯一
- 外键 `college_id → College(college_id) ON DELETE RESTRICT`

### 8. `AcademicTerm` 学年学期表

| 字段名             | 类型            | 约束                                                    | 备注                     |
| --------------- | ------------- | ----------------------------------------------------- | ---------------------- |
| `term_id`       | `BIGINT`      | 主键，自增                                                 | 学年学期主键，唯一标识一个学期        |
| `term_code`     | `VARCHAR(20)` | 非空，唯一                                                 | 学期编码，如 `2024-2025-1`   |
| `academic_year` | `INT`         | 非空                                                    | 学年，如 `2024`            |
| `semester`      | `INT`         | 非空                                                    | 学期序号，`1` 第一学期，`2` 第二学期 |
| `start_date`    | `DATE`        | 非空                                                    | 学期开始日期                 |
| `end_date`      | `DATE`        | 非空                                                    | 学期结束日期                 |
| `created_at`    | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                 |
| `updated_at`    | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间               |

补充约束：

- `UNIQUE (term_code)`：学期编码全局唯一

***

## 第3组：毕业要求体系实体（2张）

### 9. `GraduationRequirement` 毕业要求表

| 字段名              | 类型            | 约束                                                    | 备注                          |
| ---------------- | ------------- | ----------------------------------------------------- | --------------------------- |
| `gr_id`          | `BIGINT`      | 主键，自增                                                 | 毕业要求主键，唯一标识一条毕业要求           |
| `gr_code`        | `VARCHAR(10)` | 非空                                                    | 毕业要求编码，如 `1`                |
| `gr_description` | `TEXT`        | 非空                                                    | 毕业要求描述，如"工程知识"              |
| `major_id`       | `BIGINT`      | 非空，外键                                                 | 所属专业 ID，对应 `Major.major_id` |
| `created_at`     | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                      |
| `updated_at`     | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                    |

补充约束：

- `UNIQUE (major_id, gr_code)`：同一专业下毕业要求编码不可重复
- 外键 `major_id → Major(major_id) ON DELETE RESTRICT`

### 10. `IndicatorPoint` 二级指标点表

| 字段名              | 类型            | 约束                                                    | 备注                                         |
| ---------------- | ------------- | ----------------------------------------------------- | ------------------------------------------ |
| `ip_id`          | `BIGINT`      | 主键，自增                                                 | 指标点主键，唯一标识一条指标点                            |
| `ip_code`        | `VARCHAR(10)` | 非空                                                    | 指标点编码，如 `1.1`                              |
| `ip_description` | `TEXT`        | 非空                                                    | 指标点描述，如"能够运用数学、自然科学和工程科学的基本原理"             |
| `gr_id`          | `BIGINT`      | 非空，外键                                                 | 所属毕业要求 ID，对应 `GraduationRequirement.gr_id` |
| `created_at`     | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                                     |
| `updated_at`     | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                                   |

补充约束：

- `UNIQUE (gr_id, ip_code)`：同一毕业要求下指标点编码不可重复
- 外键 `gr_id → GraduationRequirement(gr_id) ON DELETE RESTRICT`

***

## 第4组：课程与教学实体（4张）

### 11. `Course` 课程表

| 字段名           | 类型             | 约束                                                    | 备注                          |
| ------------- | -------------- | ----------------------------------------------------- | --------------------------- |
| `course_id`   | `BIGINT`       | 主键，自增                                                 | 课程主键，唯一标识课程                 |
| `course_code` | `VARCHAR(20)`  | 非空，唯一                                                 | 课程编码，如 `CS001`              |
| `course_name` | `VARCHAR(100)` | 非空                                                    | 课程名称，如"数据结构"                |
| `credit`      | `FLOAT`        | 非空                                                    | 学分                          |
| `major_id`    | `BIGINT`       | 非空，外键                                                 | 所属专业 ID，对应 `Major.major_id` |
| `created_at`  | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                      |
| `updated_at`  | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                    |

补充约束：

- `UNIQUE (course_code)`：课程编码全局唯一
- 外键 `major_id → Major(major_id) ON DELETE RESTRICT`

### 12. `TeachingClass` 教学班级表

| 字段名          | 类型            | 约束                                                    | 备注                                |
| ------------ | ------------- | ----------------------------------------------------- | --------------------------------- |
| `class_id`   | `BIGINT`      | 主键，自增                                                 | 教学班级主键，唯一标识一个班级                   |
| `class_name` | `VARCHAR(50)` | 非空                                                    | 班级名称，如"数据结构 2024-2025-1 班"        |
| `course_id`  | `BIGINT`      | 非空，外键                                                 | 所属课程 ID，对应 `Course.course_id`     |
| `term_id`    | `BIGINT`      | 非空，外键                                                 | 所属学期 ID，对应 `AcademicTerm.term_id` |
| `teacher_id` | `BIGINT`      | 非空，外键                                                 | 主讲教师 ID，对应 `sys_user.id`          |
| `created_at` | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                            |
| `updated_at` | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                          |

补充约束：

- `UNIQUE (course_id, term_id, class_name)`：同一课程同一学期内班级名称唯一
- 外键 `course_id → Course(course_id) ON DELETE RESTRICT`
- 外键 `term_id → AcademicTerm(term_id) ON DELETE RESTRICT`
- 外键 `teacher_id → sys_user(id) ON DELETE RESTRICT`

### 13. `Student` 学生表

| 字段名               | 类型            | 约束                                                    | 备注                          |
| ----------------- | ------------- | ----------------------------------------------------- | --------------------------- |
| `student_id`      | `BIGINT`      | 主键，自增                                                 | 学生主键，唯一标识学生                 |
| `student_number`  | `VARCHAR(20)` | 非空，唯一                                                 | 学号                          |
| `student_name`    | `VARCHAR(50)` | 非空                                                    | 学生姓名                        |
| `major_id`        | `BIGINT`      | 非空，外键                                                 | 所属专业 ID，对应 `Major.major_id` |
| `enrollment_year` | `INT`         | 非空                                                    | 入学年份，如 `2024`               |
| `created_at`      | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                      |
| `updated_at`      | `DATETIME`    | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                    |

补充约束：

- `UNIQUE (student_number)`：学号全局唯一
- 外键 `major_id → Major(major_id) ON DELETE RESTRICT`

### 14. `StudentClass` 学生班级关联表

| 字段名          | 类型         | 约束                        | 备注                                  |
| ------------ | ---------- | ------------------------- | ----------------------------------- |
| `sc_id`      | `BIGINT`   | 主键，自增                     | 学生班级关联主键                            |
| `student_id` | `BIGINT`   | 非空，外键                     | 关联学生 ID，对应 `Student.student_id`     |
| `class_id`   | `BIGINT`   | 非空，外键                     | 关联班级 ID，对应 `TeachingClass.class_id` |
| `created_at` | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP` | 关联关系创建时间                            |

补充约束：

- `UNIQUE (student_id, class_id)`：同一个学生不能重复加入同一个班级
- 外键 `student_id → Student(student_id) ON DELETE RESTRICT`
- 外键 `class_id → TeachingClass(class_id) ON DELETE RESTRICT`

***

## 第5组：支撑关系实体（3张）

### 15. `CourseIndicatorSupport` 课程-指标点宏观支撑表

| 字段名            | 类型         | 约束                                                    | 备注                                   |
| -------------- | ---------- | ----------------------------------------------------- | ------------------------------------ |
| `cis_id`       | `BIGINT`   | 主键，自增                                                 | 课程指标支撑主键，唯一标识一条支撑关系                  |
| `course_id`    | `BIGINT`   | 非空，外键                                                 | 关联课程 ID，对应 `Course.course_id`        |
| `ip_id`        | `BIGINT`   | 非空，外键                                                 | 关联指标点 ID，对应 `IndicatorPoint.ip_id`   |
| `total_weight` | `FLOAT`    | 非空                                                    | 总支撑权重 W，范围 `0~1`，同一指标点所有课程权重和为 `1.0` |
| `created_at`   | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                               |
| `updated_at`   | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                             |

补充约束：

- `UNIQUE (course_id, ip_id)`：同一课程对同一指标点只能有一条支撑记录
- `CHECK (total_weight >= 0 AND total_weight <= 1)`：权重范围校验
- 外键 `course_id → Course(course_id) ON DELETE RESTRICT`
- 外键 `ip_id → IndicatorPoint(ip_id) ON DELETE RESTRICT`

### 16. `CourseObjective` 课程目标表

| 字段名              | 类型         | 约束                                                    | 备注                            |
| ---------------- | ---------- | ----------------------------------------------------- | ----------------------------- |
| `co_id`          | `BIGINT`   | 主键，自增                                                 | 课程目标主键，唯一标识一条课程目标             |
| `co_description` | `TEXT`     | 非空                                                    | 课程目标描述                        |
| `course_id`      | `BIGINT`   | 非空，外键                                                 | 所属课程 ID，对应 `Course.course_id` |
| `created_at`     | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                        |
| `updated_at`     | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                      |

补充约束：

- 外键 `course_id → Course(course_id) ON DELETE RESTRICT`

### 17. `ObjectiveIndicatorContribution` 课程目标-指标点内部贡献表

| 字段名               | 类型         | 约束                                                    | 备注                                      |
| ----------------- | ---------- | ----------------------------------------------------- | --------------------------------------- |
| `oic_id`          | `BIGINT`   | 主键，自增                                                 | 目标指标贡献主键，唯一标识一条贡献关系                     |
| `co_id`           | `BIGINT`   | 非空，外键                                                 | 关联课程目标 ID，对应 `CourseObjective.co_id`    |
| `ip_id`           | `BIGINT`   | 非空，外键                                                 | 关联指标点 ID，对应 `IndicatorPoint.ip_id`      |
| `internal_weight` | `FLOAT`    | 非空                                                    | 内部贡献权重 w，范围 `0~1`，同一指标点所有课程目标权重和为 `1.0` |
| `created_at`      | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                                  |
| `updated_at`      | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                                |

补充约束：

- `UNIQUE (co_id, ip_id)`：同一课程目标对同一指标点只能有一条贡献记录
- `CHECK (internal_weight >= 0 AND internal_weight <= 1)`：权重范围校验
- 外键 `co_id → CourseObjective(co_id) ON DELETE RESTRICT`
- 外键 `ip_id → IndicatorPoint(ip_id) ON DELETE RESTRICT`

***

## 第6组：考核与成绩实体（2张）

### 18. `AssessmentPoint` 考核点表

| 字段名          | 类型             | 约束                                                    | 备注                                   |
| ------------ | -------------- | ----------------------------------------------------- | ------------------------------------ |
| `ap_id`      | `BIGINT`       | 主键，自增                                                 | 考核点主键，唯一标识一个考核点                      |
| `ap_name`    | `VARCHAR(100)` | 非空                                                    | 考核点名称，如"期末卷第1题"、"实验报告2"              |
| `full_score` | `FLOAT`        | 非空                                                    | 满分分值                                 |
| `co_id`      | `BIGINT`       | 非空，外键                                                 | 关联课程目标 ID，对应 `CourseObjective.co_id` |
| `created_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                               |
| `updated_at` | `DATETIME`     | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                             |

补充约束：

- 外键 `co_id → CourseObjective(co_id) ON DELETE RESTRICT`

### 19. `StudentAssessmentScore` 学生考核点成绩表

| 字段名            | 类型         | 约束                                                    | 备注                                  |
| -------------- | ---------- | ----------------------------------------------------- | ----------------------------------- |
| `sas_id`       | `BIGINT`   | 主键，自增                                                 | 学生考核成绩主键，唯一标识一条成绩记录                 |
| `student_id`   | `BIGINT`   | 非空，外键                                                 | 关联学生 ID，对应 `Student.student_id`     |
| `ap_id`        | `BIGINT`   | 非空，外键                                                 | 关联考核点 ID，对应 `AssessmentPoint.ap_id` |
| `class_id`     | `BIGINT`   | 非空，外键                                                 | 关联班级 ID，对应 `TeachingClass.class_id` |
| `actual_score` | `FLOAT`    | 非空                                                    | 实际得分                                |
| `created_at`   | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                              |
| `updated_at`   | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                            |

补充约束：

- `UNIQUE (student_id, ap_id, class_id)`：同一学生在同一班级的同一考核点只能有一条成绩记录
- 外键 `student_id → Student(student_id) ON DELETE RESTRICT`
- 外键 `ap_id → AssessmentPoint(ap_id) ON DELETE RESTRICT`
- 外键 `class_id → TeachingClass(class_id) ON DELETE RESTRICT`

***

## 第7组：计算结果实体（3张）

### 20. `CourseObjectiveAchievement` 课程目标达成度表

| 字段名                   | 类型         | 约束                                                    | 备注                                   |
| --------------------- | ---------- | ----------------------------------------------------- | ------------------------------------ |
| `coa_id`              | `BIGINT`   | 主键，自增                                                 | 课程目标达成度主键                            |
| `class_id`            | `BIGINT`   | 非空，外键                                                 | 关联班级 ID，对应 `TeachingClass.class_id`  |
| `co_id`               | `BIGINT`   | 非空，外键                                                 | 关联课程目标 ID，对应 `CourseObjective.co_id` |
| `average_achievement` | `FLOAT`    | 非空                                                    | 班级平均达成度 Cj¯，范围 `0~1`                 |
| `created_at`          | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                               |
| `updated_at`          | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                             |

补充约束：

- `UNIQUE (class_id, co_id)`：同一班级的同一课程目标只能有一条达成度记录
- `CHECK (average_achievement >= 0 AND average_achievement <= 1)`：达成度范围校验
- 外键 `class_id → TeachingClass(class_id) ON DELETE RESTRICT`
- 外键 `co_id → CourseObjective(co_id) ON DELETE RESTRICT`

### 21. `CourseIndicatorAchievement` 课程级指标点达成度表

| 字段名           | 类型         | 约束                                                    | 备注                                  |
| ------------- | ---------- | ----------------------------------------------------- | ----------------------------------- |
| `cia_id`      | `BIGINT`   | 主键，自增                                                 | 课程指标达成度主键                           |
| `class_id`    | `BIGINT`   | 非空，外键                                                 | 关联班级 ID，对应 `TeachingClass.class_id` |
| `ip_id`       | `BIGINT`   | 非空，外键                                                 | 关联指标点 ID，对应 `IndicatorPoint.ip_id`  |
| `achievement` | `FLOAT`    | 非空                                                    | 课程级达成度 Ek，范围 `0~1`                  |
| `is_locked`   | `BOOLEAN`  | 非空，默认 `FALSE`                                         | 是否已锁定，计算完成后设为 `TRUE`，禁止修改           |
| `created_at`  | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                              |
| `updated_at`  | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                            |

补充约束：

- `UNIQUE (class_id, ip_id)`：同一班级对同一指标点只能有一条达成度记录
- `CHECK (achievement >= 0 AND achievement <= 1)`：达成度范围校验
- 外键 `class_id → TeachingClass(class_id) ON DELETE RESTRICT`
- 外键 `ip_id → IndicatorPoint(ip_id) ON DELETE RESTRICT`

### 22. `MajorIndicatorAchievement` 专业级指标点达成度表

| 字段名                 | 类型         | 约束                                                    | 备注                                 |
| ------------------- | ---------- | ----------------------------------------------------- | ---------------------------------- |
| `mia_id`            | `BIGINT`   | 主键，自增                                                 | 专业指标达成度主键                          |
| `major_id`          | `BIGINT`   | 非空，外键                                                 | 关联专业 ID，对应 `Major.major_id`        |
| `term_id`           | `BIGINT`   | 非空，外键                                                 | 关联学期 ID，对应 `AcademicTerm.term_id`  |
| `ip_id`             | `BIGINT`   | 非空，外键                                                 | 关联指标点 ID，对应 `IndicatorPoint.ip_id` |
| `final_achievement` | `FLOAT`    | 非空                                                    | 专业级达成度 Gk，范围 `0~1`                 |
| `created_at`        | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP`                             | 记录创建时间                             |
| `updated_at`        | `DATETIME` | 非空，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 记录最后修改时间                           |

补充约束：

- `UNIQUE (major_id, term_id, ip_id)`：同一专业同一学期对同一指标点只能有一条最终达成度记录
- `CHECK (final_achievement >= 0 AND final_achievement <= 1)`：达成度范围校验
- 外键 `major_id → Major(major_id) ON DELETE RESTRICT`
- 外键 `term_id → AcademicTerm(term_id) ON DELETE RESTRICT`
- 外键 `ip_id → IndicatorPoint(ip_id) ON DELETE RESTRICT`

