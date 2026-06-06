package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.achievement.AssessmentFilterOptionsResponse;
import com.oss.osscourse.dto.achievement.MacroDashboardRequest;
import com.oss.osscourse.dto.achievement.MacroDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResultResponse;
import com.oss.osscourse.service.AssessmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
@Tag(name = "专业级看板与结果", description = "专业负责人/教务查看宏观看板、汇总结果的接口")
public class AssessmentQueryController {

    private static final List<String> ALLOWED_ROLES = List.of("program_director", "academic_affairs");

    private final AssessmentQueryService assessmentQueryService;

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

    private void ensureAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(ALLOWED_ROLES::contains)) {
            throw new com.oss.osscourse.common.BusinessException(403, "当前账号无权访问专业级看板或结果");
        }
    }
}
