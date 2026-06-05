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
@Schema(description = "课程计算状态汇总响应")
public class CourseCalcStatusResponse {

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "学期ID", example = "1")
    private Long termId;

    @Schema(description = "学期编码", example = "2024-2025-1")
    private String termCode;

    @Schema(description = "年级", example = "2022")
    private Integer gradeYear;

    @Schema(description = "是否满足专业级汇总前置条件", example = "true")
    private Boolean canCalcMajor;

    @Schema(description = "不满足条件的原因")
    private String blockReason;

    @Schema(description = "支撑课程列表")
    private List<CourseStatus> courseStatuses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "课程计算状态")
    public static class CourseStatus {
        @Schema(description = "课程ID", example = "1")
        private Long courseId;

        @Schema(description = "课程编码", example = "CS201")
        private String courseCode;

        @Schema(description = "课程名称", example = "数据结构")
        private String courseName;

        @Schema(description = "教学班ID", example = "1")
        private Long classId;

        @Schema(description = "教学班名称", example = "数据结构2024-2025-1班")
        private String className;

        @Schema(description = "计算状态", example = "locked")
        private String calcStatus;

        @Schema(description = "是否已锁定", example = "true")
        private Boolean isLocked;
    }
}
