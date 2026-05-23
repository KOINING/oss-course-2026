package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专业ID请求")
public class MajorIdRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID")
    private Long majorId;
}
