package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_objective_achievement")
public class StudentObjectiveAchievement {
    @TableId(value = "soa_id", type = IdType.AUTO)
    private Long soaId;
    private Long studentId;
    private Long classId;
    private Long coId;
    private Float achievement;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
