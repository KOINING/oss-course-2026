package com.oss.osscourse.dto.basic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcademicTermDeleteRequest {
    @NotNull(message = "学期ID不能为空")
    private Long termId;
}
