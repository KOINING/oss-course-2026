-- ================================================================
-- upgrade_week6_20260618.sql — 第六周数据库优化增量脚本
-- ================================================================
-- 执行前提：已执行 gra_db_full.sql + week5_sample_data.sql
-- 数据库：GraduationDB
-- 功能：
--   1. 重算 CIA（归一化加权公式）
--   2. 重算 MIA（归一化加权公式）+ 补齐 CS2022_MAIN
--   3. 修复 unlock_audit_log.approved_by 脏数据
--   4. 补充第六周分页查询索引
--   5. 清理冗余索引
--   6. 新增 schema_version
-- ================================================================

USE GraduationDB;

-- ================================================================
-- 一、重算课程级指标点达成度（CIA）—— 归一化加权
-- ================================================================
-- 旧口径：SUM(coa.average_achievement * oic.internal_weight)
-- 新口径：SUM(coa.average_achievement * oic.internal_weight) / SUM(oic.internal_weight)
-- 当 SUM(w)=1.0 时两者等价，但新口径更健壮

UPDATE course_indicator_achievement cia
JOIN (
    SELECT
        coa.class_id,
        oic.ip_id,
        ROUND(SUM(coa.average_achievement * oic.internal_weight)
              / NULLIF(SUM(oic.internal_weight), 0), 4) AS new_achievement
    FROM course_objective_achievement coa
    JOIN teaching_class tc ON tc.class_id = coa.class_id
    JOIN course_objective co ON co.co_id = coa.co_id AND co.course_id = tc.course_id
    JOIN objective_indicator_contribution oic ON oic.co_id = coa.co_id
    WHERE tc.calc_status = 'locked'
    GROUP BY coa.class_id, oic.ip_id
) calc ON calc.class_id = cia.class_id AND calc.ip_id = cia.ip_id
SET cia.achievement = calc.new_achievement;

-- 验证：CIA 重算前后差异
-- SELECT cia.class_id, cia.ip_id, cia.achievement AS new_val
-- FROM course_indicator_achievement cia
-- ORDER BY cia.class_id, cia.ip_id;


-- ================================================================
-- 二、重算专业级指标点达成度（MIA）+ 补齐 CS2022_MAIN
-- ================================================================
-- 旧口径：SUM(cia.achievement * cis.total_weight)
-- 新口径：SUM(cia.achievement * cis.total_weight) / SUM(cis.total_weight)
-- 同时补齐 CS2022_MAIN 场景的 MIA（原来只生成了 SE2022_UNLOCK）

SET @major_cs := (SELECT major_id FROM major WHERE major_code = '080901' LIMIT 1);
SET @major_se := (SELECT major_id FROM major WHERE major_code = '080902' LIMIT 1);
SET @term_main := (SELECT term_id FROM academic_term WHERE term_code = '2025-2026-1' LIMIT 1);

-- 2.1 重算已有 MIA（SE2022_UNLOCK：软工 2022 级 E2E 教学班）
UPDATE major_indicator_achievement mia
JOIN (
    SELECT
        cia.ip_id,
        ROUND(SUM(cia.achievement * cis.total_weight)
              / NULLIF(SUM(cis.total_weight), 0), 4) AS new_final
    FROM course_indicator_achievement cia
    JOIN teaching_class tc ON tc.class_id = cia.class_id
    JOIN course c ON c.course_id = tc.course_id
    JOIN course_major cm ON cm.course_id = c.course_id
        AND cm.grade_year = tc.grade_year
    JOIN course_indicator_support cis ON cis.course_id = tc.course_id AND cis.ip_id = cia.ip_id
    WHERE cm.major_id = @major_se
      AND tc.grade_year = 2022
      AND tc.term_id = @term_main
      AND tc.calc_status = 'locked'
    GROUP BY cia.ip_id
) calc ON calc.ip_id = mia.ip_id
SET mia.final_achievement = calc.new_final
WHERE mia.major_id = @major_se
  AND mia.grade_year = 2022
  AND mia.term_id = @term_main;

-- 2.2 为 CS2022_MAIN 生成 MIA（此前缺失：计科 2022 级 E2E 教学班）
INSERT INTO major_indicator_achievement (major_id, grade_year, term_id, ip_id, final_achievement)
SELECT
    @major_cs AS major_id,
    2022 AS grade_year,
    @term_main AS term_id,
    cia.ip_id,
    ROUND(SUM(cia.achievement * cis.total_weight)
          / NULLIF(SUM(cis.total_weight), 0), 4) AS final_achievement
FROM course_indicator_achievement cia
JOIN teaching_class tc ON tc.class_id = cia.class_id
JOIN course c ON c.course_id = tc.course_id
JOIN course_major cm ON cm.course_id = c.course_id
    AND cm.grade_year = tc.grade_year
JOIN course_indicator_support cis ON cis.course_id = tc.course_id AND cis.ip_id = cia.ip_id
WHERE cm.major_id = @major_cs
  AND tc.grade_year = 2022
  AND tc.term_id = @term_main
  AND tc.calc_status = 'locked'
GROUP BY cia.ip_id
ON DUPLICATE KEY UPDATE final_achievement = VALUES(final_achievement);

-- 验证：MIA 完整性
-- SELECT m.major_code, mia.grade_year, at.term_code, COUNT(*) AS cnt
-- FROM major_indicator_achievement mia
-- JOIN major m ON m.major_id = mia.major_id
-- JOIN academic_term at ON at.term_id = mia.term_id
-- GROUP BY m.major_code, mia.grade_year, at.term_code;
-- 期望：080901 / 2022 / 2025-2026-1 = 16, 080902 / 2022 / 2025-2026-1 = 16


-- ================================================================
-- 三、修复 unlock_audit_log.approved_by 脏数据
-- ================================================================
UPDATE unlock_audit_log
SET approved_by = 1
WHERE approved_by = 0;


-- ================================================================
-- 四、补充第六周分页查询索引
-- ================================================================

-- 4.1 学院
CREATE INDEX IF NOT EXISTS idx_college_name
    ON college(college_name);

-- 4.2 专业
CREATE INDEX IF NOT EXISTS idx_major_name
    ON major(major_name);
CREATE INDEX IF NOT EXISTS idx_major_college_status
    ON major(college_id, status);

-- 4.3 用户
CREATE INDEX IF NOT EXISTS idx_user_real_name
    ON sys_user(real_name);

-- 4.4 课程
CREATE INDEX IF NOT EXISTS idx_course_name
    ON course(course_name);

-- 4.5 学生
CREATE INDEX IF NOT EXISTS idx_student_name
    ON student(student_name);
CREATE INDEX IF NOT EXISTS idx_student_major_year_status
    ON student(major_id, enrollment_year, status);

-- 4.6 教学班（第六周核心查询）
CREATE INDEX IF NOT EXISTS idx_tc_course_grade_status
    ON teaching_class(course_id, grade_year, calc_status);
CREATE INDEX IF NOT EXISTS idx_tc_teacher_term_status
    ON teaching_class(teacher_id, term_id, calc_status);


-- ================================================================
-- 五、清理冗余索引
-- ================================================================
-- 以下索引被 UNIQUE KEY 或复合索引左前缀覆盖，属于冗余
DROP INDEX IF EXISTS idx_soa_student ON student_objective_achievement;
DROP INDEX IF EXISTS idx_coa_class ON course_objective_achievement;
DROP INDEX IF EXISTS idx_cia_class ON course_indicator_achievement;


-- ================================================================
-- 六、更新 schema_version
-- ================================================================
INSERT INTO system_config (config_key, config_value, config_desc) VALUES
('schema_version', '6', '当前数据库 DDL 版本号')
ON DUPLICATE KEY UPDATE config_value = '6';


-- ================================================================
-- 七、全链路自检（按需手动执行）
-- ================================================================

-- 7.1 成绩考核点课程 vs 教学班课程一致性
-- SELECT COUNT(*) AS mismatch_count
-- FROM student_assessment_score sas
-- JOIN assessment_point ap ON ap.ap_id = sas.ap_id
-- JOIN course_objective co ON co.co_id = ap.co_id
-- JOIN teaching_class tc ON tc.class_id = sas.class_id
-- WHERE co.course_id != tc.course_id;
-- 期望：0

-- 7.2 L1 可还原性（COA = AVG(SOA)）
-- SELECT tc.class_code, co.objective_code,
--        coa.average_achievement AS stored,
--        ROUND(AVG(soa.achievement), 4) AS computed,
--        CASE WHEN ABS(coa.average_achievement - ROUND(AVG(soa.achievement), 4)) < 0.0001
--             THEN 'OK' ELSE 'MISMATCH' END AS result
-- FROM course_objective_achievement coa
-- JOIN teaching_class tc ON tc.class_id = coa.class_id
-- JOIN course_objective co ON co.co_id = coa.co_id
-- JOIN student_objective_achievement soa ON soa.class_id = coa.class_id AND soa.co_id = coa.co_id
-- GROUP BY tc.class_code, co.objective_code, coa.average_achievement;
-- 期望：全部 OK

-- 7.3 L2 可还原性（CIA = 归一化加权(COA)）
-- SELECT tc.class_code, ip.ip_code,
--        cia.achievement AS stored,
--        ROUND(SUM(coa.average_achievement * oic.internal_weight)
--              / NULLIF(SUM(oic.internal_weight), 0), 4) AS computed,
--        CASE WHEN ABS(cia.achievement - ROUND(SUM(coa.average_achievement * oic.internal_weight)
--              / NULLIF(SUM(oic.internal_weight), 0), 4)) < 0.0001
--             THEN 'OK' ELSE 'MISMATCH' END AS result
-- FROM course_indicator_achievement cia
-- JOIN teaching_class tc ON tc.class_id = cia.class_id
-- JOIN indicator_point ip ON ip.ip_id = cia.ip_id
-- JOIN course_objective_achievement coa ON coa.class_id = tc.class_id
-- JOIN course_objective co ON co.co_id = coa.co_id AND co.course_id = tc.course_id
-- JOIN objective_indicator_contribution oic ON oic.co_id = coa.co_id AND oic.ip_id = cia.ip_id
-- GROUP BY tc.class_code, ip.ip_code, cia.achievement;
-- 期望：全部 OK

-- 7.4 MIA 覆盖完整性
-- SELECT m.major_code, mia.grade_year, at.term_code, COUNT(*) AS cnt
-- FROM major_indicator_achievement mia
-- JOIN major m ON m.major_id = mia.major_id
-- JOIN academic_term at ON at.term_id = mia.term_id
-- GROUP BY m.major_code, mia.grade_year, at.term_code;
-- 期望：080901/2022 = 16, 080902/2022 = 16

-- 7.5 年级一致性
-- SELECT COUNT(*) AS mismatch_count
-- FROM teaching_class tc
-- JOIN student_class sc ON sc.class_id = tc.class_id
-- JOIN student s ON s.student_id = sc.student_id
-- JOIN course_major cm ON cm.course_id = tc.course_id AND cm.grade_year = tc.grade_year
-- JOIN graduation_requirement gr ON gr.major_id = cm.major_id AND gr.grade_year = cm.grade_year
-- WHERE tc.grade_year != s.enrollment_year OR tc.grade_year != gr.grade_year;
-- 期望：0

-- ================================================================
-- 完成
-- ================================================================
