package com.oss.osscourse.service;

import com.oss.osscourse.dto.assessmentpoint.AssessmentPointCreateRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointQueryRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointResponse;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointUpdateRequest;

import java.util.List;

public interface AssessmentPointService {

    /**
     * 查询考核点列表，支持按名称（模糊）、课程ID、课程目标ID 筛选。
     *
     * @param request     查询条件
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 考核点列表
     */
    List<AssessmentPointResponse> list(AssessmentPointQueryRequest request,
                                        List<String> roles,
                                        List<String> permissions);

    /**
     * 按ID查询考核点详情。
     *
     * @param apId        考核点ID
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 考核点详情
     */
    AssessmentPointResponse getById(Long apId,
                                     List<String> roles,
                                     List<String> permissions);

    /**
     * 新增考核点。
     * 校验：绑定目标coId有效、同课程下名称不重复、满分>0。
     *
     * @param request     新增请求
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void create(AssessmentPointCreateRequest request,
                List<String> roles,
                List<String> permissions);

    /**
     * 更新考核点。
     * 校验：绑定目标coId有效、同课程下名称不重复（排除自身）、满分>0。
     *
     * @param request     更新请求
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void update(AssessmentPointUpdateRequest request,
                List<String> roles,
                List<String> permissions);

    /**
     * 删除考核点。
     * 若已被学生成绩引用则拒绝删除。
     *
     * @param apId        考核点ID
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void delete(Long apId,
                List<String> roles,
                List<String> permissions);
}
