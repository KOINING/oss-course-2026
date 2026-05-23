package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.requirement.*;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.service.RequirementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping("/listGraduationRequirements")
    public Result<List<GraduationRequirementResponse>> listGraduationRequirements(
            @RequestBody(required = false) GraduationRequirementQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listGraduationRequirements(request, roles, permissions));
    }

    @PostMapping("/addGraduationRequirement")
    public Result<Void> addGraduationRequirement(
            @Valid @RequestBody AddGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.addGraduationRequirement(request, roles, permissions);
        return Result.ok("毕业要求创建成功", null);
    }

    @PostMapping("/updateGraduationRequirement")
    public Result<Void> updateGraduationRequirement(
            @Valid @RequestBody UpdateGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateGraduationRequirement(request, roles, permissions);
        return Result.ok("毕业要求更新成功", null);
    }

    @PostMapping("/deleteGraduationRequirement")
    public Result<Void> deleteGraduationRequirement(
            @Valid @RequestBody DeleteGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.deleteGraduationRequirement(request.getGrId(), roles, permissions);
        return Result.ok("毕业要求删除成功", null);
    }

    @PostMapping("/listIndicatorPoints")
    public Result<List<IndicatorPointResponse>> listIndicatorPoints(
            @RequestBody(required = false) IndicatorPointQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listIndicatorPoints(request, roles, permissions));
    }

    @PostMapping("/addIndicatorPoint")
    public Result<Void> addIndicatorPoint(
            @Valid @RequestBody AddIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.addIndicatorPoint(request, roles, permissions);
        return Result.ok("指标点创建成功", null);
    }

    @PostMapping("/updateIndicatorPoint")
    public Result<Void> updateIndicatorPoint(
            @Valid @RequestBody UpdateIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateIndicatorPoint(request, roles, permissions);
        return Result.ok("指标点更新成功", null);
    }

    @PostMapping("/deleteIndicatorPoint")
    public Result<Void> deleteIndicatorPoint(
            @Valid @RequestBody DeleteIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.deleteIndicatorPoint(request.getIpId(), roles, permissions);
        return Result.ok("指标点删除成功", null);
    }

    @PostMapping("/listMajors")
    public Result<List<Major>> listMajors(
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listMajors(roles, permissions));
    }
}
