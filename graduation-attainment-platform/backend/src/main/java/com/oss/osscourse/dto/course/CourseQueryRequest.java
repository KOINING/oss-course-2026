package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "课程查询请求")
public class CourseQueryRequest {

    @Schema(description = "课程编码，模糊查询")
    private String courseCode;

    @Schema(description = "课程名称，模糊查询")
    private String courseName;

    @Schema(description = "所属专业ID")
    private Long majorId;

    @Schema(description = "状态：1=启用，0=停用")
    private Integer status;
}
