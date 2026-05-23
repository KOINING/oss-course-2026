package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "课程视图对象")
public class CourseVO {
    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "学分")
    private Double credit;

    @Schema(description = "所属专业ID")
    private Long majorId;

    @Schema(description = "所属专业名称")
    private String majorName;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
