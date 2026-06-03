package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵-指标点项")
public class MatrixIndicatorPointResponse {
    @Schema(description = "指标点ID", example = "1")
    private Long ipId;

    @Schema(description = "指标点编号", example = "IP01-1")
    private String ipCode;

    @Schema(description = "指标点描述")
    private String ipDescription;

    @Schema(description = "所属毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "所属毕业要求编号", example = "GR01")
    private String grCode;

    @Schema(description = "所属毕业要求描述")
    private String grDescription;

    @Schema(description = "培养方案适用年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "指标点状态：1=启用，0=停用", example = "1")
    private Integer status;
}
