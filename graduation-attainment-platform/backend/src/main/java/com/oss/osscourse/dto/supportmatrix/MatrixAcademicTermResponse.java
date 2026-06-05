package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵-学年学期下拉项")
public class MatrixAcademicTermResponse {
    @Schema(description = "学期ID", example = "1")
    private Long termId;

    @Schema(description = "学期编码", example = "2025-2026-1")
    private String termCode;

    @Schema(description = "学年", example = "2025")
    private Integer academicYear;

    @Schema(description = "学期序号", example = "1")
    private Integer semester;

    @Schema(description = "展示名", example = "2025-2026 学年 第1学期")
    private String termName;
}
