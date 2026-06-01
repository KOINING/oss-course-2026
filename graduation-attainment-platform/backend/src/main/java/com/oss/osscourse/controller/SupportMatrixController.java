package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.supportmatrix.AddCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportListRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportResponse;
import com.oss.osscourse.dto.supportmatrix.DeleteCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.MatrixAcademicTermResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixCourseOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixGraduationRequirementResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixIndicatorPointResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixMajorOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.dto.supportmatrix.ResetSupportMatrixRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixGetRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixMajorFilterRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixSaveRequest;
import com.oss.osscourse.dto.supportmatrix.UpdateCourseIndicatorSupportRequest;
import com.oss.osscourse.service.SupportMatrixService;
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
@RequestMapping("/api/supportMatrix")
@RequiredArgsConstructor
@Tag(name = "支撑矩阵配置", description = "课程-毕业要求指标点支撑关系的查询、保存和校验接口")
public class SupportMatrixController {
    private final SupportMatrixService supportMatrixService;

    @PostMapping("/listMajors")
    @Operation(summary = "查询专业列表", description = "供支撑矩阵筛选使用")
    public Result<List<MatrixMajorOptionResponse>> listMajors(
            @Parameter(description = "可选筛选条件")
            @RequestBody(required = false) SupportMatrixMajorFilterRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listMajors(request, roles, permissions));
    }

    @PostMapping("/listCourses")
    @Operation(summary = "按专业查询课程列表", description = "查询指定专业下可配置支撑关系的课程")
    public Result<List<MatrixCourseOptionResponse>> listCourses(
            @Parameter(description = "按专业筛选请求", required = true)
            @Valid @RequestBody SupportMatrixMajorFilterRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listCourses(request, roles, permissions));
    }

    @PostMapping("/listAcademicTerms")
    @Operation(summary = "查询学年学期列表", description = "供支撑矩阵筛选使用")
    public Result<List<MatrixAcademicTermResponse>> listAcademicTerms(
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listAcademicTerms(roles, permissions));
    }

    @PostMapping("/listGraduationRequirements")
    @Operation(summary = "按专业查询毕业要求", description = "查询指定专业下可用毕业要求")
    public Result<List<MatrixGraduationRequirementResponse>> listGraduationRequirements(
            @Parameter(description = "按专业筛选请求", required = true)
            @Valid @RequestBody SupportMatrixMajorFilterRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listGraduationRequirements(request, roles, permissions));
    }

    @PostMapping("/listIndicatorPoints")
    @Operation(summary = "按专业查询指标点", description = "查询指定专业下可用指标点")
    public Result<List<MatrixIndicatorPointResponse>> listIndicatorPoints(
            @Parameter(description = "按专业筛选请求", required = true)
            @Valid @RequestBody SupportMatrixMajorFilterRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listIndicatorPoints(request, roles, permissions));
    }

    @PostMapping("/getSupportMatrix")
    @Operation(summary = "查询支撑矩阵关系", description = "按专业查询课程-指标点支撑关系")
    public Result<List<MatrixRelationResponse>> getSupportMatrix(
            @Parameter(description = "查询支撑矩阵请求", required = true)
            @Valid @RequestBody SupportMatrixGetRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.getSupportMatrix(request, roles, permissions));
    }

    @PostMapping("/listCourseIndicatorSupports")
    @Operation(summary = "查询课程-指标点支撑关系", description = "支持按专业、课程、指标点筛选")
    public Result<List<CourseIndicatorSupportResponse>> listCourseIndicatorSupports(
            @Parameter(description = "查询课程-指标点支撑关系请求")
            @RequestBody(required = false) CourseIndicatorSupportListRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(supportMatrixService.listCourseIndicatorSupports(request, roles, permissions));
    }

    @PostMapping("/addCourseIndicatorSupport")
    @Operation(summary = "新增课程-指标点支撑关系", description = "单条新增 courseId + ipId + totalWeight")
    public Result<Void> addCourseIndicatorSupport(
            @Parameter(description = "新增支撑关系请求", required = true)
            @Valid @RequestBody AddCourseIndicatorSupportRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        supportMatrixService.addCourseIndicatorSupport(request, roles, permissions);
        return Result.ok("支撑关系创建成功", null);
    }

    @PostMapping("/updateCourseIndicatorSupport")
    @Operation(summary = "更新课程-指标点支撑关系", description = "单条更新 courseId + ipId + totalWeight")
    public Result<Void> updateCourseIndicatorSupport(
            @Parameter(description = "更新支撑关系请求", required = true)
            @Valid @RequestBody UpdateCourseIndicatorSupportRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        supportMatrixService.updateCourseIndicatorSupport(request, roles, permissions);
        return Result.ok("支撑关系更新成功", null);
    }

    @PostMapping("/deleteCourseIndicatorSupport")
    @Operation(summary = "删除课程-指标点支撑关系", description = "按关系ID删除单条支撑关系")
    public Result<Void> deleteCourseIndicatorSupport(
            @Parameter(description = "删除支撑关系请求", required = true)
            @Valid @RequestBody DeleteCourseIndicatorSupportRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        supportMatrixService.deleteCourseIndicatorSupport(request, roles, permissions);
        return Result.ok("支撑关系删除成功", null);
    }

    @PostMapping("/saveSupportMatrix")
    @Operation(summary = "批量保存支撑矩阵", description = "按专业批量提交课程-指标点支撑关系并执行权重校验")
    public Result<Void> saveSupportMatrix(
            @Parameter(description = "批量保存支撑矩阵请求", required = true)
            @Valid @RequestBody SupportMatrixSaveRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        supportMatrixService.saveSupportMatrix(request, roles, permissions);
        return Result.ok("支撑矩阵保存成功", null);
    }

    @PostMapping("/resetSupportMatrix")
    @Operation(summary = "重置支撑矩阵", description = "清空指定专业下的课程-指标点支撑关系")
    public Result<Void> resetSupportMatrix(
            @Parameter(description = "重置支撑矩阵请求", required = true)
            @Valid @RequestBody ResetSupportMatrixRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        supportMatrixService.resetSupportMatrix(request, roles, permissions);
        return Result.ok("支撑矩阵已重置", null);
    }
}
