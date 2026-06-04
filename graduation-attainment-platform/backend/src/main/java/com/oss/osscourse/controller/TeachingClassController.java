package com.oss.osscourse.controller;

import com.oss.osscourse.common.AcademicAffairsAccessGuard;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.teachingclass.TeachingClassDeleteRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassGetRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassQueryRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassResponse;
import com.oss.osscourse.dto.teachingclass.TeachingClassSaveRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassStatusRequest;
import com.oss.osscourse.service.TeachingClassService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "教学班管理", description = "教学班增删改查接口")
public class TeachingClassController {

    private final TeachingClassService teachingClassService;
    private final AcademicAffairsAccessGuard accessGuard;

    @PostMapping("/listTeachingClasses")
    @Operation(summary = "查询教学班列表", description = "根据班级编号、班级名称、课程、学期、教师、计算状态查询教学班列表")
    public Result<List<TeachingClassResponse>> listTeachingClasses(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) TeachingClassQueryRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(teachingClassService.listTeachingClasses(request));
    }

    @PostMapping("/getTeachingClass")
    @Operation(summary = "查询教学班详情", description = "根据教学班 ID 查询详情")
    public Result<TeachingClassResponse> getTeachingClass(
            @Parameter(description = "教学班详情查询请求", required = true)
            @Valid @RequestBody TeachingClassGetRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(teachingClassService.getTeachingClassById(request.getClassId()));
    }

    @PostMapping("/listTeachingClassesForSelect")
    @Operation(summary = "查询教学班下拉列表", description = "返回可选教学班供其他业务链路使用")
    public Result<List<TeachingClassResponse>> listTeachingClassesForSelect(
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(teachingClassService.listTeachingClassesForSelect());
    }

    @PostMapping("/saveTeachingClass")
    @Operation(summary = "新增或更新教学班", description = "未传 classId 时新增，传 classId 时更新")
    public Result<Void> saveTeachingClass(
            @Parameter(description = "教学班保存请求", required = true)
            @Valid @RequestBody TeachingClassSaveRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        boolean isCreate = request.getClassId() == null;
        teachingClassService.saveTeachingClass(request);
        return Result.ok(isCreate ? "教学班创建成功" : "教学班更新成功", null);
    }

    @PostMapping("/updateTeachingClassStatus")
    @Operation(summary = "更新教学班计算状态", description = "更新教学班计算状态")
    public Result<Void> updateTeachingClassStatus(
            @Parameter(description = "教学班状态更新请求", required = true)
            @Valid @RequestBody TeachingClassStatusRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        teachingClassService.updateTeachingClassStatus(request);
        return Result.ok("教学班状态更新成功", null);
    }

    @PostMapping("/deleteTeachingClass")
    @Operation(summary = "删除教学班", description = "删除指定教学班记录")
    public Result<Void> deleteTeachingClass(
            @Parameter(description = "教学班删除请求", required = true)
            @Valid @RequestBody TeachingClassDeleteRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        teachingClassService.deleteTeachingClass(request.getClassId());
        return Result.ok("教学班已删除", null);
    }
}
