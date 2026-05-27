package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "毕业要求查询请求")
public class GraduationRequirementQueryRequest {
    @Schema(description = "毕业要求编号，模糊查询", example = "GR01")
    private String grCode;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;
}
