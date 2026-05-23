package com.oss.osscourse.dto.requirement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteGraduationRequirementRequest {
    @NotNull(message = "毕业要求ID不能为空")
    private Long grId;
}
