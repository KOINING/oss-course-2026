package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程目标到考核点与原始成绩穿透查询请求")
public class ObjectiveToScoreTraceRequest {
    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotNull(message = "课程目标ID不能为空")
    @Schema(description = "课程目标ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long coId;

    @Schema(description = "考核点ID；不传则返回该课程目标下全部考核点")
    private Long apId;
}
