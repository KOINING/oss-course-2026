package com.oss.osscourse.dto.courseobjective;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程目标响应对象")
public class CourseObjectiveResponse {
    @Schema(description = "课程目标ID", example = "1")
    private Long coId;

    @Schema(description = "课程目标编号", example = "CO1")
    private String objectiveCode;

    @Schema(description = "纯文本描述，用于列表摘要展示和计算链关联", example = "掌握线性表、栈、队列等基本数据结构的逻辑结构与物理实现")
    private String description;

    @Schema(description = "富文本描述（HTML），仅详情接口返回，列表接口为null", example = "<p>掌握<b>线性表</b>、栈、队列等基本数据结构的逻辑结构与物理实现</p>")
    private String descriptionRich;

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
