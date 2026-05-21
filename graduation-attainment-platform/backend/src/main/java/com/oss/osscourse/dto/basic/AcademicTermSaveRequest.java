package com.oss.osscourse.dto.basic;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AcademicTermSaveRequest {
    private Long termId;

    @NotBlank(message = "学期编码不能为空")
    @Size(max = 20, message = "学期编码长度不能超过20")
    private String termCode;

    @NotNull(message = "学年不能为空")
    @Min(value = 2000, message = "学年不合法")
    @Max(value = 2100, message = "学年不合法")
    private Integer academicYear;

    @NotNull(message = "学期不能为空")
    @Min(value = 1, message = "学期只能为1或2")
    @Max(value = 2, message = "学期只能为1或2")
    private Integer semester;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
}
