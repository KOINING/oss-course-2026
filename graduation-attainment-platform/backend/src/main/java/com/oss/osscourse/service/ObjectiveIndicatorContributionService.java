package com.oss.osscourse.service;

import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionBatchSaveRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionQueryRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionResponse;

import java.util.List;

public interface ObjectiveIndicatorContributionService {

    /**
     * 按课程 + 年级查询已配置的内部权重列表。
     *
     * @param request     查询条件（courseId, gradeYear）
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 内部权重配置列表
     */
    List<ObjectiveIndicatorContributionResponse> query(ObjectiveIndicatorContributionQueryRequest request,
                                                        List<String> roles,
                                                        List<String> permissions);

    /**
     * 批量保存课程目标的内部权重配置。
     * 策略：先删后插——删除该课程下所有已有权重，再插入本次提交的全部配置。
     * 保存前严格校验：coId 归属、ipId 合法性、权重范围、同指标点权重和 = 1.0。
     *
     * @param request     批量保存请求
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     */
    void batchSave(ObjectiveIndicatorContributionBatchSaveRequest request,
                   List<String> roles,
                   List<String> permissions);
}
