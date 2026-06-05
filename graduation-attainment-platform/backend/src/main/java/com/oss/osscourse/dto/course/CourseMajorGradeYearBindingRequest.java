package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "课程适用的专业-年级绑定关系请求")
public class CourseMajorGradeYearBindingRequest {
    @NotNull(message = "所属专业不能为空")
    @Schema(description = "所属专业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long majorId;

    @NotEmpty(message = "适用年级不能为空")
    @Schema(description = "适用年级列表", example = "[2022,2023]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Min(2000) @Max(2100) Integer> gradeYears;
}
