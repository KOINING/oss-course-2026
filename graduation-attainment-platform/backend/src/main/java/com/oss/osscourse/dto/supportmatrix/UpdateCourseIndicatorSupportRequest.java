package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新课程-指标点支撑关系请求")
public class UpdateCourseIndicatorSupportRequest {
    @NotNull(message = "cisId不能为空")
    @Schema(description = "关系ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cisId;

    @NotNull(message = "courseId不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "ipId不能为空")
    @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ipId;

    @NotNull(message = "totalWeight不能为空")
    @Schema(description = "总支撑权重（0~1）", example = "0.55", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float totalWeight;
}
