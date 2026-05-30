package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵关系项")
public class MatrixRelationResponse {
    @Schema(description = "关系ID", example = "1")
    private Long cisId;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "指标点ID", example = "1")
    private Long ipId;

    @Schema(description = "总支撑权重", example = "0.25")
    private Float totalWeight;

    @Schema(description = "兼容前端字段（同 totalWeight）", example = "0.25")
    private Float weight;
}
