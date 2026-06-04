-- 第四周数据库升级脚本：学生-课程目标达成度表
-- 用于保存课程级计算的中间结果

CREATE TABLE IF NOT EXISTS student_objective_achievement (
    soa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    co_id BIGINT NOT NULL,
    achievement FLOAT NOT NULL CHECK (achievement >= 0 AND achievement <= 1),
    UNIQUE KEY uk_student_class_co(student_id, class_id, co_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES teaching_class(class_id) ON DELETE RESTRICT,
    FOREIGN KEY (co_id) REFERENCES course_objective(co_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 添加索引
CREATE INDEX idx_soa_student ON student_objective_achievement(student_id);
CREATE INDEX idx_soa_class ON student_objective_achievement(class_id);
CREATE INDEX idx_soa_co ON student_objective_achievement(co_id);
CREATE INDEX idx_sao_class_co ON student_objective_achievement(class_id, co_id);

-- 添加注释
ALTER TABLE student_objective_achievement COMMENT = '学生-课程目标达成度表，保存课程级计算中间结果';
