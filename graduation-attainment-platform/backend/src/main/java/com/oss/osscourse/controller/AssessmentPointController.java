package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointCreateRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointQueryRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointResponse;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointUpdateRequest;
import com.oss.osscourse.service.AssessmentPointService;
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
@RequestMapping("/api/assessment-points")
@RequiredArgsConstructor
@Tag(name = "考核点管理", description = "考核点的列表、查询、新增、编辑和删除接口")
public class AssessmentPointController {

    private final AssessmentPointService apService;

    @GetMapping
    @Operation(summary = "查询考核点列表", description = "支持按考核点名称（模糊）、所属课程ID、所属课程目标ID筛选")
    public Result<List<AssessmentPointResponse>> list(
            @Parameter(description = "考核点名称，模糊查询")
            @RequestParam(required = false) String apName,
            @Parameter(description = "所属课程ID")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "所属课程目标ID")
            @RequestParam(required = false) Long coId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        AssessmentPointQueryRequest request = new AssessmentPointQueryRequest();
        request.setApName(apName);
        request.setCourseId(courseId);
        request.setCoId(coId);

        return Result.ok(apService.list(request, roles, permissions));
    }

    @GetMapping("/{apId}")
    @Operation(summary = "查询考核点详情", description = "根据考核点ID查询详情，含课程目标和课程信息")
    public Result<AssessmentPointResponse> getById(
            @Parameter(description = "考核点ID", required = true)
            @PathVariable Long apId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        return Result.ok(apService.getById(apId, roles, permissions));
    }

    @PostMapping
    @Operation(summary = "新增考核点", description = "创建新的考核点。校验：绑定目标有效、同课程下名称不重复、满分>0。")
    public Result<Void> create(
            @Parameter(description = "新增考核点请求", required = true)
            @Valid @RequestBody AssessmentPointCreateRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        apService.create(request, roles, permissions);
        return Result.ok("考核点创建成功", null);
    }

    @PutMapping("/{apId}")
    @Operation(summary = "更新考核点", description = "更新指定的考核点。校验：绑定目标有效、同课程下名称不重复、满分>0。")
    public Result<Void> update(
            @Parameter(description = "考核点ID", required = true)
            @PathVariable Long apId,
            @Parameter(description = "更新考核点请求", required = true)
            @Valid @RequestBody AssessmentPointUpdateRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        request.setApId(apId);
        apService.update(request, roles, permissions);
        return Result.ok("考核点更新成功", null);
    }

    @DeleteMapping("/{apId}")
    @Operation(summary = "删除考核点", description = "删除指定的考核点。若已被学生成绩引用则拒绝删除。")
    public Result<Void> delete(
            @Parameter(description = "考核点ID", required = true)
            @PathVariable Long apId,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {

        apService.delete(apId, roles, permissions);
        return Result.ok("考核点删除成功", null);
    }
}
