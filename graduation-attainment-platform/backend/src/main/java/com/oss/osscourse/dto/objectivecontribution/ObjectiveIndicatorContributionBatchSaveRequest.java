package com.oss.osscourse.dto.objectivecontribution;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量保存课程目标-指标点内部权重请求")
public class ObjectiveIndicatorContributionBatchSaveRequest {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "培养方案年级不能为空")
    @Schema(description = "培养方案适用年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @NotEmpty(message = "权重配置列表不能为空")
    @Valid
    @Schema(description = "内部权重配置列表")
    private List<ContributionItem> contributions;

    @Data
    @Schema(description = "单条内部权重配置")
    public static class ContributionItem {
        @NotNull(message = "课程目标ID不能为空")
        @Schema(description = "课程目标ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long coId;

        @NotNull(message = "指标点ID不能为空")
        @Schema(description = "指标点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long ipId;

        @NotNull(message = "内部权重不能为空")
        @Schema(description = "内部权重，范围(0, 1]", example = "0.6", requiredMode = Schema.RequiredMode.REQUIRED)
        private Float internalWeight;
    }
}
