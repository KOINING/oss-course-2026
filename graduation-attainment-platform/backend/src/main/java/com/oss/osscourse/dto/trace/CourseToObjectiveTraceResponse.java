package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "课程级到课程目标穿透查询响应")
public class CourseToObjectiveTraceResponse {
    private Long classId;
    private String classCode;
    private String className;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long ipId;
    private String ipCode;
    private String ipDescription;
    private Float courseIndicatorAchievement;
    private List<ObjectiveContribution> objectiveContributions;

    @Data
    @Builder
    @Schema(description = "课程目标贡献项：说明 Ek 来源于哪些课程目标与权重")
    public static class ObjectiveContribution {
        private Long coId;
        private String objectiveCode;
        private String coDescription;
        private Float objectiveAchievement;
        private Float internalWeight;
        private Float weightedContribution;
    }
}
