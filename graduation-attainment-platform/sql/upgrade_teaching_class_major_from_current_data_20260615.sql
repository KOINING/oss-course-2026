-- ================================================================
-- upgrade_teaching_class_major_from_current_data_20260615.sql
-- 第三步增量迁移：teaching_class 增加 major_id，并建立
-- “专业 + 年级 + 课程”唯一口径。
--
-- 适用数据来源：
--   1. GraduationDB_backup_2026-06-13_230940.sql
--   2. sql/rebuild_rich_test_data.sql
--
-- 迁移原则：
--   1. 不重建全库，不清空业务数据。
--   2. teaching_class.major_id 优先从 student_class -> student.major_id 回填。
--   3. 没有学生名单的教学班，从 course_major(course_id, grade_year) 兜底回填。
--   4. 回填后建立 major_id + grade_year + course_id 唯一约束。
--
-- 执行前建议备份数据库。
-- ================================================================

USE GraduationDB;

SET NAMES utf8mb4;

-- 1. 增加 teaching_class.major_id。使用 information_schema 做幂等保护。
SET @tc_major_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'teaching_class'
      AND COLUMN_NAME = 'major_id'
);

SET @sql := IF(
    @tc_major_column_exists = 0,
    'ALTER TABLE teaching_class ADD COLUMN major_id BIGINT NULL AFTER course_id',
    'SELECT ''teaching_class.major_id already exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 基于教学班学生名单推断教学班所属专业。
DROP TEMPORARY TABLE IF EXISTS tmp_tc_major_from_students;
CREATE TEMPORARY TABLE tmp_tc_major_from_students AS
SELECT
    sc.class_id,
    MIN(s.major_id) AS major_id,
    COUNT(DISTINCT s.major_id) AS major_count
FROM student_class sc
JOIN student s ON s.student_id = sc.student_id
GROUP BY sc.class_id;

-- 该查询必须为空。若不为空，说明同一个教学班混入了多个专业学生，需要先清理名单。
SELECT
    'ERROR_MIXED_STUDENT_MAJOR_IN_CLASS' AS check_item,
    tc.class_id,
    tc.class_code,
    tc.class_name,
    GROUP_CONCAT(DISTINCT CONCAT(m.major_id, ':', m.major_name) ORDER BY m.major_id SEPARATOR ', ') AS detected_majors
FROM teaching_class tc
JOIN student_class sc ON sc.class_id = tc.class_id
JOIN student s ON s.student_id = sc.student_id
JOIN major m ON m.major_id = s.major_id
GROUP BY tc.class_id, tc.class_code, tc.class_name
HAVING COUNT(DISTINCT s.major_id) > 1;

-- 3. 基于课程-专业-年级绑定推断专业，作为无学生名单教学班的兜底来源。
DROP TEMPORARY TABLE IF EXISTS tmp_tc_major_from_course;
CREATE TEMPORARY TABLE tmp_tc_major_from_course AS
SELECT
    tc.class_id,
    MIN(cm.major_id) AS major_id,
    COUNT(DISTINCT cm.major_id) AS major_count
FROM teaching_class tc
JOIN course_major cm
  ON cm.course_id = tc.course_id
 AND cm.grade_year = tc.grade_year
GROUP BY tc.class_id;

-- 该查询必须为空。若不为空，说明无学生名单时同一课程同一年级绑定多个专业，无法自动判定教学班专业。
SELECT
    'ERROR_AMBIGUOUS_COURSE_MAJOR_BINDING' AS check_item,
    tc.class_id,
    tc.class_code,
    tc.class_name,
    tc.course_id,
    tc.grade_year,
    GROUP_CONCAT(DISTINCT CONCAT(m.major_id, ':', m.major_name) ORDER BY m.major_id SEPARATOR ', ') AS candidate_majors
FROM teaching_class tc
JOIN course_major cm
  ON cm.course_id = tc.course_id
 AND cm.grade_year = tc.grade_year
JOIN major m ON m.major_id = cm.major_id
LEFT JOIN tmp_tc_major_from_students sm ON sm.class_id = tc.class_id
WHERE COALESCE(sm.major_count, 0) = 0
GROUP BY tc.class_id, tc.class_code, tc.class_name, tc.course_id, tc.grade_year
HAVING COUNT(DISTINCT cm.major_id) > 1;

-- 4. 回填 major_id：学生名单优先，course_major 兜底。
UPDATE teaching_class tc
LEFT JOIN tmp_tc_major_from_students sm ON sm.class_id = tc.class_id
LEFT JOIN tmp_tc_major_from_course cm ON cm.class_id = tc.class_id
SET tc.major_id = COALESCE(
    CASE WHEN sm.major_count = 1 THEN sm.major_id END,
    CASE WHEN cm.major_count = 1 THEN cm.major_id END
)
WHERE tc.major_id IS NULL;

-- 该查询必须为空。若不为空，说明存在无法回填专业的教学班。
SELECT
    'ERROR_UNRESOLVED_TEACHING_CLASS_MAJOR' AS check_item,
    tc.class_id,
    tc.class_code,
    tc.class_name,
    tc.course_id,
    tc.grade_year
FROM teaching_class tc
WHERE tc.major_id IS NULL;

-- 该查询必须为空。若不为空，说明现有数据不满足“同一专业同一年级同一课程一个教学班”。
SELECT
    'ERROR_DUPLICATE_MAJOR_GRADE_COURSE_CLASS' AS check_item,
    tc.major_id,
    m.major_name,
    tc.grade_year,
    tc.course_id,
    c.course_name,
    COUNT(*) AS class_count,
    GROUP_CONCAT(CONCAT(tc.class_id, ':', tc.class_code) ORDER BY tc.class_id SEPARATOR ', ') AS classes
FROM teaching_class tc
JOIN major m ON m.major_id = tc.major_id
JOIN course c ON c.course_id = tc.course_id
GROUP BY tc.major_id, m.major_name, tc.grade_year, tc.course_id, c.course_name
HAVING COUNT(*) > 1;

-- 5. 建立非空约束、外键、索引和唯一约束。
-- 如果上面的 ERROR_* 查询有结果，下面的 NOT NULL 或 UNIQUE 约束会失败，应先处理数据再重跑。
ALTER TABLE teaching_class
    MODIFY COLUMN major_id BIGINT NOT NULL;

SET @tc_major_fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'teaching_class'
      AND CONSTRAINT_NAME = 'fk_tc_major'
);

SET @sql := IF(
    @tc_major_fk_exists = 0,
    'ALTER TABLE teaching_class ADD CONSTRAINT fk_tc_major FOREIGN KEY (major_id) REFERENCES major(major_id) ON DELETE RESTRICT',
    'SELECT ''fk_tc_major already exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tc_major_grade_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'teaching_class'
      AND INDEX_NAME = 'idx_class_major_grade'
);

SET @sql := IF(
    @tc_major_grade_index_exists = 0,
    'CREATE INDEX idx_class_major_grade ON teaching_class(major_id, grade_year)',
    'SELECT ''idx_class_major_grade already exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tc_major_grade_course_uk_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'teaching_class'
      AND INDEX_NAME = 'uk_tc_major_grade_course'
);

SET @sql := IF(
    @tc_major_grade_course_uk_exists = 0,
    'ALTER TABLE teaching_class ADD UNIQUE KEY uk_tc_major_grade_course(major_id, grade_year, course_id)',
    'SELECT ''uk_tc_major_grade_course already exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 课程计算进度视图改为直接使用 teaching_class.major_id，
-- 避免同一课程被多个专业/年级绑定时通过 course_major 反推导致重复行或错配。
CREATE OR REPLACE VIEW v_course_calc_progress AS
SELECT
    tc.class_id,
    tc.class_name,
    tc.calc_status,
    tc.grade_year,
    c.course_code,
    c.course_name,
    t.teacher_name,
    tc.major_id,
    tc.term_id,
    (SELECT COUNT(*) FROM student_class sc WHERE sc.class_id = tc.class_id) AS student_count,
    (SELECT COUNT(*) FROM student_assessment_score sas WHERE sas.class_id = tc.class_id) AS score_count
FROM teaching_class tc
JOIN course c ON c.course_id = tc.course_id
JOIN teacher t ON t.id = tc.teacher_id;

-- 7. 迁移后校验。
SELECT
    'teaching_class_without_major' AS check_item,
    COUNT(*) AS check_value
FROM teaching_class
WHERE major_id IS NULL
UNION ALL
SELECT
    'duplicate_major_grade_course_class',
    COUNT(*)
FROM (
    SELECT major_id, grade_year, course_id
    FROM teaching_class
    GROUP BY major_id, grade_year, course_id
    HAVING COUNT(*) > 1
) duplicated
UNION ALL
SELECT
    'teaching_class_total',
    COUNT(*)
FROM teaching_class;

DROP TEMPORARY TABLE IF EXISTS tmp_tc_major_from_students;
DROP TEMPORARY TABLE IF EXISTS tmp_tc_major_from_course;

