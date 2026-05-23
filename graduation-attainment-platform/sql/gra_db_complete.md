# 毕业要求达成度计算平台 — 数据库 28 张核心表字段说明（第四版完整修订）

基于 `GRA_db2.md`（25 表）修订，新增 **3 张补充表**（审计日志、暂存表），**4 个视图**，**8 张表补充 `status` 字段**，`teaching_class` 补充 `calc_status` 字段。共计 **28 张表** + **4 个视图**。

---

## 修订记录

| 版本 | 表数 | 变更内容 |
|------|------|----------|
| v3  | 25 | 原 GRA_db2.md |
| v4  | 28 | +8表status字段 / +teaching_class.calc_status / +3补充表 / +4视图 / +6索引 |

---

## 第1组：标准 RBAC 权限体系（5张）

### 1. `sys_user` 用户表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 用户主键 |
| `username` | `VARCHAR(50)` | 非空，唯一 | 登录账号 |
| `password` | `VARCHAR(255)` | 非空 | 密码密文 |
| `real_name` | `VARCHAR(50)` | 非空 | 用户真实姓名 |
| `email` | `VARCHAR(128)` | 可空 | 电子邮箱 |
| `phone` | `VARCHAR(20)` | 可空 | 手机号 |
| `status` | `TINYINT` | 非空，默认 `1` | 1=启用 0=禁用 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (username)`：登录账号全局唯一

### 2. `sys_role` 角色表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 角色主键 |
| `role_code` | `VARCHAR(50)` | 非空，唯一 | 角色编码，如 `admin`、`instructor` |
| `role_name` | `VARCHAR(100)` | 非空 | 角色名称 |
| `status` | `TINYINT` | 非空，默认 `1` | 1=启用 0=禁用 |
| `remark` | `VARCHAR(255)` | 可空 | 角色备注 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (role_code)`：角色编码全局唯一

### 3. `sys_permission` 权限表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 权限主键 |
| `perm_code` | `VARCHAR(100)` | 非空，唯一 | 权限编码 |
| `perm_name` | `VARCHAR(100)` | 非空 | 权限名称 |
| `module_name` | `VARCHAR(50)` | 可空 | 所属模块 |
| `remark` | `VARCHAR(255)` | 可空 | 权限备注 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (perm_code)`：权限编码全局唯一

### 4. `sys_user_role` 用户角色关联表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 关联主键 |
| `user_id` | `BIGINT` | 非空，FK → `sys_user.id` | 用户ID |
| `role_id` | `BIGINT` | 非空，FK → `sys_role.id` | 角色ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |

补充约束：

- `UNIQUE (user_id, role_id)`：同一用户不能重复分配同一角色
- `FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT`
- `FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT`

### 5. `sys_role_permission` 角色权限关联表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 关联主键 |
| `role_id` | `BIGINT` | 非空，FK → `sys_role.id` | 角色ID |
| `permission_id` | `BIGINT` | 非空，FK → `sys_permission.id` | 权限ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |

补充约束：

- `UNIQUE (role_id, permission_id)`：同一角色不能重复分配同一权限
- `FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT`
- `FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE RESTRICT`

---

## 第2组：基础组织与时间实体（3张）

### 6. `college` 学院表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `college_id` | `BIGINT` | 主键，自增 | 学院主键 |
| `college_code` | `VARCHAR(20)` | 非空，唯一 | 学院编码，如 `"CS"` |
| `college_name` | `VARCHAR(100)` | 非空 | 学院名称 |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=启用 0=禁用 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (college_code)`：学院编码全局唯一

### 7. `major` 专业表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `major_id` | `BIGINT` | 主键，自增 | 专业主键 |
| `major_code` | `VARCHAR(20)` | 非空，唯一 | 专业编码，如 `"080901"` |
| `major_name` | `VARCHAR(100)` | 非空 | 专业名称 |
| `college_id` | `BIGINT` | 非空，FK → `college.college_id` RESTRICT | 所属学院ID |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=招生中 0=停招 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (major_code)`：专业编码全局唯一
- `FOREIGN KEY (college_id) REFERENCES college(college_id) ON DELETE RESTRICT`

### 8. `academic_term` 学年学期表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `term_id` | `BIGINT` | 主键，自增 | 学期主键 |
| `term_code` | `VARCHAR(20)` | 非空，唯一 | 学期编码，如 `"2024-2025-1"` |
| `academic_year` | `INT` | 非空 | 学年（起始年） |
| `semester` | `INT` | 非空，CHECK(1,2,3) | 学期号：1=第一学期 2=第二学期 3=夏季短学期 |
| `start_date` | `DATE` | 非空 | 学期开始日期 |
| `end_date` | `DATE` | 非空 | 学期结束日期 |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=当前学期 0=历史学期 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (term_code)`：学期编码全局唯一
- `CHECK (semester IN (1, 2, 3))`：学期号仅限 1/2/3

---

## 第3组：人员实体（2张）

### 9. `teacher` 教师信息表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 教师主键 |
| `teacher_no` | `VARCHAR(32)` | 非空，唯一 | 教师编号/工号，如 `"T2024001"` |
| `teacher_name` | `VARCHAR(64)` | 非空 | 教师姓名 |
| `title` | `VARCHAR(64)` | 可空 | 职称，如"教授"、"副教授" |
| `major_id` | `BIGINT` | 可空，FK → `major.major_id` SET NULL | 所属专业ID |
| `user_id` | `BIGINT` | 可空，唯一，FK → `sys_user.id` SET NULL | 关联系统用户ID |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=在职 0=离职 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (teacher_no)`：教师编号全局唯一
- `UNIQUE (user_id)`：一个系统用户只能对应一个教师身份
- `FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE SET NULL`
- `FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL`

### 10. `student` 学生表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `student_id` | `BIGINT` | 主键，自增 | 学生主键 |
| `student_no` | `VARCHAR(20)` | 非空，唯一 | 学号，如 `"20220101001"` |
| `student_name` | `VARCHAR(50)` | 非空 | 学生姓名 |
| `major_id` | `BIGINT` | 非空，FK → `major.major_id` RESTRICT | 所属专业ID |
| `enrollment_year` | `INT` | 非空 | 入学年份 |
| `user_id` | `BIGINT` | 可空，唯一，FK → `sys_user.id` SET NULL | 关联系统用户ID |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=在读 2=毕业 3=休学 0=退学 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (student_no)`：学号全局唯一
- `UNIQUE (user_id)`：一个系统用户只能对应一个学生身份
- `FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT`
- `FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL`

---

## 第4组：毕业要求体系（2张）

### 11. `graduation_requirement` 毕业要求表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `gr_id` | `BIGINT` | 主键，自增 | 毕业要求主键 |
| `gr_code` | `VARCHAR(10)` | 非空，UK (与major_id组合) | 毕业要求编码，如 `"1"` |
| `gr_description` | `TEXT` | 非空 | 毕业要求的完整文字描述 |
| `major_id` | `BIGINT` | 非空，UK (与gr_code组合)，FK → `major.major_id` RESTRICT | 所属专业ID |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=启用 0=停用 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (major_id, gr_code)`：同一专业下毕业要求编码不可重复
- `FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT`

### 12. `indicator_point` 二级指标点表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `ip_id` | `BIGINT` | 主键，自增 | 指标点主键 |
| `ip_code` | `VARCHAR(10)` | 非空，UK (与gr_id组合) | 指标点编码，如 `"1.1"` |
| `ip_description` | `TEXT` | 非空 | 指标点详细描述 |
| `gr_id` | `BIGINT` | 非空，UK (与ip_code组合)，FK → `graduation_requirement.gr_id` RESTRICT | 所属毕业要求ID |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=启用 0=停用 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (gr_id, ip_code)`：同一毕业要求下指标点编码不可重复
- `FOREIGN KEY (gr_id) REFERENCES graduation_requirement(gr_id) ON DELETE RESTRICT`

---

## 第5组：课程与教学（4张）

### 13. `course` 课程表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `course_id` | `BIGINT` | 主键，自增 | 课程主键 |
| `course_code` | `VARCHAR(20)` | 非空，唯一 | 课程编码，如 `"CS201"` |
| `course_name` | `VARCHAR(100)` | 非空 | 课程名称 |
| `credit` | `FLOAT` | 非空 | 学分 |
| `status` | `TINYINT` | 非空，默认 `1` | **v4新增** 1=开课中 0=停开 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (course_code)`：课程编码全局唯一

### 14. `course_major` 课程-专业关联表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `cm_id` | `BIGINT` | 主键，自增 | 关联主键 |
| `course_id` | `BIGINT` | 非空，UK (与major_id组合)，FK → `course.course_id` CASCADE | 课程ID |
| `major_id` | `BIGINT` | 非空，UK (与course_id组合)，FK → `major.major_id` RESTRICT | 专业ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 关联关系创建时间 |

补充约束：

- `UNIQUE (course_id, major_id)`：同一课程不能重复绑定同一专业
- `FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE`
- `FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT`

### 15. `teaching_class` 教学班级表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `class_id` | `BIGINT` | 主键，自增 | 班级主键 |
| `class_name` | `VARCHAR(50)` | 非空，UK (与course_id,term_id组合) | 班级名称 |
| `course_id` | `BIGINT` | 非空，UK (与term_id,class_name组合)，FK → `course.course_id` RESTRICT | 所属课程ID |
| `term_id` | `BIGINT` | 非空，UK (与course_id,class_name组合)，FK → `academic_term.term_id` RESTRICT | 所属学期ID |
| `teacher_id` | `BIGINT` | 非空，FK → `teacher.id` RESTRICT | 主讲教师ID |
| `calc_status` | `ENUM` | 非空，默认 `'unsubmitted'` | **v4新增** unsubmitted=未提交 / score_imported=已导入 / calculating=计算中 / locked=已锁定 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (course_id, term_id, class_name)`：同一课程同一学期内班级名称唯一
- `FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT`
- `FOREIGN KEY (term_id) REFERENCES academic_term(term_id) ON DELETE RESTRICT`
- `FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT`

### 16. `student_class` 学生班级关联表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `sc_id` | `BIGINT` | 主键，自增 | 关联主键 |
| `student_id` | `BIGINT` | 非空，UK (与class_id组合)，FK → `student.student_id` RESTRICT | 学生ID |
| `class_id` | `BIGINT` | 非空，UK (与student_id组合)，FK → `teaching_class.class_id` RESTRICT | 班级ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 关联关系创建时间 |

补充约束：

- `UNIQUE (student_id, class_id)`：同一学生不能重复加入同一班级
- `FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT`
- `FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT`

---

## 第6组：支撑关系（3张）

### 17. `course_indicator_support` 课程-指标点宏观支撑表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `cis_id` | `BIGINT` | 主键，自增 | 支撑关系主键 |
| `course_id` | `BIGINT` | 非空，UK (与ip_id组合)，FK → `course.course_id` RESTRICT | 支撑课程ID |
| `ip_id` | `BIGINT` | 非空，UK (与course_id组合)，FK → `indicator_point.ip_id` RESTRICT | 被支撑的指标点ID |
| `total_weight` | `FLOAT` | 非空，CHECK ≥0 ≤1 | 总支撑权重 W |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (course_id, ip_id)`：同一课程对同一指标点只能有一条支撑记录
- `CHECK (total_weight >= 0 AND total_weight <= 1)`：权重范围校验
- `FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT`
- `FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT`

业务规则：同一指标点下所有课程的 `total_weight` 之和应等于 1.0（应用层校验）。

### 18. `course_objective` 课程目标表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `co_id` | `BIGINT` | 主键，自增 | 课程目标主键 |
| `objective_code` | `VARCHAR(16)` | 非空，UK (与course_id组合) | 课程目标编码，如 `"CO1"` |
| `co_description` | `TEXT` | 非空 | 课程目标详细描述 |
| `course_id` | `BIGINT` | 非空，UK (与objective_code组合)，FK → `course.course_id` RESTRICT | 所属课程ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (course_id, objective_code)`：同一课程下目标编码不可重复
- `FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT`

### 19. `objective_indicator_contribution` 目标-指标点内部贡献表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `oic_id` | `BIGINT` | 主键，自增 | 贡献关系主键 |
| `co_id` | `BIGINT` | 非空，UK (与ip_id组合)，FK → `course_objective.co_id` RESTRICT | 课程目标ID |
| `ip_id` | `BIGINT` | 非空，UK (与co_id组合)，FK → `indicator_point.ip_id` RESTRICT | 指标点ID |
| `internal_weight` | `FLOAT` | 非空，CHECK ≥0 ≤1 | 内部贡献权重 w |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (co_id, ip_id)`：同一课程目标对同一指标点只能有一条贡献记录
- `CHECK (internal_weight >= 0 AND internal_weight <= 1)`：权重范围校验
- `FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT`
- `FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT`

业务规则：同一课程内，所有目标对同一指标点的 `internal_weight` 之和应等于 1.0（应用层校验）。

---

## 第7组：考核与成绩（2张）

### 20. `assessment_point` 考核点表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `ap_id` | `BIGINT` | 主键，自增 | 考核点主键 |
| `ap_name` | `VARCHAR(100)` | 非空 | 考核点名称，如"期末卷-链表操作题" |
| `full_score` | `FLOAT` | 非空 | 满分分值 |
| `co_id` | `BIGINT` | 非空，FK → `course_objective.co_id` RESTRICT | 所属课程目标ID |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT`

### 21. `student_assessment_score` 学生考核成绩表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `sas_id` | `BIGINT` | 主键，自增 | 成绩记录主键 |
| `student_id` | `BIGINT` | 非空，UK (与ap_id,class_id组合)，FK → `student.student_id` RESTRICT | 学生ID |
| `ap_id` | `BIGINT` | 非空，UK (与student_id,class_id组合)，FK → `assessment_point.ap_id` RESTRICT | 考核点ID |
| `class_id` | `BIGINT` | 非空，UK (与student_id,ap_id组合)，FK → `teaching_class.class_id` RESTRICT | 教学班级ID |
| `actual_score` | `FLOAT` | 非空 | 实际得分 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (student_id, ap_id, class_id)`：同一学生在同一班级的同一考核点只能有一条成绩
- `FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT`
- `FOREIGN KEY (ap_id) REFERENCES assessment_point(ap_id) ON DELETE RESTRICT`
- `FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT`

---

## 第8组：计算结果（3张）

### 22. `course_objective_achievement` 课程目标达成度表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `coa_id` | `BIGINT` | 主键，自增 | 达成度记录主键 |
| `class_id` | `BIGINT` | 非空，UK (与co_id组合)，FK → `teaching_class.class_id` RESTRICT | 教学班级ID |
| `co_id` | `BIGINT` | 非空，UK (与class_id组合)，FK → `course_objective.co_id` RESTRICT | 课程目标ID |
| `average_achievement` | `FLOAT` | 非空，CHECK ≥0 ≤1 | 班级课程目标达成度 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (class_id, co_id)`：每个班级每个目标仅一条达成度记录
- `CHECK (average_achievement >= 0 AND average_achievement <= 1)`
- `FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT`
- `FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT`

### 23. `course_indicator_achievement` 课程级指标点达成度表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `cia_id` | `BIGINT` | 主键，自增 | 达成度记录主键 |
| `class_id` | `BIGINT` | 非空，UK (与ip_id组合)，FK → `teaching_class.class_id` RESTRICT | 教学班级ID |
| `ip_id` | `BIGINT` | 非空，UK (与class_id组合)，FK → `indicator_point.ip_id` RESTRICT | 指标点ID |
| `achievement` | `FLOAT` | 非空，CHECK ≥0 ≤1 | 课程级指标点达成度 |
| `is_locked` | `BOOLEAN` | 非空，默认 `FALSE` | 是否已锁定（锁定后禁止修改） |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (class_id, ip_id)`：每个班级每个指标点仅一条达成度记录
- `CHECK (achievement >= 0 AND achievement <= 1)`
- `FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT`
- `FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT`

### 24. `major_indicator_achievement` 专业级指标点达成度表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `mia_id` | `BIGINT` | 主键，自增 | 达成度记录主键 |
| `major_id` | `BIGINT` | 非空，UK (与term_id,ip_id组合)，FK → `major.major_id` RESTRICT | 专业ID |
| `term_id` | `BIGINT` | 非空，UK (与major_id,ip_id组合)，FK → `academic_term.term_id` RESTRICT | 学期ID |
| `ip_id` | `BIGINT` | 非空，UK (与major_id,term_id组合)，FK → `indicator_point.ip_id` RESTRICT | 指标点ID |
| `final_achievement` | `FLOAT` | 非空，CHECK ≥0 ≤1 | 专业级最终达成度 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (major_id, term_id, ip_id)`：每个专业每学期每个指标点仅一条达成度记录
- `CHECK (final_achievement >= 0 AND final_achievement <= 1)`
- `FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT`
- `FOREIGN KEY (term_id) REFERENCES academic_term(term_id) ON DELETE RESTRICT`
- `FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT`

---

## 第9组：系统配置（1张）

### 25. `system_config` 系统配置表

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `id` | `BIGINT` | 主键，自增 | 配置主键 |
| `config_key` | `VARCHAR(128)` | 非空，唯一 | 配置键名 |
| `config_value` | `VARCHAR(512)` | 非空 | 配置值 |
| `config_desc` | `VARCHAR(256)` | 可空 | 配置描述 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |
| `updated_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP ON UPDATE | 记录最后修改时间 |

补充约束：

- `UNIQUE (config_key)`：配置键名全局唯一

---

## 第10组：审计日志（2张）**v4新增**

### 26. `calc_audit_log` 计算审计日志表

> 关联需求：5.3 事务完整性。记录每次计算操作的元数据及结果快照，用于历史溯源。

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `log_id` | `BIGINT` | 主键，自增 | 日志主键 |
| `operator_id` | `BIGINT` | 非空 | 触发计算的用户ID（sys_user.id） |
| `action_type` | `VARCHAR(32)` | 非空 | 操作类型：course_obj_calc / course_ind_calc / major_calc / unlock |
| `target_type` | `VARCHAR(32)` | 非空 | 目标类型：teaching_class / major |
| `target_id` | `BIGINT` | 非空 | 目标ID（class_id 或 major_id） |
| `term_id` | `BIGINT` | 可空 | 学期ID（仅专业级计算时有值） |
| `result_json` | `JSON` | 可空 | 计算结果JSON快照，用于历史回溯 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |

索引：

- `INDEX idx_cal_target (target_type, target_id)`
- `INDEX idx_cal_operator (operator_id)`
- `INDEX idx_cal_time (created_at)`

### 27. `unlock_audit_log` 解锁审计日志表

> 关联需求：5.3 勘误工单→管理员解锁。需要提供解锁原因并经过审批。

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `ulog_id` | `BIGINT` | 主键，自增 | 日志主键 |
| `class_id` | `BIGINT` | 非空，FK → `teaching_class.class_id` RESTRICT | 被解锁的教学班级ID |
| `request_by` | `BIGINT` | 非空 | 申请解锁的教师ID（teacher.id） |
| `approved_by` | `BIGINT` | 非空 | 审批解锁的教务管理员ID（sys_user.id） |
| `reason` | `VARCHAR(512)` | 非空 | 解锁原因（必填） |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |

补充约束：

- `FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT`

---

## 第11组：数据导入（1张）**v4新增**

### 28. `temp_import_staging` Excel 导入暂存表

> 关联需求：A-3 课程导入 / C-2 成绩导入。后端批量导入时先写入此表进行数据校验，通过后再写入正式表。

| 字段名 | 类型 | 约束 | 备注 |
|--------|------|------|------|
| `staging_id` | `BIGINT` | 主键，自增 | 暂存记录主键 |
| `batch_id` | `VARCHAR(36)` | 非空 | 导入批次UUID，用于定位和回滚 |
| `table_name` | `VARCHAR(64)` | 非空 | 目标表名 |
| `row_index` | `INT` | 非空 | Excel行号（从2开始，第1行为表头） |
| `row_data` | `JSON` | 非空 | 行原始数据的 JSON 格式 |
| `status` | `ENUM` | 非空，默认 `'pending'` | pending=待处理 / validated=已校验 / imported=已导入 / error=校验失败 |
| `error_msg` | `VARCHAR(512)` | 可空 | 校验失败原因 |
| `created_at` | `DATETIME` | 非空，默认 CURRENT_TIMESTAMP | 记录创建时间 |

索引：

- `INDEX idx_staging_batch (batch_id)`
- `INDEX idx_staging_status (batch_id, status)`

---

## 第12组：监控视图（4个）**v4新增**

### VIEW-01：`v_course_calc_progress` 课程计算进度看板

> 关联需求：C-4 前置校验——展示所有教学班的计算状态和成绩导入情况。

| 列名 | 来源 | 备注 |
|------|------|------|
| `class_id` | `teaching_class` | 班级ID |
| `class_name` | `teaching_class` | 班级名称 |
| `calc_status` | `teaching_class` | 计算状态 |
| `course_code` | `course` | 课程编码 |
| `course_name` | `course` | 课程名称 |
| `teacher_name` | `teacher` | 主讲教师 |
| `major_id` | `course_major` | 所属专业ID |
| `term_id` | `teaching_class` | 学期ID |
| `student_count` | 子查询 FROM `student_class` | 班级人数 |
| `score_count` | 子查询 FROM `student_assessment_score` | 成绩条数 |

### VIEW-02：`v_major_achievement_dashboard` 专业级达成度看板

> 关联需求：D-2 专业级报告——带合格判断的达成度汇总。

| 列名 | 来源 | 备注 |
|------|------|------|
| `major_id` / `term_id` / `ip_id` | `major_indicator_achievement` | 三级联合主键 |
| `ip_code` / `ip_description` | `indicator_point` | 指标点信息 |
| `gr_code` / `gr_description` | `graduation_requirement` | 毕业要求信息 |
| `final_achievement` | `major_indicator_achievement` | 最终达成度值 |
| `pass_status` | 计算列 | 达成度 ≥0.60 为"合格"，否则"不合格" |

### VIEW-03：`v_score_drilldown` 穿透式追溯视图

> 关联需求：D-2 认证专家查阅——从指标点层层追溯至考核点得分明细。

| 列名 | 来源 | 备注 |
|------|------|------|
| `gr_code` / `gr_desc` | `graduation_requirement` | 毕业要求 |
| `ip_code` / `ip_desc` | `indicator_point` | 指标点 |
| `course_code` / `course_name` | `course` | 课程 |
| `objective_code` / `co_description` | `course_objective` | 课程目标 |
| `ap_name` / `full_score` | `assessment_point` | 考核点名称与满分 |
| `actual_score` | `student_assessment_score` | 实际得分 |
| `student_no` / `student_name` | `student` | 学生 |
| `class_name` / `term_id` | `teaching_class` | 班级与学期 |
| `macro_weight` | `course_indicator_support.total_weight` | 宏观支撑权重 W |
| `micro_weight` | `objective_indicator_contribution.internal_weight` | 内部贡献权重 w |

涉及联表：`student_assessment_score` → `assessment_point` → `course_objective` → `objective_indicator_contribution` → `indicator_point` → `graduation_requirement` → `teaching_class` → `course` → `course_indicator_support` → `student`（共 10 表联查）

### VIEW-04：`v_weight_validation` 权重校验辅助视图

> 关联需求：A-4 宏观支撑矩阵按列实时求和。后端通过查询此视图获取每个指标点的权重和校验结果。

| 列名 | 来源 | 备注 |
|------|------|------|
| `ip_id` / `ip_code` | `indicator_point` | 指标点 |
| `gr_code` | `graduation_requirement` | 毕业要求编码 |
| `major_id` / `major_name` | `major` | 所属专业 |
| `support_course_count` | `course_indicator_support` | 支撑该指标点的课程数 |
| `weight_sum` | `SUM(total_weight)` | 当前权重和 |
| `is_valid` | 计算列 | 权重和 = 1.0（容差 0.001）为 OK，否则 FAIL |

---

## 业务索引汇总（31条）

### 原有索引（25条）

```
idx_obj_course        ON course_objective(course_id)
idx_oic_objective     ON objective_indicator_contribution(co_id)
idx_oic_indicator     ON objective_indicator_contribution(ip_id)
idx_ap_objective      ON assessment_point(co_id)
idx_sas_class         ON student_assessment_score(class_id)
idx_sas_student       ON student_assessment_score(student_id)
idx_cis_course        ON course_indicator_support(course_id)
idx_cis_indicator     ON course_indicator_support(ip_id)
idx_class_course      ON teaching_class(course_id)
idx_class_term        ON teaching_class(term_id)
idx_class_teacher     ON teaching_class(teacher_id)
idx_req_major         ON graduation_requirement(major_id)
idx_ind_req           ON indicator_point(gr_id)
idx_ur_role           ON sys_user_role(role_id)
idx_rp_role           ON sys_role_permission(role_id)
idx_rp_perm           ON sys_role_permission(permission_id)
idx_coa_class         ON course_objective_achievement(class_id)
idx_cia_class         ON course_indicator_achievement(class_id)
idx_mia_major         ON major_indicator_achievement(major_id)
idx_mia_term          ON major_indicator_achievement(term_id)
idx_student_major     ON student(major_id)
idx_teacher_major     ON teacher(major_id)
idx_cm_course         ON course_major(course_id)
idx_cm_major          ON course_major(major_id)
idx_teacher_user      ON teacher(user_id)  -- UNIQUE KEY
idx_student_user      ON student(user_id)  -- UNIQUE KEY
```

### 补充索引（6条）**v4新增**

```
idx_sas_ap_student    ON student_assessment_score(ap_id, student_id)
idx_sas_class_ap      ON student_assessment_score(class_id, ap_id)
idx_coa_class_co      ON course_objective_achievement(class_id, co_id)
idx_cia_class_ip      ON course_indicator_achievement(class_id, ip_id)
idx_oic_co_ip         ON objective_indicator_contribution(co_id, ip_id)
idx_tc_calc_status    ON teaching_class(calc_status, term_id)
```

---

## 全文总览

| 组 | 表数 | 表名 |
|----|------|------|
| 系统用户权限 | 5 | sys_user, sys_role, sys_permission, sys_user_role, sys_role_permission |
| 基础组织与时间 | 3 | college, major, academic_term |
| 人员实体 | 2 | teacher, student |
| 毕业要求体系 | 2 | graduation_requirement, indicator_point |
| 课程与教学 | 4 | course, course_major, teaching_class, student_class |
| 支撑关系 | 3 | course_indicator_support, course_objective, objective_indicator_contribution |
| 考核与成绩 | 2 | assessment_point, student_assessment_score |
| 计算结果 | 3 | course_objective_achievement, course_indicator_achievement, major_indicator_achievement |
| 系统配置 | 1 | system_config |
| 审计日志 | 2 | calc_audit_log, unlock_audit_log |
| 数据导入 | 1 | temp_import_staging |
| **合计** | **28** | |

| 类型 | 数量 |
|------|------|
| 表 | 28 |
| 视图 | 4 |
| 索引 | 31 |
| v4 新增 status 字段 | 8 表（college, major, academic_term, graduation_requirement, indicator_point, course, teacher, student） |
| v4 新增 calc_status 字段 | 1 表（teaching_class） |