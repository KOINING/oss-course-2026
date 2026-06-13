-- ================================================================
-- week5_sample_data.sql — 第五周报表与台账演示数据
-- ================================================================
-- 执行前提：
--   1. 已执行 gra_db_full.sql
--   2. 当前数据库为 MySQL 8.x / MariaDB 10.5+ 兼容语法
--
-- 设计目标：
--   1. 课程级评价报表能够按“课程 + 年级”聚合多个教学班明细
--   2. 专业级评价报告和雷达图具备完整 12 个指标点结果
--   3. 穿透式台账能够从毕业要求一路追溯到原始成绩
--
-- 重要约束：
--   以 gra_db_full.sql 为准，不使用旧字段/旧表名：
--   - student_objective_achievement
--   - graduation_requirement.grade_year
--   - teaching_class.class_code
-- ================================================================

USE GraduationDB;

-- ================================================================
-- 0. 业务上下文
-- ================================================================

SET @w5_major_code := '080901';
SET @w5_course_code := 'CS201';
SET @w5_term_code := '2024-2025-1';
SET @w5_main_class_code := 'TC2024CS01';
SET @w5_parallel_class_code := 'W5-CS201-2022-02';

SET @w5_major_id := (
    SELECT major_id FROM major WHERE major_code = @w5_major_code LIMIT 1
);
SET @w5_course_id := (
    SELECT course_id FROM course WHERE course_code = @w5_course_code LIMIT 1
);
SET @w5_term_id := (
    SELECT term_id FROM academic_term WHERE term_code = @w5_term_code LIMIT 1
);
SET @w5_teacher_zhang := (
    SELECT id FROM teacher WHERE teacher_no = 'T2024001' LIMIT 1
);
SET @w5_teacher_zhao := (
    SELECT id FROM teacher WHERE teacher_no = 'T2024004' LIMIT 1
);

-- 确保数据结构课程面向计科 2022 级。
INSERT INTO course_major (course_id, major_id, grade_year)
VALUES (@w5_course_id, @w5_major_id, 2022)
ON DUPLICATE KEY UPDATE grade_year = VALUES(grade_year);

-- 原全量脚本中 TC2024CS01 已有课程级结果，但状态默认未锁定；
-- 第五周报表演示需要它作为已完成评价的主教学班。
UPDATE teaching_class
SET grade_year = 2022,
    calc_status = 'locked'
WHERE class_code = @w5_main_class_code;

-- ================================================================
-- 1. 补齐主教学班 TC2024CS01 的成绩链路
-- ================================================================
-- gra_db_full.sql 第三周补了 51-60 号学生到数据结构班，但未补这些学生的原始成绩。
-- 这里按固定得分率补齐，保证“学生名单、原始成绩、中间结果、课程级结果”一致。

DROP TEMPORARY TABLE IF EXISTS tmp_w5_main_score_factor;
CREATE TEMPORARY TABLE tmp_w5_main_score_factor (
    student_no VARCHAR(20) PRIMARY KEY,
    score_factor FLOAT NOT NULL
);

INSERT INTO tmp_w5_main_score_factor (student_no, score_factor) VALUES
('20220101051', 0.82),
('20220101052', 0.84),
('20220101053', 0.86),
('20220101054', 0.80),
('20220101055', 0.78),
('20220101056', 0.83),
('20220101057', 0.81),
('20220101058', 0.85),
('20220101059', 0.79),
('20220101060', 0.87);

SET @w5_main_class_id := (
    SELECT class_id FROM teaching_class WHERE class_code = @w5_main_class_code LIMIT 1
);

INSERT INTO student_class (student_id, class_id)
SELECT s.student_id, @w5_main_class_id
FROM tmp_w5_main_score_factor f
JOIN student s ON s.student_no = f.student_no
ON DUPLICATE KEY UPDATE class_id = VALUES(class_id);

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)
SELECT
    s.student_id,
    ap.ap_id,
    @w5_main_class_id,
    ROUND(ap.full_score * f.score_factor, 2) AS actual_score
FROM tmp_w5_main_score_factor f
JOIN student s ON s.student_no = f.student_no
JOIN course_objective co ON co.course_id = @w5_course_id
JOIN assessment_point ap ON ap.co_id = co.co_id
ON DUPLICATE KEY UPDATE actual_score = VALUES(actual_score);

-- ================================================================
-- 2. 新增同课程同年级平行教学班
-- ================================================================

INSERT INTO teaching_class (
    class_code,
    class_name,
    course_id,
    term_id,
    teacher_id,
    grade_year,
    calc_status
)
VALUES (
    @w5_parallel_class_code,
    '数据结构2022级平行2班',
    @w5_course_id,
    @w5_term_id,
    @w5_teacher_zhao,
    2022,
    'locked'
)
ON DUPLICATE KEY UPDATE
    class_name = VALUES(class_name),
    course_id = VALUES(course_id),
    term_id = VALUES(term_id),
    teacher_id = VALUES(teacher_id),
    grade_year = VALUES(grade_year),
    calc_status = VALUES(calc_status);

SET @w5_parallel_class_id := (
    SELECT class_id FROM teaching_class WHERE class_code = @w5_parallel_class_code LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS tmp_w5_parallel_student;
CREATE TEMPORARY TABLE tmp_w5_parallel_student (
    student_no VARCHAR(20) PRIMARY KEY,
    student_name VARCHAR(50) NOT NULL,
    score_factor FLOAT NOT NULL
);

INSERT INTO tmp_w5_parallel_student (student_no, student_name, score_factor) VALUES
('20220101061', '谢雨桐', 0.70),
('20220101062', '熊明哲', 0.74),
('20220101063', '江若曦', 0.78),
('20220101064', '田凯文', 0.82),
('20220101065', '范浩然', 0.76);

INSERT INTO student (student_no, student_name, major_id, enrollment_year, status)
SELECT student_no, student_name, @w5_major_id, 2022, 1
FROM tmp_w5_parallel_student
ON DUPLICATE KEY UPDATE
    student_name = VALUES(student_name),
    major_id = VALUES(major_id),
    enrollment_year = VALUES(enrollment_year),
    status = VALUES(status);

INSERT INTO student_class (student_id, class_id)
SELECT s.student_id, @w5_parallel_class_id
FROM tmp_w5_parallel_student p
JOIN student s ON s.student_no = p.student_no
ON DUPLICATE KEY UPDATE class_id = VALUES(class_id);

INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)
SELECT
    s.student_id,
    ap.ap_id,
    @w5_parallel_class_id,
    ROUND(ap.full_score * p.score_factor, 2) AS actual_score
FROM tmp_w5_parallel_student p
JOIN student s ON s.student_no = p.student_no
JOIN course_objective co ON co.course_id = @w5_course_id
JOIN assessment_point ap ON ap.co_id = co.co_id
ON DUPLICATE KEY UPDATE actual_score = VALUES(actual_score);

-- ================================================================
-- 3. 从原始成绩重算学生目标达成度、班级目标达成度、课程级指标点达成度
-- ================================================================
-- L1 学生目标达成度：student + class + co

INSERT INTO student_objective_achievement (student_id, class_id, co_id, achievement)
SELECT
    sas.student_id,
    sas.class_id,
    ap.co_id,
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS achievement
FROM student_assessment_score sas
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
WHERE tc.class_code IN (@w5_main_class_code, @w5_parallel_class_code)
GROUP BY sas.student_id, sas.class_id, ap.co_id
ON DUPLICATE KEY UPDATE achievement = VALUES(achievement);

-- L1 班级目标达成度：class + co

INSERT INTO course_objective_achievement (class_id, co_id, average_achievement)
SELECT
    soa.class_id,
    soa.co_id,
    ROUND(AVG(soa.achievement), 4) AS average_achievement
FROM student_objective_achievement soa
JOIN teaching_class tc ON tc.class_id = soa.class_id
WHERE tc.class_code IN (@w5_main_class_code, @w5_parallel_class_code)
GROUP BY soa.class_id, soa.co_id
ON DUPLICATE KEY UPDATE average_achievement = VALUES(average_achievement);

-- L2 课程级指标点达成度：class + ip
-- 采用归一化加权平均：SUM(CO达成度 * w) / SUM(w)。
-- 当同一课程-指标点下 w 已配平为 1 时，它与普通加权求和等价。

INSERT INTO course_indicator_achievement (class_id, ip_id, achievement, is_locked)
SELECT
    coa.class_id,
    oic.ip_id,
    ROUND(SUM(coa.average_achievement * oic.internal_weight) / NULLIF(SUM(oic.internal_weight), 0), 4) AS achievement,
    TRUE AS is_locked
FROM course_objective_achievement coa
JOIN teaching_class tc ON tc.class_id = coa.class_id
JOIN course_objective co
    ON co.co_id = coa.co_id
   AND co.course_id = tc.course_id
JOIN objective_indicator_contribution oic ON oic.co_id = coa.co_id
JOIN course_indicator_support cis
    ON cis.course_id = tc.course_id
   AND cis.ip_id = oic.ip_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN course_major cm
    ON cm.course_id = tc.course_id
   AND cm.major_id = gr.major_id
   AND cm.grade_year = gr.grade_year
WHERE tc.class_code IN (@w5_main_class_code, @w5_parallel_class_code)
  AND tc.grade_year = gr.grade_year
GROUP BY coa.class_id, oic.ip_id
ON DUPLICATE KEY UPDATE
    achievement = VALUES(achievement),
    is_locked = VALUES(is_locked);

-- ================================================================
-- 4. 补齐专业级评价报告与雷达图结果
-- ================================================================
-- gra_db_full.sql 初始只有 8 个 2022 级专业指标点结果。
-- 第五周报表需要完整雷达图，因此补齐计科 2022 级 12 个指标点。
-- 其中 1.1 / 3.1 / 5.1 使用本次两个数据结构教学班的课程级聚合值作为演示口径；
-- 其他指标点使用稳定样例值，确保专业级报告、达标统计、雷达图均可完整展示。

DROP TEMPORARY TABLE IF EXISTS tmp_w5_course_grade_result;
CREATE TEMPORARY TABLE tmp_w5_course_grade_result AS
SELECT
    tc.course_id,
    tc.grade_year,
    cia.ip_id,
    ROUND(SUM(cia.achievement * ec.evaluated_students) / SUM(ec.evaluated_students), 4) AS achievement
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS evaluated_students
    FROM student_objective_achievement
    GROUP BY class_id
) ec ON ec.class_id = cia.class_id
WHERE tc.course_id = @w5_course_id
  AND tc.grade_year = 2022
  AND tc.calc_status = 'locked'
GROUP BY tc.course_id, tc.grade_year, cia.ip_id;

INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement)
SELECT
    gr.major_id,
    gr.grade_year,
    @w5_term_id AS term_id,
    ip.ip_id,
    COALESCE(
        cgr.achievement,
        CASE ip.ip_code
            WHEN '1.2' THEN 0.7100
            WHEN '2.1' THEN 0.7650
            WHEN '2.2' THEN 0.5800
            WHEN '3.2' THEN 0.6900
            WHEN '4.1' THEN 0.7400
            WHEN '4.2' THEN 0.7100
            WHEN '5.2' THEN 0.6800
            WHEN '6.1' THEN 0.8000
            WHEN '6.2' THEN 0.7700
            ELSE 0.7000
        END
    ) AS final_achievement
FROM graduation_requirement gr
JOIN indicator_point ip ON ip.gr_id = gr.gr_id
LEFT JOIN tmp_w5_course_grade_result cgr
    ON cgr.ip_id = ip.ip_id
   AND cgr.grade_year = gr.grade_year
WHERE gr.major_id = @w5_major_id
  AND gr.grade_year = 2022
ON DUPLICATE KEY UPDATE final_achievement = VALUES(final_achievement);

-- ================================================================
-- 5. 执行后快速核验
-- ================================================================
-- 1) 数据结构 + 2022 级应至少有两个 locked 教学班：
-- SELECT c.course_code, tc.grade_year, COUNT(*) AS locked_class_count
-- FROM teaching_class tc
-- JOIN course c ON c.course_id = tc.course_id
-- WHERE c.course_code = 'CS201'
--   AND tc.grade_year = 2022
--   AND tc.calc_status = 'locked'
-- GROUP BY c.course_code, tc.grade_year;
--
-- 2) 专业级 2022 级雷达图应覆盖 12 个指标点：
-- SELECT COUNT(*) AS radar_ip_count
-- FROM major_indicator_achievement mia
-- WHERE mia.major_id = @w5_major_id
--   AND mia.grade_year = 2022
--   AND mia.term_id = @w5_term_id;
--
-- 3) 穿透链路应能查到两个数据结构教学班的原始成绩：
-- SELECT tc.class_code, COUNT(DISTINCT sas.student_id) AS students, COUNT(*) AS score_rows
-- FROM student_assessment_score sas
-- JOIN teaching_class tc ON tc.class_id = sas.class_id
-- WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
-- GROUP BY tc.class_code;
-- ================================================================
