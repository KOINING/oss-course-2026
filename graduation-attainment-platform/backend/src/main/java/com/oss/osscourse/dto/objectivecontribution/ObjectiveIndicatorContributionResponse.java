package com.oss.osscourse.dto.objectivecontribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "课程目标-指标点内部权重响应对象")
public class ObjectiveIndicatorContributionResponse {
    @Schema(description = "关系ID", example = "1")
    private Long oicId;

    @Schema(description = "课程目标ID", example = "1")
    private Long coId;

    @Schema(description = "课程目标编号", example = "CO1")
    private String objectiveCode;

    @Schema(description = "课程目标纯文本描述")
    private String coDescription;

    @Schema(description = "指标点ID", example = "1")
    private Long ipId;

    @Schema(description = "指标点编号", example = "1.1")
    private String ipCode;

    @Schema(description = "指标点描述")
    private String ipDescription;

    @Schema(description = "毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "毕业要求编号", example = "3")
    private String grCode;

    @Schema(description = "内部权重", example = "0.6")
    private Float internalWeight;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
