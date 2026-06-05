package com.oss.osscourse.dto.teachingclass;

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
@Schema(description = "教学班学生导入结果")
public class StudentClassImportResult {

    @Schema(description = "总记录数")
    private int totalCount;

    @Schema(description = "成功条数")
    private int successCount;

    @Schema(description = "失败条数")
    private int failureCount;

    @Schema(description = "失败明细")
    private List<FailedItem> failedItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "导入失败项")
    public static class FailedItem {

        @Schema(description = "失败行号（从1开始）")
        private int rowNumber;

        @Schema(description = "失败原因")
        private String reason;
    }
}
