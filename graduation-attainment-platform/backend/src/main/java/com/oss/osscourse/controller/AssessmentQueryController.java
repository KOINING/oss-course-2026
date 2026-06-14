package com.oss.osscourse.controller;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.achievement.AssessmentFilterOptionsResponse;
import com.oss.osscourse.dto.achievement.MacroDashboardRequest;
import com.oss.osscourse.dto.achievement.MacroDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResultResponse;
import com.oss.osscourse.dto.achievement.UnlockRequestApproveRequest;
import com.oss.osscourse.dto.report.MajorReportRequest;
import com.oss.osscourse.service.AssessmentQueryService;
import com.oss.osscourse.service.MajorReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
@Tag(name = "专业级看板与结果", description = "专业负责人/教务查看宏观看板、汇总结果的接口")
public class AssessmentQueryController {

    private static final List<String> ALLOWED_ROLES = List.of("program_director", "academic_affairs");
    private static final List<String> UNLOCK_ROLES = List.of("academic_affairs");

    private final AssessmentQueryService assessmentQueryService;
    private final MajorReportService majorReportService;

    @PostMapping("/listMajorGradeYearTerms")
    @Operation(summary = "查询专业级看板筛选项")
    public Result<AssessmentFilterOptionsResponse> listMajorGradeYearTerms(
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(assessmentQueryService.listMajorGradeYearTerms());
    }

    @PostMapping("/getMacroDashboard")
    @Operation(summary = "查询专业级宏观看板")
    public Result<MacroDashboardResponse> getMacroDashboard(
            @Valid @RequestBody MacroDashboardRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(assessmentQueryService.getMacroDashboard(request));
    }

    @PostMapping("/getMajorCalcResult")
    @Operation(summary = "查询专业级汇总结果")
    public Result<MajorCalcResultResponse> getMajorCalcResult(
            @Valid @RequestBody MacroDashboardRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(assessmentQueryService.getMajorCalcResult(request));
    }

    @PostMapping("/approveUnlock")
    @Operation(summary = "教务执行教学班解锁")
    public Result<Void> approveUnlock(
            @Valid @RequestBody UnlockRequestApproveRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        ensureUnlockAccess(roles);
        assessmentQueryService.approveUnlock(request, userId);
        return Result.ok("教学班已解锁", null);
    }

    @PostMapping("/exportMajorReport")
    @Operation(summary = "导出专业级评价报告 Excel")
    public ResponseEntity<byte[]> exportMajorReport(
            @Valid @RequestBody MacroDashboardRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);

        if (request.getGradeYear() == null) {
            throw new BusinessException(400, "年级不能为空");
        }

        // 转换请求体，复用 MajorReportService 的统一结果源
        MajorReportRequest reportRequest = new MajorReportRequest();
        reportRequest.setMajorId(request.getMajorId());
        reportRequest.setGradeYear(request.getGradeYear());
        reportRequest.setTermId(request.getTermId());

        byte[] excelBytes = majorReportService.exportMajorReport(reportRequest);

        String fileName = "专业级达成度评价报告_" + request.getGradeYear() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(excelBytes.length);
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    private void ensureAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(ALLOWED_ROLES::contains)) {
            throw new com.oss.osscourse.common.BusinessException(403, "当前账号无权访问专业级看板或结果");
        }
    }

    private void ensureUnlockAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(UNLOCK_ROLES::contains)) {
            throw new com.oss.osscourse.common.BusinessException(403, "当前账号无权执行教学班解锁");
        }
    }
}
