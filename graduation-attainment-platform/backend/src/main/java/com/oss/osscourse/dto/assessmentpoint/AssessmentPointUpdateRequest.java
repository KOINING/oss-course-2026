package com.oss.osscourse.dto.assessmentpoint;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "更新考核点请求")
public class AssessmentPointUpdateRequest {
    @NotNull(message = "考核点ID不能为空")
    @Schema(description = "考核点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apId;

    @NotBlank(message = "考核点名称不能为空")
    @Schema(description = "考核点名称", example = "在线作业、课堂测试（修订）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apName;

    @NotNull(message = "满分不能为空")
    @Positive(message = "满分必须大于 0")
    @Schema(description = "满分（支持小数）", example = "12.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float fullScore;

    @NotNull(message = "绑定的课程目标不能为空")
    @Schema(description = "绑定的课程目标ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long coId;
}
