package com.oss.osscourse.controller;

import com.oss.osscourse.common.AcademicAffairsAccessGuard;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.course.CourseDeleteRequest;
import com.oss.osscourse.dto.course.CourseGetRequest;
import com.oss.osscourse.dto.course.CourseImportResult;
import com.oss.osscourse.dto.course.CourseQueryRequest;
import com.oss.osscourse.dto.course.CourseResponse;
import com.oss.osscourse.dto.course.CourseSaveRequest;
import com.oss.osscourse.dto.course.CourseStatusRequest;
import com.oss.osscourse.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "课程管理", description = "课程增删改查接口")
public class CourseController {

    private final CourseService courseService;
    private final AcademicAffairsAccessGuard accessGuard;

    @PostMapping("/listCourses")
    @Operation(summary = "查询课程列表", description = "根据课程编码、课程名称、所属专业、状态查询课程列表")
    public Result<List<CourseResponse>> listCourses(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) CourseQueryRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(courseService.listCourses(request));
    }

    @PostMapping("/getCourse")
    @Operation(summary = "查询课程详情", description = "根据课程 ID 查询课程详情")
    public Result<CourseResponse> getCourse(
            @Parameter(description = "课程详情查询请求", required = true)
            @Valid @RequestBody CourseGetRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(courseService.getCourseById(request.getCourseId()));
    }

    @PostMapping("/saveCourse")
    @Operation(summary = "新增或更新课程", description = "未传 courseId 时新增，传 courseId 时更新")
    public Result<Void> saveCourse(
            @Parameter(description = "课程保存请求", required = true)
            @Valid @RequestBody CourseSaveRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        boolean isCreate = request.getCourseId() == null;
        courseService.saveCourse(request);
        return Result.ok(isCreate ? "课程创建成功" : "课程更新成功", null);
    }

    @PostMapping("/updateCourseStatus")
    @Operation(summary = "更新课程状态", description = "启用或停用课程")
    public Result<Void> updateCourseStatus(
            @Parameter(description = "课程状态更新请求", required = true)
            @Valid @RequestBody CourseStatusRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        courseService.updateCourseStatus(request);
        return Result.ok(request.getStatus() == 1 ? "课程已启用" : "课程已停用", null);
    }

    @PostMapping("/deleteCourse")
    @Operation(summary = "删除课程", description = "删除指定课程及其专业关联")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程删除请求", required = true)
            @Valid @RequestBody CourseDeleteRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        courseService.deleteCourse(request.getCourseId());
        return Result.ok("课程已删除", null);
    }

    @PostMapping("/importCourses")
    @Operation(summary = "导入课程清单", description = "上传 Excel 批量导入课程数据")
    public Result<CourseImportResult> importCourses(
            @Parameter(description = "课程 Excel 文件", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(courseService.importCourses(file));
    }
}
