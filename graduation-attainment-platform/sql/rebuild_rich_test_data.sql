-- ================================================================
-- rebuild_rich_test_data.sql
-- 毕业要求达成度平台：完整重建业务测试数据
--
-- 口径：
--   1. 保留 academic_term 表和 term_id 字段，仅放 1 条默认学期记录。
--   2. 业务统计主体为“专业 + 年级”，学期仅作为现有结构兼容字段。
--   3. 每个专业每届学生修读同一套必修课程。
--   4. 每个“专业 + 年级 + 课程”只有一个教学班，包含该专业该届全部学生。
--
-- 使用方式：
--   先确保当前库结构已按项目最新 schema 创建，然后在 GraduationDB 中执行本脚本。
--   本脚本会清空并重建所有基础与业务数据，请勿在生产库执行。
-- ================================================================

USE GraduationDB;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE temp_import_staging;
TRUNCATE TABLE unlock_audit_log;
TRUNCATE TABLE calc_audit_log;
TRUNCATE TABLE major_indicator_achievement;
TRUNCATE TABLE course_indicator_achievement;
TRUNCATE TABLE course_objective_achievement;
TRUNCATE TABLE student_objective_achievement;
TRUNCATE TABLE student_assessment_score;
TRUNCATE TABLE assessment_point;
TRUNCATE TABLE objective_indicator_contribution;
TRUNCATE TABLE course_objective;
TRUNCATE TABLE course_indicator_support;
TRUNCATE TABLE student_class;
TRUNCATE TABLE teaching_class;
TRUNCATE TABLE course_major;
TRUNCATE TABLE course;
TRUNCATE TABLE indicator_point;
TRUNCATE TABLE graduation_requirement;
TRUNCATE TABLE student;
TRUNCATE TABLE teacher;
TRUNCATE TABLE academic_term;
TRUNCATE TABLE major;
TRUNCATE TABLE college;
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE system_config;

SET FOREIGN_KEY_CHECKS = 1;

-- 所有测试账号密码均为 123456 对应的 BCrypt 值，便于本地功能测试。
SET @pwd_123456 := '$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06';

-- ================================================================
-- 1. RBAC 与基础配置
-- ================================================================

INSERT INTO sys_user (id, username, password, real_name, email, phone, status) VALUES
(1, 'admin',          @pwd_123456, '赵明远', 'admin@university.edu.cn',          '13800000001', 1),
(2, 'academic_wu',    @pwd_123456, '吴雅琴', 'academic@university.edu.cn',       '13800000002', 1),
(3, 'director_chen',  @pwd_123456, '陈志远', 'director@university.edu.cn',       '13800000003', 1),
(4, 'teacher_zhang',  @pwd_123456, '张文博', 'zhang.wb@university.edu.cn',       '13800000004', 1),
(5, 'teacher_li',     @pwd_123456, '李嘉宁', 'li.jn@university.edu.cn',          '13800000005', 1),
(6, 'teacher_wang',   @pwd_123456, '王若愚', 'wang.ry@university.edu.cn',        '13800000006', 1),
(7, 'teacher_sun',    @pwd_123456, '孙晓峰', 'sun.xf@university.edu.cn',         '13800000007', 1),
(8, 'teacher_zhao',   @pwd_123456, '赵清扬', 'zhao.qy@university.edu.cn',        '13800000008', 1),
(9, 'teacher_huang',  @pwd_123456, '黄若兰', 'huang.rl@university.edu.cn',       '13800000009', 1),
(10,'teacher_luo',    @pwd_123456, '罗景辰', 'luo.jc@university.edu.cn',         '13800000010', 1),
(11,'teacher_xu',     @pwd_123456, '许思远', 'xu.sy@university.edu.cn',          '13800000011', 1);

INSERT INTO sys_role (id, role_code, role_name, status, remark) VALUES
(1, 'admin', '系统管理员', 1, '系统全局配置、用户账号管理'),
(2, 'academic_affairs', '教务管理员', 1, '课程、教学班、学生名单和专业级结果管理'),
(3, 'program_director', '专业负责人', 1, '毕业要求、支撑矩阵、专业级达成度分析'),
(4, 'instructor', '课程主讲教师', 1, '课程目标、考核点、成绩录入和课程级计算');

INSERT INTO sys_user_role (user_id, role_id)
SELECT id, 1 FROM sys_user WHERE username = 'admin'
UNION ALL SELECT id, 2 FROM sys_user WHERE username = 'academic_wu'
UNION ALL SELECT id, 3 FROM sys_user WHERE username = 'director_chen'
UNION ALL SELECT id, 4 FROM sys_user WHERE username LIKE 'teacher_%';

INSERT INTO sys_permission (id, perm_code, perm_name, module_name, remark) VALUES
(1,  'college:manage',     '学院管理',       'system',     NULL),
(2,  'major:manage',       '专业管理',       'system',     NULL),
(3,  'user:manage',        '用户管理',       'system',     NULL),
(4,  'role:assign',        '角色分配',       'system',     NULL),
(5,  'dict:manage',        '字典管理',       'system',     NULL),
(6,  'requirement:write',  '毕业要求编辑',   'macro',      NULL),
(7,  'matrix:write',       '支撑矩阵编辑',   'macro',      NULL),
(8,  'course:import',      '课程导入',       'macro',      NULL),
(9,  'class:import',       '班级学生导入',   'macro',      NULL),
(10, 'objective:write',    '课程目标编辑',   'syllabus',   NULL),
(11, 'weight:write',       '内部权重编辑',   'syllabus',   NULL),
(12, 'point:write',        '考核点编辑',     'syllabus',   NULL),
(13, 'score:import',       '成绩导入录入',   'assessment', NULL),
(14, 'calc:trigger',       '达成度计算触发', 'assessment', NULL),
(15, 'report:export',      '报表导出',       'report',     NULL);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission
UNION ALL SELECT 2, id FROM sys_permission WHERE perm_code IN ('course:import', 'class:import', 'calc:trigger', 'report:export')
UNION ALL SELECT 3, id FROM sys_permission WHERE perm_code IN ('requirement:write', 'matrix:write', 'calc:trigger', 'report:export')
UNION ALL SELECT 4, id FROM sys_permission WHERE perm_code IN ('objective:write', 'weight:write', 'point:write', 'score:import', 'calc:trigger', 'report:export');

INSERT INTO system_config (config_key, config_value, config_desc) VALUES
('score_import_max_rows', '1000', '成绩导入单次最大行数'),
('score_precision', '2', '成绩小数保留位数'),
('calc_precision', '4', '达成度小数保留位数'),
('calc_threshold_pass', '0.60', '达成度合格阈值'),
('report_export_timeout', '120', '报表导出超时时间（秒）'),
('default_password', '123456', '本地测试新用户默认密码');

-- ================================================================
-- 2. 学院、专业、默认学期、教师
-- ================================================================

INSERT INTO college (college_id, college_code, college_name, status) VALUES
(1, 'CS', '计算机学院', 1),
(2, 'SE', '软件学院', 1);

INSERT INTO major (major_id, major_code, major_name, college_id, status) VALUES
(1, '080901', '计算机科学与技术', 1, 1),
(2, '080903', '网络工程', 1, 1),
(3, '080902', '软件工程', 2, 1),
(4, '080910T', '数据科学与大数据技术', 2, 1);

INSERT INTO academic_term (term_id, term_code, academic_year, semester, start_date, end_date, status) VALUES
(1, '2025-2026-1', 2025, 1, '2025-09-01', '2026-01-16', 1);

INSERT INTO teacher (id, teacher_no, teacher_name, title, major_id, user_id, status) VALUES
(1, 'T2025001', '张文博', '教授', 1, 4, 1),
(2, 'T2025002', '李嘉宁', '副教授', 1, 5, 1),
(3, 'T2025003', '王若愚', '副教授', 2, 6, 1),
(4, 'T2025004', '孙晓峰', '讲师', 2, 7, 1),
(5, 'T2025005', '赵清扬', '教授', 3, 8, 1),
(6, 'T2025006', '黄若兰', '副教授', 3, 9, 1),
(7, 'T2025007', '罗景辰', '副教授', 4, 10, 1),
(8, 'T2025008', '许思远', '讲师', 4, 11, 1);

-- ================================================================
-- 3. 临时模板数据
-- ================================================================

DROP TEMPORARY TABLE IF EXISTS tmp_num32;
CREATE TEMPORARY TABLE tmp_num32 (seq INT PRIMARY KEY);
INSERT INTO tmp_num32 (seq) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),
(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32);

DROP TEMPORARY TABLE IF EXISTS tmp_student_name;
CREATE TEMPORARY TABLE tmp_student_name (
    seq INT PRIMARY KEY,
    given_name VARCHAR(20) NOT NULL
);
INSERT INTO tmp_student_name VALUES
(1, '子涵'), (2, '思远'), (3, '嘉宁'), (4, '明轩'),
(5, '雨桐'), (6, '一凡'), (7, '若曦'), (8, '景辰'),
(9, '书瑶'), (10, '浩然'), (11, '清扬'), (12, '语晨'),
(13, '梓萱'), (14, '亦辰'), (15, '佳怡'), (16, '睿哲'),
(17, '欣然'), (18, '文博'), (19, '雅琪'), (20, '承宇'),
(21, '诗涵'), (22, '俊逸'), (23, '梦瑶'), (24, '泽宇'),
(25, '彦霖'), (26, '知行'), (27, '沐阳'), (28, '可欣'),
(29, '靖川'), (30, '思齐'), (31, '星辰'), (32, '予安');

DROP TEMPORARY TABLE IF EXISTS tmp_grade;
CREATE TEMPORARY TABLE tmp_grade (grade_year INT PRIMARY KEY);
INSERT INTO tmp_grade VALUES (2022), (2023);

DROP TEMPORARY TABLE IF EXISTS tmp_gr_template;
CREATE TEMPORARY TABLE tmp_gr_template (
    gr_no INT PRIMARY KEY,
    gr_code VARCHAR(10) NOT NULL,
    gr_description VARCHAR(500) NOT NULL
);
INSERT INTO tmp_gr_template VALUES
(1, 'GR1', '能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题。'),
(2, 'GR2', '能够应用数学、自然科学和工程科学的基本原理识别、表达并分析复杂工程问题。'),
(3, 'GR3', '能够设计满足特定需求的系统、单元或工艺流程，并体现创新意识。'),
(4, 'GR4', '能够基于科学原理并采用科学方法对复杂工程问题开展研究。'),
(5, 'GR5', '能够选择、使用与开发恰当的技术、资源、现代工程工具和信息技术工具。'),
(6, 'GR6', '能够基于工程相关背景知识进行合理分析，评价工程实践对社会、健康、安全、法律及文化的影响。'),
(7, 'GR7', '能够理解和评价针对复杂工程问题的工程实践对环境、社会可持续发展的影响。'),
(8, 'GR8', '具有人文社会科学素养、社会责任感和工程职业道德。');

DROP TEMPORARY TABLE IF EXISTS tmp_ip_template;
CREATE TEMPORARY TABLE tmp_ip_template (
    gr_no INT NOT NULL,
    ip_no INT NOT NULL,
    ip_code VARCHAR(10) NOT NULL,
    ip_description VARCHAR(500) NOT NULL,
    PRIMARY KEY (gr_no, ip_no)
);
INSERT INTO tmp_ip_template VALUES
(1, 1, '1-1', '能够将数学与工程基础知识用于专业问题建模。'),
(1, 2, '1-2', '能够解释专业系统中的关键机制并进行基础推演。'),
(2, 1, '2-1', '能够识别复杂工程问题中的关键需求与约束。'),
(2, 2, '2-2', '能够基于证据对问题进行分析并给出判断。'),
(3, 1, '3-1', '能够针对需求设计可实现的软件或系统方案。'),
(3, 2, '3-2', '能够在设计中综合考虑技术、成本与规范约束。'),
(4, 1, '4-1', '能够设计实验或测试方案验证关键假设。'),
(4, 2, '4-2', '能够解释实验结果并形成有效结论。'),
(5, 1, '5-1', '能够使用开发工具完成实现、调试与验证。'),
(5, 2, '5-2', '能够根据任务特点选择合适的平台与工具链。'),
(6, 1, '6-1', '能够理解工程实践中的责任、规范与职业要求。'),
(6, 2, '6-2', '能够在工程活动中识别并处理伦理与合规问题。'),
(7, 1, '7-1', '能够在团队中有效承担角色并协同完成任务。'),
(7, 2, '7-2', '能够进行计划、沟通、跟踪与交付管理。'),
(8, 1, '8-1', '能够以书面和口头方式清晰表达技术方案。'),
(8, 2, '8-2', '能够基于反馈开展反思并持续改进。');

DROP TEMPORARY TABLE IF EXISTS tmp_course_plan;
CREATE TEMPORARY TABLE tmp_course_plan (
    major_code VARCHAR(20) NOT NULL,
    course_order INT NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    credit FLOAT NOT NULL,
    teacher_id BIGINT NOT NULL,
    PRIMARY KEY (major_code, course_order)
);
INSERT INTO tmp_course_plan VALUES
('080901', 1, 'CS-MATH', '高等数学A', 4, 1),
('080901', 2, 'CS-PROG', '程序设计基础', 3, 2),
('080901', 3, 'CS-DS', '数据结构', 3, 1),
('080901', 4, 'CS-DB', '数据库系统', 3, 2),
('080901', 5, 'CS-OS', '操作系统', 3, 1),
('080901', 6, 'CS-NET', '计算机网络', 3, 2),
('080901', 7, 'CS-SE', '软件工程', 3, 1),
('080901', 8, 'CS-PRA', '综合工程实践', 2, 2),
('080903', 1, 'NE-MATH', '高等数学A', 4, 3),
('080903', 2, 'NE-PROG', '程序设计基础', 3, 4),
('080903', 3, 'NE-DS', '数据结构', 3, 3),
('080903', 4, 'NE-NET', '计算机网络', 3, 4),
('080903', 5, 'NE-PROTO', '网络协议分析', 3, 3),
('080903', 6, 'NE-SEC', '网络安全基础', 3, 4),
('080903', 7, 'NE-ADMIN', '网络系统管理', 2, 3),
('080903', 8, 'NE-PRA', '网络工程实践', 2, 4),
('080902', 1, 'SE-MATH', '高等数学A', 4, 5),
('080902', 2, 'SE-PROG', '程序设计基础', 3, 6),
('080902', 3, 'SE-DS', '数据结构', 3, 5),
('080902', 4, 'SE-REQ', '软件需求分析', 3, 6),
('080902', 5, 'SE-DESIGN', '软件设计与体系结构', 3, 5),
('080902', 6, 'SE-TEST', '软件测试技术', 3, 6),
('080902', 7, 'SE-PM', '软件项目管理', 2, 5),
('080902', 8, 'SE-PRA', '软件工程实践', 2, 6),
('080910T', 1, 'DS-MATH', '高等数学A', 4, 7),
('080910T', 2, 'DS-PROG', 'Python程序设计', 3, 8),
('080910T', 3, 'DS-DS', '数据结构', 3, 7),
('080910T', 4, 'DS-DB', '数据库系统', 3, 8),
('080910T', 5, 'DS-STAT', '统计学习基础', 3, 7),
('080910T', 6, 'DS-ML', '机器学习基础', 3, 8),
('080910T', 7, 'DS-BIGDATA', '大数据平台技术', 3, 7),
('080910T', 8, 'DS-PRA', '数据科学实践', 2, 8);

-- ================================================================
-- 4. 培养体系、课程、学生、教学班
-- ================================================================

INSERT INTO graduation_requirement (gr_code, gr_description, major_id, grade_year, status)
SELECT t.gr_code, CONCAT(m.major_name, CAST(g.grade_year AS CHAR), '级：', t.gr_description), m.major_id, g.grade_year, 1
FROM major m
CROSS JOIN tmp_grade g
CROSS JOIN tmp_gr_template t;

INSERT INTO indicator_point (ip_code, ip_description, gr_id, status)
SELECT it.ip_code, it.ip_description, gr.gr_id, 1
FROM graduation_requirement gr
JOIN tmp_gr_template gt ON gt.gr_code = gr.gr_code
JOIN tmp_ip_template it ON it.gr_no = gt.gr_no;

INSERT INTO course (course_code, course_name, credit, status)
SELECT course_code, course_name, credit, 1
FROM tmp_course_plan;

INSERT INTO course_major (course_id, major_id, grade_year)
SELECT c.course_id, m.major_id, g.grade_year
FROM tmp_course_plan p
JOIN course c ON c.course_code = p.course_code
JOIN major m ON m.major_code = p.major_code
CROSS JOIN tmp_grade g;

INSERT INTO student (student_no, student_name, major_id, enrollment_year, status)
SELECT
    CONCAT(m.major_code, g.grade_year, LPAD(n.seq, 3, '0')) AS student_no,
    CONCAT(
        ELT(1 + MOD(n.seq * 5 + m.major_id * 3 + g.grade_year, 28),
            '李','王','张','刘','陈','杨','赵','黄','周','吴','徐','孙','胡','朱',
            '高','林','何','郭','马','罗','梁','宋','郑','谢','韩','唐','冯','邓'),
        sn.given_name
    ) AS student_name,
    m.major_id,
    g.grade_year,
    1
FROM major m
CROSS JOIN tmp_grade g
CROSS JOIN tmp_num32 n
JOIN tmp_student_name sn ON sn.seq = 1 + MOD(n.seq + m.major_id + g.grade_year, 32);

INSERT INTO teaching_class (class_code, class_name, course_id, term_id, teacher_id, grade_year, calc_status)
SELECT
    CONCAT(REPLACE(p.course_code, '-', ''), '-', g.grade_year) AS class_code,
    CONCAT(m.major_name, g.grade_year, '级-', p.course_name) AS class_name,
    c.course_id,
    1 AS term_id,
    p.teacher_id,
    g.grade_year,
    CASE
        WHEN p.course_order = 8 AND g.grade_year = 2023 THEN 'unsubmitted'
        WHEN p.course_order = 7 AND g.grade_year = 2022 THEN 'score_imported'
        ELSE 'locked'
    END AS calc_status
FROM tmp_course_plan p
JOIN major m ON m.major_code = p.major_code
JOIN course c ON c.course_code = p.course_code
CROSS JOIN tmp_grade g;

INSERT INTO student_class (student_id, class_id)
SELECT s.student_id, tc.class_id
FROM teaching_class tc
JOIN course_major cm ON cm.course_id = tc.course_id AND cm.grade_year = tc.grade_year
JOIN student s ON s.major_id = cm.major_id AND s.enrollment_year = tc.grade_year;

-- ================================================================
-- 5. 课程目标、考核点、支撑矩阵
-- ================================================================

INSERT INTO course_objective (objective_code, co_description, course_id)
SELECT 'CO1', CONCAT(c.course_name, '：掌握核心概念、基本原理和基础方法。'), c.course_id FROM course c
UNION ALL
SELECT 'CO2', CONCAT(c.course_name, '：能够运用课程方法完成分析、设计或验证。'), c.course_id FROM course c
UNION ALL
SELECT 'CO3', CONCAT(c.course_name, '：能够完成综合实现、表达方案并进行改进。'), c.course_id FROM course c;

INSERT INTO assessment_point (ap_name, full_score, co_id)
SELECT CONCAT('平时作业(', c.course_name, ')'), 20, co.co_id
FROM course_objective co JOIN course c ON c.course_id = co.course_id
WHERE co.objective_code = 'CO1'
UNION ALL
SELECT CONCAT('阶段测验(', c.course_name, ')'), 20, co.co_id
FROM course_objective co JOIN course c ON c.course_id = co.course_id
WHERE co.objective_code = 'CO1'
UNION ALL
SELECT CONCAT('实验报告(', c.course_name, ')'), 30, co.co_id
FROM course_objective co JOIN course c ON c.course_id = co.course_id
WHERE co.objective_code = 'CO2'
UNION ALL
SELECT CONCAT('期末考核(', c.course_name, ')'), 30, co.co_id
FROM course_objective co JOIN course c ON c.course_id = co.course_id
WHERE co.objective_code = 'CO3';

INSERT INTO course_indicator_support (course_id, ip_id, total_weight)
SELECT DISTINCT c.course_id, ip.ip_id, 0.65
FROM tmp_course_plan p
JOIN course c ON c.course_code = p.course_code
JOIN major m ON m.major_code = p.major_code
JOIN course_major cm ON cm.course_id = c.course_id AND cm.major_id = m.major_id
JOIN graduation_requirement gr ON gr.major_id = m.major_id AND gr.grade_year = cm.grade_year AND gr.gr_code = CONCAT('GR', p.course_order)
JOIN indicator_point ip ON ip.gr_id = gr.gr_id;

INSERT INTO course_indicator_support (course_id, ip_id, total_weight)
SELECT DISTINCT c.course_id, ip.ip_id, 0.35
FROM tmp_course_plan p
JOIN course c ON c.course_code = p.course_code
JOIN major m ON m.major_code = p.major_code
JOIN course_major cm ON cm.course_id = c.course_id AND cm.major_id = m.major_id
JOIN graduation_requirement gr ON gr.major_id = m.major_id AND gr.grade_year = cm.grade_year AND gr.gr_code = CONCAT('GR', p.course_order - 1)
JOIN indicator_point ip ON ip.gr_id = gr.gr_id
WHERE p.course_order BETWEEN 2 AND 8;

INSERT INTO course_indicator_support (course_id, ip_id, total_weight)
SELECT DISTINCT c.course_id, ip.ip_id, 0.35
FROM tmp_course_plan p
JOIN course c ON c.course_code = p.course_code
JOIN major m ON m.major_code = p.major_code
JOIN course_major cm ON cm.course_id = c.course_id AND cm.major_id = m.major_id
JOIN graduation_requirement gr ON gr.major_id = m.major_id AND gr.grade_year = cm.grade_year AND gr.gr_code = 'GR8'
JOIN indicator_point ip ON ip.gr_id = gr.gr_id
WHERE p.course_order = 7;

INSERT INTO objective_indicator_contribution (co_id, ip_id, internal_weight)
SELECT co.co_id, cis.ip_id,
       CASE
           WHEN RIGHT(ip.ip_code, 1) = '1' AND co.objective_code = 'CO1' THEN 0.35
           WHEN RIGHT(ip.ip_code, 1) = '1' AND co.objective_code = 'CO2' THEN 0.40
           WHEN RIGHT(ip.ip_code, 1) = '1' AND co.objective_code = 'CO3' THEN 0.25
           WHEN RIGHT(ip.ip_code, 1) = '2' AND co.objective_code = 'CO1' THEN 0.25
           WHEN RIGHT(ip.ip_code, 1) = '2' AND co.objective_code = 'CO2' THEN 0.35
           ELSE 0.40
       END AS internal_weight
FROM course_indicator_support cis
JOIN indicator_point ip ON ip.ip_id = cis.ip_id
JOIN course_objective co ON co.course_id = cis.course_id;

-- ================================================================
-- 6. 成绩与达成度结果
-- ================================================================

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)
SELECT
    sc.student_id,
    ap.ap_id,
    sc.class_id,
    ROUND(
        ap.full_score * LEAST(
            0.98,
            GREATEST(
                0.52,
                0.70
                + CASE p.course_order
                    WHEN 1 THEN 0.055
                    WHEN 2 THEN 0.020
                    WHEN 3 THEN -0.015
                    WHEN 4 THEN -0.035
                    WHEN 5 THEN -0.055
                    WHEN 6 THEN -0.020
                    WHEN 7 THEN 0.010
                    ELSE 0.035
                  END
                + CASE
                    WHEN ap.ap_name LIKE '平时作业%' THEN 0.075
                    WHEN ap.ap_name LIKE '阶段测验%' THEN 0.010
                    WHEN ap.ap_name LIKE '实验报告%' THEN 0.040
                    ELSE -0.055
                  END
                + CASE
                    WHEN MOD(s.student_id, 23) = 0 THEN -0.180
                    WHEN MOD(s.student_id, 17) = 0 THEN -0.125
                    WHEN MOD(s.student_id, 11) = 0 THEN -0.065
                    WHEN MOD(s.student_id, 7) = 0 THEN 0.055
                    WHEN MOD(s.student_id, 5) = 0 THEN 0.025
                    ELSE 0
                  END
                + ((MOD(s.student_id * 11 + tc.class_id * 5 + ap.ap_id * 3, 25) - 12) / 100)
                + CASE WHEN tc.grade_year = 2023 THEN 0.012 ELSE 0 END
                + CASE m.major_code
                    WHEN '080901' THEN 0.012
                    WHEN '080903' THEN -0.006
                    WHEN '080902' THEN 0.004
                    ELSE -0.012
                  END
            )
        ) * 2,
        0
    ) / 2 AS actual_score
FROM student_class sc
JOIN student s ON s.student_id = sc.student_id
JOIN teaching_class tc ON tc.class_id = sc.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN tmp_course_plan p ON p.course_code = c.course_code
JOIN major m ON m.major_code = p.major_code
JOIN course_objective co ON co.course_id = tc.course_id
JOIN assessment_point ap ON ap.co_id = co.co_id
WHERE tc.calc_status IN ('score_imported', 'locked');

INSERT INTO student_objective_achievement (student_id, class_id, co_id, achievement)
SELECT
    sas.student_id,
    sas.class_id,
    ap.co_id,
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS achievement
FROM student_assessment_score sas
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
GROUP BY sas.student_id, sas.class_id, ap.co_id;

INSERT INTO course_objective_achievement (class_id, co_id, average_achievement)
SELECT class_id, co_id, ROUND(AVG(achievement), 4)
FROM student_objective_achievement
GROUP BY class_id, co_id;

INSERT INTO course_indicator_achievement (class_id, ip_id, achievement, is_locked)
SELECT
    tc.class_id,
    cis.ip_id,
    ROUND(
        COALESCE(SUM(coa.average_achievement * oic.internal_weight), 0.60 + MOD(tc.class_id + cis.ip_id, 30) / 100),
        4
    ) AS achievement,
    tc.calc_status = 'locked' AS is_locked
FROM teaching_class tc
JOIN course_indicator_support cis ON cis.course_id = tc.course_id
JOIN objective_indicator_contribution oic ON oic.ip_id = cis.ip_id
JOIN course_objective co ON co.co_id = oic.co_id AND co.course_id = tc.course_id
LEFT JOIN course_objective_achievement coa ON coa.class_id = tc.class_id AND coa.co_id = co.co_id
WHERE tc.calc_status IN ('score_imported', 'locked')
GROUP BY tc.class_id, cis.ip_id, tc.calc_status;

INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement)
SELECT
    gr.major_id,
    gr.grade_year,
    1 AS term_id,
    ip.ip_id,
    ROUND(
        LEAST(0.96, GREATEST(0.48,
            COALESCE(
                SUM(cia.achievement * cis.total_weight) / NULLIF(SUM(cis.total_weight), 0),
                0.56
                + MOD(gr.major_id * 11 + gr.grade_year + ip.ip_id, 33) / 100
                + CASE WHEN gr.grade_year = 2023 THEN 0.012 ELSE 0 END
            )
        )),
        4
    ) AS final_achievement
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
LEFT JOIN course_major cm ON cm.course_id = cis.course_id
    AND cm.major_id = gr.major_id
    AND cm.grade_year = gr.grade_year
LEFT JOIN teaching_class tc ON tc.course_id = cm.course_id
    AND tc.grade_year = gr.grade_year
    AND tc.term_id = 1
LEFT JOIN course_indicator_achievement cia ON cia.class_id = tc.class_id
    AND cia.ip_id = ip.ip_id
GROUP BY gr.major_id, gr.grade_year, ip.ip_id;

INSERT INTO calc_audit_log (operator_id, action_type, target_type, target_id, term_id, result_json)
SELECT 2, 'major_calc', 'major', m.major_id, 1,
       JSON_OBJECT('majorName', m.major_name, 'gradeYear', g.grade_year, 'termCode', '2025-2026-1', 'source', 'rich-test-data')
FROM major m
CROSS JOIN tmp_grade g;

INSERT INTO unlock_audit_log (class_id, request_by, approved_by, reason)
SELECT tc.class_id, tc.teacher_id, 2, '预置测试数据：成绩修订后重新计算'
FROM teaching_class tc
WHERE tc.calc_status = 'score_imported'
LIMIT 4;

-- ================================================================
-- 7. 校验查询
-- ================================================================

SELECT 'academic_term_count' AS check_item, COUNT(*) AS check_value FROM academic_term
UNION ALL SELECT 'college_count', COUNT(*) FROM college
UNION ALL SELECT 'major_count', COUNT(*) FROM major
UNION ALL SELECT 'login_user_count', COUNT(*) FROM sys_user
UNION ALL SELECT 'program_director_user_count', COUNT(*) FROM sys_user_role WHERE role_id = 3
UNION ALL SELECT 'student_login_user_count', COUNT(*) FROM student s JOIN sys_user u ON u.id = s.user_id
UNION ALL SELECT 'student_count', COUNT(*) FROM student
UNION ALL SELECT 'course_count', COUNT(*) FROM course
UNION ALL SELECT 'teaching_class_count', COUNT(*) FROM teaching_class
UNION ALL SELECT 'student_class_count', COUNT(*) FROM student_class
UNION ALL SELECT 'assessment_score_count', COUNT(*) FROM student_assessment_score
UNION ALL SELECT 'major_indicator_achievement_count', COUNT(*) FROM major_indicator_achievement;

SELECT
    m.major_name,
    s.enrollment_year AS grade_year,
    COUNT(*) AS student_count
FROM student s
JOIN major m ON m.major_id = s.major_id
GROUP BY m.major_name, s.enrollment_year
ORDER BY m.major_name, s.enrollment_year;

SELECT
    m.major_name,
    cm.grade_year,
    COUNT(DISTINCT tc.class_id) AS teaching_class_count,
    COUNT(DISTINCT cm.course_id) AS required_course_count
FROM course_major cm
JOIN major m ON m.major_id = cm.major_id
LEFT JOIN teaching_class tc ON tc.course_id = cm.course_id AND tc.grade_year = cm.grade_year
GROUP BY m.major_name, cm.grade_year
ORDER BY m.major_name, cm.grade_year;

SELECT
    m.major_name,
    gr.grade_year,
    ip.ip_code,
    ROUND(SUM(cis.total_weight), 4) AS support_weight_sum
FROM indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
LEFT JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
GROUP BY m.major_name, gr.grade_year, ip.ip_code
ORDER BY m.major_name, gr.grade_year, ip.ip_code;

DROP TEMPORARY TABLE IF EXISTS tmp_num32;
DROP TEMPORARY TABLE IF EXISTS tmp_student_name;
DROP TEMPORARY TABLE IF EXISTS tmp_grade;
DROP TEMPORARY TABLE IF EXISTS tmp_gr_template;
DROP TEMPORARY TABLE IF EXISTS tmp_ip_template;
DROP TEMPORARY TABLE IF EXISTS tmp_course_plan;

-- ================================================================
-- 完成
-- ================================================================
