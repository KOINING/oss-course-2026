package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.dto.LoginRequest;
import com.oss.osscourse.dto.LoginResponse;
import com.oss.osscourse.entity.SysUser;
import com.oss.osscourse.mapper.SysUserMapper;
import com.oss.osscourse.service.AuthService;
import com.oss.osscourse.service.SysUserService;
import com.oss.osscourse.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysUserService sysUserService,
                           JwtUtil jwtUtil,
                           BCryptPasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserService = sysUserService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        List<String> roles = sysUserService.getUserRoles(user.getId());
        List<String> permissions = sysUserService.getUserPermissions(user.getId());

        String token = jwtUtil.generateToken(
                user.getId(), user.getUsername(), user.getRealName(), roles, permissions);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions)
                .build();

        return LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }
}
