package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.course.*;
import com.oss.osscourse.service.CourseService;
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
@Tag(name = "课程管理", description = "课程的增删改查接口")
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/listCourses")
    @Operation(summary = "查询课程列表", description = "根据条件查询课程列表，支持按课程编码、课程名称、所属专业、状态筛选")
    public Result<List<CourseResponse>> listCourses(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) CourseQueryRequest request) {
        List<CourseResponse> list = courseService.listCourses(request);
        return Result.ok(list);
    }

    @PostMapping("/getCourse")
    @Operation(summary = "查询课程详情", description = "根据课程ID查询课程详情")
    public Result<CourseResponse> getCourse(
            @Parameter(description = "课程ID请求", required = true)
            @Valid @RequestBody CourseGetRequest request) {
        CourseResponse response = courseService.getCourseById(request.getCourseId());
        return Result.ok(response);
    }

    @PostMapping("/saveCourse")
    @Operation(summary = "新增或更新课程", description = "若未传 courseId 则新增，传了 courseId 则更新")
    public Result<Void> saveCourse(
            @Parameter(description = "课程保存请求", required = true)
            @Valid @RequestBody CourseSaveRequest request) {
        boolean isCreate = request.getCourseId() == null;
        courseService.saveCourse(request);
        return Result.ok(isCreate ? "课程创建成功" : "课程更新成功", null);
    }

    @PostMapping("/updateCourseStatus")
    @Operation(summary = "更新课程状态", description = "启用或停用课程")
    public Result<Void> updateCourseStatus(
            @Parameter(description = "课程状态更新请求", required = true)
            @Valid @RequestBody CourseStatusRequest request) {
        courseService.updateCourseStatus(request);
        String msg = request.getStatus() == 1 ? "课程已启用" : "课程已停用";
        return Result.ok(msg, null);
    }

    @PostMapping("/deleteCourse")
    @Operation(summary = "删除课程", description = "删除指定的课程记录，同时删除课程与专业的关联")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程删除请求", required = true)
            @Valid @RequestBody CourseDeleteRequest request) {
        courseService.deleteCourse(request.getCourseId());
        return Result.ok("课程已删除", null);
    }
}
