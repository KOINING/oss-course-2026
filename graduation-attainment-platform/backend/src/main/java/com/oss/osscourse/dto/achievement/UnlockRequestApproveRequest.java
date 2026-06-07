package com.oss.osscourse.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理端执行解锁请求")
public class UnlockRequestApproveRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;
}
