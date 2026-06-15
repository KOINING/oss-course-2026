package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "教学班保存请求（新增或更新）")
public class TeachingClassSaveRequest {

    @Schema(description = "教学班ID，新增时不传，更新时必传", example = "1")
    private Long classId;

    @NotBlank(message = "教学班编号不能为空")
    @Size(max = 32, message = "教学班编号长度不能超过32个字符")
    @Schema(description = "教学班编号", example = "TC2024CS01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String classCode;

    @NotBlank(message = "教学班名称不能为空")
    @Size(max = 50, message = "教学班名称长度不能超过50个字符")
    @Schema(description = "教学班名称", example = "数据结构2024-2025-1班", requiredMode = Schema.RequiredMode.REQUIRED)
    private String className;

    @NotNull(message = "所属课程不能为空")
    @Schema(description = "所属课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "所属年级不能为空")
    @Schema(description = "所属年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @NotNull(message = "所属学期不能为空")
    @Schema(description = "所属学期ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long termId;

    @NotNull(message = "主讲教师不能为空")
    @Schema(description = "主讲教师ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teacherId;
}
