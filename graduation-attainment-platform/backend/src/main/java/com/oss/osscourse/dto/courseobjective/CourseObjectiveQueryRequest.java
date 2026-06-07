package com.oss.osscourse.dto.courseobjective;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "课程目标查询请求")
public class CourseObjectiveQueryRequest {
    @Schema(description = "课程目标编号，模糊查询", example = "CO1")
    private String objectiveCode;

    @Schema(description = "所属课程ID", example = "1")
    private Long courseId;

    @Schema(description = "教学班ID，用于根据教学班反查课程", example = "1")
    private Long teachingClassId;
}
