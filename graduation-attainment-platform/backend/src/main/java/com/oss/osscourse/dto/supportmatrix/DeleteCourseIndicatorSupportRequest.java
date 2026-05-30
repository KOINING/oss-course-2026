package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "删除课程-指标点支撑关系请求")
public class DeleteCourseIndicatorSupportRequest {
    @NotNull(message = "cisId不能为空")
    @Schema(description = "关系ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cisId;
}
