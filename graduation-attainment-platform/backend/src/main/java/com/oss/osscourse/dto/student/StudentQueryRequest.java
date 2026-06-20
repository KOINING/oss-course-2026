package com.oss.osscourse.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学生查询请求")
public class StudentQueryRequest {

    @Schema(description = "学号，模糊查询")
    private String studentNo;

    @Schema(description = "学生姓名，模糊查询")
    private String studentName;

    @Schema(description = "所属专业ID")
    private Long majorId;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "状态：1=在读，2=毕业，3=休学，0=退学")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize;
}
