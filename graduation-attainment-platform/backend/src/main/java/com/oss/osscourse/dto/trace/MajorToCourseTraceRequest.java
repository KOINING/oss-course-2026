package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业级到课程级穿透查询请求")
public class MajorToCourseTraceRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "培养方案年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @Schema(description = "学期ID；不传则查询当前专业年级下已有结果")
    private Long termId;

    @Schema(description = "指标点ID；不传则返回全部指标点")
    private Long ipId;
}
