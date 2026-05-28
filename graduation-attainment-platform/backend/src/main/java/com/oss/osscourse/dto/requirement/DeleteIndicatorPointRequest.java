package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "删除指标点请求")
public class DeleteIndicatorPointRequest {
    @NotNull(message = "指标点ID不能为空")
    @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ipId;
}
