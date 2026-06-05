package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.teachercontext.ScoreImportContextResponse;
import com.oss.osscourse.dto.teachercontext.TeacherClassRequest;
import com.oss.osscourse.dto.teachercontext.TeacherClassStudentResponse;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassQueryRequest;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassResponse;
import com.oss.osscourse.service.TeacherContextService;
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
@RequestMapping("/api/teacherContext")
@RequiredArgsConstructor
@Tag(name = "教师端上下文", description = "课程主讲教师查询本人课程、教学班、学生名单与成绩录入上下文接口")
public class TeacherContextController {
    private final TeacherContextService teacherContextService;

    @PostMapping("/listMyTeachingClasses")
    @Operation(summary = "查询当前教师课程与教学班", description = "根据当前登录用户映射教师身份，只返回该教师负责的课程和教学班")
    public Result<List<TeacherTeachingClassResponse>> listMyTeachingClasses(
            @Parameter(description = "查询筛选条件")
            @RequestBody(required = false) TeacherTeachingClassQueryRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        return Result.ok(teacherContextService.listMyTeachingClasses(request, userId, roles));
    }

    @PostMapping("/listMyClassStudents")
    @Operation(summary = "查询当前教师教学班学生名单", description = "仅允许课程主讲教师查询自己负责的教学班学生名单")
    public Result<List<TeacherClassStudentResponse>> listMyClassStudents(
            @Parameter(description = "教学班请求", required = true)
            @Valid @RequestBody TeacherClassRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        return Result.ok(teacherContextService.listMyClassStudents(request, userId, roles));
    }

    @PostMapping("/getScoreImportContext")
    @Operation(summary = "查询成绩录入上下文", description = "返回成绩模板生成与成绩导入所需的课程、教学班、学生、目标、考核点、状态与权限判断结果")
    public Result<ScoreImportContextResponse> getScoreImportContext(
            @Parameter(description = "教学班请求", required = true)
            @Valid @RequestBody TeacherClassRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        return Result.ok(teacherContextService.getScoreImportContext(request, userId, roles));
    }
}
