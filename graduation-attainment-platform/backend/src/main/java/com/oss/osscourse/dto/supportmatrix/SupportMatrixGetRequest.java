package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "查询支撑矩阵请求")
public class SupportMatrixGetRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @Schema(description = "学期ID（当前版本仅透传，不参与关系主键）", example = "1")
    private Long termId;
}
