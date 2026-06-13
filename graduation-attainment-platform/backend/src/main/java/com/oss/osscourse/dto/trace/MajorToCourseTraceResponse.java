package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "专业级到课程级穿透查询响应")
public class MajorToCourseTraceResponse {
    private Long majorId;
    private String majorName;
    private Integer gradeYear;
    private Long termId;
    private String termCode;
    private Long ipId;
    private String ipCode;
    private String ipDescription;
    private Float finalAchievement;
    private List<CourseContribution> courseContributions;

    @Data
    @Builder
    @Schema(description = "课程级贡献项：说明 Gk 来源于哪些 Ek")
    public static class CourseContribution {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Long classId;
        private String classCode;
        private String className;
        private Float courseIndicatorAchievement;
        private Float macroWeight;
        private Float weightedContribution;
        private String calcStatus;
    }
}
