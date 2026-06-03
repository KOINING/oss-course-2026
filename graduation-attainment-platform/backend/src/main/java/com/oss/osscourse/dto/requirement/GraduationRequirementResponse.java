package com.oss.osscourse.dto.requirement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "毕业要求响应对象")
public class GraduationRequirementResponse {
    @Schema(description = "毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "毕业要求编号", example = "GR01")
    private String grCode;

    @Schema(description = "毕业要求描述", example = "能够应用工程知识解决复杂工程问题")
    private String grDescription;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "所属专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "培养方案适用年级", example = "2022")
    private Integer gradeYear;
}
