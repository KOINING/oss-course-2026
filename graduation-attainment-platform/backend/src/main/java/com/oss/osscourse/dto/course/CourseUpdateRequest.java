package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "课程更新请求")
public class CourseUpdateRequest {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @Size(max = 20, message = "课程编码长度不能超过20个字符")
    @Schema(description = "课程编码，如 CS201", example = "CS201")
    private String courseCode;

    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    @Schema(description = "课程名称，如 数据结构", example = "数据结构")
    private String courseName;

    @Schema(description = "学分", example = "4.0")
    private Float credit;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "状态：1=开课中，0=停开", example = "1")
    private Integer status;
}
