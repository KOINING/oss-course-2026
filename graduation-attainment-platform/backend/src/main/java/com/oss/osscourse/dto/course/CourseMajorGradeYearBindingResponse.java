package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程适用的专业-年级绑定关系响应")
public class CourseMajorGradeYearBindingResponse {
    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "所属专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "适用年级列表", example = "[2022,2023]")
    private List<Integer> gradeYears;
}
