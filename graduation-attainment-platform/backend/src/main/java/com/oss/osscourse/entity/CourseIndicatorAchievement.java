package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_indicator_achievement")
public class CourseIndicatorAchievement {
    @TableId(value = "cia_id", type = IdType.AUTO)
    private Long ciaId;
    private Long classId;
    private Long ipId;
    private Float achievement;
    private Boolean isLocked;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
