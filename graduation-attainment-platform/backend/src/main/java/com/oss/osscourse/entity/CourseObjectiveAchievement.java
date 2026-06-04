package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_objective_achievement")
public class CourseObjectiveAchievement {
    @TableId(value = "coa_id", type = IdType.AUTO)
    private Long coaId;
    private Long classId;
    private Long coId;
    private Float averageAchievement;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
