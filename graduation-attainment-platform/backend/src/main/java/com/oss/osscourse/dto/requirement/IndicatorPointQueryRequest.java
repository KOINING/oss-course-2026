package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "指标点查询请求")
public class IndicatorPointQueryRequest {
    @Schema(description = "指标点编号，模糊查询", example = "IP01")
    private String ipCode;

    @Schema(description = "所属毕业要求ID", example = "1")
    private Long grId;
}
