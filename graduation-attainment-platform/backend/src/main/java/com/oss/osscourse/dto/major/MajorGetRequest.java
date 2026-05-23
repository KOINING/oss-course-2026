package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业详情查询请求")
public class MajorGetRequest {

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;
}
