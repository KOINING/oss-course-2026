package com.oss.osscourse.dto.score;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "成绩导入请求")
public class ScoreImportRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotNull(message = "导入数据不能为空")
    @Schema(description = "导入的Excel数据（JSON格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jsonData;
}
