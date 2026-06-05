package com.oss.osscourse.controller;

import com.oss.osscourse.common.AcademicAffairsAccessGuard;
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
@Tag(name = "教学班学生关联管理", description = "教学班与学生关联接口")
public class StudentClassController {

    private final StudentClassService studentClassService;
    private final AcademicAffairsAccessGuard accessGuard;

    @PostMapping("/importStudentClasses")
    @Operation(summary = "导入教学班学生名单", description = "上传 Excel 批量导入学生与教学班关联")
    public Result<StudentClassImportResult> importStudentClasses(
            @Parameter(description = "教学班学生名单 Excel 文件", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentClassService.importStudentClasses(file));
    }

    @PostMapping("/listStudentsByTeachingClass")
    @Operation(summary = "按教学班查询学生列表", description = "根据教学班 ID 查询学生关联记录")
    public Result<List<StudentClass>> listStudentsByTeachingClass(
            @Parameter(description = "查询请求", required = true)
            @Valid @RequestBody StudentClassListRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentClassService.listByTeachingClassId(request.getTeachingClassId()));
    }

    @PostMapping("/listTeachingClassesByStudent")
    @Operation(summary = "按学生查询教学班列表", description = "根据学生 ID 查询教学班关联记录")
    public Result<List<StudentClass>> listTeachingClassesByStudent(
            @Parameter(description = "查询请求", required = true)
            @Valid @RequestBody StudentClassListRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        return Result.ok(studentClassService.listByStudentId(request.getStudentId()));
    }

    @PostMapping("/removeStudentFromClass")
    @Operation(summary = "移除学生与教学班关联", description = "根据关联 ID 删除学生与教学班关系")
    public Result<Void> removeStudentFromClass(
            @Parameter(description = "关联删除请求", required = true)
            @Valid @RequestBody StudentClassRemoveRequest request,
            @RequestAttribute(value = "roles", required = false) List<String> roles) {
        accessGuard.assertAcademicAffairs(roles);
        studentClassService.removeStudentFromClass(request.getScId());
        return Result.ok("学生已从教学班移除", null);
    }
}
