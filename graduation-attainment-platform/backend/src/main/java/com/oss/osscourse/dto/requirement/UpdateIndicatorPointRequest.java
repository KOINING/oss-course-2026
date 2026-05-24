package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新指标点请求")
public class UpdateIndicatorPointRequest {
    @NotNull(message = "指标点ID不能为空")
    @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ipId;

    @NotBlank(message = "指标点编号不能为空")
    @Schema(description = "指标点编号", example = "IP01-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ipCode;

    @NotBlank(message = "指标点描述不能为空")
    @Schema(description = "指标点描述", example = "能够运用数学与自然科学原理分析工程问题（修订）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ipDescription;

    @NotNull(message = "所属毕业要求不能为空")
    @Schema(description = "所属毕业要求ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long grId;

    @Schema(description = "状态：1=启用，0=停用；不传则保持原状态", example = "1")
    private Integer status;
}
