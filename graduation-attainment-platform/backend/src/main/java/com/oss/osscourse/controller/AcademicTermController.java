package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.academicterm.*;
import com.oss.osscourse.service.AcademicTermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-terms")
@RequiredArgsConstructor
@Tag(name = "学年学期管理", description = "学年学期的增删改查接口")
public class AcademicTermController {

    private final AcademicTermService academicTermService;

    @GetMapping
    @Operation(summary = "查询学年学期列表", description = "根据条件查询学年学期列表，支持按学期编码、学年、学期序号筛选")
    public Result<List<AcademicTermResponse>> listAcademicTerms(
            @Parameter(description = "学期编码，模糊查询") @RequestParam(required = false) String termCode,
            @Parameter(description = "学年") @RequestParam(required = false) Integer academicYear,
            @Parameter(description = "学期序号，1=第一学期，2=第二学期") @RequestParam(required = false) Integer semester) {

        AcademicTermQueryRequest request = new AcademicTermQueryRequest();
        request.setTermCode(termCode);
        request.setAcademicYear(academicYear);
        request.setSemester(semester);

        List<AcademicTermResponse> list = academicTermService.listAcademicTerms(request);
        return Result.ok(list);
    }

    @GetMapping("/{termId}")
    @Operation(summary = "查询学年学期详情", description = "根据学期ID查询学年学期详情")
    public Result<AcademicTermResponse> getAcademicTermById(
            @Parameter(description = "学期ID", required = true) @PathVariable Long termId) {

        AcademicTermResponse response = academicTermService.getAcademicTermById(termId);
        return Result.ok(response);
    }

    @PostMapping
    @Operation(summary = "新增学年学期", description = "创建新的学年学期记录")
    public Result<Void> createAcademicTerm(
            @Parameter(description = "学年学期新增请求", required = true)
            @Valid @RequestBody AcademicTermCreateRequest request) {

        academicTermService.createAcademicTerm(request);
        return Result.success();
    }

    @PutMapping("/{termId}")
    @Operation(summary = "更新学年学期", description = "更新指定的学年学期记录")
    public Result<Void> updateAcademicTerm(
            @Parameter(description = "学期ID", required = true) @PathVariable Long termId,
            @Parameter(description = "学年学期更新请求", required = true)
            @Valid @RequestBody AcademicTermUpdateRequest request) {

        request.setTermId(termId);
        academicTermService.updateAcademicTerm(request);
        return Result.success();
    }

    @DeleteMapping("/{termId}")
    @Operation(summary = "删除学年学期", description = "删除指定的学年学期记录")
    public Result<Void> deleteAcademicTerm(
            @Parameter(description = "学期ID", required = true) @PathVariable Long termId) {

        academicTermService.deleteAcademicTerm(termId);
        return Result.success();
    }
}
