package com.oss.osscourse.dto.courseobjective;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新增课程目标请求")
public class CourseObjectiveCreateRequest {
    @NotBlank(message = "课程目标编号不能为空")
    @Schema(description = "课程目标编号", example = "CO1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectiveCode;

    @NotBlank(message = "课程目标纯文本描述不能为空")
    @Schema(description = "纯文本描述，用于列表摘要和计算链关联", example = "掌握线性表、栈、队列等基本数据结构的逻辑结构与物理实现", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("coDescription")
    private String description;

    @Schema(description = "富文本描述（HTML），用于详情页完整展示。不传时等同于 description", example = "<p>掌握<b>线性表</b>、栈、队列等基本数据结构的逻辑结构与物理实现</p>")
    private String descriptionRich;

    @NotNull(message = "所属课程不能为空")
    @Schema(description = "所属课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;
}
