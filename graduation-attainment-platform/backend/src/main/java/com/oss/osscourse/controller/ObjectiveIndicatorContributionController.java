package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionBatchSaveRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionQueryRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionResponse;
import com.oss.osscourse.service.ObjectiveIndicatorContributionService;
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
@RequestMapping("/api/objective-indicator-contributions")
@RequiredArgsConstructor
@Tag(name = "课程目标-指标点内部权重管理", description = "按课程查询和批量保存课程目标对毕业要求指标点的内部贡献权重(w)")
public class ObjectiveIndicatorContributionController {

    private final ObjectiveIndicatorContributionService oicService;

    @PostMapping("/query")
    @Operation(summary = "查询内部权重配置", description = "按课程ID和培养方案年级查询该课程下所有课程目标到指标点的内部权重配置")
    public Result<List<ObjectiveIndicatorContributionResponse>> query(
            @Parameter(description = "查询请求（courseId + gradeYear）", required = true)
            @Valid @RequestBody ObjectiveIndicatorContributionQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(oicService.query(request, roles, permissions));
    }

    @PostMapping("/batch-save")
    @Operation(summary = "批量保存内部权重", description = "批量保存课程目标到指标点的内部权重配置。保存前校验：coId属于当前课程、ipId合法、权重>0、同指标点权重和=1.0、不重复配置。采用先删后插策略。")
    public Result<Void> batchSave(
            @Parameter(description = "批量保存请求", required = true)
            @Valid @RequestBody ObjectiveIndicatorContributionBatchSaveRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        oicService.batchSave(request, roles, permissions);
        return Result.ok("内部权重批量保存成功", null);
    }
}
