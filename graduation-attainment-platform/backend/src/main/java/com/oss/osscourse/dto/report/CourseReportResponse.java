package com.oss.osscourse.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程级评价报表响应")
public class CourseReportResponse {

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程代码", example = "CS201")
    private String courseCode;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "学分", example = "3.0")
    private Float credit;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "授课教师", example = "张三")
    private String teacherName;

    @Schema(description = "涉及教学班数量", example = "2")
    private Integer classCount;

    @Schema(description = "教学班简表")
    private List<ClassSummary> classSummaries;

    @Schema(description = "考核点表头")
    private List<AssessmentPointHeader> assessmentPoints;

    @Schema(description = "各教学班单项平均分")
    private List<ClassScoreSummary> classScoreSummaries;

    @Schema(description = "教学班报表明细")
    private List<TeachingClassReport> teachingClasses;

    @Schema(description = "课程目标达成度明细")
    private List<ObjectiveAchievementSummary> objectiveAchievements;

    @Schema(description = "课程级指标点达成度明细")
    private List<IndicatorAchievementSummary> indicatorAchievements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考核点表头")
    public static class AssessmentPointHeader {
        @Schema(description = "考核点ID", example = "1")
        private Long apId;

        @Schema(description = "考核点名称", example = "期末实验")
        private String apName;

        @Schema(description = "满分", example = "20.0")
        private Float fullScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "教学班简表")
    public static class ClassSummary {
        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班代码", example = "TC2024CS01")
        private String classCode;

        @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
        private String className;

        @Schema(description = "学期代码", example = "2025-2026-1")
        private String termCode;

        @Schema(description = "学生人数", example = "30")
        private Integer studentCount;

        @Schema(description = "计算状态", example = "locked")
        private String calcStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "各教学班单项平均分")
    public static class ClassScoreSummary {
        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班代码", example = "TC2024CS01")
        private String classCode;

        @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
        private String className;

        @Schema(description = "学期代码", example = "2025-2026-1")
        private String termCode;

        @Schema(description = "学生人数", example = "30")
        private Integer studentCount;

        @Schema(description = "计算状态", example = "locked")
        private String calcStatus;

        @Schema(description = "各考核点平均分，key 为 apId")
        private Map<Long, Float> apAverages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "教学班报表")
    public static class TeachingClassReport {

        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班代码", example = "TC2024CS01")
        private String classCode;

        @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
        private String className;

        @Schema(description = "学期代码", example = "2025-2026-1")
        private String termCode;

        @Schema(description = "学生人数", example = "30")
        private Integer studentCount;

        @Schema(description = "计算状态", example = "locked")
        private String calcStatus;

        @Schema(description = "各考核点平均分")
        private List<AssessmentPointAverage> assessmentPointAverages;

        @Schema(description = "课程目标达成度明细")
        private List<ObjectiveAchievementDetail> objectiveAchievementDetails;

        @Schema(description = "课程级指标点达成度")
        private List<IndicatorAchievementDetail> indicatorAchievementDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考核点平均分")
    public static class AssessmentPointAverage {

        @Schema(description = "考核点ID", example = "1")
        private Long apId;

        @Schema(description = "考核点名称", example = "期末实验")
        private String apName;

        @Schema(description = "满分", example = "20.0")
        private Float fullScore;

        @Schema(description = "平均分", example = "15.5")
        private Float averageScore;

        @Schema(description = "得分率", example = "0.775")
        private Float scoreRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程目标达成度明细")
    public static class ObjectiveAchievementDetail {

        @Schema(description = "课程目标ID", example = "1")
        private Long coId;

        @Schema(description = "课程目标代码", example = "CO1")
        private String objectiveCode;

        @Schema(description = "课程目标名称", example = "课程目标 1")
        private String objectiveName;

        @Schema(description = "课程目标描述", example = "能够运用数据结构知识解决实际问题")
        private String description;

        @Schema(description = "班级平均达成度", example = "0.75")
        private Float averageAchievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程级指标点达成度明细")
    public static class IndicatorAchievementDetail {

        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点代码", example = "1.1")
        private String ipCode;

        @Schema(description = "指标点描述", example = "能够运用数学、自然科学和工程科学的基本原理")
        private String ipDescription;

        @Schema(description = "课程级指标点达成度", example = "0.72")
        private Float achievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程目标达成度汇总")
    public static class ObjectiveAchievementSummary {

        @Schema(description = "课程目标ID", example = "1")
        private Long coId;

        @Schema(description = "课程目标代码", example = "CO1")
        private String objectiveCode;

        @Schema(description = "课程目标名称", example = "课程目标 1")
        private String objectiveName;

        @Schema(description = "课程目标描述", example = "能够运用数据结构知识解决实际问题")
        private String description;

        @Schema(description = "各教学班达成度")
        private List<ClassAchievement> classAchievements;

        @Schema(description = "课程级平均达成度", example = "0.73")
        private Float courseAverage;

        @Schema(description = "兼容字段：平均达成度", example = "0.73")
        private Float averageAchievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程级指标点达成度汇总")
    public static class IndicatorAchievementSummary {

        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点代码", example = "1.1")
        private String ipCode;

        @Schema(description = "指标点描述", example = "能够运用数学、自然科学和工程科学的基本原理")
        private String ipDescription;

        @Schema(description = "各教学班达成度")
        private List<ClassAchievement> classAchievements;

        @Schema(description = "课程级达成度", example = "0.70")
        private Float courseAchievement;

        @Schema(description = "兼容字段：平均达成度", example = "0.70")
        private Float averageAchievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "教学班达成度")
    public static class ClassAchievement {

        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
        private String className;

        @Schema(description = "达成度值", example = "0.75")
        private Float achievement;
    }
}
