package com.oss.osscourse.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminResetUserPasswordRequest {
    @NotNull(message = "用户 ID 不能为空")
    private Long id;
}
