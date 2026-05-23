package com.oss.osscourse.dto.college;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "学院更新请求")
public class CollegeUpdateRequest {

    @NotNull(message = "学院ID不能为空")
    @Schema(description = "学院ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long collegeId;

    @Size(max = 20, message = "学院编码长度不能超过20个字符")
    @Schema(description = "学院编码，如 001", example = "001")
    private String collegeCode;

    @Size(max = 100, message = "学院名称长度不能超过100个字符")
    @Schema(description = "学院名称，如 计算机科学与技术学院", example = "计算机科学与技术学院")
    private String collegeName;
}
