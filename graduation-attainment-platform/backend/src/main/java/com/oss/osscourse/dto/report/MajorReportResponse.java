package com.oss.osscourse.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "专业级评价报告响应——图表、表格、导出共用一个结果源")
public class MajorReportResponse {

    // ==================== 报告头 ====================

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "报告对应的学期ID", example = "3")
    private Long termId;

    @Schema(description = "报告对应的学期编码", example = "2025-2026-1")
    private String termCode;

    @Schema(description = "报告生成时间")
    private LocalDateTime reportGeneratedAt;

    @Schema(description = "结果是否就绪")
    private Boolean resultReady;

    @Schema(description = "提示信息")
    private String message;

    // ==================== 指标点达成度明细 ====================

    @Schema(description = "各指标点达成度明细列表")
    private List<IndicatorReportRow> indicatorAchievements;

    // ==================== 数据源摘要 ====================

    @Schema(description = "数据源摘要说明")
    private DataSourceSummary dataSourceSummary;

    // ==================== 内部类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单个指标点的报告行")
    public static class IndicatorReportRow {

        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点编码", example = "3-1")
        private String ipCode;

        @Schema(description = "指标点描述", example = "知晓计算机学科的历史...")
        private String ipDescription;

        @Schema(description = "所属毕业要求编码", example = "3")
        private String grCode;

        @Schema(description = "专业级最终达成度 G_k", example = "0.8045")
        private Float finalAchievement;

        @Schema(description = "支撑该指标点的课程数量")
        private Integer contributingCourseCount;

        @Schema(description = "宏观权重之和（应≈1.0）", example = "1.0")
        private Float totalWeightSum;

        @Schema(description = "各支撑课程的贡献明细")
        private List<ContributingCourse> contributingCourses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "支撑课程贡献明细")
    public static class ContributingCourse {

        @Schema(description = "课程ID", example = "1")
        private Long courseId;

        @Schema(description = "课程代码", example = "080900X130B002")
        private String courseCode;

        @Schema(description = "课程名称", example = "数据结构")
        private String courseName;

        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班名称", example = "2022级数据结构01班")
        private String className;

        @Schema(description = "课程级指标点达成度 E_k", example = "0.81")
        private Float courseAchievement;

        @Schema(description = "宏观总支撑权重 W", example = "0.40")
        private Float totalWeight;

        @Schema(description = "加权贡献 E_k × W", example = "0.324")
        private Float weightedContribution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据源摘要，说明结果来自哪些计算批次")
    public static class DataSourceSummary {

        @Schema(description = "数据来源表", example = "major_indicator_achievement")
        private String sourceTable;

        @Schema(description = "参与的支撑课程总数")
        private Integer supportCourseCount;

        @Schema(description = "已锁定的教学班数量")
        private Integer lockedClassCount;

        @Schema(description = "各课程-教学班对应的计算批次学期ID")
        private Long snapshotTermId;

        @Schema(description = "备注")
        private String remark;
    }
}
