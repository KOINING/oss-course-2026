-- ================================================================
-- gra_db_full.sql — 毕业要求达成度平台 全量建库脚本（初始至第四周）
-- ================================================================
-- 合并来源（全部包含）：
--   GRA_db.sql
--   week3_init_data.sql
--   upgrade_grade_year_20260603.sql
--   upgrade_week4_score_calc.sql
--   upgrade_week4_e2e_test_data.sql
-- 账号约束：原有 8 个登录账号及 RBAC 配置保持原样，仅追加第三周 2 个教师账号
-- ================================================================

DROP DATABASE IF EXISTS GraduationDB;
CREATE DATABASE GraduationDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
USE GraduationDB;

-- ================================================================
-- 1. 系统用户权限表 (RBAC) — 5 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(128) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(100) NOT NULL,
    module_name VARCHAR(50),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role(user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission(role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ================================================================
-- 2. 基础组织与时间实体 — 3 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS college (
    college_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    college_code VARCHAR(20) NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS major (
    major_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_code VARCHAR(20) NOT NULL UNIQUE,
    major_name VARCHAR(100) NOT NULL,
    college_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=招生中 0=停招',
    FOREIGN KEY (college_id) REFERENCES college(college_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_term (
    term_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    term_code VARCHAR(20) NOT NULL UNIQUE,
    academic_year INT NOT NULL,
    semester INT NOT NULL CHECK (semester IN (1, 2, 3)),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=当前学期 0=历史学期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 3. 人员实体 — 2 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_no VARCHAR(32) NOT NULL UNIQUE,
    teacher_name VARCHAR(64) NOT NULL,
    title VARCHAR(64) DEFAULT NULL,
    major_id BIGINT DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=在职 0=离职',
    UNIQUE KEY uk_teacher_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(20) NOT NULL UNIQUE,
    student_name VARCHAR(50) NOT NULL,
    major_id BIGINT NOT NULL,
    enrollment_year INT NOT NULL,
    user_id BIGINT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=在读 2=毕业 3=休学 0=退学',
    UNIQUE KEY uk_student_user(user_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 4. 毕业要求体系 — 2 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS graduation_requirement (
    gr_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gr_code VARCHAR(10) NOT NULL,
    gr_description TEXT NOT NULL,
    major_id BIGINT NOT NULL,
    grade_year INT NOT NULL DEFAULT 2022 COMMENT '适用年级',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    UNIQUE KEY uk_major_grade_gr_code(major_id, grade_year, gr_code),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS indicator_point (
    ip_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_code VARCHAR(10) NOT NULL,
    ip_description TEXT NOT NULL,
    gr_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    UNIQUE KEY uk_gr_ip_code(gr_id, ip_code),
    FOREIGN KEY (gr_id) REFERENCES graduation_requirement(gr_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 5. 课程与教学 — 4 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS course (
    course_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credit FLOAT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=开课中 0=停开',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course_major (
    cm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    grade_year INT NOT NULL DEFAULT 2022 COMMENT '适用年级',
    UNIQUE KEY uk_course_major_grade(course_id, major_id, grade_year),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS teaching_class (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_code VARCHAR(32) NOT NULL UNIQUE COMMENT '教学班编号，业务唯一标识',
    class_name VARCHAR(50) NOT NULL,
    course_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    grade_year INT NOT NULL DEFAULT 2022 COMMENT '适用年级',
    calc_status ENUM('unsubmitted', 'score_imported', 'calculating', 'locked')
        NOT NULL DEFAULT 'unsubmitted'
        COMMENT '计算状态：unsubmitted=未提交成绩 / score_imported=成绩已导入 / calculating=计算中 / locked=已锁定',
    UNIQUE KEY uk_course_term_class(course_id, term_id, class_name),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (term_id) REFERENCES academic_term(term_id) ON DELETE RESTRICT,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_class (
    sc_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    UNIQUE KEY uk_student_class(student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 6. 支撑关系 — 3 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS course_indicator_support (
    cis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    total_weight FLOAT NOT NULL,
    UNIQUE KEY uk_course_ip(course_id, ip_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (total_weight >= 0 AND total_weight <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course_objective (
    co_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    objective_code VARCHAR(16) NOT NULL,
    co_description TEXT NOT NULL,
    course_id BIGINT NOT NULL,
    UNIQUE KEY uk_course_obj_code(course_id, objective_code),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS objective_indicator_contribution (
    oic_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    co_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    internal_weight FLOAT NOT NULL,
    UNIQUE KEY uk_co_ip(co_id, ip_id),
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (internal_weight >= 0 AND internal_weight <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 7. 考核与成绩 — 2 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS assessment_point (
    ap_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ap_name VARCHAR(100) NOT NULL,
    full_score FLOAT NOT NULL,
    co_id BIGINT NOT NULL,
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_assessment_score (
    sas_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    ap_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    actual_score FLOAT NOT NULL,
    UNIQUE KEY uk_student_ap_class(student_id, ap_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (ap_id) REFERENCES assessment_point(ap_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 8. 计算结果 — 4 张表
-- ================================================================

-- 8.0 学生课程目标达成度中间结果表
CREATE TABLE IF NOT EXISTS student_objective_achievement (
    soa_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    class_id BIGINT NOT NULL COMMENT '教学班ID',
    co_id BIGINT NOT NULL COMMENT '课程目标ID',
    achievement FLOAT NOT NULL COMMENT '学生对课程目标的达成度，取值范围 0 到 1',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_soa_student_class_co (student_id, class_id, co_id),
    KEY idx_soa_student (student_id),
    KEY idx_soa_class (class_id),
    KEY idx_soa_co (co_id),
    KEY idx_soa_class_co (class_id, co_id),

    CONSTRAINT fk_soa_student
        FOREIGN KEY (student_id) REFERENCES student(student_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_soa_class
        FOREIGN KEY (class_id) REFERENCES teaching_class(class_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_soa_co
        FOREIGN KEY (co_id) REFERENCES course_objective(co_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_soa_achievement
        CHECK (achievement >= 0 AND achievement <= 1)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='学生课程目标达成度中间结果表';

CREATE TABLE IF NOT EXISTS course_objective_achievement (
    coa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    co_id BIGINT NOT NULL,
    average_achievement FLOAT NOT NULL,
    UNIQUE KEY uk_class_co(class_id, co_id),
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    CHECK (average_achievement >= 0 AND average_achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course_indicator_achievement (
    cia_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    achievement FLOAT NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_class_ip(class_id, ip_id),
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (achievement >= 0 AND achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS major_indicator_achievement (
    mia_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    grade_year INT NOT NULL DEFAULT 2022 COMMENT '适用年级',
    term_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    final_achievement FLOAT NOT NULL,
    UNIQUE KEY uk_major_grade_term_ip(major_id, grade_year, term_id, ip_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT,
    FOREIGN KEY (term_id) REFERENCES academic_term(term_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES indicator_point(ip_id) ON DELETE RESTRICT,
    CHECK (final_achievement >= 0 AND final_achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 9. 系统配置 — 1 张表
-- ================================================================
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value VARCHAR(512) NOT NULL,
    config_desc VARCHAR(256) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ================================================================
-- 10. 审计日志 — 2 张表
-- ================================================================

-- 10.1 计算操作审计日志
-- 关联需求：5.3 事务完整性
CREATE TABLE IF NOT EXISTS calc_audit_log (
    log_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '触发计算的用户ID sys_user.id',
    action_type VARCHAR(32) NOT NULL COMMENT 'course_obj_calc / course_ind_calc / major_calc / unlock',
    target_type VARCHAR(32) NOT NULL COMMENT 'teaching_class / major',
    target_id   BIGINT NOT NULL COMMENT 'class_id 或 major_id',
    term_id     BIGINT COMMENT '学期ID，仅专业级计算时有值',
    result_json JSON COMMENT '计算结果JSON快照，用于历史回溯',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cal_target (target_type, target_id),
    INDEX idx_cal_operator (operator_id),
    INDEX idx_cal_time (created_at)
) ENGINE=InnoDB;

-- 10.2 解锁操作审计日志
-- 关联需求：5.3 勘误工单→管理员解锁
CREATE TABLE IF NOT EXISTS unlock_audit_log (
    ulog_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id    BIGINT NOT NULL COMMENT '被解锁的教学班级 teaching_class.class_id',
    request_by  BIGINT NOT NULL COMMENT '申请解锁的教师 teacher.id',
    approved_by BIGINT NOT NULL COMMENT '审批解锁的教务管理员 sys_user.id',
    reason      VARCHAR(512) NOT NULL COMMENT '解锁原因（必填）',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ulog_class (class_id),
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ================================================================
-- 11. Excel 导入暂存表 — 1 张表
-- 关联需求：A-3 课程导入 / C-2 成绩导入
-- ================================================================
CREATE TABLE IF NOT EXISTS temp_import_staging (
    staging_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_id     VARCHAR(36) NOT NULL COMMENT '导入批次UUID',
    table_name   VARCHAR(64) NOT NULL COMMENT '目标表名',
    row_index    INT NOT NULL COMMENT 'Excel行号（从2开始）',
    row_data     JSON NOT NULL COMMENT '行原始数据的 JSON',
    status       ENUM('pending', 'validated', 'imported', 'error') DEFAULT 'pending',
    error_msg    VARCHAR(512) COMMENT '校验失败原因',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_staging_batch (batch_id),
    INDEX idx_staging_status (batch_id, status)
) ENGINE=InnoDB;

-- ================================================================
-- 12. 业务索引
-- ================================================================
CREATE INDEX idx_obj_course        ON course_objective(course_id);
CREATE INDEX idx_oic_objective     ON objective_indicator_contribution(co_id);
CREATE INDEX idx_oic_indicator     ON objective_indicator_contribution(ip_id);
CREATE INDEX idx_ap_objective      ON assessment_point(co_id);
CREATE INDEX idx_sas_class         ON student_assessment_score(class_id);
CREATE INDEX idx_sas_student       ON student_assessment_score(student_id);
CREATE INDEX idx_cis_course        ON course_indicator_support(course_id);
CREATE INDEX idx_cis_indicator     ON course_indicator_support(ip_id);
CREATE INDEX idx_class_course      ON teaching_class(course_id);
CREATE INDEX idx_class_term        ON teaching_class(term_id);
CREATE INDEX idx_class_teacher     ON teaching_class(teacher_id);
CREATE INDEX idx_req_major         ON graduation_requirement(major_id);
CREATE INDEX idx_ind_req           ON indicator_point(gr_id);
CREATE INDEX idx_ur_role           ON sys_user_role(role_id);
CREATE INDEX idx_rp_role           ON sys_role_permission(role_id);
CREATE INDEX idx_rp_perm           ON sys_role_permission(permission_id);
CREATE INDEX idx_coa_class         ON course_objective_achievement(class_id);
CREATE INDEX idx_cia_class         ON course_indicator_achievement(class_id);
CREATE INDEX idx_mia_major         ON major_indicator_achievement(major_id);
CREATE INDEX idx_mia_term          ON major_indicator_achievement(term_id);
CREATE INDEX idx_student_major     ON student(major_id);
CREATE INDEX idx_teacher_major     ON teacher(major_id);
CREATE INDEX idx_cm_course         ON course_major(course_id);
CREATE INDEX idx_cm_major          ON course_major(major_id);

-- 补充索引 (GRA_db2_migration.sql)
CREATE INDEX idx_sas_ap_student    ON student_assessment_score(ap_id, student_id);
CREATE INDEX idx_sas_class_ap      ON student_assessment_score(class_id, ap_id);
CREATE INDEX idx_coa_class_co      ON course_objective_achievement(class_id, co_id);
CREATE INDEX idx_cia_class_ip      ON course_indicator_achievement(class_id, ip_id);
CREATE INDEX idx_oic_co_ip         ON objective_indicator_contribution(co_id, ip_id);
CREATE INDEX idx_tc_calc_status    ON teaching_class(calc_status, term_id);
CREATE INDEX idx_req_major_grade ON graduation_requirement(major_id, grade_year);
CREATE INDEX idx_cm_major_grade ON course_major(major_id, grade_year);
CREATE INDEX idx_class_grade ON teaching_class(grade_year);
CREATE INDEX idx_mia_major_grade ON major_indicator_achievement(major_id, grade_year);


-- ================================================================
-- 13. 监控视图 — 4 个
-- ================================================================

CREATE OR REPLACE VIEW v_course_calc_progress AS
SELECT
    tc.class_id,
    tc.class_name,
    tc.calc_status,
    tc.grade_year,
    c.course_code,
    c.course_name,
    t.teacher_name,
    cm.major_id,
    tc.term_id,
    (SELECT COUNT(*) FROM student_class sc WHERE sc.class_id = tc.class_id) AS student_count,
    (SELECT COUNT(*) FROM student_assessment_score sas WHERE sas.class_id = tc.class_id) AS score_count
FROM teaching_class tc
JOIN course c ON c.course_id = tc.course_id
JOIN teacher t ON t.id = tc.teacher_id
JOIN course_major cm ON cm.course_id = c.course_id
    AND cm.grade_year = tc.grade_year;

CREATE OR REPLACE VIEW v_major_achievement_dashboard AS
SELECT
    mia.major_id,
    mia.grade_year,
    mia.term_id,
    mia.ip_id,
    ip.ip_code,
    ip.ip_description,
    gr.gr_code,
    gr.gr_description,
    mia.final_achievement,
    CASE WHEN mia.final_achievement >= 0.60 THEN '合格' ELSE '不合格' END AS pass_status
FROM major_indicator_achievement mia
JOIN indicator_point ip ON ip.ip_id = mia.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id;

CREATE OR REPLACE VIEW v_score_drilldown AS
SELECT
    gr.gr_code,
    gr.gr_description AS gr_desc,
    gr.grade_year,
    ip.ip_code,
    ip.ip_description AS ip_desc,
    c.course_code,
    c.course_name,
    co.objective_code,
    co.co_description,
    ap.ap_name,
    ap.full_score,
    sas.actual_score,
    s.student_no,
    s.student_name,
    tc.class_name,
    tc.term_id,
    cis.total_weight AS macro_weight,
    oic.internal_weight AS micro_weight
FROM student_assessment_score sas
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN objective_indicator_contribution oic ON oic.co_id = co.co_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN course_major cm ON cm.course_id = c.course_id
    AND cm.major_id = gr.major_id
    AND cm.grade_year = gr.grade_year
JOIN course_indicator_support cis ON cis.course_id = c.course_id AND cis.ip_id = ip.ip_id
JOIN student s ON s.student_id = sas.student_id;

CREATE OR REPLACE VIEW v_weight_validation AS
SELECT
    ip.ip_id,
    ip.ip_code,
    gr.gr_code,
    gr.grade_year,
    m.major_id,
    m.major_name,
    COUNT(DISTINCT CASE WHEN cm.cm_id IS NOT NULL THEN cis.course_id END) AS support_course_count,
    COALESCE(SUM(CASE WHEN cm.cm_id IS NOT NULL THEN cis.total_weight ELSE 0 END), 0) AS weight_sum,
    CASE
        WHEN ABS(COALESCE(SUM(CASE WHEN cm.cm_id IS NOT NULL THEN cis.total_weight ELSE 0 END), 0) - 1.0) < 0.001
            THEN 'OK'
        ELSE 'FAIL'
    END AS is_valid
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
LEFT JOIN course_major cm ON cm.course_id = cis.course_id
    AND cm.major_id = gr.major_id
    AND cm.grade_year = gr.grade_year
GROUP BY ip.ip_id, ip.ip_code, gr.gr_code, gr.grade_year, m.major_id, m.major_name;



-- ================================================================
--                    测 试 数 据
-- 场景：某理工大学计算机相关专业 2024-2025 学年 OBE 达成度评价
-- ================================================================


-- ================================================================
-- A. 学院（2 条）
-- ================================================================
INSERT INTO college (college_code, college_name) VALUES
('CS', '计算机科学与技术学院'),
('EE', '电子信息工程学院');

-- ================================================================
-- B. 专业（3 条）
-- ================================================================
INSERT INTO major (major_code, major_name, college_id) VALUES
('080901', '计算机科学与技术', 1),
('080902', '软件工程', 1),
('080701', '电子信息工程', 2);

-- ================================================================
-- C. 学期（3 条）
-- ================================================================
INSERT INTO academic_term (term_code, academic_year, semester, start_date, end_date) VALUES
('2024-2025-1', 2024, 1, '2024-09-01', '2025-01-18'),
('2024-2025-2', 2024, 2, '2025-02-24', '2025-07-05'),
('2025-2026-1', 2025, 1, '2025-09-01', '2026-01-17');

-- ================================================================
-- D. 系统用户（10 条，原 8 条不变 + 第三周新增 2 条）
-- ================================================================
INSERT INTO sys_user (username, password, real_name, email, phone, status) VALUES
('admin',          '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '赵管理员', 'admin@university.edu.cn', '13800000001', 1),
('teacher_zhang',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '张教授',   'zhang@university.edu.cn',  '13800000002', 1),
('teacher_li',     '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '李副教授', 'li@university.edu.cn',    '13800000003', 1),
('teacher_wang',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '王讲师',   'wang@university.edu.cn',  '13800000004', 1),
('director_chen',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '陈主任',   'chen@university.edu.cn',  '13800000005', 1),
('academic_wu',    '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '吴老师',   'wu@university.edu.cn',    '13800000006', 1),
('student_zhou',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '周一帆',   'zhou@university.edu.cn',   '13800000007', 1),
('student_chen',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '陈思远',   'chen2@university.edu.cn',  '13800000008', 1),
('teacher_zhao', '$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06', '赵讲师',   'zhao@university.edu.cn',  '13800000009', 1),
('teacher_sun',  '$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06', '孙副教授', 'sun@university.edu.cn',   '13800000010', 1);

-- ================================================================
-- E. 教师（5 条）
-- ================================================================
INSERT INTO teacher (teacher_no, teacher_name, title, major_id, user_id) VALUES
('T2024001', '张教授',   '教授',   1, 2),
('T2024002', '李副教授', '副教授', 1, 3),
('T2024003', '王讲师',   '讲师',   1, 4),
('T2024004', '赵讲师',   '讲师',   1, 9),
('T2024005', '孙副教授', '副教授', 1, 10);

-- ================================================================
-- F. 角色（5 条）
-- ================================================================
INSERT INTO sys_role (role_code, role_name, remark) VALUES
('admin',             '系统管理员',   '系统全局配置、用户账号管理'),
('academic_affairs',  '教务管理员',   '培养方案导入、班级学生管理、报表导出'),
('program_director',  '专业负责人',   '毕业要求维护、支撑矩阵配置、专业级计算'),
('instructor',        '课程主讲教师', '课程大纲编写、考核点设定、成绩录入、课程级计算'),
('student',           '学生',         '查看本人成绩和达成度评价结果');

-- ================================================================
-- G. 用户角色分配
-- ================================================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 4),
(3, 4),
(4, 4),
(5, 3),
(6, 2),
(7, 5),
(8, 5),
(9, 4),
(10, 4);

-- ================================================================
-- H. 权限（15 条）
-- ================================================================
INSERT INTO sys_permission (perm_code, perm_name, module_name) VALUES
('college:manage',    '学院管理',      'system'),
('major:manage',      '专业管理',      'system'),
('user:manage',       '用户管理',      'system'),
('role:assign',       '角色分配',      'system'),
('dict:manage',       '字典管理',      'system'),
('requirement:write', '毕业要求编辑',   'macro'),
('matrix:write',      '支撑矩阵编辑',   'macro'),
('course:import',     '课程导入',      'macro'),
('class:import',      '班级学生导入',   'macro'),
('objective:write',   '课程目标编辑',   'syllabus'),
('weight:write',      '内部权重编辑',   'syllabus'),
('point:write',       '考核点编辑',     'syllabus'),
('score:import',      '成绩导入录入',   'assessment'),
('calc:trigger',      '达成度计算触发', 'assessment'),
('report:export',     '报表导出',      'report');

-- ================================================================
-- I. 角色权限分配
-- ================================================================
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15);
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (2,8),(2,9),(2,15);
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (3,6),(3,7),(3,14),(3,15);
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (4,10),(4,11),(4,12),(4,13),(4,14);

-- ================================================================
-- J. 系统配置（6 条）
-- ================================================================
INSERT INTO system_config (config_key, config_value, config_desc) VALUES
('score_import_max_rows',  '500',  '成绩导入单次最大行数'),
('score_precision',        '2',    '成绩小数保留位数'),
('calc_precision',         '4',    '达成度小数保留位数'),
('calc_threshold_pass',    '0.60', '达成度合格阈值'),
('report_export_timeout',  '120',  '报表导出超时时间（秒）'),
('default_password',       '123456','新用户默认密码');

-- ================================================================
-- K. 毕业要求（计算机科学与技术，6 条）
-- ================================================================
INSERT INTO graduation_requirement (gr_code, gr_description, major_id, grade_year) VALUES
('1', '工程知识：能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题', 1, 2022),
('2', '问题分析：能够应用数学、自然科学和工程科学的基本原理，识别、表达并通过文献研究分析复杂工程问题', 1, 2022),
('3', '设计/开发解决方案：能够设计针对复杂工程问题的解决方案，并能够在设计环节中体现创新意识', 1, 2022),
('4', '研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究', 1, 2022),
('5', '使用现代工具：能够针对复杂工程问题，开发、选择与使用恰当的技术、资源、现代工程工具', 1, 2022),
('6', '工程与社会：能够基于工程相关背景知识进行合理分析，评价专业工程实践和复杂工程问题解决方案对社会的影响', 1, 2022);
-- ================================================================
-- L. 二级指标点（12 条）
-- ================================================================
INSERT INTO indicator_point (ip_code, ip_description, gr_id) VALUES
('1.1', '能将数学和自然科学的基本概念运用于计算机工程问题的建模与求解', 1),
('1.2', '能运用工程基础知识解释计算机系统的设计原理与工作机制', 1),
('2.1', '能识别和判断计算机复杂工程问题的关键环节与技术瓶颈', 2),
('2.2', '能通过查阅文献对计算机复杂工程问题进行深入分析与分解', 2),
('3.1', '能设计满足特定需求的算法、模块或软件系统架构方案', 3),
('3.2', '能够在系统设计中综合考虑安全性、经济性、环境适应性等非技术因素', 3),
('4.1', '能针对计算机复杂工程问题设计有效的实验方案并正确采集数据', 4),
('4.2', '能运用统计学方法对实验数据进行科学分析与解释，得出有效结论', 4),
('5.1', '能熟练使用主流开发工具、调试工具和性能分析工具完成软件开发任务', 5),
('5.2', '能根据具体问题选择并运用适当的仿真软件或云计算平台进行模拟分析', 5),
('6.1', '理解计算机技术发展对社会、法律及伦理的影响，具有社会责任感', 6),
('6.2', '能在工程实践中考虑信息安全、知识产权保护等社会约束因素', 6);

-- ================================================================
-- M. 课程（5 门）
-- ================================================================
INSERT INTO course (course_code, course_name, credit) VALUES
('CS201', '数据结构',         4.0),
('CS301', '操作系统',         3.0),
('CS302', '计算机网络',       3.0),
('SW201', '软件工程',         3.0),
('EE301', '数字电路与逻辑设计', 3.0);

-- ================================================================
-- M2. 课程-专业关联（course_major）
-- ================================================================
INSERT INTO course_major (course_id, major_id, grade_year) VALUES
(1, 1, 2022),
(2, 1, 2022),
(3, 1, 2022),
(4, 2, 2022),
(5, 3, 2022);
-- ================================================================
-- N. 教学班级（3 个）
-- ================================================================
INSERT INTO teaching_class (class_code, class_name, course_id, term_id, teacher_id, grade_year) VALUES
('TC2024CS01', '数据结构2024-2025-1班', 1, 1, 1, 2022),
('TC2024CS02', '操作系统2024-2025-1班', 2, 1, 2, 2022),
('TC2024CS03', '计算机网络2024-2025-1班', 3, 1, 3, 2022);

-- ================================================================
-- O. 学生（10 名，计算机科学与技术 2022 级）
-- ================================================================
INSERT INTO student (student_no, student_name, major_id, enrollment_year, user_id) VALUES
('20220101001', '周一帆', 1, 2022, 7),
('20220101002', '陈思远', 1, 2022, 8),
('20220101003', '林晓彤', 1, 2022, NULL),
('20220101004', '王浩然', 1, 2022, NULL),
('20220101005', '赵雨涵', 1, 2022, NULL),
('20220101006', '刘子轩', 1, 2022, NULL),
('20220101007', '黄诗琪', 1, 2022, NULL),
('20220101008', '杨俊杰', 1, 2022, NULL),
('20220101009', '吴佳怡', 1, 2022, NULL),
('20220101010', '郑明辉', 1, 2022, NULL);

-- ================================================================
-- P. 学生选班
-- ================================================================
INSERT INTO student_class (student_id, class_id) VALUES
(1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),
(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),(9,2),(10,2),
(1,3),(2,3),(3,3),(4,3),(5,3),(6,3);

-- ================================================================
-- Q. 课程目标（每门课 3~4 个）
-- ================================================================
INSERT INTO course_objective (course_id, objective_code, co_description) VALUES
(1, 'CO1', '掌握线性表、栈、队列、串等基本数据结构的逻辑结构与物理实现'),
(1, 'CO2', '掌握树、图等复杂数据结构的定义、存储方式及遍历算法'),
(1, 'CO3', '能运用查找和排序算法解决实际应用问题，并分析算法的时间空间复杂度'),
(1, 'CO4', '能针对具体问题选择恰当的数据结构并编写C++/Java高效实现代码'),
(2, 'CO1', '理解进程、线程的概念及调度算法，掌握并发编程的基本方法'),
(2, 'CO2', '理解内存管理机制，包括分页、分段、虚拟内存及页面置换算法'),
(2, 'CO3', '理解文件系统和I/O子系统的设计原理及磁盘调度策略'),
(3, 'CO1', '掌握TCP/IP协议栈各层功能及常见协议（HTTP/DNS/TCP/IP）'),
(3, 'CO2', '理解路由算法、拥塞控制机制，能进行网络拓扑设计与性能分析'),
(3, 'CO3', '掌握Socket编程并能搭建简单的客户端/服务器网络应用');

-- ================================================================
-- R. 宏观支撑矩阵（课程→指标点）
-- ================================================================
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(1, 1, 0.40),
(1, 5, 0.30),
(1, 9, 0.30),
(2, 2, 0.40),
(2, 3, 0.25),
(2, 6, 0.35),
(3, 2, 0.30),
(3, 4, 0.30),
(3, 10, 0.40);

-- ================================================================
-- S. 内部贡献权重（课程目标→指标点）
-- ================================================================
INSERT INTO objective_indicator_contribution (co_id, ip_id, internal_weight) VALUES
(1, 1, 0.60),
(2, 5, 0.55),
(3, 9, 0.70),
(4, 5, 0.45),
(4, 9, 0.30),
(5, 2, 0.50),
(6, 3, 0.65),
(7, 6, 0.55),
(8, 2, 0.50),
(9, 4, 0.60),
(10, 10, 0.80);

-- ================================================================
-- T. 考核点（19 个）
-- ================================================================
INSERT INTO assessment_point (ap_name, full_score, co_id) VALUES
('期末卷-链表操作题',   15, 1),
('期末卷-栈队列应用题', 10, 1),
('实验-二叉树遍历实现', 20, 2),
('期末卷-图算法题',     15, 2),
('期末卷-排序算法分析', 10, 3),
('实验-查找算法对比',   15, 3),
('课程设计-综合编程',   20, 4);

INSERT INTO assessment_point (ap_name, full_score, co_id) VALUES
('期末卷-进程调度题',   15, 5),
('实验-多线程编程',     20, 5),
('期末卷-内存管理题',   15, 6),
('实验-页面置换模拟',   15, 6),
('期末卷-文件系统题',   10, 7),
('实验-磁盘调度模拟',   10, 7);

INSERT INTO assessment_point (ap_name, full_score, co_id) VALUES
('期末卷-TCP/IP协议题',        15, 8),
('实验-Wireshark抓包分析',     15, 8),
('期末卷-路由算法题',           10, 9),
('实验-网络拓扑设计',           20, 9),
('期末卷-Socket编程题',        10, 10),
('实验-简易聊天室开发',         15, 10);

-- ================================================================
-- U. 学生成绩（132 条，正态分布）
-- ================================================================
INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score) VALUES
(1,1,1,14),(2,1,1,12),(3,1,1,13),(4,1,1,10),(5,1,1,15),(6,1,1,9),(7,1,1,11),(8,1,1,13),
(1,2,1,9),(2,2,1,8),(3,2,1,10),(4,2,1,7),(5,2,1,9),(6,2,1,6),(7,2,1,8),(8,2,1,7),
(1,3,1,19),(2,3,1,16),(3,3,1,18),(4,3,1,14),(5,3,1,20),(6,3,1,13),(7,3,1,15),(8,3,1,17),
(1,4,1,13),(2,4,1,11),(3,4,1,14),(4,4,1,9),(5,4,1,13),(6,4,1,8),(7,4,1,10),(8,4,1,12),
(1,5,1,9),(2,5,1,7),(3,5,1,8),(4,5,1,6),(5,5,1,10),(6,5,1,5),(7,5,1,7),(8,5,1,8),
(1,6,1,14),(2,6,1,12),(3,6,1,13),(4,6,1,10),(5,6,1,15),(6,6,1,9),(7,6,1,11),(8,6,1,13),
(1,7,1,18),(2,7,1,15),(3,7,1,17),(4,7,1,13),(5,7,1,19),(6,7,1,12),(7,7,1,14),(8,7,1,16);

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score) VALUES
(3,8,2,13),(4,8,2,11),(5,8,2,14),(6,8,2,10),(7,8,2,12),(8,8,2,9),(9,8,2,13),(10,8,2,8),
(3,9,2,18),(4,9,2,15),(5,9,2,19),(6,9,2,14),(7,9,2,16),(8,9,2,13),(9,9,2,17),(10,9,2,12),
(3,10,2,12),(4,10,2,10),(5,10,2,13),(6,10,2,9),(7,10,2,11),(8,10,2,8),(9,10,2,12),(10,10,2,7),
(3,11,2,14),(4,11,2,11),(5,11,2,15),(6,11,2,10),(7,11,2,12),(8,11,2,9),(9,11,2,13),(10,11,2,8),
(3,12,2,9),(4,12,2,7),(5,12,2,8),(6,12,2,6),(7,12,2,8),(8,12,2,5),(9,12,2,9),(10,12,2,6),
(3,13,2,8),(4,13,2,7),(5,13,2,9),(6,13,2,6),(7,13,2,7),(8,13,2,5),(9,13,2,8),(10,13,2,6);

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score) VALUES
(1,14,3,14),(2,14,3,11),(3,14,3,13),(4,14,3,10),(5,14,3,15),(6,14,3,9),
(1,15,3,13),(2,15,3,12),(3,15,3,14),(4,15,3,10),(5,15,3,15),(6,15,3,9),
(1,16,3,9),(2,16,3,7),(3,16,3,8),(4,16,3,6),(5,16,3,10),(6,16,3,5),
(1,17,3,18),(2,17,3,15),(3,17,3,17),(4,17,3,13),(5,17,3,19),(6,17,3,12),
(1,18,3,8),(2,18,3,7),(3,18,3,9),(4,18,3,6),(5,18,3,9),(6,18,3,5),
(1,19,3,13),(2,19,3,11),(3,19,3,14),(4,19,3,9),(5,19,3,15),(6,19,3,8);

-- ================================================================
-- V. 课程目标达成度
-- ================================================================
INSERT INTO course_objective_achievement (class_id, co_id, average_achievement) VALUES
(1, 1, 0.82), (1, 2, 0.78), (1, 3, 0.75), (1, 4, 0.80);

INSERT INTO course_objective_achievement (class_id, co_id, average_achievement) VALUES
(2, 5, 0.79), (2, 6, 0.72), (2, 7, 0.74);

INSERT INTO course_objective_achievement (class_id, co_id, average_achievement) VALUES
(3, 8, 0.80), (3, 9, 0.76), (3, 10, 0.78);

-- ================================================================
-- W. 课程级指标点达成度
-- ================================================================
INSERT INTO course_indicator_achievement (class_id, ip_id, achievement, is_locked) VALUES
(1, 1, 0.82, TRUE),
(1, 5, 0.79, TRUE),
(1, 9, 0.80, TRUE),
(2, 2, 0.79, TRUE),
(2, 3, 0.72, TRUE),
(2, 6, 0.74, TRUE),
(3, 2, 0.80, TRUE),
(3, 4, 0.76, TRUE),
(3, 10, 0.78, TRUE);

-- ================================================================
-- X. 专业级指标点达成度（计算机科学与技术 major_id=1, term_id=1）
-- ================================================================
INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement) VALUES
(1, 2022, 1, 1, 0.82),
(1, 2022, 1, 2, 0.80),
(1, 2022, 1, 3, 0.72),
(1, 2022, 1, 4, 0.76),
(1, 2022, 1, 5, 0.79),
(1, 2022, 1, 6, 0.74),
(1, 2022, 1, 9, 0.80),
(1, 2022, 1, 10, 0.78);

-- ================================================================
-- Y. 第三周补充业务数据（week3_init_data.sql）
-- ================================================================
-- C. 新增课程（10门，id 6~15，均归属计算机科学与技术 major_id=1）
-- ================================================================
INSERT INTO course (course_code, course_name, credit) VALUES
('CS202', '离散数学',         3.5),
('CS203', '计算机组成原理',   3.5),
('CS204', '数据库原理',       3.0),
('CS205', '编译原理',         3.0),
('CS206', '算法设计与分析',   3.0),
('CS207', '人工智能导论',     2.5),
('CS208', '计算机图形学',     2.5),
('CS209', '嵌入式系统',       3.0),
('CS210', '软件测试',         2.0),
('CS211', '信息安全导论',     2.0);

INSERT INTO course_major (course_id, major_id, grade_year) VALUES
(6,1,2022),(7,1,2022),(8,1,2022),(9,1,2022),(10,1,2022),
(11,1,2022),(12,1,2022),(13,1,2022),(14,1,2022),(15,1,2022);
-- ================================================================
-- D. 新增教学班级（7个，id 4~10，均为2024-2025-1学期）
-- ================================================================
INSERT INTO teaching_class (class_code, class_name, course_id, term_id, teacher_id, grade_year) VALUES
('TC2024CS04', '离散数学2024-2025-1班',       6,  1, 4, 2022),  -- id=4
('TC2024CS05', '计算机组成原理2024-2025-1班', 7,  1, 5, 2022),  -- id=5
('TC2024CS06', '数据库原理2024-2025-1班',     8,  1, 1, 2022),  -- id=6  张教授
('TC2024CS07', '编译原理2024-2025-1班',       9,  1, 2, 2022),  -- id=7  李副教授
('TC2024CS08', '算法设计与分析2024-2025-1班', 10, 1, 3, 2022),  -- id=8  王讲师
('TC2024CS09', '人工智能导论2024-2025-1班',   11, 1, 4, 2022),  -- id=9  赵讲师
('TC2024CS10', '计算机图形学2024-2025-2班',   12, 2, 5, 2022);  -- id=10 孙副教授(第二学期)

-- ================================================================
-- E. 新增学生（50名，id 11~60，计算机科学与技术 2022级 major_id=1）
-- ================================================================
INSERT INTO student (student_no, student_name, major_id, enrollment_year) VALUES
('20220101011', '张伟', 1, 2022),
('20220101012', '李强', 1, 2022),
('20220101013', '王磊', 1, 2022),
('20220101014', '赵明', 1, 2022),
('20220101015', '刘洋', 1, 2022),
('20220101016', '陈刚', 1, 2022),
('20220101017', '杨帆', 1, 2022),
('20220101018', '黄勇', 1, 2022),
('20220101019', '周杰', 1, 2022),
('20220101020', '吴昊', 1, 2022),
('20220101021', '孙丽', 1, 2022),
('20220101022', '马玲', 1, 2022),
('20220101023', '朱婷', 1, 2022),
('20220101024', '胡敏', 1, 2022),
('20220101025', '林芳', 1, 2022),
('20220101026', '何静', 1, 2022),
('20220101027', '郭娜', 1, 2022),
('20220101028', '高洁', 1, 2022),
('20220101029', '罗琳', 1, 2022),
('20220101030', '梁雪', 1, 2022),
('20220101031', '宋涛', 1, 2022),
('20220101032', '唐磊', 1, 2022),
('20220101033', '韩冰', 1, 2022),
('20220101034', '冯浩', 1, 2022),
('20220101035', '董文', 1, 2022),
('20220101036', '程亮', 1, 2022),
('20220101037', '曹峰', 1, 2022),
('20220101038', '袁博', 1, 2022),
('20220101039', '邓超', 1, 2022),
('20220101040', '许刚', 1, 2022),
('20220101041', '沈璐', 1, 2022),
('20220101042', '彭娟', 1, 2022),
('20220101043', '吕萍', 1, 2022),
('20220101044', '苏艳', 1, 2022),
('20220101045', '蒋颖', 1, 2022),
('20220101046', '蔡宇', 1, 2022),
('20220101047', '贾琪', 1, 2022),
('20220101048', '丁蕾', 1, 2022),
('20220101049', '魏芳', 1, 2022),
('20220101050', '薛敏', 1, 2022),
('20220101051', '叶飞', 1, 2022),
('20220101052', '余波', 1, 2022),
('20220101053', '潘越', 1, 2022),
('20220101054', '戴晴', 1, 2022),
('20220101055', '夏雨', 1, 2022),
('20220101056', '田晓', 1, 2022),
('20220101057', '任远', 1, 2022),
('20220101058', '姜琳', 1, 2022),
('20220101059', '范鑫', 1, 2022),
('20220101060', '方圆', 1, 2022);

-- ================================================================
-- F. 学生-教学班关联（约100条）
-- ================================================================

-- F1. 班级1（数据结构，class_id=1）：原有1~8 + 新增51~60
INSERT INTO student_class (student_id, class_id) VALUES
(51,1),(52,1),(53,1),(54,1),(55,1),(56,1),(57,1),(58,1),(59,1),(60,1);

-- F2. 班级2（操作系统，class_id=2）：原有3~10 + 新增11~20、51~60
INSERT INTO student_class (student_id, class_id) VALUES
(11,2),(12,2),(13,2),(14,2),(15,2),(16,2),(17,2),(18,2),(19,2),(20,2),
(51,2),(52,2),(53,2),(54,2),(55,2),(56,2),(57,2),(58,2),(59,2),(60,2);

-- F3. 班级3（计算机网络，class_id=3）：原有1~6 + 新增21~30
INSERT INTO student_class (student_id, class_id) VALUES
(21,3),(22,3),(23,3),(24,3),(25,3),(26,3),(27,3),(28,3),(29,3),(30,3);

-- F4. 班级4（离散数学，class_id=4）：31~45
INSERT INTO student_class (student_id, class_id) VALUES
(31,4),(32,4),(33,4),(34,4),(35,4),(36,4),(37,4),(38,4),
(39,4),(40,4),(41,4),(42,4),(43,4),(44,4),(45,4);

-- F5. 班级5（计算机组成原理，class_id=5）：41~55
INSERT INTO student_class (student_id, class_id) VALUES
(41,5),(42,5),(43,5),(44,5),(45,5),(46,5),(47,5),(48,5),
(49,5),(50,5),(51,5),(52,5),(53,5),(54,5),(55,5);

-- F6. 班级6（数据库原理，class_id=6）：1~8原有 + 11~20、31~40
INSERT INTO student_class (student_id, class_id) VALUES
(1,6),(2,6),(3,6),(4,6),(5,6),(6,6),(7,6),(8,6),
(11,6),(12,6),(13,6),(14,6),(15,6),(16,6),(17,6),(18,6),(19,6),(20,6),
(31,6),(32,6),(33,6),(34,6),(35,6),(36,6),(37,6),(38,6),(39,6),(40,6);

-- F7. 班级7（编译原理，class_id=7）：21~35
INSERT INTO student_class (student_id, class_id) VALUES
(21,7),(22,7),(23,7),(24,7),(25,7),(26,7),(27,7),(28,7),
(29,7),(30,7),(31,7),(32,7),(33,7),(34,7),(35,7);

-- F8. 班级8（算法设计，class_id=8）：36~50
INSERT INTO student_class (student_id, class_id) VALUES
(36,8),(37,8),(38,8),(39,8),(40,8),(41,8),(42,8),(43,8),
(44,8),(45,8),(46,8),(47,8),(48,8),(49,8),(50,8);

-- F9. 班级9（人工智能导论，class_id=9）：1~8原有 + 51~60
INSERT INTO student_class (student_id, class_id) VALUES
(1,9),(2,9),(3,9),(4,9),(5,9),(6,9),(7,9),(8,9),
(51,9),(52,9),(53,9),(54,9),(55,9),(56,9),(57,9),(58,9),(59,9),(60,9);

-- F10. 班级10（计算机图形学，class_id=10）：11~15、31~35、46~55
INSERT INTO student_class (student_id, class_id) VALUES
(11,10),(12,10),(13,10),(14,10),(15,10),
(31,10),(32,10),(33,10),(34,10),(35,10),
(46,10),(47,10),(48,10),(49,10),(50,10),
(51,10),(52,10),(53,10),(54,10),(55,10);

-- ================================================================
-- G. 宏观支撑矩阵（21条新增，原有9条，共30条）
-- ================================================================
-- 设计目标：
--   IP 1(1.1): 原有0.40 + 新增0.30+0.30 = 1.00 ✓ 校验通过
--   IP 2(1.2): 原有0.40+0.30 + 新增0.15        = 0.85 ✗ 校验失败
--   IP 4(2.2): 原有0.30 + 新增0.20              = 0.50 ✗ 校验失败
--   IP 5(3.1): 原有0.30 + 新增0.25+0.25+0.20    = 1.00 ✓ 校验通过
--   IP 9(5.1): 原有0.30 + 新增0.35+0.35          = 1.00 ✓ 校验通过
-- ================================================================

-- G1. IP 1(1.1) 补至 1.00：原有(1,1,0.40)，新增两门课各0.30
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(6, 1, 0.30),   -- 离散数学 → 1.1
(7, 1, 0.30);   -- 计算机组成原理 → 1.1

-- G2. IP 2(1.2) 补至 0.85（故意不满足1.0）：原有(2,2,0.40)+(3,2,0.30)=0.70
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(8, 2, 0.15);   -- 数据库原理 → 1.2

-- G3. IP 3(2.1)：新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(8,  3, 0.30),  -- 数据库原理 → 2.1
(10, 3, 0.20);  -- 算法设计与分析 → 2.1

-- G4. IP 4(2.2) 补至 0.50（故意不满足1.0）：原有(3,4,0.30)
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(9, 4, 0.20);   -- 编译原理 → 2.2

-- G5. IP 5(3.1) 补至 1.00：原有(1,5,0.30)
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(6, 5, 0.25),   -- 离散数学 → 3.1
(8, 5, 0.25),   -- 数据库原理 → 3.1
(9, 5, 0.20);   -- 编译原理 → 3.1

-- G6. IP 6(3.2)：原有(2,6,0.35)，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(7, 6, 0.30);   -- 计算机组成原理 → 3.2

-- G7. IP 7(4.1)：无原有，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(6,  7, 0.40),  -- 离散数学 → 4.1
(10, 7, 0.30);  -- 算法设计与分析 → 4.1

-- G8. IP 8(4.2)：无原有，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(7,  8, 0.35),  -- 计算机组成原理 → 4.2
(11, 8, 0.30);  -- 人工智能导论 → 4.2

-- G9. IP 9(5.1) 补至 1.00：原有(1,9,0.30)
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(10, 9, 0.35),  -- 算法设计与分析 → 5.1
(11, 9, 0.35);  -- 人工智能导论 → 5.1

-- G10. IP 10(5.2)：原有(3,10,0.40)，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(8, 10, 0.30);  -- 数据库原理 → 5.2

-- G11. IP 11(6.1)：无原有，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(9,  11, 0.25), -- 编译原理 → 6.1
(11, 11, 0.35); -- 人工智能导论 → 6.1

-- G12. IP 12(6.2)：无原有，新增覆盖
INSERT INTO course_indicator_support (course_id, ip_id, total_weight) VALUES
(10, 12, 0.30), -- 算法设计与分析 → 6.2
(12, 12, 0.30); -- 计算机图形学 → 6.2

-- ================================================================
-- H. 验证：查询 v_weight_validation 查看校验结果
--    预期：IP 1/5/9 显示 OK，IP 2/4 显示 FAIL，其他 FAIL
-- ================================================================
-- SELECT * FROM v_weight_validation WHERE major_id = 1 ORDER BY ip_id;

-- ================================================================
-- 补充完成
-- 总计：+2教师 / +10课程 / +7教学班 / +50学生 / +100条选班 / +21条支撑矩阵
-- ================================================================

-- ================================================================
-- Z. 第四周全链路 E2E 测试数据（upgrade_week4_e2e_test_data.sql）
-- ================================================================
-- ================================================================
-- 0. 临时模板表
-- ================================================================
DROP TEMPORARY TABLE IF EXISTS tmp_e2e_context;
CREATE TEMPORARY TABLE tmp_e2e_context (
    context_code VARCHAR(32) PRIMARY KEY,
    major_code VARCHAR(20) NOT NULL,
    grade_year INT NOT NULL,
    scenario_code VARCHAR(32) NOT NULL
);

INSERT INTO tmp_e2e_context (context_code, major_code, grade_year, scenario_code) VALUES
('CS2022_MAIN',    '080901', 2022, 'main'),
('CS2023_BLOCK',   '080901', 2023, 'blocked'),
('SE2022_UNLOCK',  '080902', 2022, 'unlock'),
('SE2023_BLOCK',   '080902', 2023, 'blocked');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_gr_template;
CREATE TEMPORARY TABLE tmp_e2e_gr_template (
    gr_code VARCHAR(10) PRIMARY KEY,
    gr_description VARCHAR(255) NOT NULL
);

INSERT INTO tmp_e2e_gr_template (gr_code, gr_description) VALUES
('EGR01', 'E2E-毕业要求1：能够运用数学、自然科学和工程基础知识分析复杂工程问题'),
('EGR02', 'E2E-毕业要求2：能够识别、表达并分析复杂工程问题的关键环节'),
('EGR03', 'E2E-毕业要求3：能够设计满足需求的系统、模块与解决方案'),
('EGR04', 'E2E-毕业要求4：能够基于科学原理设计实验并分析数据'),
('EGR05', 'E2E-毕业要求5：能够选择并使用现代工程工具解决问题'),
('EGR06', 'E2E-毕业要求6：能够理解工程实践中的社会、法律与伦理责任'),
('EGR07', 'E2E-毕业要求7：能够在团队协作中承担角色并完成项目任务'),
('EGR08', 'E2E-毕业要求8：能够通过文献、报告和交流持续改进工程实践');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_ip_template;
CREATE TEMPORARY TABLE tmp_e2e_ip_template (
    gr_code VARCHAR(10) NOT NULL,
    ip_code VARCHAR(10) NOT NULL,
    ip_description VARCHAR(255) NOT NULL,
    PRIMARY KEY (gr_code, ip_code)
);

INSERT INTO tmp_e2e_ip_template (gr_code, ip_code, ip_description) VALUES
('EGR01', 'E1-1', 'E2E-能够把数学与自然科学知识用于问题建模'),
('EGR01', 'E1-2', 'E2E-能够把工程基础知识用于系统机理解释'),
('EGR02', 'E2-1', 'E2E-能够识别复杂工程问题的关键环节'),
('EGR02', 'E2-2', 'E2E-能够通过资料与事实分析复杂工程问题'),
('EGR03', 'E3-1', 'E2E-能够设计满足需求的算法或模块方案'),
('EGR03', 'E3-2', 'E2E-能够在设计中兼顾约束条件与可实施性'),
('EGR04', 'E4-1', 'E2E-能够设计实验并规范采集过程数据'),
('EGR04', 'E4-2', 'E2E-能够分析实验结果并解释结论'),
('EGR05', 'E5-1', 'E2E-能够使用开发工具完成实现与调试'),
('EGR05', 'E5-2', 'E2E-能够选择合适平台或工具开展验证'),
('EGR06', 'E6-1', 'E2E-能够理解信息安全、规范与社会责任'),
('EGR06', 'E6-2', 'E2E-能够在工程实践中考虑法律伦理约束'),
('EGR07', 'E7-1', 'E2E-能够在团队中承担职责并协同推进任务'),
('EGR07', 'E7-2', 'E2E-能够基于目标进行计划、沟通与交付'),
('EGR08', 'E8-1', 'E2E-能够形成结构化报告并清晰表达技术方案'),
('EGR08', 'E8-2', 'E2E-能够基于反馈和证据持续优化工程方案');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_course_def;
CREATE TEMPORARY TABLE tmp_e2e_course_def (
    course_code VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credit FLOAT NOT NULL,
    major_code VARCHAR(20) NOT NULL
);

INSERT INTO tmp_e2e_course_def (course_code, course_name, credit, major_code) VALUES
('E2E-CS-DS',   'E2E-数据结构',       3.0, '080901'),
('E2E-CS-OS',   'E2E-操作系统',       3.0, '080901'),
('E2E-CS-NET',  'E2E-计算机网络',     3.0, '080901'),
('E2E-CS-DB',   'E2E-数据库原理',     3.0, '080901'),
('E2E-CS-SE',   'E2E-软件工程基础',   2.0, '080901'),
('E2E-CS-PRA',  'E2E-工程实践',       2.0, '080901'),
('E2E-SE-REQ',  'E2E-需求分析',       3.0, '080902'),
('E2E-SE-DES',  'E2E-软件设计',       3.0, '080902'),
('E2E-SE-TEST', 'E2E-软件测试',       3.0, '080902'),
('E2E-SE-PM',   'E2E-项目管理',       2.0, '080902');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_objective_template;
CREATE TEMPORARY TABLE tmp_e2e_objective_template (
    objective_code VARCHAR(16) PRIMARY KEY,
    co_description VARCHAR(255) NOT NULL
);

INSERT INTO tmp_e2e_objective_template (objective_code, co_description) VALUES
('CO1', 'E2E-能够理解课程核心概念与基础原理'),
('CO2', 'E2E-能够将课程方法用于分析、设计或验证'),
('CO3', 'E2E-能够结合工程场景完成综合实现与改进');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_ap_template;
CREATE TEMPORARY TABLE tmp_e2e_ap_template (
    ap_name VARCHAR(50) PRIMARY KEY,
    full_score FLOAT NOT NULL,
    objective_code VARCHAR(16) NOT NULL
);

INSERT INTO tmp_e2e_ap_template (ap_name, full_score, objective_code) VALUES
('平时作业', 20, 'CO1'),
('阶段测验', 20, 'CO1'),
('实验报告', 30, 'CO2'),
('期末考核', 30, 'CO3');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_support_template;
CREATE TEMPORARY TABLE tmp_e2e_support_template (
    major_code VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    gr_code VARCHAR(10) NOT NULL,
    ip_code VARCHAR(10) NOT NULL,
    co_code_a VARCHAR(16) NOT NULL,
    weight_a FLOAT NOT NULL,
    co_code_b VARCHAR(16) NOT NULL,
    weight_b FLOAT NOT NULL,
    PRIMARY KEY (major_code, course_code, gr_code, ip_code)
);

INSERT INTO tmp_e2e_support_template (major_code, course_code, gr_code, ip_code, co_code_a, weight_a, co_code_b, weight_b) VALUES
('080901', 'E2E-CS-DS',  'EGR01', 'E1-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-DS',  'EGR01', 'E1-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-DS',  'EGR02', 'E2-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-OS',  'EGR02', 'E2-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-OS',  'EGR03', 'E3-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-OS',  'EGR03', 'E3-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-NET', 'EGR04', 'E4-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-NET', 'EGR04', 'E4-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-NET', 'EGR05', 'E5-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-DB',  'EGR05', 'E5-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-DB',  'EGR06', 'E6-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-DB',  'EGR06', 'E6-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-SE',  'EGR07', 'E7-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-SE',  'EGR07', 'E7-2', 'CO2', 0.5, 'CO3', 0.5),
('080901', 'E2E-CS-PRA', 'EGR08', 'E8-1', 'CO1', 0.6, 'CO2', 0.4),
('080901', 'E2E-CS-PRA', 'EGR08', 'E8-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-REQ',  'EGR01', 'E1-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-REQ',  'EGR01', 'E1-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-REQ',  'EGR02', 'E2-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-REQ',  'EGR02', 'E2-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-DES',  'EGR03', 'E3-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-DES',  'EGR03', 'E3-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-DES',  'EGR04', 'E4-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-DES',  'EGR04', 'E4-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-TEST', 'EGR05', 'E5-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-TEST', 'EGR05', 'E5-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-TEST', 'EGR06', 'E6-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-TEST', 'EGR06', 'E6-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-PM',   'EGR07', 'E7-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-PM',   'EGR07', 'E7-2', 'CO2', 0.5, 'CO3', 0.5),
('080902', 'E2E-SE-PM',   'EGR08', 'E8-1', 'CO1', 0.6, 'CO2', 0.4),
('080902', 'E2E-SE-PM',   'EGR08', 'E8-2', 'CO2', 0.5, 'CO3', 0.5);

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_class_plan;
CREATE TEMPORARY TABLE tmp_e2e_class_plan (
    class_code VARCHAR(32) PRIMARY KEY,
    context_code VARCHAR(32) NOT NULL,
    major_code VARCHAR(20) NOT NULL,
    grade_year INT NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    teacher_no VARCHAR(32) NOT NULL,
    calc_status VARCHAR(20) NOT NULL,
    class_name VARCHAR(100) NOT NULL
);

INSERT INTO tmp_e2e_class_plan (class_code, context_code, major_code, grade_year, course_code, teacher_no, calc_status, class_name) VALUES
('E2E-CS22-01', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-DS',   'T2024001', 'locked',         'E2E-计科2022-数据结构班'),
('E2E-CS22-02', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-OS',   'T2024001', 'locked',         'E2E-计科2022-操作系统班'),
('E2E-CS22-03', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-NET',  'T2024001', 'locked',         'E2E-计科2022-计算机网络班'),
('E2E-CS22-04', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-DB',   'T2024001', 'locked',         'E2E-计科2022-数据库原理班'),
('E2E-CS22-05', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-SE',   'T2024001', 'locked',         'E2E-计科2022-软件工程基础班'),
('E2E-CS22-06', 'CS2022_MAIN',   '080901', 2022, 'E2E-CS-PRA',  'T2024001', 'unsubmitted',    'E2E-计科2022-工程实践班'),
('E2E-CS23-01', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-DS',   'T2024001', 'locked',         'E2E-计科2023-数据结构班'),
('E2E-CS23-02', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-OS',   'T2024001', 'locked',         'E2E-计科2023-操作系统班'),
('E2E-CS23-03', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-NET',  'T2024001', 'locked',         'E2E-计科2023-计算机网络班'),
('E2E-CS23-04', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-DB',   'T2024001', 'locked',         'E2E-计科2023-数据库原理班'),
('E2E-CS23-05', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-SE',   'T2024001', 'locked',         'E2E-计科2023-软件工程基础班'),
('E2E-CS23-06', 'CS2023_BLOCK',  '080901', 2023, 'E2E-CS-PRA',  'T2024001', 'score_imported', 'E2E-计科2023-工程实践班'),
('E2E-SE22-01', 'SE2022_UNLOCK', '080902', 2022, 'E2E-SE-REQ',  'T2024002', 'locked',         'E2E-软工2022-需求分析班'),
('E2E-SE22-02', 'SE2022_UNLOCK', '080902', 2022, 'E2E-SE-DES',  'T2024002', 'locked',         'E2E-软工2022-软件设计班'),
('E2E-SE22-03', 'SE2022_UNLOCK', '080902', 2022, 'E2E-SE-TEST', 'T2024003', 'locked',         'E2E-软工2022-软件测试班'),
('E2E-SE22-04', 'SE2022_UNLOCK', '080902', 2022, 'E2E-SE-PM',   'T2024003', 'locked',         'E2E-软工2022-项目管理班'),
('E2E-SE23-01', 'SE2023_BLOCK',  '080902', 2023, 'E2E-SE-REQ',  'T2024002', 'locked',         'E2E-软工2023-需求分析班'),
('E2E-SE23-02', 'SE2023_BLOCK',  '080902', 2023, 'E2E-SE-DES',  'T2024002', 'locked',         'E2E-软工2023-软件设计班'),
('E2E-SE23-03', 'SE2023_BLOCK',  '080902', 2023, 'E2E-SE-TEST', 'T2024003', 'locked',         'E2E-软工2023-软件测试班'),
('E2E-SE23-04', 'SE2023_BLOCK',  '080902', 2023, 'E2E-SE-PM',   'T2024003', 'unsubmitted',    'E2E-软工2023-项目管理班');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_num10;
CREATE TEMPORARY TABLE tmp_e2e_num10 (
    seq INT PRIMARY KEY,
    score_factor FLOAT NOT NULL
);

INSERT INTO tmp_e2e_num10 (seq, score_factor) VALUES
(1, 0.60),
(2, 0.65),
(3, 0.70),
(4, 0.75),
(5, 0.80),
(6, 0.66),
(7, 0.72),
(8, 0.78),
(9, 0.84),
(10, 0.88);

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_student_plan;
CREATE TEMPORARY TABLE tmp_e2e_student_plan AS
SELECT
    ctx.context_code,
    ctx.major_code,
    ctx.grade_year,
    CONCAT(ctx.grade_year, RIGHT(ctx.major_code, 3), LPAD(n.seq, 3, '0')) AS student_no,
    CASE
        WHEN ctx.major_code = '080901' THEN CONCAT('计科', RIGHT(ctx.grade_year, 2), '测', LPAD(n.seq, 2, '0'))
        ELSE CONCAT('软工', RIGHT(ctx.grade_year, 2), '测', LPAD(n.seq, 2, '0'))
    END AS student_name,
    n.score_factor
FROM tmp_e2e_context ctx
JOIN tmp_e2e_num10 n ON 1 = 1;

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_support_plan;
CREATE TEMPORARY TABLE tmp_e2e_support_plan AS
SELECT
    ctx.context_code,
    ctx.major_code,
    ctx.grade_year,
    tpl.course_code,
    tpl.gr_code,
    tpl.ip_code,
    tpl.co_code_a,
    tpl.weight_a,
    tpl.co_code_b,
    tpl.weight_b
FROM tmp_e2e_context ctx
JOIN tmp_e2e_support_template tpl
    ON tpl.major_code = ctx.major_code;

-- ================================================================
-- 1. 基础主数据确保存在
-- ================================================================
INSERT INTO college (college_code, college_name, status)
SELECT 'CS', '计算机科学与技术学院', 1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM college WHERE college_code = 'CS'
);

SET @college_cs := (
    SELECT college_id FROM college WHERE college_code = 'CS' LIMIT 1
);

INSERT INTO major (major_code, major_name, college_id, status)
SELECT '080901', '计算机科学与技术', @college_cs, 1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM major WHERE major_code = '080901'
);

INSERT INTO major (major_code, major_name, college_id, status)
SELECT '080902', '软件工程', @college_cs, 1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM major WHERE major_code = '080902'
);

UPDATE major
SET college_id = @college_cs, status = 1
WHERE major_code IN ('080901', '080902');

INSERT INTO academic_term (term_code, academic_year, semester, start_date, end_date, status)
SELECT '2025-2026-1', 2025, 1, '2025-09-01', '2026-01-16', 1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM academic_term WHERE term_code = '2025-2026-1'
);

UPDATE academic_term
SET academic_year = 2025,
    semester = 1,
    start_date = '2025-09-01',
    end_date = '2026-01-16'
WHERE term_code = '2025-2026-1';

SET @major_cs := (SELECT major_id FROM major WHERE major_code = '080901' LIMIT 1);
SET @major_se := (SELECT major_id FROM major WHERE major_code = '080902' LIMIT 1);
SET @term_main := (SELECT term_id FROM academic_term WHERE term_code = '2025-2026-1' LIMIT 1);

-- 复用现有教师映射，不新增账号，不改 user_id。
UPDATE teacher
SET major_id = @major_cs, status = 1
WHERE teacher_no = 'T2024001';

UPDATE teacher
SET major_id = @major_se, status = 1
WHERE teacher_no IN ('T2024002', 'T2024003');

-- ================================================================
-- 2. 数据清理段（仅清理本次 E2E 范围）
-- ================================================================
DELETE temp
FROM temp_import_staging temp
WHERE temp.batch_id LIKE 'E2E-W4-%';

DELETE ual
FROM unlock_audit_log ual
JOIN teaching_class tc ON tc.class_id = ual.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE soa
FROM student_objective_achievement soa
JOIN teaching_class tc ON tc.class_id = soa.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE coa
FROM course_objective_achievement coa
JOIN teaching_class tc ON tc.class_id = coa.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE cia
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE sas
FROM student_assessment_score sas
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE mia
FROM major_indicator_achievement mia
JOIN major m ON m.major_id = mia.major_id
JOIN indicator_point ip ON ip.ip_id = mia.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN tmp_e2e_context ctx ON ctx.major_code = m.major_code AND ctx.grade_year = mia.grade_year
JOIN tmp_e2e_gr_template grt ON grt.gr_code = gr.gr_code
JOIN tmp_e2e_ip_template ipt ON ipt.gr_code = gr.gr_code AND ipt.ip_code = ip.ip_code;

DELETE sc
FROM student_class sc
JOIN student s ON s.student_id = sc.student_id
JOIN tmp_e2e_student_plan sp ON sp.student_no = s.student_no;

DELETE tc
FROM teaching_class tc
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code;

DELETE oic
FROM objective_indicator_contribution oic
JOIN course_objective co ON co.co_id = oic.co_id
JOIN course c ON c.course_id = co.course_id
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

DELETE ap
FROM assessment_point ap
JOIN course_objective co ON co.co_id = ap.co_id
JOIN course c ON c.course_id = co.course_id
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

DELETE co
FROM course_objective co
JOIN course c ON c.course_id = co.course_id
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

DELETE cis
FROM course_indicator_support cis
JOIN course c ON c.course_id = cis.course_id
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

DELETE ip
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
JOIN tmp_e2e_context ctx ON ctx.major_code = m.major_code AND ctx.grade_year = gr.grade_year
JOIN tmp_e2e_ip_template ipt ON ipt.gr_code = gr.gr_code AND ipt.ip_code = ip.ip_code;

DELETE gr
FROM graduation_requirement gr
JOIN major m ON m.major_id = gr.major_id
JOIN tmp_e2e_context ctx ON ctx.major_code = m.major_code AND ctx.grade_year = gr.grade_year
JOIN tmp_e2e_gr_template grt ON grt.gr_code = gr.gr_code;

DELETE cm
FROM course_major cm
JOIN course c ON c.course_id = cm.course_id
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

DELETE s
FROM student s
JOIN tmp_e2e_student_plan sp ON sp.student_no = s.student_no;

DELETE c
FROM course c
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code;

-- ================================================================
-- 3. 基础业务数据整理段
-- ================================================================
INSERT INTO course (course_code, course_name, credit, status)
SELECT course_code, course_name, credit, 1
FROM tmp_e2e_course_def;

INSERT INTO course_major (course_id, major_id, grade_year)
SELECT
    c.course_id,
    m.major_id,
    ctx.grade_year
FROM tmp_e2e_course_def cd
JOIN course c ON c.course_code = cd.course_code
JOIN major m ON m.major_code = cd.major_code
JOIN tmp_e2e_context ctx ON ctx.major_code = cd.major_code;

INSERT INTO graduation_requirement (gr_code, gr_description, major_id, grade_year, status)
SELECT
    grt.gr_code,
    CONCAT(grt.gr_description, '（', m.major_name, '-', ctx.grade_year, '级）'),
    m.major_id,
    ctx.grade_year,
    1
FROM tmp_e2e_context ctx
JOIN major m ON m.major_code = ctx.major_code
JOIN tmp_e2e_gr_template grt ON 1 = 1;

INSERT INTO indicator_point (ip_code, ip_description, gr_id, status)
SELECT
    ipt.ip_code,
    CONCAT(ipt.ip_description, '（', m.major_name, '-', ctx.grade_year, '级）'),
    gr.gr_id,
    1
FROM tmp_e2e_context ctx
JOIN major m ON m.major_code = ctx.major_code
JOIN graduation_requirement gr
    ON gr.major_id = m.major_id
   AND gr.grade_year = ctx.grade_year
JOIN tmp_e2e_ip_template ipt
    ON ipt.gr_code = gr.gr_code;

INSERT INTO course_indicator_support (course_id, ip_id, total_weight)
SELECT
    c.course_id,
    ip.ip_id,
    1.0
FROM tmp_e2e_support_plan sp
JOIN course c ON c.course_code = sp.course_code
JOIN major m ON m.major_code = sp.major_code
JOIN graduation_requirement gr
    ON gr.major_id = m.major_id
   AND gr.grade_year = sp.grade_year
   AND gr.gr_code = sp.gr_code
JOIN indicator_point ip
    ON ip.gr_id = gr.gr_id
   AND ip.ip_code = sp.ip_code;

-- ================================================================
-- 4. 模块 B 数据整理段
-- ================================================================
INSERT INTO course_objective (objective_code, co_description, course_id)
SELECT
    ot.objective_code,
    CONCAT(ot.co_description, '（', c.course_name, '）'),
    c.course_id
FROM course c
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code
JOIN tmp_e2e_objective_template ot ON 1 = 1;

INSERT INTO assessment_point (ap_name, full_score, co_id)
SELECT
    CONCAT(apt.ap_name, '（', c.course_name, '）'),
    apt.full_score,
    co.co_id
FROM course c
JOIN tmp_e2e_course_def cd ON cd.course_code = c.course_code
JOIN course_objective co ON co.course_id = c.course_id
JOIN tmp_e2e_ap_template apt ON apt.objective_code = co.objective_code;

INSERT INTO objective_indicator_contribution (co_id, ip_id, internal_weight)
SELECT
    co.co_id,
    ip.ip_id,
    CASE WHEN map.seq_no = 1 THEN sp.weight_a ELSE sp.weight_b END AS internal_weight
FROM tmp_e2e_support_plan sp
JOIN course c ON c.course_code = sp.course_code
JOIN major m ON m.major_code = sp.major_code
JOIN graduation_requirement gr
    ON gr.major_id = m.major_id
   AND gr.grade_year = sp.grade_year
   AND gr.gr_code = sp.gr_code
JOIN indicator_point ip
    ON ip.gr_id = gr.gr_id
   AND ip.ip_code = sp.ip_code
JOIN (
    SELECT 1 AS seq_no
    UNION ALL
    SELECT 2 AS seq_no
) map ON 1 = 1
JOIN course_objective co
    ON co.course_id = c.course_id
   AND co.objective_code = CASE WHEN map.seq_no = 1 THEN sp.co_code_a ELSE sp.co_code_b END;

-- ================================================================
-- 5. 模块 C 数据整理段
-- ================================================================
INSERT INTO student (student_no, student_name, major_id, enrollment_year, status)
SELECT
    sp.student_no,
    sp.student_name,
    m.major_id,
    sp.grade_year,
    1
FROM tmp_e2e_student_plan sp
JOIN major m ON m.major_code = sp.major_code;

INSERT INTO teaching_class (class_code, class_name, course_id, term_id, teacher_id, grade_year, calc_status)
SELECT
    cp.class_code,
    cp.class_name,
    c.course_id,
    @term_main,
    t.id,
    cp.grade_year,
    cp.calc_status
FROM tmp_e2e_class_plan cp
JOIN course c ON c.course_code = cp.course_code
JOIN teacher t ON t.teacher_no = cp.teacher_no;

INSERT INTO student_class (student_id, class_id)
SELECT
    s.student_id,
    tc.class_id
FROM tmp_e2e_student_plan sp
JOIN student s ON s.student_no = sp.student_no
JOIN tmp_e2e_class_plan cp
    ON cp.major_code = sp.major_code
   AND cp.grade_year = sp.grade_year
JOIN teaching_class tc ON tc.class_code = cp.class_code;

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)
SELECT
    s.student_id,
    ap.ap_id,
    tc.class_id,
    ROUND(ap.full_score * sp.score_factor, 2) AS actual_score
FROM tmp_e2e_student_plan sp
JOIN student s ON s.student_no = sp.student_no
JOIN tmp_e2e_class_plan cp
    ON cp.major_code = sp.major_code
   AND cp.grade_year = sp.grade_year
   AND cp.calc_status IN ('locked', 'score_imported')
JOIN teaching_class tc ON tc.class_code = cp.class_code
JOIN course c ON c.course_id = tc.course_id
JOIN course_objective co ON co.course_id = c.course_id
JOIN assessment_point ap ON ap.co_id = co.co_id;

-- ================================================================
-- 6. 结果层整理段
-- 仅为 locked 教学班生成课程级结果；
-- 仅为 SE2022_UNLOCK 预置专业级结果。
-- ================================================================
INSERT INTO student_objective_achievement (student_id, class_id, co_id, achievement)
SELECT
    sas.student_id,
    sas.class_id,
    ap.co_id,
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS achievement
FROM student_assessment_score sas
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code
WHERE tc.calc_status = 'locked'
GROUP BY sas.student_id, sas.class_id, ap.co_id;

INSERT INTO course_objective_achievement (class_id, co_id, average_achievement)
SELECT
    soa.class_id,
    soa.co_id,
    ROUND(AVG(soa.achievement), 4) AS average_achievement
FROM student_objective_achievement soa
GROUP BY soa.class_id, soa.co_id;

INSERT INTO course_indicator_achievement (class_id, ip_id, achievement, is_locked)
SELECT
    coa.class_id,
    oic.ip_id,
    ROUND(SUM(coa.average_achievement * oic.internal_weight), 4) AS achievement,
    TRUE AS is_locked
FROM course_objective_achievement coa
JOIN teaching_class tc ON tc.class_id = coa.class_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code
JOIN course_objective co
    ON co.co_id = coa.co_id
   AND co.course_id = tc.course_id
JOIN objective_indicator_contribution oic ON oic.co_id = coa.co_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN course_major cm
    ON cm.course_id = tc.course_id
   AND cm.major_id = gr.major_id
   AND cm.grade_year = gr.grade_year
WHERE tc.calc_status = 'locked'
  AND tc.grade_year = gr.grade_year
GROUP BY coa.class_id, oic.ip_id;

INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement)
SELECT
    @major_se AS major_id,
    2022 AS grade_year,
    @term_main AS term_id,
    cia.ip_id,
    ROUND(SUM(cia.achievement * cis.total_weight), 4) AS final_achievement
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course_indicator_support cis
    ON cis.course_id = tc.course_id
   AND cis.ip_id = cia.ip_id
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code
WHERE cp.context_code = 'SE2022_UNLOCK'
  AND tc.calc_status = 'locked'
GROUP BY cia.ip_id;

INSERT INTO unlock_audit_log (class_id, request_by, approved_by, reason)
SELECT
    tc.class_id,
    t.id,
    0,
    'E2E-预置解锁申请：用于验证专业负责人/教务审批解锁后课程级与专业级结果回退'
FROM teaching_class tc
JOIN tmp_e2e_class_plan cp ON cp.class_code = tc.class_code
JOIN teacher t ON t.teacher_no = 'T2024002'
WHERE cp.class_code = 'E2E-SE22-01';

-- ================================================================
-- 7. 末尾验证查询段注释
-- 执行后建议依次运行以下查询确认数据到位。
-- ================================================================

-- 7.1 账号体系未改动（只读核对）
-- SELECT COUNT(*) FROM sys_user;
-- SELECT COUNT(*) FROM sys_role;
-- SELECT COUNT(*) FROM sys_permission;
-- SELECT COUNT(*) FROM sys_role_permission;
-- SELECT COUNT(*) FROM sys_user_role;

-- 7.2 专业与年级覆盖
-- SELECT m.major_code, m.major_name, gr.grade_year,
--        COUNT(DISTINCT gr.gr_id) AS gr_count,
--        COUNT(DISTINCT ip.ip_id) AS ip_count
-- FROM graduation_requirement gr
-- JOIN major m ON m.major_id = gr.major_id
-- LEFT JOIN indicator_point ip ON ip.gr_id = gr.gr_id
-- WHERE gr.gr_code LIKE 'EGR%'
-- GROUP BY m.major_code, m.major_name, gr.grade_year
-- ORDER BY m.major_code, gr.grade_year;

-- 7.3 宏观支撑矩阵配平
-- SELECT m.major_code, gr.grade_year, gr.gr_code, ip.ip_code,
--        COUNT(*) AS support_course_count,
--        ROUND(SUM(cis.total_weight), 4) AS weight_sum
-- FROM course_indicator_support cis
-- JOIN indicator_point ip ON ip.ip_id = cis.ip_id
-- JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
-- JOIN major m ON m.major_id = gr.major_id
-- WHERE gr.gr_code LIKE 'EGR%'
-- GROUP BY m.major_code, gr.grade_year, gr.gr_code, ip.ip_code
-- ORDER BY m.major_code, gr.grade_year, gr.gr_code, ip.ip_code;

-- 7.4 微观权重配平
-- SELECT c.course_code, gr.grade_year, ip.ip_code,
--        ROUND(SUM(oic.internal_weight), 4) AS weight_sum
-- FROM objective_indicator_contribution oic
-- JOIN course_objective co ON co.co_id = oic.co_id
-- JOIN course c ON c.course_id = co.course_id
-- JOIN indicator_point ip ON ip.ip_id = oic.ip_id
-- JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
-- WHERE c.course_code LIKE 'E2E-%'
-- GROUP BY c.course_code, gr.grade_year, ip.ip_code
-- ORDER BY c.course_code, gr.grade_year, ip.ip_code;

-- 7.5 教学班状态分布
-- SELECT class_code, class_name, grade_year, calc_status
-- FROM teaching_class
-- WHERE class_code LIKE 'E2E-%'
-- ORDER BY class_code;

-- 7.6 成绩完整性
-- SELECT tc.class_code, tc.calc_status,
--        COUNT(DISTINCT sc.student_id) AS student_count,
--        COUNT(DISTINCT ap.ap_id) AS assessment_count,
--        COUNT(sas.sas_id) AS score_count
-- FROM teaching_class tc
-- JOIN student_class sc ON sc.class_id = tc.class_id
-- JOIN course_objective co ON co.course_id = tc.course_id
-- JOIN assessment_point ap ON ap.co_id = co.co_id
-- LEFT JOIN student_assessment_score sas
--        ON sas.class_id = tc.class_id
--       AND sas.student_id = sc.student_id
--       AND sas.ap_id = ap.ap_id
-- WHERE tc.class_code LIKE 'E2E-%'
-- GROUP BY tc.class_code, tc.calc_status
-- ORDER BY tc.class_code;

-- 7.7 课程级结果可查
-- SELECT tc.class_code,
--        COUNT(DISTINCT soa.soa_id) AS soa_count,
--        COUNT(DISTINCT coa.coa_id) AS coa_count,
--        COUNT(DISTINCT cia.cia_id) AS cia_count
-- FROM teaching_class tc
-- LEFT JOIN student_objective_achievement soa ON soa.class_id = tc.class_id
-- LEFT JOIN course_objective_achievement coa ON coa.class_id = tc.class_id
-- LEFT JOIN course_indicator_achievement cia ON cia.class_id = tc.class_id
-- WHERE tc.class_code LIKE 'E2E-%'
-- GROUP BY tc.class_code
-- ORDER BY tc.class_code;

-- 7.8 专业级结果与阻断态
-- SELECT m.major_code, mia.grade_year, at.term_code, COUNT(*) AS mia_count
-- FROM major_indicator_achievement mia
-- JOIN major m ON m.major_id = mia.major_id
-- JOIN academic_term at ON at.term_id = mia.term_id
-- JOIN indicator_point ip ON ip.ip_id = mia.ip_id
-- JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
-- WHERE gr.gr_code LIKE 'EGR%'
-- GROUP BY m.major_code, mia.grade_year, at.term_code
-- ORDER BY m.major_code, mia.grade_year;
