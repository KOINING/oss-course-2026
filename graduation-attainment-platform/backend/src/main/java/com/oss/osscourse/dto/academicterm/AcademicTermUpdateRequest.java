package com.oss.osscourse.dto.academicterm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "学年学期更新请求")
public class AcademicTermUpdateRequest {

    @NotNull(message = "学期ID不能为空")
    @Schema(description = "学期ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long termId;

    @Pattern(regexp = "^\\d{4}-\\d{4}-[12]$", message = "学期编码格式应为 YYYY-YYYY-1 或 YYYY-YYYY-2")
    @Schema(description = "学期编码，如 2024-2025-1", example = "2024-2025-1")
    private String termCode;

    @Schema(description = "学年，如 2024", example = "2024")
    private Integer academicYear;

    @Schema(description = "学期序号，1=第一学期，2=第二学期", example = "1")
    private Integer semester;

    @Schema(description = "学期开始日期", example = "2024-09-01")
    private LocalDate startDate;

    @Schema(description = "学期结束日期", example = "2025-01-15")
    private LocalDate endDate;
}
