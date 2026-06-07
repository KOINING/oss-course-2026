package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveCreateRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveQueryRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveResponse;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveUpdateRequest;
import com.oss.osscourse.service.CourseObjectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course-objectives")
@RequiredArgsConstructor
@Tag(name = "课程目标管理", description = "课程目标的列表、查询、新增、编辑和删除接口")
public class CourseObjectiveController {

    private final CourseObjectiveService courseObjectiveService;

    @GetMapping
    @Operation(summary = "查询课程目标列表", description = "支持按课程目标编号、课程ID或教学班ID筛选")
    public Result<List<CourseObjectiveResponse>> list(
            @Parameter(description = "课程目标编号，模糊查询")
            @RequestParam(required = false) String objectiveCode,
            @Parameter(description = "所属课程ID")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "教学班ID，允许前端携带上下文参数")
            @RequestParam(required = false) Long teachingClassId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        CourseObjectiveQueryRequest request = new CourseObjectiveQueryRequest();
        request.setObjectiveCode(objectiveCode);
        request.setCourseId(courseId);
        request.setTeachingClassId(teachingClassId);

        return Result.ok(courseObjectiveService.list(request, roles, permissions));
    }

    @GetMapping("/{coId}")
    @Operation(summary = "查询课程目标详情", description = "根据课程目标ID查询详情，返回纯文本描述和富文本描述")
    public Result<CourseObjectiveResponse> getById(
            @Parameter(description = "课程目标ID", required = true)
            @PathVariable Long coId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        return Result.ok(courseObjectiveService.getById(coId, roles, permissions));
    }

    @PostMapping
    @Operation(summary = "新增课程目标", description = "创建新的课程目标")
    public Result<Void> create(
            @Parameter(description = "新增课程目标请求", required = true)
            @Valid @RequestBody CourseObjectiveCreateRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        courseObjectiveService.create(request, roles, permissions);
        return Result.ok("课程目标创建成功", null);
    }

    @PutMapping("/{coId}")
    @Operation(summary = "更新课程目标", description = "更新指定的课程目标")
    public Result<Void> update(
            @Parameter(description = "课程目标ID", required = true)
            @PathVariable Long coId,
            @Parameter(description = "更新课程目标请求", required = true)
            @Valid @RequestBody CourseObjectiveUpdateRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        request.setCoId(coId);
        courseObjectiveService.update(request, roles, permissions);
        return Result.ok("课程目标更新成功", null);
    }

    @DeleteMapping("/{coId}")
    @Operation(summary = "删除课程目标", description = "删除指定的课程目标")
    public Result<Void> delete(
            @Parameter(description = "课程目标ID", required = true)
            @PathVariable Long coId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        courseObjectiveService.delete(coId, roles, permissions);
        return Result.ok("课程目标删除成功", null);
    }
}
