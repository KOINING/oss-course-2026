package com.oss.osscourse.dto.score;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "成绩保存请求")
public class ScoreSaveRequest {

    @NotNull(message = "教学班ID不能为空")
    @Schema(description = "教学班ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @NotNull(message = "成绩数据不能为空")
    @Schema(description = "成绩数据列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ScoreItem> scores;

    @Data
    @Schema(description = "成绩项")
    public static class ScoreItem {
        @Schema(description = "学生ID", example = "1")
        private Long studentId;

        @Schema(description = "考核点ID", example = "1")
        private Long apId;

        @Schema(description = "实际得分", example = "18.5")
        private Float actualScore;
    }
}
