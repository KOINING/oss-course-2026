package com.oss.osscourse.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "教师端申请解锁请求")
public class UnlockRequestCreateRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotBlank(message = "解锁原因不能为空")
    @Size(max = 512, message = "解锁原因不能超过512个字符")
    @Schema(description = "解锁原因", example = "录入成绩后发现期中测试分值有误，需要更正", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
