package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新毕业要求请求")
public class UpdateGraduationRequirementRequest {
    @NotNull(message = "毕业要求ID不能为空")
    @Schema(description = "毕业要求ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long grId;

    @NotBlank(message = "毕业要求编号不能为空")
    @Schema(description = "毕业要求编号", example = "GR01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grCode;

    @NotBlank(message = "毕业要求描述不能为空")
    @Schema(description = "毕业要求描述", example = "能够应用工程知识解决复杂工程问题（修订）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grDescription;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Min(value = 2000, message = "年级不能早于2000")
    @Max(value = 2100, message = "年级不能晚于2100")
    @Schema(description = "培养方案适用年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;
}
