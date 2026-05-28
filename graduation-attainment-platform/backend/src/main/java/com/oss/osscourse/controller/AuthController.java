package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.LoginRequest;
import com.oss.osscourse.dto.LoginResponse;
import com.oss.osscourse.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.ok("登录成功", response);
    }

    @GetMapping("/userinfo")
    public Result<?> userInfo(@RequestAttribute("userId") Long userId,
                              @RequestAttribute("username") String username,
                              @RequestAttribute("realName") String realName,
                              @RequestAttribute("roles") List<String> roles,
                              @RequestAttribute("permissions") List<String> permissions) {
        LoginResponse.UserInfo info = LoginResponse.UserInfo.builder()
                .id(userId)
                .username(username)
                .realName(realName)
                .roles(roles)
                .permissions(permissions)
                .build();
        return Result.ok(info);
    }
}
