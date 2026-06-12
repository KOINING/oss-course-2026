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
@Schema(description = "专业级雷达图数据——图表、表格、导出共用一个结果源")
public class MajorRadarResponse {

    // ==================== 报告头 ====================

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "统计学期ID")
    private Long termId;

    @Schema(description = "统计学期编码", example = "2025-2026-1")
    private String termCode;

    @Schema(description = "报告生成时间")
    private LocalDateTime reportGeneratedAt;

    @Schema(description = "结果是否就绪")
    private Boolean resultReady;

    @Schema(description = "提示信息")
    private String message;

    // ==================== 雷达图数据 ====================

    @Schema(description = "雷达图数据体")
    private RadarData radar;

    // ==================== 数据源说明 ====================

    @Schema(description = "数据来源说明")
    private String dataSource;

    // ==================== 内部类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "雷达图数据体")
    public static class RadarData {

        @Schema(description = "指标点信息列表（作为雷达图轴标签的数据源）")
        private List<AxisIndicator> indicators;

        @Schema(description = "数据系列")
        private List<SeriesItem> series;

        @Schema(description = "雷达图最大值（通常为 1.0）", example = "1.0")
        private Float maxValue;

        @Schema(description = "参考线配置，如合格线、良好线")
        private List<ReferenceLine> referenceLines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "雷达图轴对应的指标点")
    public static class AxisIndicator {

        @Schema(description = "指标点ID", example = "1")
        private Long ipId;

        @Schema(description = "指标点编码（雷达轴标签）", example = "3-1")
        private String ipCode;

        @Schema(description = "指标点描述（雷达轴 tooltip）", example = "知晓计算机学科的历史...")
        private String ipDescription;

        @Schema(description = "所属毕业要求编码", example = "3")
        private String grCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "雷达图数据系列")
    public static class SeriesItem {

        @Schema(description = "系列名称", example = "专业级达成度")
        private String name;

        @Schema(description = "各轴对应的值（与 indicators 顺序一一对应）")
        private List<Float> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "参考线")
    public static class ReferenceLine {

        @Schema(description = "参考线值", example = "0.7")
        private Float value;

        @Schema(description = "参考线名称", example = "合格线")
        private String name;
    }
}
