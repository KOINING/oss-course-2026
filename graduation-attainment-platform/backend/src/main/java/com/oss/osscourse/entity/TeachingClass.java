package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teaching_class")
public class TeachingClass {
    @TableId(value = "class_id", type = IdType.AUTO)
    private Long classId;
    private String classCode;
    private String className;
    private Long courseId;
    private Long majorId;
    private Long termId;
    private Long teacherId;
    private Integer gradeYear;
    private String calcStatus;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
