package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程级到课程目标穿透查询请求")
public class CourseToObjectiveTraceRequest {
    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotNull(message = "指标点ID不能为空")
    @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ipId;
}
