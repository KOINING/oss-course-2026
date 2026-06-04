package com.oss.osscourse.service;

import com.oss.osscourse.dto.courseobjective.CourseObjectiveCreateRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveQueryRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveResponse;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveUpdateRequest;

import java.util.List;

public interface CourseObjectiveService {

    /**
     * 查询课程目标列表。
     * 返回的 response 中 description 为纯文本（从 DB 富文本中提取），
     * descriptionRich 为 null（列表不返回富文本以保持响应体积小）。
     *
     * @param request     查询条件
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 课程目标列表
     */
    List<CourseObjectiveResponse> list(CourseObjectiveQueryRequest request,
                                        List<String> roles,
                                        List<String> permissions);

    /**
     * 按ID查询课程目标详情。
     * 返回的 response 同时包含 description（纯文本）和 descriptionRich（原始值，可能是HTML）。
     *
     * @param coId        课程目标ID
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 课程目标详情
     */
    CourseObjectiveResponse getById(Long coId,
                                     List<String> roles,
                                     List<String> permissions);

    /**
     * 新增课程目标。
     * 若传了 descriptionRich，将其存入 DB；否则存入 description。
     *
     * @param request     新增请求
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void create(CourseObjectiveCreateRequest request,
                List<String> roles,
                List<String> permissions);

    /**
     * 更新课程目标。
     * 若传了 descriptionRich，将其存入 DB；否则存入 description。
     *
     * @param request     更新请求
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void update(CourseObjectiveUpdateRequest request,
                List<String> roles,
                List<String> permissions);

    /**
     * 删除课程目标。
     * 若课程目标已被考核点或内部权重引用，则拒绝删除。
     *
     * @param coId        课程目标ID
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void delete(Long coId,
                List<String> roles,
                List<String> permissions);
}
