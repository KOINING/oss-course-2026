package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teaching_class")
public class TeachingClass {
    @TableId(value = "teaching_class_id", type = IdType.AUTO)
    private Long teachingClassId;
    private String teachingClassCode;
    private String teachingClassName;
    private Long courseId;
    private Long termId;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
