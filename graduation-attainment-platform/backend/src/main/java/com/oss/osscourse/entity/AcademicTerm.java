package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("academic_term")
public class AcademicTerm {
    @TableId(value = "term_id", type = IdType.AUTO)
    private Long termId;
    private String termCode;
    private Integer academicYear;
    private Integer semester;
    private LocalDate startDate;
    private LocalDate endDate;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
