package com.oss.osscourse.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "专业级达成度汇总响应")
public class MajorCalcResponse {

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "学期ID", example = "1")
    private Long termId;

    @Schema(description = "学期编码", example = "2024-2025-1")
    private String termCode;

    @Schema(description = "年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "指标点达成度列表")
    private List<IndicatorAchievement> indicatorAchievements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "专业级指标点达成度")
    public static class IndicatorAchievement {
        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点编码", example = "1.1")
        private String ipCode;

        @Schema(description = "指标点描述", example = "能够运用数学、自然科学和工程科学的基本原理")
        private String ipDescription;

        @Schema(description = "专业级最终达成度", example = "0.78")
        private Float finalAchievement;
    }
}
