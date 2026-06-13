-- ================================================================
-- week5_self_check.sql — 第五周报表、台账、字段链路自检 SQL
-- ================================================================
-- 执行前提：
--   1. 已执行 gra_db_full.sql
--   2. 已执行 week5_sample_data.sql
--
-- 自检目标：
--   1. 课程级评价报表按“课程 + 年级 + 指标点”聚合多个教学班
--   2. 专业级评价报告和雷达图覆盖完整指标点
--   3. 穿透式台账可从 GR/IP 追溯到课程、教学班、课程目标、考核点、原始成绩
--   4. 原始成绩表、考核点表、课程目标表、学生目标达成结果表字段链路一致
-- ================================================================

USE GraduationDB;

SET @w5_major_code := '080901';
SET @w5_course_code := 'CS201';
SET @w5_grade_year := 2022;
SET @w5_term_code := '2024-2025-1';

SET @w5_major_id := (
    SELECT major_id FROM major WHERE major_code = @w5_major_code LIMIT 1
);
SET @w5_course_id := (
    SELECT course_id FROM course WHERE course_code = @w5_course_code LIMIT 1
);
SET @w5_term_id := (
    SELECT term_id FROM academic_term WHERE term_code = @w5_term_code LIMIT 1
);

-- ================================================================
-- 一、课程级评价报表自检
-- ================================================================

-- 1.1 确认“课程 + 年级”存在多个已锁定教学班
SELECT
    c.course_code,
    c.course_name,
    tc.grade_year,
    COUNT(DISTINCT tc.class_id) AS locked_class_count,
    GROUP_CONCAT(
        CONCAT(tc.class_code, ':', tc.class_name)
        ORDER BY tc.class_code SEPARATOR ' | '
    ) AS locked_classes,
    CASE
        WHEN COUNT(DISTINCT tc.class_id) > 1 THEN 'OK: 可演示多教学班聚合'
        ELSE 'MISMATCH: 仍是单教学班，课程级报表可能误收口'
    END AS check_result
FROM teaching_class tc
JOIN course c ON c.course_id = tc.course_id
WHERE c.course_code = @w5_course_code
  AND tc.grade_year = @w5_grade_year
  AND tc.calc_status = 'locked'
GROUP BY c.course_id, c.course_code, c.course_name, tc.grade_year;

-- 1.2 聚合前：课程级各教学班明细
SELECT
    c.course_code,
    c.course_name,
    tc.grade_year,
    tc.class_code,
    tc.class_name,
    t.teacher_name,
    ip.ip_code,
    ip.ip_description,
    cia.achievement AS class_ip_achievement,
    COALESCE(ec.evaluated_students, 0) AS evaluated_students,
    COALESCE(sc.enrolled_students, 0) AS enrolled_students,
    COALESCE(score.score_rows, 0) AS score_rows
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN teacher t ON t.id = tc.teacher_id
JOIN indicator_point ip ON ip.ip_id = cia.ip_id
LEFT JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS evaluated_students
    FROM student_objective_achievement
    GROUP BY class_id
) ec ON ec.class_id = tc.class_id
LEFT JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS enrolled_students
    FROM student_class
    GROUP BY class_id
) sc ON sc.class_id = tc.class_id
LEFT JOIN (
    SELECT class_id, COUNT(*) AS score_rows
    FROM student_assessment_score
    GROUP BY class_id
) score ON score.class_id = tc.class_id
WHERE c.course_code = @w5_course_code
  AND tc.grade_year = @w5_grade_year
  AND tc.calc_status = 'locked'
ORDER BY ip.ip_code, tc.class_code;

-- 1.3 聚合后：课程级报表结果
-- 正确口径：GROUP BY course_id + grade_year + ip_id。
-- 加权口径：按已形成 student_objective_achievement 的参评学生数加权。
SELECT
    c.course_code AS '课程代码',
    c.course_name AS '课程名称',
    tc.grade_year AS '年级',
    ip.ip_code AS '指标点编码',
    ip.ip_description AS '指标点描述',
    ROUND(
        SUM(cia.achievement * ec.evaluated_students) / NULLIF(SUM(ec.evaluated_students), 0),
        4
    ) AS '课程级达成度',
    CASE
        WHEN ROUND(SUM(cia.achievement * ec.evaluated_students) / NULLIF(SUM(ec.evaluated_students), 0), 4) >= 0.60
            THEN '合格'
        ELSE '不合格'
    END AS '评价',
    SUM(ec.evaluated_students) AS '参评学生数',
    SUM(sc.enrolled_students) AS '选课学生数',
    COUNT(DISTINCT tc.class_id) AS '教学班数',
    GROUP_CONCAT(
        CONCAT(tc.class_code, '=', cia.achievement, '(', ec.evaluated_students, '人)')
        ORDER BY tc.class_code SEPARATOR ' | '
    ) AS '教学班明细'
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN indicator_point ip ON ip.ip_id = cia.ip_id
JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS evaluated_students
    FROM student_objective_achievement
    GROUP BY class_id
) ec ON ec.class_id = tc.class_id
LEFT JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS enrolled_students
    FROM student_class
    GROUP BY class_id
) sc ON sc.class_id = tc.class_id
WHERE c.course_code = @w5_course_code
  AND tc.grade_year = @w5_grade_year
  AND tc.calc_status = 'locked'
GROUP BY c.course_id, c.course_code, c.course_name, tc.grade_year, ip.ip_id, ip.ip_code, ip.ip_description
ORDER BY ip.ip_code;

-- 1.4 反例对照：简单平均 vs 参评学生数加权平均
-- 若两个值不同，说明不能把多教学班报表误写成 AVG(cia.achievement)。
SELECT
    c.course_code,
    tc.grade_year,
    ip.ip_code,
    ROUND(AVG(cia.achievement), 4) AS simple_avg_for_compare,
    ROUND(SUM(cia.achievement * ec.evaluated_students) / NULLIF(SUM(ec.evaluated_students), 0), 4) AS weighted_avg,
    COUNT(DISTINCT tc.class_id) AS class_count,
    GROUP_CONCAT(
        CONCAT(tc.class_code, ':', cia.achievement, '/', ec.evaluated_students, '人')
        ORDER BY tc.class_code SEPARATOR ' | '
    ) AS detail
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN indicator_point ip ON ip.ip_id = cia.ip_id
JOIN (
    SELECT class_id, COUNT(DISTINCT student_id) AS evaluated_students
    FROM student_objective_achievement
    GROUP BY class_id
) ec ON ec.class_id = tc.class_id
WHERE c.course_code = @w5_course_code
  AND tc.grade_year = @w5_grade_year
  AND tc.calc_status = 'locked'
GROUP BY c.course_id, tc.grade_year, ip.ip_id, ip.ip_code
HAVING COUNT(DISTINCT tc.class_id) > 1
ORDER BY ip.ip_code;

-- ================================================================
-- 二、专业级评价报告与雷达图自检
-- ================================================================

-- 2.1 专业级评价报告
SELECT
    m.major_code AS '专业代码',
    m.major_name AS '专业',
    mia.grade_year AS '年级',
    at.term_code AS '学期',
    gr.gr_code AS '毕业要求编码',
    ip.ip_code AS '指标点编码',
    ip.ip_description AS '指标点描述',
    mia.final_achievement AS '达成度',
    CASE WHEN mia.final_achievement >= 0.60 THEN '合格' ELSE '不合格' END AS '合格状态',
    CASE
        WHEN mia.final_achievement >= 0.70 THEN '#22c55e'
        WHEN mia.final_achievement >= 0.60 THEN '#eab308'
        ELSE '#ef4444'
    END AS '雷达图颜色'
FROM major_indicator_achievement mia
JOIN major m ON m.major_id = mia.major_id
JOIN academic_term at ON at.term_id = mia.term_id
JOIN indicator_point ip ON ip.ip_id = mia.ip_id
JOIN graduation_requirement gr
    ON gr.gr_id = ip.gr_id
   AND gr.major_id = mia.major_id
   AND gr.grade_year = mia.grade_year
WHERE m.major_code = @w5_major_code
  AND mia.grade_year = @w5_grade_year
  AND at.term_code = @w5_term_code
ORDER BY gr.gr_code, ip.ip_code;

-- 2.2 雷达图数据：按毕业要求分组输出 JSON
SELECT
    gr.gr_code,
    gr.gr_description,
    JSON_ARRAYAGG(
        JSON_OBJECT(
            'ipCode', ip.ip_code,
            'achievement', mia.final_achievement,
            'pass', mia.final_achievement >= 0.60,
            'color',
                CASE
                    WHEN mia.final_achievement >= 0.70 THEN '#22c55e'
                    WHEN mia.final_achievement >= 0.60 THEN '#eab308'
                    ELSE '#ef4444'
                END
        )
    ) AS radar_points
FROM major_indicator_achievement mia
JOIN indicator_point ip ON ip.ip_id = mia.ip_id
JOIN graduation_requirement gr
    ON gr.gr_id = ip.gr_id
   AND gr.major_id = mia.major_id
   AND gr.grade_year = mia.grade_year
WHERE mia.major_id = @w5_major_id
  AND mia.grade_year = @w5_grade_year
  AND mia.term_id = @w5_term_id
GROUP BY gr.gr_id, gr.gr_code, gr.gr_description
ORDER BY gr.gr_code;

-- 2.3 专业级达标统计
SELECT
    m.major_name AS major_name,
    mia.grade_year,
    at.term_code,
    COUNT(*) AS total_ip_count,
    SUM(CASE WHEN mia.final_achievement >= 0.60 THEN 1 ELSE 0 END) AS passed_ip_count,
    SUM(CASE WHEN mia.final_achievement < 0.60 THEN 1 ELSE 0 END) AS failed_ip_count,
    ROUND(AVG(mia.final_achievement), 4) AS avg_achievement,
    ROUND(MIN(mia.final_achievement), 4) AS min_achievement,
    ROUND(MAX(mia.final_achievement), 4) AS max_achievement
FROM major_indicator_achievement mia
JOIN major m ON m.major_id = mia.major_id
JOIN academic_term at ON at.term_id = mia.term_id
WHERE m.major_code = @w5_major_code
  AND mia.grade_year = @w5_grade_year
  AND at.term_code = @w5_term_code
GROUP BY m.major_name, mia.grade_year, at.term_code;

-- 2.4 雷达图完整性：实际指标点数应等于该专业该年级 GR 下的指标点总数
SELECT
    m.major_code,
    m.major_name,
    @w5_grade_year AS grade_year,
    @w5_term_code AS term_code,
    expected.expected_ip_count,
    COALESCE(actual.actual_ip_count, 0) AS actual_ip_count,
    CASE
        WHEN COALESCE(actual.actual_ip_count, 0) = expected.expected_ip_count THEN 'OK: 雷达图完整'
        ELSE CONCAT('MISMATCH: 缺失 ', expected.expected_ip_count - COALESCE(actual.actual_ip_count, 0), ' 个指标点')
    END AS check_result
FROM major m
JOIN (
    SELECT gr.major_id, gr.grade_year, COUNT(DISTINCT ip.ip_id) AS expected_ip_count
    FROM graduation_requirement gr
    JOIN indicator_point ip ON ip.gr_id = gr.gr_id
    WHERE gr.grade_year = @w5_grade_year
    GROUP BY gr.major_id, gr.grade_year
) expected ON expected.major_id = m.major_id
LEFT JOIN (
    SELECT mia.major_id, mia.grade_year, mia.term_id, COUNT(DISTINCT mia.ip_id) AS actual_ip_count
    FROM major_indicator_achievement mia
    GROUP BY mia.major_id, mia.grade_year, mia.term_id
) actual
    ON actual.major_id = expected.major_id
   AND actual.grade_year = expected.grade_year
   AND actual.term_id = @w5_term_id
WHERE m.major_code = @w5_major_code;

-- ================================================================
-- 三、穿透式台账自检
-- ================================================================

-- 3.1 完整穿透台账：GR -> IP -> 课程 -> 教学班 -> 课程目标 -> 考核点 -> 原始成绩
SELECT
    gr.gr_code AS '毕业要求',
    gr.grade_year AS '年级',
    ip.ip_code AS '指标点',
    c.course_code AS '支撑课程',
    c.course_name AS '课程名称',
    tc.class_code AS '教学班编号',
    tc.class_name AS '教学班',
    co.objective_code AS '课程目标',
    ap.ap_name AS '考核点',
    ap.full_score AS '满分',
    sas.actual_score AS '实际得分',
    ROUND(sas.actual_score / ap.full_score, 4) AS '得分率',
    s.student_no AS '学号',
    s.student_name AS '姓名',
    cis.total_weight AS '宏观权重W',
    oic.internal_weight AS '微观权重w'
FROM student_assessment_score sas
JOIN student s ON s.student_id = sas.student_id
JOIN student_class sc
    ON sc.student_id = sas.student_id
   AND sc.class_id = sas.class_id
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN teaching_class tc
    ON tc.class_id = sas.class_id
   AND tc.course_id = co.course_id
JOIN course c ON c.course_id = tc.course_id
JOIN objective_indicator_contribution oic ON oic.co_id = co.co_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN course_major cm
    ON cm.course_id = c.course_id
   AND cm.major_id = gr.major_id
   AND cm.grade_year = gr.grade_year
JOIN course_indicator_support cis
    ON cis.course_id = c.course_id
   AND cis.ip_id = ip.ip_id
WHERE gr.major_id = @w5_major_id
  AND gr.grade_year = @w5_grade_year
  AND c.course_code = @w5_course_code
ORDER BY ip.ip_code, tc.class_code, s.student_no, co.objective_code, ap.ap_id;

-- 3.2 台账汇总：按“指标点 + 教学班 + 学生 + 课程目标”聚合
SELECT
    ip.ip_code AS '指标点',
    c.course_code AS '课程',
    tc.class_code AS '教学班编号',
    s.student_no AS '学号',
    s.student_name AS '姓名',
    co.objective_code AS '课程目标',
    SUM(sas.actual_score) AS '目标总得分',
    SUM(ap.full_score) AS '目标总分',
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS '目标达成度',
    oic.internal_weight AS 'w权重',
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score) * oic.internal_weight, 4) AS '加权贡献'
FROM student_assessment_score sas
JOIN student s ON s.student_id = sas.student_id
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN teaching_class tc
    ON tc.class_id = sas.class_id
   AND tc.course_id = co.course_id
JOIN course c ON c.course_id = tc.course_id
JOIN objective_indicator_contribution oic ON oic.co_id = co.co_id
JOIN indicator_point ip ON ip.ip_id = oic.ip_id
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
WHERE gr.major_id = @w5_major_id
  AND gr.grade_year = @w5_grade_year
  AND c.course_code = @w5_course_code
GROUP BY ip.ip_code, c.course_code, tc.class_code, s.student_no, s.student_name, co.objective_code, oic.internal_weight
ORDER BY ip.ip_code, tc.class_code, s.student_no, co.objective_code;

-- 3.3 穿透链路覆盖检查：每个数据结构指标点都应有课程目标、考核点、成绩记录
SELECT
    gr.gr_code,
    ip.ip_code,
    c.course_code,
    COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL THEN co.co_id END) AS objective_count,
    COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL THEN ap.ap_id END) AS assessment_point_count,
    COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL AND tc.class_id IS NOT NULL THEN sas.sas_id END) AS score_row_count,
    COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL AND tc.class_id IS NOT NULL THEN sas.student_id END) AS scored_student_count,
    CASE
        WHEN COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL THEN co.co_id END) = 0 THEN 'MISMATCH: 无课程目标'
        WHEN COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL THEN ap.ap_id END) = 0 THEN 'MISMATCH: 无考核点'
        WHEN COUNT(DISTINCT CASE WHEN oic.oic_id IS NOT NULL AND tc.class_id IS NOT NULL THEN sas.sas_id END) = 0 THEN 'MISMATCH: 无原始成绩'
        ELSE 'OK: 穿透链路完整'
    END AS check_result
FROM graduation_requirement gr
JOIN indicator_point ip ON ip.gr_id = gr.gr_id
JOIN course_indicator_support cis ON cis.ip_id = ip.ip_id
JOIN course c ON c.course_id = cis.course_id
LEFT JOIN course_objective co ON co.course_id = c.course_id
LEFT JOIN objective_indicator_contribution oic
    ON oic.co_id = co.co_id
   AND oic.ip_id = ip.ip_id
LEFT JOIN assessment_point ap ON ap.co_id = co.co_id
LEFT JOIN student_assessment_score sas ON sas.ap_id = ap.ap_id
LEFT JOIN teaching_class tc
    ON tc.class_id = sas.class_id
   AND tc.course_id = c.course_id
   AND tc.grade_year = gr.grade_year
WHERE gr.major_id = @w5_major_id
  AND gr.grade_year = @w5_grade_year
  AND c.course_code = @w5_course_code
GROUP BY gr.gr_code, ip.ip_code, c.course_code
ORDER BY gr.gr_code, ip.ip_code;

-- ================================================================
-- 四、字段链路一致性自检
-- ================================================================

-- 4.1 原始成绩表 -> 考核点表 -> 课程目标表 -> 教学班课程 是否一致
SELECT
    sas.sas_id,
    s.student_no,
    tc.class_code,
    ap.ap_name,
    co.objective_code,
    c.course_code AS objective_course,
    tc_course.course_code AS class_course,
    CASE
        WHEN co.course_id = tc.course_id THEN 'OK'
        ELSE 'MISMATCH: 成绩考核点所属课程与教学班课程不一致'
    END AS check_result
FROM student_assessment_score sas
JOIN student s ON s.student_id = sas.student_id
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN course c ON c.course_id = co.course_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN course tc_course ON tc_course.course_id = tc.course_id
WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
  AND co.course_id <> tc.course_id;

-- 期望：无结果。

-- 4.2 原始成绩重算 SOA：student_objective_achievement 应等于按 co_id 汇总的得分率
SELECT
    tc.class_code,
    s.student_no,
    co.objective_code,
    ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) AS computed_achievement,
    soa.achievement AS stored_achievement,
    CASE
        WHEN ABS(ROUND(SUM(sas.actual_score) / SUM(ap.full_score), 4) - soa.achievement) < 0.0001
            THEN 'OK'
        ELSE 'MISMATCH'
    END AS check_result
FROM student_assessment_score sas
JOIN student s ON s.student_id = sas.student_id
JOIN assessment_point ap ON ap.ap_id = sas.ap_id
JOIN course_objective co ON co.co_id = ap.co_id
JOIN teaching_class tc ON tc.class_id = sas.class_id
JOIN student_objective_achievement soa
    ON soa.student_id = sas.student_id
   AND soa.class_id = sas.class_id
   AND soa.co_id = co.co_id
WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
GROUP BY tc.class_code, s.student_no, co.objective_code, soa.achievement
ORDER BY tc.class_code, s.student_no, co.objective_code;

-- 4.3 SOA 重算 COA：course_objective_achievement 应等于学生目标达成度均值
SELECT
    tc.class_code,
    co.objective_code,
    ROUND(AVG(soa.achievement), 4) AS computed_average,
    coa.average_achievement AS stored_average,
    CASE
        WHEN ABS(ROUND(AVG(soa.achievement), 4) - coa.average_achievement) < 0.0001
            THEN 'OK'
        ELSE 'MISMATCH'
    END AS check_result
FROM course_objective_achievement coa
JOIN teaching_class tc ON tc.class_id = coa.class_id
JOIN course_objective co ON co.co_id = coa.co_id
JOIN student_objective_achievement soa
    ON soa.class_id = coa.class_id
   AND soa.co_id = coa.co_id
WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
GROUP BY tc.class_code, co.objective_code, coa.average_achievement
ORDER BY tc.class_code, co.objective_code;

-- 4.4 COA + w 重算 CIA：course_indicator_achievement 应等于归一化加权平均
SELECT
    tc.class_code,
    ip.ip_code,
    ROUND(SUM(coa.average_achievement * oic.internal_weight) / NULLIF(SUM(oic.internal_weight), 0), 4) AS computed_ip_achievement,
    cia.achievement AS stored_ip_achievement,
    ROUND(SUM(oic.internal_weight), 4) AS micro_weight_sum,
    CASE
        WHEN ABS(
            ROUND(SUM(coa.average_achievement * oic.internal_weight) / NULLIF(SUM(oic.internal_weight), 0), 4)
            - cia.achievement
        ) < 0.0001 THEN 'OK'
        ELSE 'MISMATCH'
    END AS check_result
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN indicator_point ip ON ip.ip_id = cia.ip_id
JOIN course_objective_achievement coa ON coa.class_id = tc.class_id
JOIN course_objective co
    ON co.co_id = coa.co_id
   AND co.course_id = tc.course_id
JOIN objective_indicator_contribution oic
    ON oic.co_id = coa.co_id
   AND oic.ip_id = cia.ip_id
WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
GROUP BY tc.class_code, ip.ip_code, cia.achievement
ORDER BY tc.class_code, ip.ip_code;

-- 4.5 年级字段一致性：教学班年级、学生入学年份、毕业要求年级应一致
SELECT
    tc.class_code,
    tc.grade_year AS teaching_class_grade,
    s.student_no,
    s.enrollment_year AS student_enrollment_year,
    gr.grade_year AS requirement_grade,
    CASE
        WHEN tc.grade_year = s.enrollment_year AND tc.grade_year = gr.grade_year THEN 'OK'
        ELSE 'MISMATCH'
    END AS check_result
FROM teaching_class tc
JOIN student_class sc ON sc.class_id = tc.class_id
JOIN student s ON s.student_id = sc.student_id
JOIN course_major cm
    ON cm.course_id = tc.course_id
   AND cm.major_id = s.major_id
   AND cm.grade_year = tc.grade_year
JOIN graduation_requirement gr
    ON gr.major_id = cm.major_id
   AND gr.grade_year = cm.grade_year
WHERE tc.class_code IN ('TC2024CS01', 'W5-CS201-2022-02')
  AND (tc.grade_year <> s.enrollment_year OR tc.grade_year <> gr.grade_year)
LIMIT 20;

-- 期望：无结果。

-- ================================================================
-- 完成
-- ================================================================
