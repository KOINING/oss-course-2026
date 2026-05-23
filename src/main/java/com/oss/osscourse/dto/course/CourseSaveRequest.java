package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "课程新增/更新请求")
public class CourseSaveRequest {
    @Schema(description = "课程ID（更新时必填）")
    private Long courseId;

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 20, message = "课程编码长度不能超过20位")
    @Schema(description = "课程编码", example = "CS201")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100位")
    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @NotNull(message = "学分不能为空")
    @Schema(description = "学分", example = "4.0")
    private Double credit;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID")
    private Long majorId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用", example = "1")
    private Integer status;
}
