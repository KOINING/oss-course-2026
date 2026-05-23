package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "专业新增/更新请求")
public class MajorSaveRequest {
    @Schema(description = "专业ID（更新时必填）")
    private Long majorId;

    @NotBlank(message = "专业编码不能为空")
    @Size(max = 20, message = "专业编码长度不能超过20位")
    @Schema(description = "专业编码", example = "080901")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100位")
    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @NotNull(message = "所属学院不能为空")
    @Schema(description = "所属学院ID")
    private Long collegeId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用", example = "1")
    private Integer status;
}
