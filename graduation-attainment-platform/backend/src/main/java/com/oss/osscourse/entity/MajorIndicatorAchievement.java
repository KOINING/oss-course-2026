package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("major_indicator_achievement")
public class MajorIndicatorAchievement {
    @TableId(value = "mia_id", type = IdType.AUTO)
    private Long miaId;
    private Long majorId;
    private Long termId;
    private Long ipId;
    private Float finalAchievement;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
