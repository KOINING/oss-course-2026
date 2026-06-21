package com.oss.osscourse.controller;

import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.college.CollegeQueryRequest;
import com.oss.osscourse.dto.college.CollegeResponse;
import com.oss.osscourse.dto.major.*;
import com.oss.osscourse.service.CollegeService;
import com.oss.osscourse.service.MajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "专业管理", description = "专业的增删改查接口")
public class MajorController {

    private final MajorService majorService;
    private final CollegeService collegeService;

    @PostMapping("/listMajors")
    @Operation(summary = "查询专业列表", description = "根据条件查询专业列表，支持按专业编码、专业名称、所属学院、状态筛选")
    public Result<List<MajorResponse>> listMajors(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) MajorQueryRequest request) {
        List<MajorResponse> list = majorService.listMajors(request);
        return Result.ok(list);
    }

    @PostMapping("/listMajorsByPage")
    @Operation(summary = "分页查询专业列表", description = "根据筛选条件分页查询专业列表，返回统一分页结构")
    public Result<PageResult<MajorResponse>> listMajorsByPage(
            @Parameter(description = "查询条件（含分页参数）")
            @RequestBody(required = false) MajorQueryRequest request) {
        return Result.ok(majorService.listMajorsByPage(request));
    }

    @PostMapping("/getMajor")
    @Operation(summary = "查询专业详情", description = "根据专业ID查询专业详情")
    public Result<MajorResponse> getMajor(
            @Parameter(description = "专业ID请求", required = true)
            @Valid @RequestBody MajorGetRequest request) {
        MajorResponse response = majorService.getMajorById(request.getMajorId());
        return Result.ok(response);
    }

    @PostMapping("/listMajorsForSelect")
    @Operation(summary = "查询专业下拉列表", description = "查询所有启用中的专业，供下拉选择使用")
    public Result<List<MajorResponse>> listMajorsForSelect() {
        List<MajorResponse> list = majorService.listMajorsForSelect();
        return Result.ok(list);
    }

    @PostMapping("/saveMajor")
    @Operation(summary = "新增或更新专业", description = "若未传 majorId 则新增，传了 majorId 则更新")
    public Result<Void> saveMajor(
            @Parameter(description = "专业保存请求", required = true)
            @Valid @RequestBody MajorSaveRequest request) {
        boolean isCreate = request.getMajorId() == null;
        majorService.saveMajor(request);
        return Result.ok(isCreate ? "专业创建成功" : "专业更新成功", null);
    }

    @PostMapping("/updateMajorStatus")
    @Operation(summary = "更新专业状态", description = "启用或停用专业")
    public Result<Void> updateMajorStatus(
            @Parameter(description = "专业状态更新请求", required = true)
            @Valid @RequestBody MajorStatusRequest request) {
        majorService.updateMajorStatus(request);
        String msg = request.getStatus() == 1 ? "专业已启用" : "专业已停用";
        return Result.ok(msg, null);
    }

    @PostMapping("/deleteMajor")
    @Operation(summary = "删除专业", description = "删除指定的专业记录")
    public Result<Void> deleteMajor(
            @Parameter(description = "专业删除请求", required = true)
            @Valid @RequestBody MajorDeleteRequest request) {
        majorService.deleteMajor(request.getMajorId());
        return Result.ok("专业已删除", null);
    }

    @PostMapping("/listColleges")
    @Operation(summary = "查询学院列表", description = "查询所有学院，供下拉选择使用")
    public Result<List<CollegeResponse>> listColleges() {
        List<CollegeResponse> list = collegeService.listColleges(new CollegeQueryRequest());
        return Result.ok(list);
    }

    @PostMapping("/listCollegesByPage")
    @Operation(summary = "分页查询学院列表", description = "根据筛选条件分页查询学院列表，返回统一分页结构")
    public Result<PageResult<CollegeResponse>> listCollegesByPage(
            @Parameter(description = "查询条件（含分页参数）")
            @RequestBody(required = false) CollegeQueryRequest request) {
        return Result.ok(collegeService.listCollegesByPage(request));
    }
}
