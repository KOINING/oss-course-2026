package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "指标点响应对象")
public class IndicatorPointResponse {
    @Schema(description = "指标点ID", example = "1")
    private Long ipId;

    @Schema(description = "指标点编号", example = "IP01-1")
    private String ipCode;

    @Schema(description = "指标点描述", example = "能够运用数学与自然科学原理分析工程问题")
    private String ipDescription;

    @Schema(description = "所属毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "所属毕业要求编号", example = "GR01")
    private String grCode;

    @Schema(description = "所属毕业要求描述", example = "能够应用工程知识解决复杂工程问题")
    private String grDescription;

    @Schema(description = "状态：1=启用，0=停用", example = "1")
    private Integer status;
}
