package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "教学班学生名单自动生成请求")
public class StudentClassGenerateRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1")
    private Long teachingClassId;
}
