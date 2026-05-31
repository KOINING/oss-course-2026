package com.oss.osscourse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.mapper.TeacherMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "教师查询", description = "教学班表单所需的教师下拉数据")
public class TeacherController {

    private final TeacherMapper teacherMapper;

    @PostMapping("/listTeachersForSelect")
    @Operation(summary = "查询教师下拉列表", description = "返回启用中的教师，供教学班新增和编辑使用")
    public Result<List<Teacher>> listTeachersForSelect() {
        List<Teacher> list = teacherMapper.selectList(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getStatus, 1)
                .orderByAsc(Teacher::getTeacherNo));
        return Result.ok(list);
    }
}
