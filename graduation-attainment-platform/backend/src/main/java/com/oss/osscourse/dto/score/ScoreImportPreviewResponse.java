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
@Schema(description = "成绩导入预览响应")
public class ScoreImportPreviewResponse {

    @Schema(description = "总记录数", example = "30")
    private Integer totalRows;

    @Schema(description = "成功记录数", example = "28")
    private Integer successCount;

    @Schema(description = "失败记录数", example = "2")
    private Integer failCount;

    @Schema(description = "是否可以保存", example = "true")
    private Boolean canSave;

    @Schema(description = "成功记录")
    private List<SuccessRow> successRows;

    @Schema(description = "失败记录")
    private List<FailRow> failRows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "成功行")
    public static class SuccessRow {
        @Schema(description = "行号", example = "2")
        private Integer rowIndex;

        @Schema(description = "学号", example = "20220101001")
        private String studentNo;

        @Schema(description = "学生姓名", example = "张三")
        private String studentName;

        @Schema(description = "学生ID", example = "1")
        private Long studentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "失败行")
    public static class FailRow {
        @Schema(description = "行号", example = "3")
        private Integer rowIndex;

        @Schema(description = "学号", example = "20220101002")
        private String studentNo;

        @Schema(description = "学生姓名", example = "李四")
        private String studentName;

        @Schema(description = "失败原因", example = "学号不存在")
        private String errorMessage;
    }
}
