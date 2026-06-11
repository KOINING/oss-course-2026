package com.oss.osscourse.dto.teachercontext;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "教师端成绩导入上下文")
public class ScoreImportContextResponse {
    @Schema(description = "当前教师权限判断结果")
    private PermissionResult permission;

    @Schema(description = "课程与教学班上下文")
    private TeacherTeachingClassResponse teachingClass;

    @Schema(description = "当前教学班学生总数", example = "35")
    private Long studentCount;

    @Schema(description = "当前课程目标数量", example = "4")
    private Long courseObjectiveCount;

    @Schema(description = "当前考核点数量", example = "8")
    private Long assessmentPointCount;

    @Schema(description = "当前内部权重配置数量", example = "8")
    private Long internalWeightCount;

    @Schema(description = "当前课程级计算状态", example = "unsubmitted")
    private String calcStatus;

    @Schema(description = "是否允许生成模板", example = "true")
    private Boolean canGenerateTemplate;

    @Schema(description = "是否允许导入成绩", example = "true")
    private Boolean canImportScore;

    @Schema(description = "阻断信息列表；为空表示通过")
    private List<String> blockReasons;

    @Data
    @Builder
    @Schema(description = "教师端权限判断结果")
    public static class PermissionResult {
        @Schema(description = "当前登录用户ID", example = "1")
        private Long userId;

        @Schema(description = "教师ID", example = "1")
        private Long teacherId;

        @Schema(description = "教师姓名", example = "李老师")
        private String teacherName;

        @Schema(description = "是否为课程主讲教师角色", example = "true")
        private Boolean hasInstructorRole;

        @Schema(description = "是否为当前教学班负责教师", example = "true")
        private Boolean ownsTeachingClass;

        @Schema(description = "是否可操作当前教学班", example = "true")
        private Boolean canOperate;

        @Schema(description = "权限阻断原因")
        private String blockReason;
    }
}
