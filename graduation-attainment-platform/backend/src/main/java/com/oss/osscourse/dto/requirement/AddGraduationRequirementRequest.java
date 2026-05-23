package com.oss.osscourse.dto.requirement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddGraduationRequirementRequest {
    @NotBlank(message = "毕业要求编号不能为空")
    private String grCode;

    @NotBlank(message = "毕业要求描述不能为空")
    private String grDescription;

    @NotNull(message = "所属专业不能为空")
    private Long majorId;
}
