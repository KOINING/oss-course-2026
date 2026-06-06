package com.oss.osscourse.dto.achievement;

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
@Schema(description = "专业级看板筛选项")
public class AssessmentFilterOptionsResponse {

    @Schema(description = "专业列表")
    private List<MajorOption> majors;

    @Schema(description = "年级列表")
    private List<Integer> gradeYears;

    @Schema(description = "学期列表")
    private List<TermOption> terms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorOption {
        private Long majorId;
        private String majorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermOption {
        private Long termId;
        private String termCode;
    }
}
