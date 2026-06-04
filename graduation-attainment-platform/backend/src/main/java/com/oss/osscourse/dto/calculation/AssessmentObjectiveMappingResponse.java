package com.oss.osscourse.dto.calculation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 考核点→课程目标映射响应。
 * 直接服务于课程目标级达成度公式：
 * C_ij = Σ(支撑目标j的考核点实际得分) / Σ(支撑目标j的考核点满分)
 */
@Data
@Builder
@Schema(description = "考核点→课程目标映射响应（服务于课程目标级公式）")
public class AssessmentObjectiveMappingResponse {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程目标列表（每个目标含其考核点集合）")
    private List<ObjectiveGroup> objectives;

    @Schema(description = "跨引用校验结果")
    private CrossValidationResult validation;

    // ---- 内嵌结构 ----

    @Data
    @Builder
    @Schema(description = "课程目标及其考核点集合")
    public static class ObjectiveGroup {
        @Schema(description = "课程目标ID")
        private Long coId;

        @Schema(description = "课程目标编号")
        private String objectiveCode;

        @Schema(description = "课程目标纯文本描述")
        private String coDescription;

        @Schema(description = "该目标下的考核点列表")
        private List<AssessmentPointItem> assessmentPoints;

        @Schema(description = "该目标下所有考核点满分之和（公式分母）")
        private Float totalFullScore;
    }

    @Data
    @Builder
    @Schema(description = "考核点摘要")
    public static class AssessmentPointItem {
        @Schema(description = "考核点ID")
        private Long apId;

        @Schema(description = "考核点名称")
        private String apName;

        @Schema(description = "满分")
        private Float fullScore;

        @Schema(description = "是否已有学生成绩录入")
        private Boolean hasScores;
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
