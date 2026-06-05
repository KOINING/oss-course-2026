package com.oss.osscourse.dto.assessmentpoint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "考核点响应对象")
public class AssessmentPointResponse {
    @Schema(description = "考核点ID", example = "1")
    private Long apId;

    @Schema(description = "考核点名称", example = "在线作业、课堂测试")
    private String apName;

    @Schema(description = "满分", example = "10.0")
    private Float fullScore;

    @Schema(description = "绑定的课程目标ID", example = "1")
    private Long coId;

    @Schema(description = "课程目标编号", example = "CO1")
    private String objectiveCode;

    @Schema(description = "课程目标纯文本描述")
    private String coDescription;

    @Schema(description = "所属课程ID", example = "1")
    private Long courseId;

    @Schema(description = "所属课程编码", example = "CS201")
    private String courseCode;

    @Schema(description = "所属课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
