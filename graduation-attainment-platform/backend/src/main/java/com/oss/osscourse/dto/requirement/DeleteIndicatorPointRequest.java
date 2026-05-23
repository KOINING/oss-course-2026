package com.oss.osscourse.dto.requirement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteIndicatorPointRequest {
    @NotNull(message = "指标点ID不能为空")
    private Long ipId;
}
