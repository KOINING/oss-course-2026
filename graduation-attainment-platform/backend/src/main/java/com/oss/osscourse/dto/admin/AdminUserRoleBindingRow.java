package com.oss.osscourse.dto.admin;

import lombok.Data;

@Data
public class AdminUserRoleBindingRow {
    private Long userId;
    private String roleCode;
    private String roleName;
}
