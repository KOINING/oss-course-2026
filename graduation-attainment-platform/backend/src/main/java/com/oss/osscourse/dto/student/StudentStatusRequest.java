package com.oss.osscourse.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "学生状态更新请求")
public class StudentStatusRequest {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为0-3")
    @Max(value = 3, message = "状态值必须为0-3")
    @Schema(description = "状态：1=在读，2=毕业，3=休学，0=退学", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
