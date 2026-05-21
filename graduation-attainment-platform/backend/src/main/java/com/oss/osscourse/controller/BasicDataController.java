package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.basic.*;
import com.oss.osscourse.service.BasicDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "基础数据", description = "学年学期、学院等基础主数据维护")
@RestController
@RequestMapping("/api/basic")
public class BasicDataController {

    private final BasicDataService basicDataService;

    public BasicDataController(BasicDataService basicDataService) {
        this.basicDataService = basicDataService;
    }

    @Operation(summary = "学院列表查询")
    @PostMapping("/college/list")
    public Result<List<CollegeResponse>> listColleges(
            @RequestBody(required = false) CollegeQueryRequest request,
            @RequestAttribute("roles") List<String> roles) {
        return Result.ok(basicDataService.listColleges(request, roles));
    }

    @Operation(summary = "新增学院")
    @PostMapping("/college/add")
    public Result<Void> addCollege(@Valid @RequestBody CollegeSaveRequest request,
                                   @RequestAttribute("roles") List<String> roles) {
        basicDataService.addCollege(request, roles);
        return Result.ok("学院创建成功", null);
    }

    @Operation(summary = "更新学院")
    @PostMapping("/college/update")
    public Result<Void> updateCollege(@Valid @RequestBody CollegeSaveRequest request,
                                     @RequestAttribute("roles") List<String> roles) {
        basicDataService.updateCollege(request, roles);
        return Result.ok("学院更新成功", null);
    }

    @Operation(summary = "删除学院")
    @PostMapping("/college/delete")
    public Result<Void> deleteCollege(@Valid @RequestBody CollegeDeleteRequest request,
                                     @RequestAttribute("roles") List<String> roles) {
        basicDataService.deleteCollege(request, roles);
        return Result.ok("学院删除成功", null);
    }

    @Operation(summary = "学年学期列表查询")
    @PostMapping("/academicTerm/list")
    public Result<List<AcademicTermResponse>> listAcademicTerms(
            @RequestBody(required = false) AcademicTermQueryRequest request,
            @RequestAttribute("roles") List<String> roles) {
        return Result.ok(basicDataService.listAcademicTerms(request, roles));
    }

    @Operation(summary = "新增学年学期")
    @PostMapping("/academicTerm/add")
    public Result<Void> addAcademicTerm(@Valid @RequestBody AcademicTermSaveRequest request,
                                        @RequestAttribute("roles") List<String> roles) {
        basicDataService.addAcademicTerm(request, roles);
        return Result.ok("学年学期创建成功", null);
    }

    @Operation(summary = "更新学年学期")
    @PostMapping("/academicTerm/update")
    public Result<Void> updateAcademicTerm(@Valid @RequestBody AcademicTermSaveRequest request,
                                          @RequestAttribute("roles") List<String> roles) {
        basicDataService.updateAcademicTerm(request, roles);
        return Result.ok("学年学期更新成功", null);
    }

    @Operation(summary = "删除学年学期")
    @PostMapping("/academicTerm/delete")
    public Result<Void> deleteAcademicTerm(@Valid @RequestBody AcademicTermDeleteRequest request,
                                           @RequestAttribute("roles") List<String> roles) {
        basicDataService.deleteAcademicTerm(request, roles);
        return Result.ok("学年学期删除成功", null);
    }
}
