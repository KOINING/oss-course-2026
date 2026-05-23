package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业状态更新请求")
public class MajorStatusRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID")
    private Long majorId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
