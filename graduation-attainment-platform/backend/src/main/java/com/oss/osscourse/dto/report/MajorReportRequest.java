package com.oss.osscourse.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业级评价报告查询请求")
public class MajorReportRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @Schema(description = "学期ID，不传则取最新计算结果的学期", example = "1")
    private Long termId;
}
