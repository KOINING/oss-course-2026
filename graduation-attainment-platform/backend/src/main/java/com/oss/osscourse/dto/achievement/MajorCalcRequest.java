package com.oss.osscourse.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业级达成度汇总请求")
public class MajorCalcRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "学期ID不能为空")
    @Schema(description = "学期ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long termId;
}
