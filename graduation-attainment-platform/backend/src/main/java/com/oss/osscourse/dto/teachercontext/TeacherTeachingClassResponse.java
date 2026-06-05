package com.oss.osscourse.dto.teachercontext;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教师端课程与教学班上下文")
public class TeacherTeachingClassResponse {
    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "培养方案年级", example = "2024")
    private Integer gradeYear;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程代码", example = "CS101")
    private String courseCode;

    @Schema(description = "课程名称", example = "程序设计基础")
    private String courseName;

    @Schema(description = "教学班ID", example = "1")
    private Long classId;

    @Schema(description = "教学班编号", example = "TC2024CS01")
    private String classCode;

    @Schema(description = "教学班名称", example = "程序设计基础1班")
    private String className;

    @Schema(description = "学期ID", example = "1")
    private Long termId;

    @Schema(description = "学期代码", example = "2025-2026-1")
    private String termCode;

    @Schema(description = "计算状态", example = "unsubmitted")
    private String calcStatus;

    @Schema(description = "是否匹配当前教师所属专业和培养方案年级", example = "true")
    private Boolean programMatched;

    @Schema(description = "阻断原因；为空表示可继续使用该上下文")
    private String blockReason;
}
