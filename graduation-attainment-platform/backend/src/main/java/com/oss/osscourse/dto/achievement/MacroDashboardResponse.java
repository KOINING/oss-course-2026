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
@Schema(description = "专业级宏观看板响应")
public class MacroDashboardResponse {

    private Long majorId;
    private String majorName;
    private Integer gradeYear;
    private Long termId;
    private String termCode;
    private Boolean aggregationAllowed;
    private Boolean unlockedWarning;
    private String blockReason;
    private Boolean majorResultExists;
    private List<CourseRow> courses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRow {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Long classId;
        private String classCode;
        private String className;
        private String teacherName;
        private Long studentCount;
        private Long scoreCount;
        private String calcStatus;
        private String blockReason;
    }
}
