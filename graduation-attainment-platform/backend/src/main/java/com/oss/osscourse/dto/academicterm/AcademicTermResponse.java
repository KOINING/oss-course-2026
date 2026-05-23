package com.oss.osscourse.dto.academicterm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学年学期响应对象")
public class AcademicTermResponse {

    @Schema(description = "学期ID", example = "1")
    private Long termId;

    @Schema(description = "学期编码", example = "2024-2025-1")
    private String termCode;

    @Schema(description = "学年", example = "2024")
    private Integer academicYear;

    @Schema(description = "学期序号", example = "1")
    private Integer semester;

    @Schema(description = "学期开始日期", example = "2024-09-01")
    private LocalDate startDate;

    @Schema(description = "学期结束日期", example = "2025-01-15")
    private LocalDate endDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
