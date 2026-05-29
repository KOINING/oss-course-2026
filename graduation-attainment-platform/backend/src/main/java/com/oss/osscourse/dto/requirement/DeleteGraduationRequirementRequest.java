package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "删除毕业要求请求")
public class DeleteGraduationRequirementRequest {
    @NotNull(message = "毕业要求ID不能为空")
    @Schema(description = "毕业要求ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long grId;
}
