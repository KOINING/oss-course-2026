package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵-专业下拉项")
public class MatrixMajorOptionResponse {
    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业编码", example = "CS")
    private String majorCode;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "状态：1=启用，0=停用", example = "1")
    private Integer status;
}
