package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.student.*;
import com.oss.osscourse.service.StudentService;
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
@Tag(name = "学生管理", description = "学生的增删改查接口")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/listStudents")
    @Operation(summary = "查询学生列表", description = "根据条件查询学生列表，支持按学号、姓名、专业、入学年份、状态筛选")
    public Result<List<StudentResponse>> listStudents(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) StudentQueryRequest request) {
        List<StudentResponse> list = studentService.listStudents(request);
        return Result.ok(list);
    }

    @PostMapping("/getStudent")
    @Operation(summary = "查询学生详情", description = "根据学生ID查询学生详情")
    public Result<StudentResponse> getStudent(
            @Parameter(description = "学生详情查询请求", required = true)
            @Valid @RequestBody StudentGetRequest request) {
        StudentResponse response = studentService.getStudentById(request.getStudentId());
        return Result.ok(response);
    }

    @PostMapping("/listStudentsForSelect")
    @Operation(summary = "查询学生下拉列表", description = "查询所有在读学生，供下拉选择使用")
    public Result<List<StudentResponse>> listStudentsForSelect() {
        List<StudentResponse> list = studentService.listStudentsForSelect();
        return Result.ok(list);
    }

    @PostMapping("/saveStudent")
    @Operation(summary = "新增或更新学生", description = "若未传 studentId 则新增，传了 studentId 则更新")
    public Result<Void> saveStudent(
            @Parameter(description = "学生保存请求", required = true)
            @Valid @RequestBody StudentSaveRequest request) {
        boolean isCreate = request.getStudentId() == null;
        studentService.saveStudent(request);
        return Result.ok(isCreate ? "学生创建成功" : "学生更新成功", null);
    }

    @PostMapping("/updateStudentStatus")
    @Operation(summary = "更新学生状态", description = "更新学生的状态（在读/毕业/休学/退学）")
    public Result<Void> updateStudentStatus(
            @Parameter(description = "学生状态更新请求", required = true)
            @Valid @RequestBody StudentStatusRequest request) {
        studentService.updateStudentStatus(request);
        return Result.ok("学生状态更新成功", null);
    }

    @PostMapping("/deleteStudent")
    @Operation(summary = "删除学生", description = "删除指定的学生记录")
    public Result<Void> deleteStudent(
            @Parameter(description = "学生删除请求", required = true)
            @Valid @RequestBody StudentDeleteRequest request) {
        studentService.deleteStudent(request.getStudentId());
        return Result.ok("学生已删除", null);
    }
}
