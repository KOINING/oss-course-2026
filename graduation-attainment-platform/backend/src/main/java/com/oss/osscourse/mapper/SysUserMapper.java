package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.admin.AdminUserRoleBindingRow;
import com.oss.osscourse.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT DISTINCT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(Long userId);

    @Select("SELECT DISTINCT p.perm_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermCodesByUserId(Long userId);

    @Select({
            "<script>",
            "SELECT u.*",
            "FROM sys_user u",
            "WHERE EXISTS (",
            "    SELECT 1 FROM sys_user_role ur",
            "    INNER JOIN sys_role r ON r.id = ur.role_id",
            "    WHERE ur.user_id = u.id",
            "      AND r.role_code IN ('academic_affairs', 'program_director', 'instructor')",
            ")",
            "AND NOT EXISTS (",
            "    SELECT 1 FROM sys_user_role ur",
            "    INNER JOIN sys_role r ON r.id = ur.role_id",
            "    WHERE ur.user_id = u.id AND r.role_code = 'admin'",
            ")",
            "<if test='username != null and username != \"\"'>",
            "AND u.username LIKE CONCAT('%', #{username}, '%')",
            "</if>",
            "<if test='realName != null and realName != \"\"'>",
            "AND u.real_name LIKE CONCAT('%', #{realName}, '%')",
            "</if>",
            "<if test='status != null'>",
            "AND u.status = #{status}",
            "</if>",
            "ORDER BY u.id ASC",
            "</script>"
    })
    List<SysUser> selectManagedUsers(@Param("username") String username,
                                     @Param("realName") String realName,
                                     @Param("status") Integer status);

    @Select({
            "<script>",
            "SELECT ur.user_id AS userId, r.role_code AS roleCode, r.role_name AS roleName",
            "FROM sys_user_role ur",
            "INNER JOIN sys_role r ON r.id = ur.role_id",
            "WHERE ur.user_id IN",
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
            "#{userId}",
            "</foreach>",
            "ORDER BY ur.user_id ASC, r.id ASC",
            "</script>"
    })
    List<AdminUserRoleBindingRow> selectRoleBindingsByUserIds(@Param("userIds") List<Long> userIds);

    @Select({
            "SELECT u.* FROM sys_user u",
            "WHERE u.id = #{userId}",
            "AND EXISTS (",
            "    SELECT 1 FROM sys_user_role ur",
            "    INNER JOIN sys_role r ON r.id = ur.role_id",
            "    WHERE ur.user_id = u.id",
            "      AND r.role_code IN ('academic_affairs', 'program_director', 'instructor')",
            ")",
            "AND NOT EXISTS (",
            "    SELECT 1 FROM sys_user_role ur",
            "    INNER JOIN sys_role r ON r.id = ur.role_id",
            "    WHERE ur.user_id = u.id AND r.role_code = 'admin'",
            ")"
    })
    SysUser selectManagedUserById(@Param("userId") Long userId);
}
