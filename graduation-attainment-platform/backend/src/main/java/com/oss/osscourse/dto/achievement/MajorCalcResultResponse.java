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
@Schema(description = "专业级结果查看响应")
public class MajorCalcResultResponse {
    private Long majorId;
    private String majorName;
    private Integer gradeYear;
    private Long termId;
    private String termCode;
    private Boolean resultReady;
    private String message;
    private List<MajorCalcResponse.IndicatorAchievement> indicatorAchievements;
}
