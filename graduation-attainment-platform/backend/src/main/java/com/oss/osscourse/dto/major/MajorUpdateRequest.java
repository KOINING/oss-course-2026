package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "专业更新请求")
public class MajorUpdateRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @Size(max = 20, message = "专业编码长度不能超过20个字符")
    @Schema(description = "专业编码，如 080901", example = "080901")
    private String majorCode;

    @Size(max = 100, message = "专业名称长度不能超过100个字符")
    @Schema(description = "专业名称，如 计算机科学与技术", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "所属学院ID", example = "1")
    private Long collegeId;

    @Schema(description = "状态：1=招生中，0=停招", example = "1")
    private Integer status;
}
