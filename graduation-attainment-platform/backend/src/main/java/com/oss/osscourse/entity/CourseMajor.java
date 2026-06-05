package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_major")
public class CourseMajor {
    @TableId(value = "cm_id", type = IdType.AUTO)
    private Long cmId;
    private Long courseId;
    private Long majorId;
    private Integer gradeYear;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
