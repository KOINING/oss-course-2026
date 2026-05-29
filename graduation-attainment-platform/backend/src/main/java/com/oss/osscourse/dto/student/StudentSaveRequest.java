package com.oss.osscourse.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "学生保存请求（新增或更新）")
public class StudentSaveRequest {

    @Schema(description = "学生ID，新增时不传，更新时必传", example = "1")
    private Long studentId;

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20个字符")
    @Schema(description = "学号，如 20220101001", example = "20220101001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentNo;

    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过50个字符")
    @Schema(description = "学生姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentName;

    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "入学年份不能为空")
    @Min(value = 2000, message = "入学年份不能早于2000年")
    @Max(value = 2100, message = "入学年份不能晚于2100年")
    @Schema(description = "入学年份", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer enrollmentYear;

    @Schema(description = "关联用户ID（可选）", example = "1")
    private Long userId;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为0-3")
    @Max(value = 3, message = "状态值必须为0-3")
    @Schema(description = "状态：1=在读，2=毕业，3=休学，0=退学", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
