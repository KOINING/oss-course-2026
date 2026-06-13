package com.oss.osscourse.controller;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.trace.CourseToObjectiveTraceRequest;
import com.oss.osscourse.dto.trace.CourseToObjectiveTraceResponse;
import com.oss.osscourse.dto.trace.MajorToCourseTraceRequest;
import com.oss.osscourse.dto.trace.MajorToCourseTraceResponse;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceRequest;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceResponse;
import com.oss.osscourse.service.AchievementTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/achievementTrace")
@RequiredArgsConstructor
@Tag(name = "达成度穿透追溯", description = "专业级Gk、课程级Ek、课程目标、考核点、原始成绩的逐层追溯与台账导出")
public class AchievementTraceController {

    private static final List<String> ALLOWED_ROLES = List.of("program_director", "academic_affairs");

    private final AchievementTraceService achievementTraceService;

    @PostMapping("/getMajorToCourseTrace")
    @Operation(summary = "专业级指标点Gk穿透到课程级Ek")
    public Result<List<MajorToCourseTraceResponse>> getMajorToCourseTrace(
            @Valid @RequestBody MajorToCourseTraceRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(achievementTraceService.getMajorToCourseTrace(request));
    }

    @PostMapping("/getCourseToObjectiveTrace")
    @Operation(summary = "课程级指标点Ek穿透到课程目标")
    public Result<CourseToObjectiveTraceResponse> getCourseToObjectiveTrace(
            @Valid @RequestBody CourseToObjectiveTraceRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(achievementTraceService.getCourseToObjectiveTrace(request));
    }

    @PostMapping("/getObjectiveToScoreTrace")
    @Operation(summary = "课程目标穿透到考核点和学生原始成绩")
    public Result<ObjectiveToScoreTraceResponse> getObjectiveToScoreTrace(
            @Valid @RequestBody ObjectiveToScoreTraceRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        return Result.ok(achievementTraceService.getObjectiveToScoreTrace(request));
    }

    @PostMapping("/exportAchievementLedger")
    @Operation(summary = "导出专业级到原始成绩逐层追溯台账")
    public ResponseEntity<byte[]> exportAchievementLedger(
            @Valid @RequestBody MajorToCourseTraceRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureAccess(roles);
        byte[] excelBytes = achievementTraceService.exportAchievementLedger(request);
        String fileName = "达成度逐层追溯台账_" + request.getMajorId() + "_" + request.getGradeYear() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(excelBytes.length);
        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    private void ensureAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(ALLOWED_ROLES::contains)) {
            throw new BusinessException(403, "当前账号无权访问达成度追溯接口");
        }
    }
}
