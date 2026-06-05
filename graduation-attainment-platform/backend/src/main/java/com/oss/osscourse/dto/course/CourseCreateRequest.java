package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "新增课程请求")
public class CourseCreateRequest {

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 20, message = "课程编码长度不能超过20个字符")
    @Schema(description = "课程编码", example = "CS201", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    @Schema(description = "课程名称", example = "数据结构", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseName;

    @NotNull(message = "学分不能为空")
    @Schema(description = "学分", example = "4.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float credit;

    @Schema(description = "所属专业ID列表，仅兼容旧版请求", example = "[1,2]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Long> majorIds;

    @Schema(description = "课程适用的专业-年级绑定关系", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CourseMajorGradeYearBindingRequest> majorGradeYearBindings;

    @Schema(description = "状态：1=启用，0=停用", example = "1", defaultValue = "1")
    private Integer status;
}
