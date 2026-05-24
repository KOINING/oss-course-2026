package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新毕业要求状态请求")
public class UpdateGraduationRequirementStatusRequest {
    @NotNull(message = "毕业要求ID不能为空")
    @Schema(description = "毕业要求ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long grId;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为0或1")
    @Max(value = 1, message = "状态值必须为0或1")
    @Schema(description = "状态：1=启用，0=停用", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
