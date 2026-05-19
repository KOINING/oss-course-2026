package com.oss.osscourse.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminManagedUserResponse {
    private Long id;
    private String username;
    private String realName;
    private Integer status;
    private List<String> roleCodes;
    private List<String> roleNames;
}
