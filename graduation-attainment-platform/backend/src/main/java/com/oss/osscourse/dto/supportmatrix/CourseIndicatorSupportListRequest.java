package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询课程-指标点支撑关系请求")
public class CourseIndicatorSupportListRequest {
    @Schema(description = "专业ID，传入后限定在该专业范围内", example = "1")
    private Long majorId;

    @Schema(description = "培养方案适用年级，传入后限定在该年级范围内", example = "2022")
    private Integer gradeYear;

    @Schema(description = "课程ID，精确筛选", example = "10")
    private Long courseId;

    @Schema(description = "指标点ID，精确筛选", example = "21")
    private Long ipId;
}
