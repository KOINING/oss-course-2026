package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "课程新增请求")
public class CourseCreateRequest {

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 20, message = "课程编码长度不能超过20个字符")
    @Schema(description = "课程编码，如 CS201", example = "CS201", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    @Schema(description = "课程名称，如 数据结构", example = "数据结构", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseName;

    @NotNull(message = "学分不能为空")
    @Schema(description = "学分", example = "4.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float credit;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @Schema(description = "状态：1=开课中，0=停开", example = "1", defaultValue = "1")
    private Integer status;
}
