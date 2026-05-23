package com.oss.osscourse.service;

import com.oss.osscourse.dto.admin.*;

import java.util.List;

public interface AdminUserManagementService {
    List<AdminManagedUserResponse> listUsers(AdminUserQueryRequest request,
                                            List<String> currentRoles,
                                            List<String> currentPermissions);

    List<AdminAssignableRoleResponse> listAssignableRoles(List<String> currentRoles,
                                                          List<String> currentPermissions);

    void addUser(AdminAddUserRequest request,
                 List<String> currentRoles,
                 List<String> currentPermissions);

    void updateUser(AdminUpdateUserRequest request,
                    List<String> currentRoles,
                    List<String> currentPermissions);

    void updateUserStatus(AdminUpdateUserStatusRequest request,
                          List<String> currentRoles,
                          List<String> currentPermissions);
}
