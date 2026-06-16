package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionBatchSaveRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionQueryRequest;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionResponse;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.service.ObjectiveIndicatorContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObjectiveIndicatorContributionServiceImpl implements ObjectiveIndicatorContributionService {

    private static final String MANAGE_ROLE = "instructor";
    private static final String MANAGE_PERMISSION = "weight:write";

    private final ObjectiveIndicatorContributionMapper oicMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final CourseMapper courseMapper;
    private final CourseMajorMapper courseMajorMapper;

    @Override
    public List<ObjectiveIndicatorContributionResponse> query(ObjectiveIndicatorContributionQueryRequest request,
                                                               List<String> roles,
                                                               List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long courseId = request.getCourseId();
        Long majorId = request.getMajorId();
        Integer gradeYear = request.getGradeYear();
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }
        if (majorId == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        if (gradeYear == null) {
            throw new BusinessException(400, "培养方案年级不能为空");
        }
        validateGradeYear(gradeYear);

        if (courseMapper.selectById(courseId) == null) {
            throw new BusinessException(404, "课程不存在");
        }
        requireCourseProgramBinding(courseId, majorId, gradeYear);

        return oicMapper.selectByCourseAndProgram(courseId, majorId, gradeYear);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(ObjectiveIndicatorContributionBatchSaveRequest request,
                           List<String> roles,
                           List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long courseId = request.getCourseId();
        Long majorId = request.getMajorId();
        Integer gradeYear = request.getGradeYear();
        List<ObjectiveIndicatorContributionBatchSaveRequest.ContributionItem> contributions =
                request.getContributions();

        // 1. 基础校验
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }
        if (majorId == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        validateGradeYear(gradeYear);
        if (courseMapper.selectById(courseId) == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (contributions == null || contributions.isEmpty()) {
            throw new BusinessException(400, "权重配置列表不能为空");
        }

        // 2. 校验该课程在指定专业年级下是否存在 course_major 关联
        requireCourseProgramBinding(courseId, majorId, gradeYear);

        // 3. 批量加载该课程下的所有课程目标（用于校验 coId 归属）
        List<CourseObjective> allObjectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId));
        Set<Long> validCoIds = allObjectives.stream()
                .map(CourseObjective::getCoId)
                .collect(Collectors.toSet());
        if (validCoIds.isEmpty()) {
            throw new BusinessException(400, "该课程下尚不存在课程目标，请先创建课程目标");
        }

        // 4. 批量加载合法 ipId 集合（本专业 + 本年级版本下的全部指标点）
        Set<Long> validIpIds = new HashSet<>(oicMapper.selectValidIpIds(courseId, majorId, gradeYear));
        if (validIpIds.isEmpty()) {
            throw new BusinessException(400, "该课程在 " + gradeYear + " 年级版本下未找到可关联的指标点，请先配置毕业要求与指标点");
        }

        // 5. 逐条校验
        Map<Long, Double> weightSumByIp = new HashMap<>(); // ipId → 权重累计
        Set<String> seenPairs = new HashSet<>();           // "coId:ipId" 去重

        for (int i = 0; i < contributions.size(); i++) {
            ObjectiveIndicatorContributionBatchSaveRequest.ContributionItem item = contributions.get(i);
            int rowNum = i + 1;

            Long coId = item.getCoId();
            Long ipId = item.getIpId();
            Float internalWeight = item.getInternalWeight();

            // 5a. coId 归属校验
            if (coId == null) {
                throw new BusinessException(400, "第 " + rowNum + " 条：课程目标ID不能为空");
            }
            if (!validCoIds.contains(coId)) {
                throw new BusinessException(400, "第 " + rowNum + " 条：课程目标(ID=" + coId + ") 不属于当前课程(ID=" + courseId + ")");
            }

            // 5b. ipId 合法性校验
            if (ipId == null) {
                throw new BusinessException(400, "第 " + rowNum + " 条：指标点ID不能为空");
            }
            if (!validIpIds.contains(ipId)) {
                throw new BusinessException(400, "第 " + rowNum + " 条：指标点(ID=" + ipId + ") 不在当前专业(" + majorId
                        + ") " + gradeYear + " 年级版本的合法指标点范围内");
            }

            // 5c. 权重范围校验
            if (internalWeight == null) {
                throw new BusinessException(400, "第 " + rowNum + " 条：内部权重不能为空");
            }
            if (internalWeight <= 0 || internalWeight > 1) {
                throw new BusinessException(400, "第 " + rowNum + " 条：内部权重必须在 (0, 1] 范围内，当前值为 " + internalWeight);
            }

            // 5d. 同一课程目标对同一指标点不能重复配置
            String pairKey = coId + ":" + ipId;
            if (!seenPairs.add(pairKey)) {
                throw new BusinessException(400, "第 " + rowNum + " 条：课程目标(ID=" + coId + ") 与指标点(ID=" + ipId
                        + ") 重复配置，同一课程目标对同一指标点只能出现一次");
            }

            // 5e. 累加同指标点权重
            weightSumByIp.merge(ipId, (double) internalWeight, Double::sum);
        }

        // 6. 校验每个指标点的权重和是否等于 1.0
        for (Map.Entry<Long, Double> entry : weightSumByIp.entrySet()) {
            double sum = entry.getValue();
            if (Math.abs(sum - 1.0) > 0.001) {
                throw new BusinessException(400, "指标点(ID=" + entry.getKey() + ") 的所有课程目标内部权重之和为 "
                        + String.format("%.4f", sum) + "，必须等于 1.0。当前参与该指标点的课程目标来自第 "
                        + getRowNumsForIp(contributions, entry.getKey()) + " 条");
            }
        }

        // 7. 先删后插（事务内，仅删除当前年级版本下的记录）
        oicMapper.deleteByCourseIdAndProgram(courseId, majorId, gradeYear);

        for (ObjectiveIndicatorContributionBatchSaveRequest.ContributionItem item : contributions) {
            ObjectiveIndicatorContribution entity = new ObjectiveIndicatorContribution();
            entity.setCoId(item.getCoId());
            entity.setIpId(item.getIpId());
            entity.setInternalWeight(item.getInternalWeight());
            oicMapper.insert(entity);
        }
    }

    // ==================== 权限校验 ====================

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行内部权重管理操作");
        }
    }

    // ==================== 工具方法 ====================

    private void validateGradeYear(Integer gradeYear) {
        if (gradeYear == null) {
            throw new BusinessException(400, "培养方案年级不能为空");
        }
        if (gradeYear < 2000 || gradeYear > 2100) {
            throw new BusinessException(400, "年级必须在2000到2100之间");
        }
    }

    private void requireCourseProgramBinding(Long courseId, Long majorId, Integer gradeYear) {
        Long count = courseMajorMapper.selectCount(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, courseId)
                .eq(CourseMajor::getMajorId, majorId)
                .eq(CourseMajor::getGradeYear, gradeYear));
        if (count == null || count == 0) {
            throw new BusinessException(400, "该课程未配置当前专业 " + majorId + "、" + gradeYear
                    + " 年级的培养方案关联，请先完成课程-专业-年级绑定");
        }
    }

    /**
     * 获取哪些行号参与了指定 ipId 的权重配置（用于错误消息）。
     */
    private String getRowNumsForIp(
            List<ObjectiveIndicatorContributionBatchSaveRequest.ContributionItem> items,
            Long targetIpId) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (targetIpId.equals(items.get(i).getIpId())) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(i + 1);
            }
        }
        return sb.toString();
    }
}
