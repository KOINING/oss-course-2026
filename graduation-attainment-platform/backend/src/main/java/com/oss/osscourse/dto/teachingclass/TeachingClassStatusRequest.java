package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "教学班状态更新请求")
public class TeachingClassStatusRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotBlank(message = "计算状态不能为空")
    @Pattern(regexp = "^(unsubmitted|score_imported|calculating|locked)$", message = "计算状态值不合法")
    @Schema(description = "计算状态：unsubmitted/score_imported/calculating/locked", example = "unsubmitted", requiredMode = Schema.RequiredMode.REQUIRED)
    private String calcStatus;
}
