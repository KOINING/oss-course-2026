package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "按专业筛选请求")
public class SupportMatrixMajorFilterRequest {
    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "培养方案适用年级", example = "2022")
    private Integer gradeYear;
}
