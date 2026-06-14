package com.oss.osscourse.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程级评价报表查询请求")
public class CourseReportRequest {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @Schema(description = "专业ID，当同一课程同一年级对应多个专业方案时建议传入", example = "1")
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "年级（入学年份）", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @Schema(description = "学期ID（可选，不传则查询该年级所有学期）", example = "1")
    private Long termId;
}
