package com.oss.osscourse.dto.admin;

import lombok.Data;

@Data
public class AdminUserQueryRequest {
    private String username;
    private String realName;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize;
}
