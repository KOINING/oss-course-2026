package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.teachingclass.StudentClassImportResult;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.service.StudentClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "教学班管理", description = "教学班学生名单导入及管理接口")
public class TeachingClassController {

    private final StudentClassService studentClassService;

    @PostMapping("/importStudentClasses")
    @Operation(summary = "教学班学生名单导入", description = "上传Excel文件批量导入学生到教学班，返回导入结果概要（总条数、成功数、失败数及失败明细）")
    public Result<StudentClassImportResult> importStudentClasses(
            @Parameter(description = "学生名单Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {
        StudentClassImportResult result = studentClassService.importStudentClasses(file);
        return Result.ok(result);
    }

    @PostMapping("/listStudentsByTeachingClass")
    @Operation(summary = "查询教学班学生列表", description = "根据教学班ID查询该教学班下所有学生关联")
    public Result<List<StudentClass>> listStudentsByTeachingClass(
            @Parameter(description = "教学班ID", required = true)
            @RequestParam Long teachingClassId) {
        List<StudentClass> list = studentClassService.listByTeachingClassId(teachingClassId);
        return Result.ok(list);
    }

    @PostMapping("/listTeachingClassesByStudent")
    @Operation(summary = "查询学生所属教学班列表", description = "根据学生ID查询该学生所属的所有教学班关联")
    public Result<List<StudentClass>> listTeachingClassesByStudent(
            @Parameter(description = "学生ID", required = true)
            @RequestParam Long studentId) {
        List<StudentClass> list = studentClassService.listByStudentId(studentId);
        return Result.ok(list);
    }

    @PostMapping("/removeStudentFromClass")
    @Operation(summary = "从教学班移除学生", description = "删除指定的学生-教学班关联记录")
    public Result<Void> removeStudentFromClass(
            @Parameter(description = "关联记录ID", required = true)
            @RequestParam Long scId) {
        studentClassService.removeStudentFromClass(scId);
        return Result.ok("学生已从教学班移除", null);
    }
}
