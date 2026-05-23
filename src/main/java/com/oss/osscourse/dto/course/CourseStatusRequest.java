package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程状态更新请求")
public class CourseStatusRequest {
    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
