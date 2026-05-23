package com.oss.osscourse.dto.academicterm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学年学期查询请求")
public class AcademicTermQueryRequest {

    @Schema(description = "学期编码，模糊查询")
    private String termCode;

    @Schema(description = "学年")
    private Integer academicYear;

    @Schema(description = "学期序号，1=第一学期，2=第二学期")
    private Integer semester;
}
