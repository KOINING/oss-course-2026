package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专业列表查询请求")
public class MajorQueryRequest {
    @Schema(description = "专业编码（模糊匹配）")
    private String majorCode;

    @Schema(description = "专业名称（模糊匹配）")
    private String majorName;

    @Schema(description = "所属学院ID")
    private Long collegeId;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
