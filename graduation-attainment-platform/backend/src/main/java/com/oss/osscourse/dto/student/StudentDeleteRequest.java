package com.oss.osscourse.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "学生删除请求")
public class StudentDeleteRequest {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;
}
