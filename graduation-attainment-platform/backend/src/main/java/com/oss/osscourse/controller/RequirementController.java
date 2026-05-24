package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.requirement.*;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@Tag(name = "毕业要求与指标点管理", description = "毕业要求、指标点的列表、筛选、新增、更新、删除和停启用接口")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping("/listGraduationRequirements")
    @Operation(summary = "查询毕业要求列表", description = "支持按毕业要求编号、所属专业、状态筛选")
    public Result<List<GraduationRequirementResponse>> listGraduationRequirements(
            @Parameter(description = "毕业要求查询条件")
            @RequestBody(required = false) GraduationRequirementQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listGraduationRequirements(request, roles, permissions));
    }

    @PostMapping("/addGraduationRequirement")
    @Operation(summary = "新增毕业要求", description = "创建毕业要求，默认状态为启用")
    public Result<Void> addGraduationRequirement(
            @Parameter(description = "新增毕业要求请求", required = true)
            @Valid @RequestBody AddGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.addGraduationRequirement(request, roles, permissions);
        return Result.ok("毕业要求创建成功", null);
    }

    @PostMapping("/updateGraduationRequirement")
    @Operation(summary = "更新毕业要求", description = "更新毕业要求编号、描述、所属专业，可同步更新状态")
    public Result<Void> updateGraduationRequirement(
            @Parameter(description = "更新毕业要求请求", required = true)
            @Valid @RequestBody UpdateGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateGraduationRequirement(request, roles, permissions);
        return Result.ok("毕业要求更新成功", null);
    }

    @PostMapping("/deleteGraduationRequirement")
    @Operation(summary = "删除毕业要求", description = "毕业要求下存在指标点或其他关联数据时拒绝删除")
    public Result<Void> deleteGraduationRequirement(
            @Parameter(description = "删除毕业要求请求", required = true)
            @Valid @RequestBody DeleteGraduationRequirementRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.deleteGraduationRequirement(request.getGrId(), roles, permissions);
        return Result.ok("毕业要求删除成功", null);
    }

    @PostMapping("/updateGraduationRequirementStatus")
    @Operation(summary = "更新毕业要求状态", description = "启用或停用毕业要求")
    public Result<Void> updateGraduationRequirementStatus(
            @Parameter(description = "毕业要求状态更新请求", required = true)
            @Valid @RequestBody UpdateGraduationRequirementStatusRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateGraduationRequirementStatus(request, roles, permissions);
        String msg = request.getStatus() == 1 ? "毕业要求已启用" : "毕业要求已停用";
        return Result.ok(msg, null);
    }

    @PostMapping("/listIndicatorPoints")
    @Operation(summary = "查询指标点列表", description = "支持按指标点编号、所属毕业要求、状态筛选")
    public Result<List<IndicatorPointResponse>> listIndicatorPoints(
            @Parameter(description = "指标点查询条件")
            @RequestBody(required = false) IndicatorPointQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listIndicatorPoints(request, roles, permissions));
    }

    @PostMapping("/addIndicatorPoint")
    @Operation(summary = "新增指标点", description = "创建指标点，默认状态为启用")
    public Result<Void> addIndicatorPoint(
            @Parameter(description = "新增指标点请求", required = true)
            @Valid @RequestBody AddIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.addIndicatorPoint(request, roles, permissions);
        return Result.ok("指标点创建成功", null);
    }

    @PostMapping("/updateIndicatorPoint")
    @Operation(summary = "更新指标点", description = "更新指标点编号、描述、所属毕业要求，可同步更新状态")
    public Result<Void> updateIndicatorPoint(
            @Parameter(description = "更新指标点请求", required = true)
            @Valid @RequestBody UpdateIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateIndicatorPoint(request, roles, permissions);
        return Result.ok("指标点更新成功", null);
    }

    @PostMapping("/deleteIndicatorPoint")
    @Operation(summary = "删除指标点", description = "删除指定指标点；若数据库存在关联约束则返回可读错误")
    public Result<Void> deleteIndicatorPoint(
            @Parameter(description = "删除指标点请求", required = true)
            @Valid @RequestBody DeleteIndicatorPointRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.deleteIndicatorPoint(request.getIpId(), roles, permissions);
        return Result.ok("指标点删除成功", null);
    }

    @PostMapping("/updateIndicatorPointStatus")
    @Operation(summary = "更新指标点状态", description = "启用或停用指标点")
    public Result<Void> updateIndicatorPointStatus(
            @Parameter(description = "指标点状态更新请求", required = true)
            @Valid @RequestBody UpdateIndicatorPointStatusRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        requirementService.updateIndicatorPointStatus(request, roles, permissions);
        String msg = request.getStatus() == 1 ? "指标点已启用" : "指标点已停用";
        return Result.ok(msg, null);
    }

    @PostMapping("/listMajors")
    @Operation(summary = "查询专业列表", description = "查询专业下拉数据，供毕业要求筛选和表单选择使用")
    public Result<List<Major>> listMajors(
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(requirementService.listMajors(roles, permissions));
    }
}
