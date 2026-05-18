package com.oss.osscourse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oss.osscourse.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    List<String> getUserRoles(Long userId);
    List<String> getUserPermissions(Long userId);
}
