package com.oss.osscourse.dto.score;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "成绩导入预校验请求")
public class ScoreImportRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "导入文件名", example = "scores.xlsx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "文件内容不能为空")
    @Schema(description = "Base64 编码的成绩文件内容，允许 data URL 前缀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileBase64;
}
