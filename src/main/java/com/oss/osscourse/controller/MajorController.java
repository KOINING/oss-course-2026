package com.oss.osscourse.controller;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.major.*;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.service.MajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专业管理", description = "专业的增删改查与启停接口")
@RestController
@RequestMapping("/api/admin")
public class MajorController {

    private final MajorService majorService;
    private final CollegeMapper collegeMapper;

    public MajorController(MajorService majorService, CollegeMapper collegeMapper) {
        this.majorService = majorService;
        this.collegeMapper = collegeMapper;
    }

    @Operation(summary = "查询专业列表", description = "支持按专业编码、专业名称、学院、状态筛选")
    @PostMapping("/listMajors")
    public Result<List<MajorVO>> listMajors(@RequestBody(required = false) MajorQueryRequest request,
                                            @RequestAttribute("roles") List<String> roles,
                                            @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(majorService.listMajors(request));
    }

    @Operation(summary = "查询专业详情")
    @PostMapping("/getMajor")
    public Result<MajorVO> getMajor(@Valid @RequestBody MajorIdRequest request,
                                    @RequestAttribute("roles") List<String> roles,
                                    @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(majorService.getMajor(request.getMajorId()));
    }

    @Operation(summary = "新增或更新专业", description = "不传majorId时为新增，传入时为更新")
    @PostMapping("/saveMajor")
    public Result<Void> saveMajor(@Valid @RequestBody MajorSaveRequest request,
                                  @RequestAttribute("roles") List<String> roles,
                                  @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        String action = request.getMajorId() == null ? "创建" : "更新";
        majorService.saveMajor(request);
        return Result.ok("专业" + action + "成功", null);
    }

    @Operation(summary = "启用/停用专业")
    @PostMapping("/updateMajorStatus")
    public Result<Void> updateMajorStatus(@Valid @RequestBody MajorStatusRequest request,
                                          @RequestAttribute("roles") List<String> roles,
                                          @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        majorService.updateMajorStatus(request);
        String action = request.getStatus() == 1 ? "启用" : "停用";
        return Result.ok("专业已" + action, null);
    }

    @Operation(summary = "删除专业", description = "物理删除专业，若存在关联数据则无法删除，建议先停用")
    @PostMapping("/deleteMajor")
    public Result<Void> deleteMajor(@Valid @RequestBody MajorIdRequest request,
                                    @RequestAttribute("roles") List<String> roles,
                                    @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        majorService.deleteMajor(request.getMajorId());
        return Result.ok("专业已删除", null);
    }

    @Operation(summary = "查询学院列表", description = "用于专业管理的学院下拉选择")
    @PostMapping("/listColleges")
    public Result<List<College>> listColleges(@RequestAttribute("roles") List<String> roles,
                                              @RequestAttribute("permissions") List<String> permissions) {
        assertManagePermission(roles, permissions);
        return Result.ok(collegeMapper.selectList(null));
    }

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && (roles.contains("admin") || roles.contains("academic_affairs"));
        boolean hasPermission = permissions != null && permissions.contains("major:manage");
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行专业管理操作");
        }
    }
}
