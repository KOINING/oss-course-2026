-- ================================================================
-- gra_db_complete.sql — 毕业要求达成度统一计算平台 完整建库脚本
-- ================================================================
-- 合并来源：GRA_db2.sql (25表DDL+测试数据) + GRA_db2_migration.sql (补充DDL)
-- 总表数：28 张（25 核心 + 3 补充）
-- 视图数：4 个
-- 新增字段：course.status / major.status / college.status /
--            academic_term.status / teaching_class.calc_status
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
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    UNIQUE KEY uk_major_gr_code(major_id, gr_code),
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
    UNIQUE KEY uk_course_major(course_id, major_id),
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
-- 8. 计算结果 — 3 张表
-- ================================================================
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
    term_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    final_achievement FLOAT NOT NULL,
    UNIQUE KEY uk_major_term_ip(major_id, term_id, ip_id),
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

-- ================================================================
-- 13. 监控视图 — 4 个
-- ================================================================

-- 13.1 课程计算进度看板
-- 关联需求：C-4 前置校验——展示所有教学班的计算状态
CREATE OR REPLACE VIEW v_course_calc_progress AS
SELECT 
    tc.class_id,
    tc.class_name,
    tc.calc_status,
    c.course_code,
    c.course_name,
    t.teacher_name,
    cm.major_id,
    tc.term_id,
    (SELECT COUNT(*) FROM student_class sc WHERE sc.class_id = tc.class_id)  AS student_count,
    (SELECT COUNT(*) FROM student_assessment_score sas WHERE sas.class_id = tc.class_id) AS score_count
FROM teaching_class tc
JOIN course c ON c.course_id = tc.course_id
JOIN teacher t ON t.id = tc.teacher_id
JOIN course_major cm ON cm.course_id = c.course_id;

-- 13.2 专业级达成度看板
-- 关联需求：D-2 专业级报告——带合格判断的达成度汇总
CREATE OR REPLACE VIEW v_major_achievement_dashboard AS
SELECT 
    mia.major_id,
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

-- 13.3 穿透式追溯视图
-- 关联需求：D-2 认证专家查阅——从指标点层层追溯至考核点得分
CREATE OR REPLACE VIEW v_score_drilldown AS
SELECT 
    gr.gr_code,
    gr.gr_description             AS gr_desc,
    ip.ip_code,
    ip.ip_description             AS ip_desc,
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
    cis.total_weight              AS macro_weight,
    oic.internal_weight           AS micro_weight
FROM student_assessment_score sas
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN objective_indicator_contribution oic ON oic.co_id = co.co_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN course_indicator_support cis ON cis.course_id = c.course_id AND cis.ip_id = ip.ip_id
JOIN student s ON s.student_id = sas.student_id;

-- 13.4 权重校验辅助视图
-- 关联需求：A-4 宏观支撑矩阵按列实时求和——后端通过此视图获取校验结果
CREATE OR REPLACE VIEW v_weight_validation AS
SELECT 
    ip.ip_id,
    ip.ip_code,
    gr.gr_code,
    m.major_id,
    m.major_name,
    COUNT(cis.course_id)                                                 AS support_course_count,
    COALESCE(SUM(cis.total_weight), 0)                                   AS weight_sum,
    CASE WHEN ABS(COALESCE(SUM(cis.total_weight), 0) - 1.0) < 0.001 
         THEN 'OK' ELSE 'FAIL' END                                       AS is_valid
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
GROUP BY ip.ip_id, ip.ip_code, gr.gr_code, m.major_id, m.major_name;


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
-- D. 系统用户（8 条）
-- ================================================================
INSERT INTO sys_user (username, password, real_name, email, phone, status) VALUES
('admin',          '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '赵管理员', 'admin@university.edu.cn', '13800000001', 1),
('teacher_zhang',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '张教授',   'zhang@university.edu.cn',  '13800000002', 1),
('teacher_li',     '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '李副教授', 'li@university.edu.cn',    '13800000003', 1),
('teacher_wang',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '王讲师',   'wang@university.edu.cn',  '13800000004', 1),
('director_chen',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '陈主任',   'chen@university.edu.cn',  '13800000005', 1),
('academic_wu',    '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '吴老师',   'wu@university.edu.cn',    '13800000006', 1),
('student_zhou',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '周一帆',   'zhou@university.edu.cn',   '13800000007', 1),
('student_chen',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '陈思远',   'chen2@university.edu.cn',  '13800000008', 1);

-- ================================================================
-- E. 教师（3 条，与 sys_user 中教师账号对应）
-- ================================================================
INSERT INTO teacher (teacher_no, teacher_name, title, major_id, user_id) VALUES
('T2024001', '张教授',   '教授',   1, 2),
('T2024002', '李副教授', '副教授', 1, 3),
('T2024003', '王讲师',   '讲师',   1, 4);

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
(8, 5);

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
INSERT INTO graduation_requirement (gr_code, gr_description, major_id) VALUES
('1', '工程知识：能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题', 1),
('2', '问题分析：能够应用数学、自然科学和工程科学的基本原理，识别、表达并通过文献研究分析复杂工程问题', 1),
('3', '设计/开发解决方案：能够设计针对复杂工程问题的解决方案，并能够在设计环节中体现创新意识', 1),
('4', '研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究', 1),
('5', '使用现代工具：能够针对复杂工程问题，开发、选择与使用恰当的技术、资源、现代工程工具', 1),
('6', '工程与社会：能够基于工程相关背景知识进行合理分析，评价专业工程实践和复杂工程问题解决方案对社会的影响', 1);

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
INSERT INTO course_major (course_id, major_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 2),
(5, 3);

-- ================================================================
-- N. 教学班级（3 个）
-- ================================================================
INSERT INTO teaching_class (class_code, class_name, course_id, term_id, teacher_id) VALUES
('TC2024CS01', '数据结构2024-2025-1班', 1, 1, 1),
('TC2024CS02', '操作系统2024-2025-1班', 2, 1, 2),
('TC2024CS03', '计算机网络2024-2025-1班', 3, 1, 3);

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
INSERT INTO major_indicator_achievement (major_id, term_id, ip_id, final_achievement) VALUES
(1, 1, 1, 0.82),
(1, 1, 2, 0.80),
(1, 1, 3, 0.72),
(1, 1, 4, 0.76),
(1, 1, 5, 0.79),
(1, 1, 6, 0.74),
(1, 1, 9, 0.80),
(1, 1, 10, 0.78);
