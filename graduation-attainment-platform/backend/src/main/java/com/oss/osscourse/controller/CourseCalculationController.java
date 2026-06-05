package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingRequest;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingRequest;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingResponse;
import com.oss.osscourse.service.CourseCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/calculation")
@RequiredArgsConstructor
@Tag(name = "课程计算数据服务", description = "提供公式导向的查询接口，直接服务于课程目标级和课程级达成度公式，并内嵌跨引用校验")
public class CourseCalculationController {

    private final CourseCalculationService calculationService;

    @PostMapping("/assessment-objective-mapping")
    @Operation(
            summary = "考核点→课程目标映射",
            description = "查询该课程下所有课程目标及其绑定的考核点与满分，直接服务于课程目标级达成度公式 "
                    + "C_ij = Σ(考核点实际得分) / Σ(考核点满分)。"
                    + " 内嵌跨课程引用校验：不允许考核点引用其他课程的目标。"
    )
    public Result<AssessmentObjectiveMappingResponse> getAssessmentObjectiveMapping(
            @Parameter(description = "查询请求（courseId）", required = true)
            @Valid @RequestBody AssessmentObjectiveMappingRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(calculationService.getAssessmentObjectiveMapping(request, roles, permissions));
    }

    @PostMapping("/objective-indicator-mapping")
    @Operation(
            summary = "课程目标→指标点内部权重映射",
            description = "查询该课程 + 年级版本下所有指标点及其贡献课程目标与内部权重 w_jk，直接服务于课程级指标点达成度公式 "
                    + "E_k = Σ_j (C̄_j × w_jk)。"
                    + " 内嵌跨年级/跨课程引用校验：不允许课程目标引用不属本课程的指标点；权重和 ≠ 1.0 时标记并告警。"
    )
    public Result<ObjectiveIndicatorMappingResponse> getObjectiveIndicatorMapping(
            @Parameter(description = "查询请求（courseId + gradeYear）", required = true)
            @Valid @RequestBody ObjectiveIndicatorMappingRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(calculationService.getObjectiveIndicatorMapping(request, roles, permissions));
    }
}
