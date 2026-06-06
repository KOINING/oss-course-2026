package com.oss.osscourse.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "宏观看板查询请求")
public class MacroDashboardRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @NotNull(message = "学期ID不能为空")
    @Schema(description = "学期ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long termId;
}
