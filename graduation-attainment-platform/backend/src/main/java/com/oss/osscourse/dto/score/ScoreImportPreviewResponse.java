package com.oss.osscourse.dto.score;

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
@Schema(description = "成绩导入预校验响应")
public class ScoreImportPreviewResponse {

    @Schema(description = "总记录数", example = "30")
    private Integer totalRows;

    @Schema(description = "成功记录数", example = "28")
    private Integer successCount;

    @Schema(description = "失败记录数", example = "2")
    private Integer failCount;

    @Schema(description = "是否允许保存", example = "true")
    private Boolean canSave;

    @Schema(description = "校验通过的行")
    private List<SuccessRow> successRows;

    @Schema(description = "校验失败的行")
    private List<FailRow> failRows;

    @Schema(description = "通过校验后可直接保存的成绩明细")
    private List<ScoreSaveRequest.ScoreItem> scoreItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "成功行")
    public static class SuccessRow {
        @Schema(description = "Excel 行号", example = "4")
        private Integer rowIndex;

        @Schema(description = "学号", example = "20220101001")
        private String studentNo;

        @Schema(description = "学生姓名", example = "张三")
        private String studentName;

        @Schema(description = "学生ID", example = "1")
        private Long studentId;

        @Schema(description = "本行已识别成绩项数量", example = "8")
        private Integer scoreCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "失败行")
    public static class FailRow {
        @Schema(description = "Excel 行号", example = "5")
        private Integer rowIndex;

        @Schema(description = "学号", example = "20220101002")
        private String studentNo;

        @Schema(description = "学生姓名", example = "李四")
        private String studentName;

        @Schema(description = "失败原因", example = "学号不属于当前教学班")
        private String errorMessage;
    }
}
