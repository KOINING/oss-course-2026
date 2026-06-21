package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.PageQueryUtils;
import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.admin.*;
import com.oss.osscourse.entity.SysRole;
import com.oss.osscourse.entity.SysUser;
import com.oss.osscourse.entity.SysUserRole;
import com.oss.osscourse.mapper.SysRoleMapper;
import com.oss.osscourse.mapper.SysUserMapper;
import com.oss.osscourse.mapper.SysUserRoleMapper;
import com.oss.osscourse.service.AdminUserManagementService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {
    private static final String DEFAULT_RESET_PASSWORD = "123456";

    private static final Set<String> ASSIGNABLE_ROLE_CODES = Set.of(
            "academic_affairs",
            "program_director",
            "instructor"
    );

    private static final Set<String> MANAGE_PERMISSIONS = Set.of("user:manage", "role:assign");

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserManagementServiceImpl(SysUserMapper sysUserMapper,
                                          SysRoleMapper sysRoleMapper,
                                          SysUserRoleMapper sysUserRoleMapper,
                                          BCryptPasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<AdminManagedUserResponse> listUsersByPage(AdminUserQueryRequest request,
                                                                 List<String> currentRoles,
                                                                 List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);

        AdminUserQueryRequest query = request == null ? new AdminUserQueryRequest() : request;
        int pageNum = PageQueryUtils.normalizePageNum(query.getPageNum());
        int pageSize = PageQueryUtils.normalizePageSize(query.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.exists("SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.id = ur.role_id"
                + " WHERE ur.user_id = sys_user.id"
                + " AND r.role_code IN ('academic_affairs','program_director','instructor')");
        wrapper.notExists("SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.id = ur.role_id"
                + " WHERE ur.user_id = sys_user.id AND r.role_code = 'admin'");

        String username = trimToNull(query.getUsername());
        if (username != null) {
            wrapper.like(SysUser::getUsername, username);
        }
        String realName = trimToNull(query.getRealName());
        if (realName != null) {
            wrapper.like(SysUser::getRealName, realName);
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysUser::getId);

        long total = sysUserMapper.selectCount(wrapper);

        int offset = PageQueryUtils.offset(pageNum, pageSize);
        wrapper.last("LIMIT " + offset + "," + pageSize);

        List<SysUser> users = sysUserMapper.selectList(wrapper);
        int actualPageSize = pageSize;
        if (users.isEmpty()) {
            return PageResult.of(List.of(), total, pageNum, actualPageSize);
        }

        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<AdminUserRoleBindingRow> bindings = sysUserMapper.selectRoleBindingsByUserIds(userIds);

        Map<Long, List<AdminUserRoleBindingRow>> roleMap = bindings.stream()
                .collect(Collectors.groupingBy(AdminUserRoleBindingRow::getUserId));

        List<AdminManagedUserResponse> records = users.stream()
                .map(user -> {
                    List<AdminUserRoleBindingRow> rows = roleMap.getOrDefault(user.getId(), List.of());
                    return AdminManagedUserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .realName(user.getRealName())
                            .status(user.getStatus())
                            .roleCodes(rows.stream().map(AdminUserRoleBindingRow::getRoleCode).toList())
                            .roleNames(rows.stream().map(AdminUserRoleBindingRow::getRoleName).toList())
                            .build();
                })
                .toList();

        return PageResult.of(records, total, pageNum, actualPageSize);
    }

    @Override
    public List<AdminAssignableRoleResponse> listAssignableRoles(List<String> currentRoles,
                                                                 List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);

        return fetchAssignableRoles(null).stream()
                .map(role -> AdminAssignableRoleResponse.builder()
                        .id(role.getId())
                        .roleCode(role.getRoleCode())
                        .roleName(role.getRoleName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(AdminAddUserRequest request,
                        List<String> currentRoles,
                        List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);
        validateStatus(request.getStatus());

        String username = normalizeRequired(request.getUsername(), "用户名不能为空");
        if (sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        List<SysRole> roles = fetchAssignableRoles(request.getRoleCodes());

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setRealName(normalizeRequired(request.getRealName(), "姓名不能为空"));
        user.setStatus(request.getStatus());
        sysUserMapper.insert(user);

        replaceUserRoles(user.getId(), roles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(AdminUpdateUserRequest request,
                           List<String> currentRoles,
                           List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);
        validateStatus(request.getStatus());

        SysUser user = requireManagedUser(request.getId());
        List<SysRole> roles = fetchAssignableRoles(request.getRoleCodes());

        user.setRealName(normalizeRequired(request.getRealName(), "姓名不能为空"));
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);

        replaceUserRoles(user.getId(), roles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(AdminUpdateUserStatusRequest request,
                                 List<String> currentRoles,
                                 List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);
        validateStatus(request.getStatus());

        SysUser user = requireManagedUser(request.getId());
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(AdminResetUserPasswordRequest request,
                                  List<String> currentRoles,
                                  List<String> currentPermissions) {
        assertManagePermission(currentRoles, currentPermissions);

        requireManagedUser(request.getId());

        SysUser user = new SysUser();
        user.setId(request.getId());
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        sysUserMapper.updateById(user);
    }

    private void assertManagePermission(List<String> currentRoles, List<String> currentPermissions) {
        boolean hasRole = currentRoles != null && currentRoles.contains("admin");
        boolean hasPermission = currentPermissions != null
                && currentPermissions.stream().anyMatch(MANAGE_PERMISSIONS::contains);

        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行账号与角色管理操作");
        }
    }

    private SysUser requireManagedUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户 ID 不能为空");
        }

        SysUser user = sysUserMapper.selectManagedUserById(userId);
        if (user == null) {
            throw new BusinessException(404, "未找到可管理的用户");
        }
        return user;
    }

    private List<SysRole> fetchAssignableRoles(List<String> requestedRoleCodes) {
        LinkedHashSet<String> roleCodes = requestedRoleCodes == null
                ? sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .in(SysRole::getRoleCode, ASSIGNABLE_ROLE_CODES)
                .orderByAsc(SysRole::getId))
                .stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                : requestedRoleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (roleCodes.isEmpty()) {
            throw new BusinessException(400, "至少分配一个业务角色");
        }

        if (!ASSIGNABLE_ROLE_CODES.containsAll(roleCodes)) {
            throw new BusinessException(400, "存在不可分配的角色");
        }

        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .in(SysRole::getRoleCode, roleCodes)
                .orderByAsc(SysRole::getId));

        if (roles.size() != roleCodes.size()) {
            throw new BusinessException(400, "角色配置不完整或角色已被停用");
        }

        Map<String, SysRole> roleMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getRoleCode, role -> role));

        return roleCodes.stream()
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private void replaceUserRoles(Long userId, List<SysRole> roles) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        for (SysRole role : roles) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为 0 或 1");
        }
    }

    private String normalizeRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(400, message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
