package com.oss.osscourse.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.entity.*;
import com.oss.osscourse.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking mock RBAC data...");

        Map<String, SysPermission> permMap = ensurePermissions();
        Map<String, SysRole> roleMap = ensureRoles();
        Map<String, SysUser> userMap = ensureUsers();
        ensureUserRoles(userMap, roleMap);
        ensureRolePermissions(roleMap, permMap);

        log.info("Mock RBAC data ready.");
        log.info("--- Mock Accounts ---");
        log.info("admin / 123456          -> 系统管理员");
        log.info("teacher_zhang / 123456  -> 课程主讲教师（张教授）");
        log.info("teacher_li / 123456     -> 课程主讲教师（李副教授）");
        log.info("teacher_wang / 123456   -> 课程主讲教师（王讲师）");
        log.info("director_chen / 123456  -> 专业负责人");
        log.info("academic_wu / 123456    -> 教务管理员");
    }

    private Map<String, SysPermission> ensurePermissions() {
        Map<String, SysPermission> map = new LinkedHashMap<>();
        String[][] perms = {
                {"college:manage",    "学院管理",       "system"},
                {"major:manage",      "专业管理",       "system"},
                {"user:manage",       "用户管理",       "system"},
                {"role:assign",       "角色分配",       "system"},
                {"dict:manage",       "字典管理",       "system"},
                {"requirement:write", "毕业要求编辑",    "macro"},
                {"matrix:write",      "支撑矩阵编辑",    "macro"},
                {"course:import",     "课程导入",       "macro"},
                {"class:import",      "班级学生导入",    "macro"},
                {"objective:write",   "课程目标编辑",    "syllabus"},
                {"weight:write",      "内部权重编辑",    "syllabus"},
                {"point:write",       "考核点编辑",      "syllabus"},
                {"score:import",      "成绩导入录入",    "assessment"},
                {"calc:trigger",      "达成度计算触发",   "assessment"},
                {"report:export",     "报表导出",       "report"},
        };
        for (String[] p : perms) {
            SysPermission existing = permMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getPermCode, p[0]));
            if (existing != null) {
                map.put(p[0], existing);
            } else {
                SysPermission perm = new SysPermission();
                perm.setPermCode(p[0]);
                perm.setPermName(p[1]);
                perm.setModuleName(p[2]);
                permMapper.insert(perm);
                map.put(p[0], perm);
            }
        }
        return map;
    }

    private Map<String, SysRole> ensureRoles() {
        Map<String, SysRole> map = new LinkedHashMap<>();
        String[][] roles = {
                {"admin",             "系统管理员",   "系统全局配置、用户账号管理"},
                {"academic_affairs",  "教务管理员",   "培养方案导入、班级学生管理、报表导出"},
                {"program_director",  "专业负责人",   "毕业要求维护、支撑矩阵配置、专业级计算"},
                {"instructor",        "课程主讲教师", "课程大纲编写、考核点设定、成绩录入、课程级计算"},
        };
        for (String[] r : roles) {
            SysRole existing = roleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, r[0]));
            if (existing != null) {
                map.put(r[0], existing);
            } else {
                SysRole role = new SysRole();
                role.setRoleCode(r[0]);
                role.setRoleName(r[1]);
                role.setRemark(r[2]);
                role.setStatus(1);
                roleMapper.insert(role);
                map.put(r[0], role);
            }
        }
        return map;
    }

    private Map<String, SysUser> ensureUsers() {
        Map<String, SysUser> map = new LinkedHashMap<>();
        String[][] users = {
                {"admin",          "123456", "赵管理员"},
                {"teacher_zhang",  "123456", "张教授"},
                {"teacher_li",     "123456", "李副教授"},
                {"teacher_wang",   "123456", "王讲师"},
                {"director_chen",  "123456", "陈主任"},
                {"academic_wu",    "123456", "吴老师"},
        };
        for (String[] u : users) {
            SysUser existing = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, u[0]));
            if (existing != null) {
                existing.setPassword(passwordEncoder.encode(u[1]));
                existing.setRealName(u[2]);
                userMapper.updateById(existing);
                map.put(u[0], existing);
            } else {
                SysUser user = new SysUser();
                user.setUsername(u[0]);
                user.setPassword(passwordEncoder.encode(u[1]));
                user.setRealName(u[2]);
                user.setStatus(1);
                userMapper.insert(user);
                map.put(u[0], user);
            }
        }
        return map;
    }

    private void ensureUserRoles(Map<String, SysUser> userMap, Map<String, SysRole> roleMap) {
        upsertUserRole(userMap.get("admin").getId(), roleMap.get("admin").getId());
        upsertUserRole(userMap.get("teacher_zhang").getId(), roleMap.get("instructor").getId());
        upsertUserRole(userMap.get("teacher_li").getId(), roleMap.get("instructor").getId());
        upsertUserRole(userMap.get("teacher_wang").getId(), roleMap.get("instructor").getId());
        upsertUserRole(userMap.get("director_chen").getId(), roleMap.get("program_director").getId());
        upsertUserRole(userMap.get("academic_wu").getId(), roleMap.get("academic_affairs").getId());
    }

    private void ensureRolePermissions(Map<String, SysRole> roleMap,
                                       Map<String, SysPermission> permMap) {
        for (SysPermission perm : permMap.values()) {
            upsertRolePerm(roleMap.get("admin").getId(), perm.getId());
        }

        String[] academicPerms = {"course:import", "class:import", "report:export"};
        for (String code : academicPerms) {
            upsertRolePerm(roleMap.get("academic_affairs").getId(), permMap.get(code).getId());
        }

        String[] directorPerms = {"requirement:write", "matrix:write", "calc:trigger", "report:export"};
        for (String code : directorPerms) {
            upsertRolePerm(roleMap.get("program_director").getId(), permMap.get(code).getId());
        }

        String[] instructorPerms = {"objective:write", "weight:write", "point:write", "score:import", "calc:trigger"};
        for (String code : instructorPerms) {
            upsertRolePerm(roleMap.get("instructor").getId(), permMap.get(code).getId());
        }
    }

    private void upsertUserRole(Long userId, Long roleId) {
        SysUserRole existing = userRoleMapper.selectOne(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, roleId));
        if (existing == null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    private void upsertRolePerm(Long roleId, Long permId) {
        SysRolePermission existing = rolePermMapper.selectOne(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
                        .eq(SysRolePermission::getPermissionId, permId));
        if (existing == null) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermMapper.insert(rp);
        }
    }
}
