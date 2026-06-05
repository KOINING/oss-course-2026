-- ================================================================
-- upgrade_week4_score_calc.sql — 第四周成绩计算中间结果表
-- ================================================================
-- 新增表：student_course_objective_achievement
-- 作用：保存学生-课程目标达成度，作为课程级计算的中间结果
-- 依赖：student_assessment_score + assessment_point → course_objective
-- 被引用：course_objective_achievement (AVG 汇总)
-- ================================================================

USE GraduationDB;

-- ================================================================
-- student_course_objective_achievement — 学生-课程目标达成度（🟡 中间计算结果）
-- ================================================================
-- 计算逻辑（后端实现）：
--   对每个学生(class_id + student_id) 在每个课程目标(co_id)下：
--     achievement = SUM(该目标下所有考核点的 actual_score) / SUM(该目标下所有考核点的 full_score)
--   该表作为 Level 1 计算的中间步骤被持久化，便于：
--     1. 追溯单个学生的达成度
--     2. course_objective_achievement 的 AVG 汇总
--     3. 避免重复计算
-- ================================================================

CREATE TABLE IF NOT EXISTS student_course_objective_achievement (
    scoa_id       BIGINT PRIMARY KEY AUTO_INCREMENT   COMMENT '记录主键',
    student_id    BIGINT  NOT NULL                    COMMENT '学生ID',
    co_id         BIGINT  NOT NULL                    COMMENT '课程目标ID',
    class_id      BIGINT  NOT NULL                    COMMENT '教学班级ID',
    achievement   FLOAT   NOT NULL                    COMMENT '学生对该课程目标的达成度(0~1)',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_student_co_class (student_id, co_id, class_id),
    INDEX idx_scoa_student (student_id),
    INDEX idx_scoa_co (co_id),
    INDEX idx_scoa_class (class_id),
    INDEX idx_scoa_class_co (class_id, co_id),

    CONSTRAINT fk_scoa_student FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_scoa_co      FOREIGN KEY (co_id)      REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    CONSTRAINT fk_scoa_class   FOREIGN KEY (class_id)   REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    CONSTRAINT chk_scoa_achievement CHECK (achievement >= 0 AND achievement <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='学生-课程目标达成度中间结果表';
