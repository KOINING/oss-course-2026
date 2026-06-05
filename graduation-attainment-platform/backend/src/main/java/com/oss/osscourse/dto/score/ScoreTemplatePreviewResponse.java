package com.oss.osscourse.dto.score;

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
@Schema(description = "成绩模板预览响应")
public class ScoreTemplatePreviewResponse {

    @Schema(description = "教学班ID", example = "1")
    private Long classId;

    @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
    private String className;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "学生数量", example = "30")
    private Integer studentCount;

    @Schema(description = "考核点数量", example = "5")
    private Integer assessmentPointCount;

    @Schema(description = "固定列头")
    private List<String> fixedHeaders;

    @Schema(description = "动态列头（考核点）")
    private List<AssessmentPointHeader> dynamicHeaders;

    @Schema(description = "学生数据行")
    private List<StudentScoreRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "考核点列头")
    public static class AssessmentPointHeader {
        @Schema(description = "考核点ID", example = "1")
        private Long apId;

        @Schema(description = "考核点名称", example = "期末卷-链表操作题")
        private String apName;

        @Schema(description = "满分", example = "20")
        private Float fullScore;

        @Schema(description = "所属课程目标编码", example = "CO1")
        private String objectiveCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "学生成绩行")
    public static class StudentScoreRow {
        @Schema(description = "学生ID", example = "1")
        private Long studentId;

        @Schema(description = "学号", example = "20220101001")
        private String studentNo;

        @Schema(description = "学生姓名", example = "张三")
        private String studentName;

        @Schema(description = "各考核点成绩（与 dynamicHeaders 顺序对应）")
        private List<Float> scores;
    }
}
