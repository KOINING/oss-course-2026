package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.teachingclass.StudentClassImportResult;
import com.oss.osscourse.dto.teachingclass.StudentClassListRequest;
import com.oss.osscourse.dto.teachingclass.StudentClassRemoveRequest;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.service.StudentClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "教学班-学生关联管理", description = "教学班与学生之间的关联关系管理接口")
public class StudentClassController {

    private final StudentClassService studentClassService;

    @PostMapping("/importStudentClasses")
    @Operation(summary = "教学班学生名单导入", description = "上传Excel文件批量导入学生到教学班，返回导入结果概要（总条数、成功数、失败数及失败明细）")
    public Result<StudentClassImportResult> importStudentClasses(
            @Parameter(description = "教学班学生名单Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {
        StudentClassImportResult result = studentClassService.importStudentClasses(file);
        return Result.ok(result);
    }

    @PostMapping("/listStudentsByTeachingClass")
    @Operation(summary = "按教学班查询学生列表", description = "根据教学班ID查询该教学班下的所有学生关联记录")
    public Result<List<StudentClass>> listStudentsByTeachingClass(
            @Parameter(description = "查询请求，传入teachingClassId", required = true)
            @Valid @RequestBody StudentClassListRequest request) {
        List<StudentClass> list = studentClassService.listByTeachingClassId(request.getTeachingClassId());
        return Result.ok(list);
    }

    @PostMapping("/listTeachingClassesByStudent")
    @Operation(summary = "按学生查询教学班列表", description = "根据学生ID查询该学生所在的所有教学班关联记录")
    public Result<List<StudentClass>> listTeachingClassesByStudent(
            @Parameter(description = "查询请求，传入studentId", required = true)
            @Valid @RequestBody StudentClassListRequest request) {
        List<StudentClass> list = studentClassService.listByStudentId(request.getStudentId());
        return Result.ok(list);
    }

    @PostMapping("/removeStudentFromClass")
    @Operation(summary = "移除学生-教学班关联", description = "根据关联ID移除学生与教学班的关联关系")
    public Result<Void> removeStudentFromClass(
            @Parameter(description = "关联移除请求", required = true)
            @Valid @RequestBody StudentClassRemoveRequest request) {
        studentClassService.removeStudentFromClass(request.getScId());
        return Result.ok("学生已从教学班移除", null);
    }
}
