package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵-毕业要求项")
public class MatrixGraduationRequirementResponse {
    @Schema(description = "毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "毕业要求编号", example = "GR01")
    private String grCode;

    @Schema(description = "毕业要求描述")
    private String grDescription;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "状态：1=启用，0=停用", example = "1")
    private Integer status;
}
