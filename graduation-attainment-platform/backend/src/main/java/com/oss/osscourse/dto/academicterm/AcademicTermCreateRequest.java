package com.oss.osscourse.dto.academicterm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "学年学期新增请求")
public class AcademicTermCreateRequest {

    @NotBlank(message = "学期编码不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{4}-[12]$", message = "学期编码格式应为 YYYY-YYYY-1 或 YYYY-YYYY-2")
    @Schema(description = "学期编码，如 2024-2025-1", example = "2024-2025-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String termCode;

    @NotNull(message = "学年不能为空")
    @Schema(description = "学年，如 2024", example = "2024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer academicYear;

    @NotNull(message = "学期序号不能为空")
    @Schema(description = "学期序号，1=第一学期，2=第二学期", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer semester;

    @NotNull(message = "开始日期不能为空")
    @Schema(description = "学期开始日期", example = "2024-09-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Schema(description = "学期结束日期", example = "2025-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;
}
