package com.oss.osscourse.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminAddUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需为 3 到 50 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需为 6 到 100 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50 位")
    private String realName;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotEmpty(message = "至少分配一个业务角色")
    private List<String> roleCodes;
}
