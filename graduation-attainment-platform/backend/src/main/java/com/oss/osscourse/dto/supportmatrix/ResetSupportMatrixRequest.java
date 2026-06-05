package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "重置支撑矩阵请求")
public class ResetSupportMatrixRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "培养方案适用年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;
}
