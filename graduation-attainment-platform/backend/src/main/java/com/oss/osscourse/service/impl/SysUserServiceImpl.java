package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oss.osscourse.entity.SysUser;
import com.oss.osscourse.mapper.SysUserMapper;
import com.oss.osscourse.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public List<String> getUserRoles(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return baseMapper.selectPermCodesByUserId(userId);
    }
}
