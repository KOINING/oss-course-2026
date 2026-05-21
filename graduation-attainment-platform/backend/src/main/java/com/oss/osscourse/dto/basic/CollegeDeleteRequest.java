package com.oss.osscourse.dto.basic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CollegeDeleteRequest {
    @NotNull(message = "学院ID不能为空")
    private Long collegeId;
}
