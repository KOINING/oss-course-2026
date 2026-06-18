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

    @Schema(description = "培养方案适用年级")
    private Integer gradeYear;

    @Schema(description = "状态：1=启用，0=停用")
    private Integer status;

    @Schema(description = "当前页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，不传则不分页")
    private Integer pageSize;
}
