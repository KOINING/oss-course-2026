package com.oss.osscourse.dto.college;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学院查询请求")
public class CollegeQueryRequest {

    @Schema(description = "学院编码，模糊查询")
    private String collegeCode;

    @Schema(description = "学院名称，模糊查询")
    private String collegeName;
}
