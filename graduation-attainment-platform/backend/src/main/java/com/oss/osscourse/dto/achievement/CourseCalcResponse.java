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
@Schema(description = "课程级达成度计算响应")
public class CourseCalcResponse {

    @Schema(description = "教学班ID", example = "1")
    private Long classId;

    @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
    private String className;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "学生数量", example = "30")
    private Integer studentCount;

    @Schema(description = "课程目标达成度列表")
    private List<ObjectiveAchievement> objectiveAchievements;

    @Schema(description = "指标点达成度列表")
    private List<IndicatorAchievement> indicatorAchievements;

    @Schema(description = "是否已锁定", example = "false")
    private Boolean isLocked;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程目标达成度")
    public static class ObjectiveAchievement {
        @Schema(description = "课程目标ID", example = "1")
        private Long coId;

        @Schema(description = "课程目标编码", example = "CO1")
        private String objectiveCode;

        @Schema(description = "课程目标描述", example = "能够运用数据结构知识解决实际问题")
        private String description;

        @Schema(description = "班级平均达成度", example = "0.75")
        private Float averageAchievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "指标点达成度")
    public static class IndicatorAchievement {
        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点编码", example = "1.1")
        private String ipCode;

        @Schema(description = "指标点描述", example = "能够运用数学、自然科学和工程科学的基本原理")
        private String ipDescription;

        @Schema(description = "课程级指标点达成度", example = "0.72")
        private Float achievement;
    }
}
