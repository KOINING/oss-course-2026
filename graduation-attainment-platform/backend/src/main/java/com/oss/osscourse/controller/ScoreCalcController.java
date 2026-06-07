package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.achievement.CourseCalcRequest;
import com.oss.osscourse.dto.achievement.CourseCalcResponse;
import com.oss.osscourse.dto.achievement.CourseCalcStatusResponse;
import com.oss.osscourse.dto.achievement.CourseObjectiveDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcRequest;
import com.oss.osscourse.dto.achievement.MajorCalcResponse;
import com.oss.osscourse.dto.score.ScoreImportPreviewResponse;
import com.oss.osscourse.dto.score.ScoreImportRequest;
import com.oss.osscourse.dto.score.ScoreSaveRequest;
import com.oss.osscourse.dto.score.ScoreTemplatePreviewResponse;
import com.oss.osscourse.service.ScoreCalcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "成绩计算管理", description = "成绩模板、成绩导入、课程级计算、专业级汇总接口")
public class ScoreCalcController {

    private static final List<String> MAJOR_ROLES = List.of("program_director", "academic_affairs");

    private final ScoreCalcService scoreCalcService;

    @PostMapping("/previewTemplate")
    @Operation(summary = "预览成绩模板")
    public Result<ScoreTemplatePreviewResponse> previewTemplate(
            @Parameter(description = "教学班ID", required = true) @RequestParam Long classId) {
        return Result.ok(scoreCalcService.previewTemplate(classId));
    }

    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载成绩模板")
    public ResponseEntity<byte[]> downloadTemplate(
            @Parameter(description = "教学班ID", required = true) @RequestParam Long classId) {
        byte[] excelBytes = scoreCalcService.downloadTemplate(classId);
        String fileName = "成绩模板_" + classId + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(excelBytes.length);
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    @PostMapping("/importScorePreview")
    @Operation(summary = "成绩导入预校验")
    public Result<ScoreImportPreviewResponse> importScorePreview(@Valid @RequestBody ScoreImportRequest request) {
        return Result.ok(scoreCalcService.importScorePreview(request));
    }

    @PostMapping("/saveScores")
    @Operation(summary = "保存成绩")
    public Result<Void> saveScores(@Valid @RequestBody ScoreSaveRequest request) {
        scoreCalcService.saveScores(request);
        return Result.ok("成绩保存成功", null);
    }

    @PostMapping("/calcCourseAchievement")
    @Operation(summary = "课程级达成度计算")
    public Result<CourseCalcResponse> calcCourseAchievement(@Valid @RequestBody CourseCalcRequest request) {
        return Result.ok(scoreCalcService.calcCourseAchievement(request));
    }

    @PostMapping("/getCourseObjectiveDashboard")
    @Operation(summary = "查询课程目标达成看板")
    public Result<CourseObjectiveDashboardResponse> getCourseObjectiveDashboard(
            @Parameter(description = "教学班ID", required = true) @RequestParam Long classId) {
        return Result.ok(scoreCalcService.getCourseObjectiveDashboard(classId));
    }

    @PostMapping("/calcMajorAchievement")
    @Operation(summary = "专业级达成度汇总")
    public Result<MajorCalcResponse> calcMajorAchievement(
            @Valid @RequestBody MajorCalcRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureMajorAccess(roles);
        return Result.ok(scoreCalcService.calcMajorAchievement(request));
    }

    @PostMapping("/getCourseCalcStatus")
    @Operation(summary = "查询课程计算状态")
    public Result<CourseCalcStatusResponse> getCourseCalcStatus(
            @RequestParam Long majorId,
            @RequestParam Long termId,
            @RequestAttribute("roles") List<String> roles) {
        ensureMajorAccess(roles);
        return Result.ok(scoreCalcService.getCourseCalcStatus(majorId, termId));
    }

    private void ensureMajorAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(MAJOR_ROLES::contains)) {
            throw new com.oss.osscourse.common.BusinessException(403, "当前账号无权执行专业级汇总操作");
        }
    }
}
