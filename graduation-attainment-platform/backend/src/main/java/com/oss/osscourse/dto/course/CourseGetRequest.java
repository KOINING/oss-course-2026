package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程详情查询请求")
public class CourseGetRequest {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;
}
