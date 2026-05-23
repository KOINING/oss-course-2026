package com.oss.osscourse.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAssignableRoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
}
