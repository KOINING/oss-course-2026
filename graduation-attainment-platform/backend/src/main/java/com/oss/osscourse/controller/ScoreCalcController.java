package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.achievement.*;
import com.oss.osscourse.dto.score.*;
import com.oss.osscourse.service.ScoreCalcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "成绩计算管理", description = "成绩模板、成绩导入、课程级计算、专业级汇总接口")
public class ScoreCalcController {

    private final ScoreCalcService scoreCalcService;

    @PostMapping("/previewTemplate")
    @Operation(summary = "预览成绩模板", description = "根据教学班ID生成动态成绩模板预览")
    public Result<ScoreTemplatePreviewResponse> previewTemplate(
            @Parameter(description = "教学班ID", required = true) @RequestParam Long classId) {
        ScoreTemplatePreviewResponse response = scoreCalcService.previewTemplate(classId);
        return Result.ok(response);
    }

    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载成绩模板", description = "根据教学班ID下载Excel格式的成绩模板")
    public ResponseEntity<byte[]> downloadTemplate(
            @Parameter(description = "教学班ID", required = true) @RequestParam Long classId) {
        byte[] excelBytes = scoreCalcService.downloadTemplate(classId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "成绩模板_" + classId + ".xlsx");
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @PostMapping("/importScorePreview")
    @Operation(summary = "成绩导入预校验", description = "预览导入结果，检查错误")
    public Result<ScoreImportPreviewResponse> importScorePreview(
            @Parameter(description = "成绩导入请求", required = true)
            @Valid @RequestBody ScoreImportRequest request) {
        ScoreImportPreviewResponse response = scoreCalcService.importScorePreview(request);
        return Result.ok(response);
    }

    @PostMapping("/saveScores")
    @Operation(summary = "保存成绩", description = "保存已校验的成绩数据")
    public Result<Void> saveScores(
            @Parameter(description = "成绩保存请求", required = true)
            @Valid @RequestBody ScoreSaveRequest request) {
        scoreCalcService.saveScores(request);
        return Result.ok("成绩保存成功", null);
    }

    @PostMapping("/calcCourseAchievement")
    @Operation(summary = "课程级达成度计算", description = "触发课程级达成度计算")
    public Result<CourseCalcResponse> calcCourseAchievement(
            @Parameter(description = "课程级计算请求", required = true)
            @Valid @RequestBody CourseCalcRequest request) {
        CourseCalcResponse response = scoreCalcService.calcCourseAchievement(request);
        return Result.ok(response);
    }

    @PostMapping("/calcMajorAchievement")
    @Operation(summary = "专业级达成度汇总", description = "触发专业级指标点达成度汇总")
    public Result<MajorCalcResponse> calcMajorAchievement(
            @Parameter(description = "专业级汇总请求", required = true)
            @Valid @RequestBody MajorCalcRequest request) {
        MajorCalcResponse response = scoreCalcService.calcMajorAchievement(request);
        return Result.ok(response);
    }

    @PostMapping("/getCourseCalcStatus")
    @Operation(summary = "查询课程计算状态", description = "查询某专业某学期所有支撑课程的计算状态")
    public Result<CourseCalcStatusResponse> getCourseCalcStatus(
            @Parameter(description = "专业ID", required = true) @RequestParam Long majorId,
            @Parameter(description = "学期ID", required = true) @RequestParam Long termId) {
        CourseCalcStatusResponse response = scoreCalcService.getCourseCalcStatus(majorId, termId);
        return Result.ok(response);
    }
}
