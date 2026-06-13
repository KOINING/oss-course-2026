package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "课程目标到考核点与原始成绩穿透查询响应")
public class ObjectiveToScoreTraceResponse {
    private Long classId;
    private String classCode;
    private String className;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long coId;
    private String objectiveCode;
    private String coDescription;
    private Float objectiveAchievement;
    private List<AssessmentPointTrace> assessmentPoints;

    @Data
    @Builder
    @Schema(description = "考核点及其原始成绩明细")
    public static class AssessmentPointTrace {
        private Long apId;
        private String apName;
        private Float fullScore;
        private Float averageScore;
        private List<StudentScoreTrace> studentScores;
    }

    @Data
    @Builder
    @Schema(description = "学生原始成绩")
    public static class StudentScoreTrace {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Float actualScore;
    }
}
