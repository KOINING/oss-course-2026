-- ================================================================
-- upgrade_week4_score_calc.sql
-- 第四周成绩计算中间结果表
-- 与当前后端实体保持一致：
--   com.oss.osscourse.entity.StudentObjectiveAchievement
-- ================================================================

USE GraduationDB;

-- 当前后端映射：
--   @TableName("student_objective_achievement")
--   @TableId("soa_id")
--
-- 用途：
--   持久化保存学生级课程目标达成度，作为课程达成度计算的第一层中间结果。
--
-- 后端计算语义：
--   对每个 (class_id, student_id, co_id)：
--     achievement =
--       该课程目标下所有考核点 actual_score 之和
--       / 该课程目标下所有考核点 full_score 之和
--
-- 说明：
--   1. 该表补齐了 student_assessment_score 与
--      course_objective_achievement 之间缺失的中间持久化层。
--   2. 旧草稿使用的表名为 student_course_objective_achievement，
--      主键名为 scoa_id，与当前后端实现不一致，因此此处统一修正。

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
