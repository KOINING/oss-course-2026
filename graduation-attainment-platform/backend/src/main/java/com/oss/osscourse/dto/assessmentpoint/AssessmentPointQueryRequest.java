package com.oss.osscourse.dto.assessmentpoint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "考核点查询请求")
public class AssessmentPointQueryRequest {
    @Schema(description = "考核点名称，模糊查询", example = "期末")
    private String apName;

    @Schema(description = "所属课程ID", example = "1")
    private Long courseId;

    @Schema(description = "所属课程目标ID", example = "1")
    private Long coId;
}
