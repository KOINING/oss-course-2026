package com.oss.osscourse.service;

import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingRequest;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingRequest;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingResponse;

import java.util.List;

/**
 * 课程计算服务——提供公式导向的查询接口。
 * 直接服务于后续计算引擎的两级公式：
 * <ul>
 *   <li>课程目标级：C_ij = Σ(考核点得分) / Σ(考核点满分) → assessmentObjectiveMapping</li>
 *   <li>课程级：E_k = Σ_j (C̄_j × w_jk) → objectiveIndicatorMapping</li>
 * </ul>
 * 每个接口内嵌跨引用校验，保证不出现跨课程/跨年级引用。
 */
public interface CourseCalculationService {

    /**
     * 查询「考核点 → 课程目标」映射。
     * 返回该课程下所有课程目标，以及每个目标绑定的考核点及其满分。
     * 数据直接服务于课程目标级达成度公式。
     *
     * @param request     查询条件（courseId）
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 考核点→课程目标映射，含跨引用校验结果
     */
    AssessmentObjectiveMappingResponse getAssessmentObjectiveMapping(
            AssessmentObjectiveMappingRequest request,
            List<String> roles,
            List<String> permissions);

    /**
     * 查询「课程目标 → 指标点」内部权重映射。
     * 返回该课程 + 年级版本下所有指标点，以及每个指标点的贡献课程目标及权重。
     * 数据直接服务于课程级指标点达成度公式。
     *
     * @param request     查询条件（courseId + gradeYear）
     * @param roles       当前用户角色
     * @param permissions 当前用户权限
     * @return 课程目标→指标点映射，含权重校验和跨引用校验
     */
    ObjectiveIndicatorMappingResponse getObjectiveIndicatorMapping(
            ObjectiveIndicatorMappingRequest request,
            List<String> roles,
            List<String> permissions);
}
