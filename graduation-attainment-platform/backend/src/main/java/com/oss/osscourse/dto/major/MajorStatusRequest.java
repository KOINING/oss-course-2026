package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "专业状态更新请求")
public class MajorStatusRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为0或1")
    @Max(value = 1, message = "状态值必须为0或1")
    @Schema(description = "状态：1=启用，0=停用", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
