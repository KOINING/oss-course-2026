package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支撑矩阵-课程下拉项")
public class MatrixCourseOptionResponse {
    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程编码", example = "CS101")
    private String courseCode;

    @Schema(description = "课程名称", example = "程序设计基础")
    private String courseName;

    @Schema(description = "状态：1=启用，0=停用", example = "1")
    private Integer status;

    @Schema(description = "培养方案适用年级", example = "2022")
    private Integer gradeYear;
}
