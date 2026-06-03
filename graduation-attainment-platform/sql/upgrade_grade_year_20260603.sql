USE GraduationDB;

ALTER TABLE graduation_requirement
    ADD COLUMN grade_year INT NOT NULL DEFAULT 2022 AFTER major_id;

UPDATE graduation_requirement
SET grade_year = 2022
WHERE grade_year IS NULL OR grade_year = 0;

ALTER TABLE graduation_requirement
    DROP INDEX uk_major_gr_code,
    ADD UNIQUE KEY uk_major_grade_gr_code(major_id, grade_year, gr_code);

ALTER TABLE course_major
    ADD COLUMN grade_year INT NOT NULL DEFAULT 2022 AFTER major_id;

UPDATE course_major
SET grade_year = 2022
WHERE grade_year IS NULL OR grade_year = 0;

ALTER TABLE course_major
    DROP INDEX uk_course_major,
    ADD UNIQUE KEY uk_course_major_grade(course_id, major_id, grade_year);

ALTER TABLE teaching_class
    ADD COLUMN grade_year INT NOT NULL DEFAULT 2022 AFTER teacher_id;

UPDATE teaching_class
SET grade_year = 2022
WHERE grade_year IS NULL OR grade_year = 0;

ALTER TABLE major_indicator_achievement
    ADD COLUMN grade_year INT NOT NULL DEFAULT 2022 AFTER major_id;

UPDATE major_indicator_achievement
SET grade_year = 2022
WHERE grade_year IS NULL OR grade_year = 0;

ALTER TABLE major_indicator_achievement
    DROP INDEX uk_major_term_ip,
    ADD UNIQUE KEY uk_major_grade_term_ip(major_id, grade_year, term_id, ip_id);

CREATE INDEX idx_req_major_grade ON graduation_requirement(major_id, grade_year);
CREATE INDEX idx_cm_major_grade ON course_major(major_id, grade_year);
CREATE INDEX idx_class_grade ON teaching_class(grade_year);
CREATE INDEX idx_mia_major_grade ON major_indicator_achievement(major_id, grade_year);

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
