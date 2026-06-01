package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵关系行请求")
public class SupportMatrixRowRequest {
    @Schema(description = "关系ID（更新时可传）", example = "1")
    private Long cisId;

    @NotNull(message = "courseId不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "ipId不能为空")
    @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ipId;

    @Schema(description = "总支撑权重（0~1）", example = "0.35")
    private Float totalWeight;

    @Schema(description = "兼容前端字段（同 totalWeight）", example = "0.35")
    private Float weight;
}
