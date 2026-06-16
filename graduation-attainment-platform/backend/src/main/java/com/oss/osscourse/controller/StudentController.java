package com.oss.osscourse.controller;

import com.oss.osscourse.common.AcademicAffairsAccessGuard;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.student.StudentDeleteRequest;
import com.oss.osscourse.dto.student.StudentGetRequest;
import com.oss.osscourse.dto.student.StudentImportResult;
import com.oss.osscourse.dto.student.StudentQueryRequest;
import com.oss.osscourse.dto.student.StudentResponse;
import com.oss.osscourse.dto.student.StudentSaveRequest;
import com.oss.osscourse.dto.student.StudentStatusRequest;
import com.oss.osscourse.service.StudentService;
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
@Tag(name = "学生管理", description = "学生增删改查接口")
public class StudentController {

    private final StudentService studentService;
    private final AcademicAffairsAccessGuard accessGuard;

    @PostMapping("/listStudents")
    @Operation(summary = "查询学生列表", description = "根据学号、姓名、专业、入学年份、状态查询学生列表")
    public Result<List<StudentResponse>> listStudents(
            @Parameter(description = "查询条件")
            @RequestBody(required = false) StudentQueryRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentService.listStudents(request));
    }

    @PostMapping("/getStudent")
    @Operation(summary = "查询学生详情", description = "根据学生 ID 查询学生详情")
    public Result<StudentResponse> getStudent(
            @Parameter(description = "学生详情查询请求", required = true)
            @Valid @RequestBody StudentGetRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentService.getStudentById(request.getStudentId()));
    }

    @PostMapping("/listStudentsForSelect")
    @Operation(summary = "查询学生下拉列表", description = "返回所有在读学生供下拉选择")
    public Result<List<StudentResponse>> listStudentsForSelect(
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentService.listStudentsForSelect());
    }

    @PostMapping("/listStudentEnrollmentYears")
    @Operation(summary = "查询学生入学年份下拉列表", description = "返回学生主数据中已存在的入学年份")
    public Result<List<Integer>> listStudentEnrollmentYears(
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentService.listEnrollmentYears());
    }

    @PostMapping("/importStudents")
    @Operation(summary = "导入学生基础信息", description = "上传 Excel 批量导入学生主数据")
    public Result<StudentImportResult> importStudents(
            @Parameter(description = "学生基础信息 Excel 文件", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentService.importStudents(file));
    }

    @PostMapping("/saveStudent")
    @Operation(summary = "新增或更新学生", description = "未传 studentId 时新增，传 studentId 时更新")
    public Result<Void> saveStudent(
            @Parameter(description = "学生保存请求", required = true)
            @Valid @RequestBody StudentSaveRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        boolean isCreate = request.getStudentId() == null;
        studentService.saveStudent(request);
        return Result.ok(isCreate ? "学生创建成功" : "学生更新成功", null);
    }

    @PostMapping("/updateStudentStatus")
    @Operation(summary = "更新学生状态", description = "更新学生状态（在读、毕业、休学、退学）")
    public Result<Void> updateStudentStatus(
            @Parameter(description = "学生状态更新请求", required = true)
            @Valid @RequestBody StudentStatusRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        studentService.updateStudentStatus(request);
        return Result.ok("学生状态更新成功", null);
    }

    @PostMapping("/deleteStudent")
    @Operation(summary = "删除学生", description = "删除指定学生记录")
    public Result<Void> deleteStudent(
            @Parameter(description = "学生删除请求", required = true)
            @Valid @RequestBody StudentDeleteRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        studentService.deleteStudent(request.getStudentId());
        return Result.ok("学生已删除", null);
    }
}
