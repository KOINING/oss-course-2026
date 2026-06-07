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
@Schema(description = "课程目标达成看板响应")
public class CourseObjectiveDashboardResponse {

    @Schema(description = "教学班ID", example = "1")
    private Long classId;

    @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
    private String className;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "教学班计算状态", example = "locked")
    private String calcStatus;

    @Schema(description = "当前教学班是否已锁定", example = "true")
    private Boolean locked;

    @Schema(description = "是否已提交解锁申请", example = "false")
    private Boolean unlockRequested;

    @Schema(description = "当前待处理解锁申请原因")
    private String unlockRequestReason;

    @Schema(description = "是否已有课程级计算结果", example = "true")
    private Boolean resultReady;

    @Schema(description = "课程目标汇总")
    private List<ObjectiveSummary> objectiveSummaries;

    @Schema(description = "课程级毕业要求指标点达成度 Ek")
    private List<IndicatorAchievement> indicatorAchievements;

    @Schema(description = "学生目标达成明细")
    private List<StudentObjectiveRow> studentRows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectiveSummary {
        private Long coId;
        private String objectiveCode;
        private String description;
        private Float averageAchievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorAchievement {
        private Long ipId;
        private String ipCode;
        private String ipDescription;
        private Float achievement;
        private Boolean locked;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentObjectiveRow {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private List<Float> achievements;
    }
}
