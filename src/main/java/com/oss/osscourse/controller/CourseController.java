package com.oss.osscourse.controller;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.course.*;
import com.oss.osscourse.dto.major.MajorVO;
import com.oss.osscourse.service.CourseService;
import com.oss.osscourse.service.MajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理", description = "课程的增删改查与启停接口")
@RestController
@RequestMapping("/api/admin")
public class CourseController {

    private final CourseService courseService;
    private final MajorService majorService;

    public CourseController(CourseService courseService, MajorService majorService) {
        this.courseService = courseService;
        this.majorService = majorService;
    }

    @Operation(summary = "查询课程列表", description = "支持按课程编码、课程名称、专业、状态筛选")
    @PostMapping("/listCourses")
    public Result<List<CourseVO>> listCourses(@RequestBody(required = false) CourseQueryRequest request,
                                              @RequestAttribute("roles") List<String> roles,
                                              @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(courseService.listCourses(request));
    }

    @Operation(summary = "查询课程详情")
    @PostMapping("/getCourse")
    public Result<CourseVO> getCourse(@Valid @RequestBody CourseIdRequest request,
                                      @RequestAttribute("roles") List<String> roles,
                                      @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(courseService.getCourse(request.getCourseId()));
    }

    @Operation(summary = "新增或更新课程", description = "不传courseId时为新增，传入时为更新")
    @PostMapping("/saveCourse")
    public Result<Void> saveCourse(@Valid @RequestBody CourseSaveRequest request,
                                   @RequestAttribute("roles") List<String> roles,
                                   @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        String action = request.getCourseId() == null ? "创建" : "更新";
        courseService.saveCourse(request);
        return Result.ok("课程" + action + "成功", null);
    }

    @Operation(summary = "启用/停用课程")
    @PostMapping("/updateCourseStatus")
    public Result<Void> updateCourseStatus(@Valid @RequestBody CourseStatusRequest request,
                                           @RequestAttribute("roles") List<String> roles,
                                           @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        courseService.updateCourseStatus(request);
        String action = request.getStatus() == 1 ? "启用" : "停用";
        return Result.ok("课程已" + action, null);
    }

    @Operation(summary = "删除课程", description = "物理删除课程，若存在关联数据则无法删除，建议先停用")
    @PostMapping("/deleteCourse")
    public Result<Void> deleteCourse(@Valid @RequestBody CourseIdRequest request,
                                     @RequestAttribute("roles") List<String> roles,
                                     @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        courseService.deleteCourse(request.getCourseId());
        return Result.ok("课程已删除", null);
    }

    @Operation(summary = "查询专业列表（供课程管理使用）", description = "返回启用的专业列表，用于课程管理的专业下拉选择")
    @PostMapping("/listMajorsForSelect")
    public Result<List<MajorVO>> listMajorsForSelect(@RequestAttribute("roles") List<String> roles,
                                                     @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(majorService.listMajors(null));
    }

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && (roles.contains("admin") || roles.contains("academic_affairs"));
        boolean hasPermission = permissions != null && permissions.contains("course:import");
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行课程管理操作");
        }
    }
}
