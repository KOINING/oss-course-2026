package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.teachingclass.*;
import com.oss.osscourse.service.TeachingClassService;
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
@Tag(name = "教学班管理", description = "教学班的增删改查接口")
public class TeachingClassController {

    private final TeachingClassService teachingClassService;

    @PostMapping("/listTeachingClasses")
    @Operation(summary = "查询教学班列表", description = "根据条件查询教学班列表，支持按班级名称、课程、学期、教师、计算状态筛选")
    public Result<List<TeachingClassResponse>> listTeachingClasses(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) TeachingClassQueryRequest request) {
        List<TeachingClassResponse> list = teachingClassService.listTeachingClasses(request);
        return Result.ok(list);
    }

    @PostMapping("/getTeachingClass")
    @Operation(summary = "查询教学班详情", description = "根据教学班ID查询教学班详情")
    public Result<TeachingClassResponse> getTeachingClass(
            @Parameter(description = "教学班详情查询请求", required = true)
            @Valid @RequestBody TeachingClassGetRequest request) {
        TeachingClassResponse response = teachingClassService.getTeachingClassById(request.getClassId());
        return Result.ok(response);
    }

    @PostMapping("/listTeachingClassesForSelect")
    @Operation(summary = "查询教学班下拉列表", description = "查询所有未锁定的教学班，供下拉选择使用")
    public Result<List<TeachingClassResponse>> listTeachingClassesForSelect() {
        List<TeachingClassResponse> list = teachingClassService.listTeachingClassesForSelect();
        return Result.ok(list);
    }

    @PostMapping("/saveTeachingClass")
    @Operation(summary = "新增或更新教学班", description = "若未传 classId 则新增，传了 classId 则更新")
    public Result<Void> saveTeachingClass(
            @Parameter(description = "教学班保存请求", required = true)
            @Valid @RequestBody TeachingClassSaveRequest request) {
        boolean isCreate = request.getClassId() == null;
        teachingClassService.saveTeachingClass(request);
        return Result.ok(isCreate ? "教学班创建成功" : "教学班更新成功", null);
    }

    @PostMapping("/updateTeachingClassStatus")
    @Operation(summary = "更新教学班计算状态", description = "更新教学班的计算状态")
    public Result<Void> updateTeachingClassStatus(
            @Parameter(description = "教学班状态更新请求", required = true)
            @Valid @RequestBody TeachingClassStatusRequest request) {
        teachingClassService.updateTeachingClassStatus(request);
        return Result.ok("教学班状态更新成功", null);
    }

    @PostMapping("/deleteTeachingClass")
    @Operation(summary = "删除教学班", description = "删除指定的教学班记录")
    public Result<Void> deleteTeachingClass(
            @Parameter(description = "教学班删除请求", required = true)
            @Valid @RequestBody TeachingClassDeleteRequest request) {
        teachingClassService.deleteTeachingClass(request.getClassId());
        return Result.ok("教学班已删除", null);
    }
}
