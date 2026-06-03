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
@Schema(description = "课程导入结果")
public class CourseImportResult {

    @Schema(description = "总记录数")
    private int totalCount;

    @Schema(description = "新增或更新成功条数")
    private int successCount;

    @Schema(description = "已存在并跳过条数")
    private int skippedCount;

    @Schema(description = "失败条数")
    private int failureCount;

    @Schema(description = "跳过明细")
    private List<SkippedItem> skippedItems;

    @Schema(description = "失败明细")
    private List<FailedItem> failedItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "导入跳过项")
    public static class SkippedItem {

        @Schema(description = "跳过行号（从1开始）")
        private int rowNumber;

        @Schema(description = "跳过原因")
        private String reason;
    }

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
