package com.oss.osscourse.dto.calculation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 课程目标→指标点映射响应。
 * 直接服务于课程级指标点达成度公式：
 * E_k = Σ_j (C̄_j × w_jk)
 */
@Data
@Builder
@Schema(description = "课程目标→指标点映射响应（服务于课程级公式）")
public class ObjectiveIndicatorMappingResponse {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "培养方案年级")
    private Integer gradeYear;

    @Schema(description = "所属专业ID")
    private Long majorId;

    @Schema(description = "指标点列表（每个指标点含其贡献课程目标及权重）")
    private List<IndicatorGroup> indicatorPoints;

    @Schema(description = "跨引用校验结果")
    private CrossValidationResult validation;

    // ---- 内嵌结构 ----

    @Data
    @Builder
    @Schema(description = "指标点及其贡献课程目标")
    public static class IndicatorGroup {
        @Schema(description = "指标点ID")
        private Long ipId;

        @Schema(description = "指标点编号")
        private String ipCode;

        @Schema(description = "指标点描述")
        private String ipDescription;

        @Schema(description = "毕业要求编号")
        private String grCode;

        @Schema(description = "贡献该指标点的课程目标及权重列表")
        private List<ObjectiveWeight> contributingObjectives;

        @Schema(description = "内部权重之和（需 = 1.0）")
        private Double weightSum;

        @Schema(description = "权重和是否合法（容差 0.001）")
        private Boolean weightValid;
    }

    @Data
    @Builder
    @Schema(description = "课程目标对指标点的内部权重")
    public static class ObjectiveWeight {
        @Schema(description = "课程目标ID")
        private Long coId;

        @Schema(description = "课程目标编号")
        private String objectiveCode;

        @Schema(description = "课程目标纯文本描述")
        private String coDescription;

        @Schema(description = "内部权重 w_jk")
        private Float internalWeight;
    }

    @Data
    @Builder
    @Schema(description = "跨引用校验结果")
    public static class CrossValidationResult {
        @Schema(description = "是否通过校验")
        private Boolean valid;

        @Schema(description = "错误数量")
        private int errorCount;

        @Schema(description = "校验错误详情")
        private List<String> errors;
    }
}
