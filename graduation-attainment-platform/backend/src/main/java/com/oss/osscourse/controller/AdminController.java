package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.admin.*;
import com.oss.osscourse.service.AdminUserManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @PostMapping("/listUsers")
    public Result<List<AdminManagedUserResponse>> listUsers(
            @RequestBody(required = false) AdminUserQueryRequest request,
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(adminUserManagementService.listUsers(request, roles, permissions));
    }

    @PostMapping("/listAssignableRoles")
    public Result<List<AdminAssignableRoleResponse>> listAssignableRoles(
            @RequestAttribute("roles") List<String> roles,
            @RequestAttribute("permissions") List<String> permissions) {
        return Result.ok(adminUserManagementService.listAssignableRoles(roles, permissions));
    }

    @PostMapping("/addUser")
    public Result<Void> addUser(@Valid @RequestBody AdminAddUserRequest request,
                                @RequestAttribute("roles") List<String> roles,
                                @RequestAttribute("permissions") List<String> permissions) {
        adminUserManagementService.addUser(request, roles, permissions);
        return Result.ok("账号创建成功", null);
    }

    @PostMapping("/updateUser")
    public Result<Void> updateUser(@Valid @RequestBody AdminUpdateUserRequest request,
                                   @RequestAttribute("roles") List<String> roles,
                                   @RequestAttribute("permissions") List<String> permissions) {
        adminUserManagementService.updateUser(request, roles, permissions);
        return Result.ok("账号更新成功", null);
    }

    @PostMapping("/updateUserStatus")
    public Result<Void> updateUserStatus(@Valid @RequestBody AdminUpdateUserStatusRequest request,
                                         @RequestAttribute("roles") List<String> roles,
                                         @RequestAttribute("permissions") List<String> permissions) {
        adminUserManagementService.updateUserStatus(request, roles, permissions);
        return Result.ok("账号状态更新成功", null);
    }
}
