-- ================================================================
-- upgrade_week4_e2e_test_data.sql
-- 鐢ㄩ€旓細绗洓鍛ㄦā鍧?C 鍏ㄩ摼璺祴璇曟暟鎹熀绾?-- 鏁版嵁搴擄細GraduationDB
--
-- 璇存槑锛?-- 1. 鏈剼鏈笉淇敼璐﹀彿銆佽鑹层€佹潈闄愪綋绯伙細
--    sys_user / sys_role / sys_permission / sys_role_permission / sys_user_role
-- 2. 鏈剼鏈細娓呯┖闄よ处鍙蜂綋绯诲鐨勪笟鍔℃暟鎹紝骞堕噸寤虹鍥涘懆妯″潡 C 鎵€闇€ E2E 鏁版嵁
-- 3. 鏈剼鏈緷璧栧凡鏈夋暀甯堣处鍙锋槧灏勶紝榛樿澶嶇敤锛?--    T2024001 / T2024002 / T2024003
-- 4. 鏈剼鏈墽琛屽墠闇€纭繚宸叉墽琛岋細
--    upgrade_grade_year_20260603.sql
--    upgrade_week4_score_calc.sql
-- ================================================================

USE GraduationDB;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET collation_connection = 'utf8mb4_unicode_ci';

-- ================================================================
-- 0. E2E 妯℃澘鏁版嵁
-- ================================================================
DROP TEMPORARY TABLE IF EXISTS tmp_e2e_context;
CREATE TEMPORARY TABLE tmp_e2e_context (
    context_code VARCHAR(32) PRIMARY KEY,
    major_code VARCHAR(20) NOT NULL,
    grade_year INT NOT NULL,
    scenario_code VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_context (context_code, major_code, grade_year, scenario_code) VALUES
('CS2022_MAIN',   '080901', 2022, 'blocked_unsubmitted'),
('CS2023_BLOCK',  '080901', 2023, 'blocked_score_imported'),
('SE2022_MAIN',   '080902', 2022, 'complete_with_unlock'),
('SE2023_BLOCK',  '080902', 2023, 'blocked_unsubmitted');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_gr_template;
CREATE TEMPORARY TABLE tmp_e2e_gr_template (
    gr_code VARCHAR(10) PRIMARY KEY,
    gr_description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_gr_template (gr_code, gr_description) VALUES
('EGR01', 'E2E_GR_01_EngineeringKnowledge'),
('EGR02', 'E2E_GR_02_ProblemAnalysis'),
('EGR03', 'E2E_GR_03_SystemDesign'),
('EGR04', 'E2E_GR_04_ExperimentAndAnalysis'),
('EGR05', 'E2E_GR_05_ModernToolUsage'),
('EGR06', 'E2E_GR_06_EngineeringEthics'),
('EGR07', 'E2E_GR_07_TeamworkAndDelivery'),
('EGR08', 'E2E_GR_08_CommunicationAndImprovement');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_ip_template;
CREATE TEMPORARY TABLE tmp_e2e_ip_template (
    gr_code VARCHAR(10) NOT NULL,
    ip_code VARCHAR(10) NOT NULL,
    ip_description VARCHAR(255) NOT NULL,
    PRIMARY KEY (gr_code, ip_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_ip_template (gr_code, ip_code, ip_description) VALUES
('EGR01', 'E1-1', 'E2E_IP_E1_1_KnowledgeModeling'),
('EGR01', 'E1-2', 'E2E_IP_E1_2_MechanismExplanation'),
('EGR02', 'E2-1', 'E2E_IP_E2_1_KeyIssueIdentification'),
('EGR02', 'E2-2', 'E2E_IP_E2_2_EvidenceAnalysis'),
('EGR03', 'E3-1', 'E2E_IP_E3_1_SolutionDesign'),
('EGR03', 'E3-2', 'E2E_IP_E3_2_ConstraintIntegration'),
('EGR04', 'E4-1', 'E2E_IP_E4_1_ExperimentDesign'),
('EGR04', 'E4-2', 'E2E_IP_E4_2_ResultInterpretation'),
('EGR05', 'E5-1', 'E2E_IP_E5_1_DevelopmentAndDebugging'),
('EGR05', 'E5-2', 'E2E_IP_E5_2_PlatformSelection'),
('EGR06', 'E6-1', 'E2E_IP_E6_1_ResponsibilityAwareness'),
('EGR06', 'E6-2', 'E2E_IP_E6_2_EthicalConstraintHandling'),
('EGR07', 'E7-1', 'E2E_IP_E7_1_Teamwork'),
('EGR07', 'E7-2', 'E2E_IP_E7_2_PlanningCommunicationDelivery'),
('EGR08', 'E8-1', 'E2E_IP_E8_1_StructuredExpression'),
('EGR08', 'E8-2', 'E2E_IP_E8_2_ContinuousImprovement');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_course_def;
CREATE TEMPORARY TABLE tmp_e2e_course_def (
    course_code VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credit FLOAT NOT NULL,
    major_code VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_course_def (course_code, course_name, credit, major_code) VALUES
('E2E-CS-DS',   'E2E-Data-Structure',         3.0, '080901'),
('E2E-CS-OS',   'E2E-Operating-System',       3.0, '080901'),
('E2E-CS-NET',  'E2E-Computer-Network',       3.0, '080901'),
('E2E-CS-DB',   'E2E-Database-Principles',    3.0, '080901'),
('E2E-CS-SE',   'E2E-SE-Fundamentals',        2.0, '080901'),
('E2E-CS-PRA',  'E2E-Engineering-Practice',   2.0, '080901'),
('E2E-SE-REQ',  'E2E-Requirement-Analysis',   3.0, '080902'),
('E2E-SE-DES',  'E2E-Software-Design',        3.0, '080902'),
('E2E-SE-TEST', 'E2E-Software-Testing',       3.0, '080902'),
('E2E-SE-PM',   'E2E-Project-Management',     2.0, '080902');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_objective_template;
CREATE TEMPORARY TABLE tmp_e2e_objective_template (
    objective_code VARCHAR(16) PRIMARY KEY,
    co_description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_objective_template (objective_code, co_description) VALUES
('CO1', 'E2E-UnderstandCoreConcepts'),
('CO2', 'E2E-ApplyMethodsForAnalysisDesignValidation'),
('CO3', 'E2E-CompleteIntegratedImplementationAndImprovement');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_ap_template;
CREATE TEMPORARY TABLE tmp_e2e_ap_template (
    ap_name VARCHAR(50) PRIMARY KEY,
    full_score FLOAT NOT NULL,
    objective_code VARCHAR(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO tmp_e2e_ap_template (ap_name, full_score, objective_code) VALUES
('E2E-Homework', 20, 'CO1'),
('E2E-Phase-Quiz', 20, 'CO1'),
('E2E-Lab-Report', 30, 'CO2'),
('E2E-Final-Assessment', 30, 'CO3');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_e2e_class_plan (class_code, context_code, major_code, grade_year, course_code, teacher_no, calc_status, class_name) VALUES
('E2E-CS22-01', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-DS',   'T2024001', 'locked',         'E2E-CS2022-DataStructure'),
('E2E-CS22-02', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-OS',   'T2024001', 'locked',         'E2E-CS2022-OperatingSystem'),
('E2E-CS22-03', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-NET',  'T2024001', 'locked',         'E2E-CS2022-Network'),
('E2E-CS22-04', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-DB',   'T2024001', 'locked',         'E2E-CS2022-Database'),
('E2E-CS22-05', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-SE',   'T2024001', 'locked',         'E2E-CS2022-SEFundamentals'),
('E2E-CS22-06', 'CS2022_MAIN',  '080901', 2022, 'E2E-CS-PRA',  'T2024001', 'unsubmitted',    'E2E-CS2022-EngineeringPractice'),
('E2E-CS23-01', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-DS',   'T2024001', 'locked',         'E2E-CS2023-DataStructure'),
('E2E-CS23-02', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-OS',   'T2024001', 'locked',         'E2E-CS2023-OperatingSystem'),
('E2E-CS23-03', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-NET',  'T2024001', 'locked',         'E2E-CS2023-Network'),
('E2E-CS23-04', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-DB',   'T2024001', 'locked',         'E2E-CS2023-Database'),
('E2E-CS23-05', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-SE',   'T2024001', 'locked',         'E2E-CS2023-SEFundamentals'),
('E2E-CS23-06', 'CS2023_BLOCK', '080901', 2023, 'E2E-CS-PRA',  'T2024001', 'score_imported', 'E2E-CS2023-EngineeringPractice'),
('E2E-SE22-01', 'SE2022_MAIN',  '080902', 2022, 'E2E-SE-REQ',  'T2024002', 'locked',         'E2E-SE2022-Requirement'),
('E2E-SE22-02', 'SE2022_MAIN',  '080902', 2022, 'E2E-SE-DES',  'T2024002', 'locked',         'E2E-SE2022-Design'),
('E2E-SE22-03', 'SE2022_MAIN',  '080902', 2022, 'E2E-SE-TEST', 'T2024003', 'locked',         'E2E-SE2022-Testing'),
('E2E-SE22-04', 'SE2022_MAIN',  '080902', 2022, 'E2E-SE-PM',   'T2024003', 'locked',         'E2E-SE2022-ProjectManagement'),
('E2E-SE23-01', 'SE2023_BLOCK', '080902', 2023, 'E2E-SE-REQ',  'T2024002', 'locked',         'E2E-SE2023-Requirement'),
('E2E-SE23-02', 'SE2023_BLOCK', '080902', 2023, 'E2E-SE-DES',  'T2024002', 'locked',         'E2E-SE2023-Design'),
('E2E-SE23-03', 'SE2023_BLOCK', '080902', 2023, 'E2E-SE-TEST', 'T2024003', 'locked',         'E2E-SE2023-Testing'),
('E2E-SE23-04', 'SE2023_BLOCK', '080902', 2023, 'E2E-SE-PM',   'T2024003', 'unsubmitted',    'E2E-SE2023-ProjectManagement');

DROP TEMPORARY TABLE IF EXISTS tmp_e2e_num10;
CREATE TEMPORARY TABLE tmp_e2e_num10 (
    seq INT PRIMARY KEY,
    score_factor FLOAT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    CONCAT(
        'E2E',
        CASE WHEN ctx.major_code = '080901' THEN 'CS' ELSE 'SE' END,
        RIGHT(ctx.grade_year, 2),
        LPAD(n.seq, 3, '0')
    ) AS student_no,
    CASE
        WHEN ctx.major_code = '080901' THEN CONCAT('CS', RIGHT(ctx.grade_year, 2), 'Student', LPAD(n.seq, 2, '0'))
        ELSE CONCAT('SE', RIGHT(ctx.grade_year, 2), 'Student', LPAD(n.seq, 2, '0'))
    END AS student_name,
    n.score_factor
FROM tmp_e2e_context ctx
JOIN tmp_e2e_num10 n ON 1 = 1;
ALTER TABLE tmp_e2e_student_plan
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE tmp_e2e_support_plan
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ================================================================
-- 1. 娓呮礂鏃т笟鍔℃暟鎹?-- 娓呮礂绛栫暐锛氫繚鐣欒处鍙蜂綋绯讳笌 teacher 琛紝娓呯┖鍏朵綑涓氬姟鏁版嵁鍚庨噸寤?-- ================================================================
DELETE FROM temp_import_staging;
DELETE FROM calc_audit_log;
DELETE FROM unlock_audit_log;
DELETE FROM major_indicator_achievement;
DELETE FROM course_indicator_achievement;
DELETE FROM course_objective_achievement;
DELETE FROM student_objective_achievement;
DELETE FROM student_assessment_score;
DELETE FROM student_class;
DELETE FROM teaching_class;
DELETE FROM assessment_point;
DELETE FROM objective_indicator_contribution;
DELETE FROM course_objective;
DELETE FROM course_indicator_support;
DELETE FROM indicator_point;
DELETE FROM graduation_requirement;
DELETE FROM course_major;
DELETE FROM student;
DELETE FROM course;
DELETE FROM academic_term;

UPDATE teacher
SET major_id = NULL;

DELETE FROM major;

-- ================================================================
-- 2. 鍩虹涓绘暟鎹噸寤?-- ================================================================
INSERT INTO college (college_code, college_name, status)
SELECT 'CS', 'School-of-Computer-Science-and-Technology', 1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM college WHERE college_code = 'CS'
);

UPDATE college
SET college_name = 'School-of-Computer-Science-and-Technology',
    status = 1
WHERE college_code = 'CS';

SET @college_cs := (
    SELECT college_id
    FROM college
    WHERE college_code = 'CS'
    LIMIT 1
);

INSERT INTO major (major_code, major_name, college_id, status) VALUES
('080901', 'Computer-Science-and-Technology', @college_cs, 1),
('080902', 'Software-Engineering',            @college_cs, 1);

SET @major_cs := (
    SELECT major_id
    FROM major
    WHERE major_code = '080901'
    LIMIT 1
);

SET @major_se := (
    SELECT major_id
    FROM major
    WHERE major_code = '080902'
    LIMIT 1
);

UPDATE teacher
SET major_id = @major_cs,
    status = 1
WHERE teacher_no = 'T2024001';

UPDATE teacher
SET major_id = @major_se,
    status = 1
WHERE teacher_no IN ('T2024002', 'T2024003');

INSERT INTO academic_term (term_code, academic_year, semester, start_date, end_date, status)
VALUES ('2025-2026-1', 2025, 1, '2025-09-01', '2026-01-16', 1);

SET @term_main := (
    SELECT term_id
    FROM academic_term
    WHERE term_code = '2025-2026-1'
    LIMIT 1
);

-- ================================================================
-- 3. 妯″潡 A 鏁版嵁閲嶅缓锛氳绋嬨€佹瘯涓氳姹傘€佹寚鏍囩偣銆佸畯瑙傛敮鎾戠煩闃?-- ================================================================
INSERT INTO course (course_code, course_name, credit, status)
SELECT course_code, course_name, credit, 1
FROM tmp_e2e_course_def;

INSERT INTO course_major (course_id, major_id, grade_year)
SELECT
    c.course_id,
    m.major_id,
    ctx.grade_year
FROM tmp_e2e_course_def cd
JOIN course c
  ON c.course_code = cd.course_code
JOIN major m
  ON m.major_code = cd.major_code
JOIN (
    SELECT DISTINCT major_code, grade_year
    FROM tmp_e2e_context
) ctx
  ON ctx.major_code = cd.major_code;

INSERT INTO graduation_requirement (gr_code, gr_description, major_id, grade_year, status)
SELECT
    grt.gr_code,
    CONCAT(grt.gr_description, '(', m.major_name, '-', ctx.grade_year, '绾?'),
    m.major_id,
    ctx.grade_year,
    1
FROM tmp_e2e_context ctx
JOIN major m
  ON m.major_code = ctx.major_code
JOIN tmp_e2e_gr_template grt
  ON 1 = 1;

INSERT INTO indicator_point (ip_code, ip_description, gr_id, status)
SELECT
    ipt.ip_code,
    CONCAT(ipt.ip_description, '(', m.major_name, '-', ctx.grade_year, '绾?'),
    gr.gr_id,
    1
FROM tmp_e2e_context ctx
JOIN major m
  ON m.major_code = ctx.major_code
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
JOIN course c
  ON c.course_code = sp.course_code
JOIN major m
  ON m.major_code = sp.major_code
JOIN graduation_requirement gr
  ON gr.major_id = m.major_id
 AND gr.grade_year = sp.grade_year
 AND gr.gr_code = sp.gr_code
JOIN indicator_point ip
  ON ip.gr_id = gr.gr_id
 AND ip.ip_code = sp.ip_code;

-- ================================================================
-- 4. 妯″潡 B 鏁版嵁閲嶅缓锛氳绋嬬洰鏍囥€佽€冩牳鐐广€佸唴閮ㄦ潈閲?w
-- ================================================================
INSERT INTO course_objective (objective_code, co_description, course_id)
SELECT
    ot.objective_code,
    ot.co_description,
    c.course_id
FROM course c
JOIN tmp_e2e_course_def cd
  ON cd.course_code = c.course_code
JOIN tmp_e2e_objective_template ot
  ON 1 = 1;

INSERT INTO assessment_point (ap_name, full_score, co_id)
SELECT
    CONCAT(apt.ap_name, '(', c.course_name, ')'),
    apt.full_score,
    co.co_id
FROM course c
JOIN tmp_e2e_course_def cd
  ON cd.course_code = c.course_code
JOIN course_objective co
  ON co.course_id = c.course_id
JOIN tmp_e2e_ap_template apt
  ON apt.objective_code = co.objective_code;

INSERT INTO objective_indicator_contribution (co_id, ip_id, internal_weight)
SELECT
    co.co_id,
    ip.ip_id,
    CASE WHEN seq_map.seq_no = 1 THEN sp.weight_a ELSE sp.weight_b END AS internal_weight
FROM tmp_e2e_support_plan sp
JOIN course c
  ON c.course_code = sp.course_code
JOIN major m
  ON m.major_code = sp.major_code
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
) seq_map
  ON 1 = 1
JOIN course_objective co
  ON co.course_id = c.course_id
 AND co.objective_code = CASE WHEN seq_map.seq_no = 1 THEN sp.co_code_a ELSE sp.co_code_b END;

-- ================================================================
-- 5. 妯″潡 C 鏁版嵁閲嶅缓锛氬鐢熴€佹暀瀛︾彮銆佸師濮嬫垚缁?-- ================================================================
INSERT INTO student (student_no, student_name, major_id, enrollment_year, status)
SELECT
    sp.student_no,
    sp.student_name,
    m.major_id,
    sp.grade_year,
    1
FROM tmp_e2e_student_plan sp
JOIN major m
  ON m.major_code = sp.major_code;

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
JOIN course c
  ON c.course_code = cp.course_code
JOIN teacher t
  ON t.teacher_no = cp.teacher_no;

INSERT INTO student_class (student_id, class_id)
SELECT
    s.student_id,
    tc.class_id
FROM tmp_e2e_student_plan sp
JOIN student s
  ON s.student_no = sp.student_no
JOIN tmp_e2e_class_plan cp
  ON cp.major_code = sp.major_code
 AND cp.grade_year = sp.grade_year
JOIN teaching_class tc
  ON tc.class_code = cp.class_code;

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)
SELECT
    s.student_id,
    ap.ap_id,
    tc.class_id,
    ROUND(ap.full_score * sp.score_factor, 2) AS actual_score
FROM tmp_e2e_student_plan sp
JOIN student s
  ON s.student_no = sp.student_no
JOIN tmp_e2e_class_plan cp
  ON cp.major_code = sp.major_code
 AND cp.grade_year = sp.grade_year
 AND cp.calc_status IN ('locked', 'score_imported')
JOIN teaching_class tc
  ON tc.class_code = cp.class_code
JOIN course c
  ON c.course_id = tc.course_id
JOIN course_objective co
  ON co.course_id = c.course_id
JOIN assessment_point ap
  ON ap.co_id = co.co_id;

-- ================================================================
-- 6. 璇剧▼绾х粨鏋滀笌涓撲笟绾х粨鏋滈噸寤?-- 浠呬负 locked 鏁欏鐝敓鎴愯绋嬬骇缁撴灉
-- 浠呬负 SE2022_MAIN 鐢熸垚涓撲笟绾х粨鏋?-- ================================================================
INSERT INTO student_objective_achievement (student_id, class_id, co_id, achievement)
SELECT
    sas.student_id,
    sas.class_id,
    ap.co_id,
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS achievement
FROM student_assessment_score sas
JOIN assessment_point ap
  ON ap.ap_id = sas.ap_id
JOIN teaching_class tc
  ON tc.class_id = sas.class_id
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
    TRUE
FROM course_objective_achievement coa
JOIN teaching_class tc
  ON tc.class_id = coa.class_id
JOIN course_objective co
  ON co.co_id = coa.co_id
 AND co.course_id = tc.course_id
JOIN objective_indicator_contribution oic
  ON oic.co_id = coa.co_id
JOIN indicator_point ip
  ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr
  ON gr.gr_id = ip.gr_id
JOIN course_major cm
  ON cm.course_id = tc.course_id
 AND cm.major_id = gr.major_id
 AND cm.grade_year = gr.grade_year
WHERE tc.calc_status = 'locked'
  AND tc.grade_year = gr.grade_year
GROUP BY coa.class_id, oic.ip_id;

INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement)
SELECT
    @major_se,
    2022,
    @term_main,
    cia.ip_id,
    ROUND(SUM(cia.achievement * cis.total_weight), 4) AS final_achievement
FROM course_indicator_achievement cia
JOIN teaching_class tc
  ON tc.class_id = cia.class_id
JOIN course_indicator_support cis
  ON cis.course_id = tc.course_id
 AND cis.ip_id = cia.ip_id
JOIN tmp_e2e_class_plan cp
  ON cp.class_code = tc.class_code
WHERE cp.context_code = 'SE2022_MAIN'
  AND tc.calc_status = 'locked'
GROUP BY cia.ip_id;

INSERT INTO unlock_audit_log (class_id, request_by, approved_by, reason)
SELECT
    tc.class_id,
    t.id,
    1,
    'E2E-Preloaded-Unlock-Request-Modify-Scores-And-Recalculate'
FROM teaching_class tc
JOIN teacher t
  ON t.teacher_no = 'T2024002'
WHERE tc.class_code = 'E2E-SE22-01';

-- ================================================================
-- 7. 鎵ц鍚庤嚜妫€ SQL锛堟寜闇€鎵嬪姩鎵ц锛?-- ================================================================

-- 7.1 璐﹀彿浣撶郴鏈敼鍔?-- SELECT COUNT(*) AS user_count FROM sys_user;
-- SELECT COUNT(*) AS role_count FROM sys_role;
-- SELECT COUNT(*) AS permission_count FROM sys_permission;
-- SELECT COUNT(*) AS role_permission_count FROM sys_role_permission;
-- SELECT COUNT(*) AS user_role_count FROM sys_user_role;

-- 7.2 闈?E2E 鏃т笟鍔℃暟鎹凡娓呯悊
-- SELECT COUNT(*) AS non_e2e_course_count
-- FROM course
-- WHERE course_code NOT LIKE 'E2E-%';
--
-- SELECT COUNT(*) AS non_e2e_class_count
-- FROM teaching_class
-- WHERE class_code NOT LIKE 'E2E-%';
--
-- SELECT COUNT(*) AS non_e2e_student_count
-- FROM student
-- WHERE student_no NOT LIKE 'E2E%';
--
-- SELECT COUNT(*) AS non_egr_count
-- FROM graduation_requirement
-- WHERE gr_code NOT LIKE 'EGR%';

-- 7.3 涓撲笟涓庡勾绾у畬鏁存€?-- SELECT m.major_code, m.major_name, gr.grade_year,
--        COUNT(DISTINCT gr.gr_id) AS gr_count,
--        COUNT(DISTINCT ip.ip_id) AS ip_count
-- FROM graduation_requirement gr
-- JOIN major m ON m.major_id = gr.major_id
-- LEFT JOIN indicator_point ip ON ip.gr_id = gr.gr_id
-- GROUP BY m.major_code, m.major_name, gr.grade_year
-- ORDER BY m.major_code, gr.grade_year;

-- 7.4 瀹忚鏀拺鐭╅樀 W 姹傚拰鏍￠獙
-- SELECT m.major_code, gr.grade_year, gr.gr_code, ip.ip_code,
--        COUNT(*) AS support_course_count,
--        ROUND(SUM(cis.total_weight), 4) AS weight_sum
-- FROM course_indicator_support cis
-- JOIN indicator_point ip ON ip.ip_id = cis.ip_id
-- JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
-- JOIN major m ON m.major_id = gr.major_id
-- GROUP BY m.major_code, gr.grade_year, gr.gr_code, ip.ip_code
-- ORDER BY m.major_code, gr.grade_year, gr.gr_code, ip.ip_code;

-- 7.5 鍐呴儴鏉冮噸 w 姹傚拰鏍￠獙
-- SELECT c.course_code, gr.grade_year, ip.ip_code,
--        ROUND(SUM(oic.internal_weight), 4) AS weight_sum
-- FROM objective_indicator_contribution oic
-- JOIN course_objective co ON co.co_id = oic.co_id
-- JOIN course c ON c.course_id = co.course_id
-- JOIN indicator_point ip ON ip.ip_id = oic.ip_id
-- JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
-- GROUP BY c.course_code, gr.grade_year, ip.ip_code
-- ORDER BY c.course_code, gr.grade_year, ip.ip_code;

-- 7.6 鏁欏鐝姸鎬佸垎甯?-- SELECT class_code, class_name, grade_year, calc_status
-- FROM teaching_class
-- ORDER BY class_code;

-- 7.7 鎴愮哗瀹屾暣鎬?-- SELECT tc.class_code, tc.calc_status,
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
-- GROUP BY tc.class_code, tc.calc_status
-- ORDER BY tc.class_code;

-- 7.8 璇剧▼绾х粨鏋滃畬鏁存€?-- SELECT tc.class_code,
--        COUNT(DISTINCT soa.soa_id) AS soa_count,
--        COUNT(DISTINCT coa.coa_id) AS coa_count,
--        COUNT(DISTINCT cia.cia_id) AS cia_count
-- FROM teaching_class tc
-- LEFT JOIN student_objective_achievement soa ON soa.class_id = tc.class_id
-- LEFT JOIN course_objective_achievement coa ON coa.class_id = tc.class_id
-- LEFT JOIN course_indicator_achievement cia ON cia.class_id = tc.class_id
-- GROUP BY tc.class_code
-- ORDER BY tc.class_code;

-- 7.9 涓撲笟绾х粨鏋滀笌闃绘柇鎬佹牎楠?-- SELECT m.major_code, mia.grade_year, at.term_code, COUNT(*) AS mia_count
-- FROM major_indicator_achievement mia
-- JOIN major m ON m.major_id = mia.major_id
-- JOIN academic_term at ON at.term_id = mia.term_id
-- GROUP BY m.major_code, mia.grade_year, at.term_code
-- ORDER BY m.major_code, mia.grade_year;
