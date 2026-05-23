package com.oss.osscourse.dto.requirement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIndicatorPointRequest {
    @NotNull(message = "指标点ID不能为空")
    private Long ipId;

    @NotBlank(message = "指标点编号不能为空")
    private String ipCode;

    @NotBlank(message = "指标点描述不能为空")
    private String ipDescription;

    @NotNull(message = "所属毕业要求不能为空")
    private Long grId;
}
