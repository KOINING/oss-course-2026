package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.college.*;
import com.oss.osscourse.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
@RequiredArgsConstructor
@Tag(name = "学院管理", description = "学院的增删改查接口")
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping
    @Operation(summary = "查询学院列表", description = "根据条件查询学院列表，支持按学院编码、学院名称筛选")
    public Result<List<CollegeResponse>> listColleges(
            @Parameter(description = "学院编码，模糊查询") @RequestParam(required = false) String collegeCode,
            @Parameter(description = "学院名称，模糊查询") @RequestParam(required = false) String collegeName) {

        CollegeQueryRequest request = new CollegeQueryRequest();
        request.setCollegeCode(collegeCode);
        request.setCollegeName(collegeName);

        List<CollegeResponse> list = collegeService.listColleges(request);
        return Result.ok(list);
    }

    @GetMapping("/{collegeId}")
    @Operation(summary = "查询学院详情", description = "根据学院ID查询学院详情")
    public Result<CollegeResponse> getCollegeById(
            @Parameter(description = "学院ID", required = true) @PathVariable Long collegeId) {

        CollegeResponse response = collegeService.getCollegeById(collegeId);
        return Result.ok(response);
    }

    @PostMapping
    @Operation(summary = "新增学院", description = "创建新的学院记录")
    public Result<Void> createCollege(
            @Parameter(description = "学院新增请求", required = true)
            @Valid @RequestBody CollegeCreateRequest request) {

        collegeService.createCollege(request);
        return Result.success();
    }

    @PutMapping("/{collegeId}")
    @Operation(summary = "更新学院", description = "更新指定的学院记录")
    public Result<Void> updateCollege(
            @Parameter(description = "学院ID", required = true) @PathVariable Long collegeId,
            @Parameter(description = "学院更新请求", required = true)
            @Valid @RequestBody CollegeUpdateRequest request) {

        request.setCollegeId(collegeId);
        collegeService.updateCollege(request);
        return Result.success();
    }

    @DeleteMapping("/{collegeId}")
    @Operation(summary = "删除学院", description = "删除指定的学院记录")
    public Result<Void> deleteCollege(
            @Parameter(description = "学院ID", required = true) @PathVariable Long collegeId) {

        collegeService.deleteCollege(collegeId);
        return Result.success();
    }
}
