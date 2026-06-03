package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "保存支撑矩阵请求")
public class SupportMatrixSaveRequest {
    @NotNull(message = "专业ID不能为空")
    @Schema(description = "专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotNull(message = "年级不能为空")
    @Schema(description = "培养方案适用年级", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gradeYear;

    @Schema(description = "学期ID（当前版本仅透传，不参与关系主键）", example = "1")
    private Long termId;

    @Valid
    @NotNull(message = "rows不能为空")
    @Schema(description = "关系行集合")
    private List<SupportMatrixRowRequest> rows = new ArrayList<>();
}
