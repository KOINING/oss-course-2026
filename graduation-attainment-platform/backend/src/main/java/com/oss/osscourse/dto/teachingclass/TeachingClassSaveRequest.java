package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "教学班保存请求（新增或更新）")
public class TeachingClassSaveRequest {

    @Schema(description = "教学班ID，新增时不传，更新时必传", example = "1")
    private Long classId;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 50, message = "班级名称长度不能超过50个字符")
    @Schema(description = "班级名称，如 数据结构2024-2025-1班", example = "数据结构2024-2025-1班", requiredMode = Schema.RequiredMode.REQUIRED)
    private String className;

    @NotNull(message = "所属课程不能为空")
    @Schema(description = "所属课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "所属学期不能为空")
    @Schema(description = "所属学期ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long termId;

    @NotNull(message = "主讲教师不能为空")
    @Schema(description = "主讲教师ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teacherId;
}
