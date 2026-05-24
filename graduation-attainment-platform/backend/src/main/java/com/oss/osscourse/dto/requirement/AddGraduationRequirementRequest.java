package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新增毕业要求请求")
public class AddGraduationRequirementRequest {
    @NotBlank(message = "毕业要求编号不能为空")
    @Schema(description = "毕业要求编号", example = "GR01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grCode;

    @NotBlank(message = "毕业要求描述不能为空")
    @Schema(description = "毕业要求描述", example = "能够应用工程知识解决复杂工程问题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grDescription;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @Schema(description = "状态：1=启用，0=停用；不传默认启用", example = "1")
    private Integer status;
}
