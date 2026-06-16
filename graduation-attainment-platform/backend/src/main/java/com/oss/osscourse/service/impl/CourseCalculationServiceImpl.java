package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingRequest;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse.AssessmentPointItem;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse.CrossValidationResult;
import com.oss.osscourse.dto.calculation.AssessmentObjectiveMappingResponse.ObjectiveGroup;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingRequest;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingResponse;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingResponse.IndicatorGroup;
import com.oss.osscourse.dto.calculation.ObjectiveIndicatorMappingResponse.ObjectiveWeight;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionResponse;
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.service.CourseCalculationService;
import com.oss.osscourse.util.HtmlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseCalculationServiceImpl implements CourseCalculationService {

    private static final String MANAGE_ROLE = "instructor";
    private static final String MANAGE_PERMISSION = "calc:trigger";

    private final CourseMapper courseMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final AssessmentPointMapper assessmentPointMapper;
    private final ObjectiveIndicatorContributionMapper oicMapper;
    private final CourseMajorMapper courseMajorMapper;

    // ==================== 考核点 → 课程目标 映射 ====================

    @Override
    public AssessmentObjectiveMappingResponse getAssessmentObjectiveMapping(
            AssessmentObjectiveMappingRequest request,
            List<String> roles,
            List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long courseId = request.getCourseId();
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        List<String> crossErrors = new ArrayList<>();

        // 加载该课程的所有课程目标
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId));
        Set<Long> validCoIds = new HashSet<>();
        for (CourseObjective co : objectives) {
            validCoIds.add(co.getCoId());
        }

        // 按课程目标ID分组收集考核点
        Map<Long, List<AssessmentPoint>> apMap = new HashMap<>();
        List<AssessmentPoint> allAPs = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>()
                        .in(AssessmentPoint::getCoId, validCoIds.isEmpty() ? Set.of(-1L) : validCoIds));
        for (AssessmentPoint ap : allAPs) {
            apMap.computeIfAbsent(ap.getCoId(), k -> new ArrayList<>()).add(ap);
        }

        // 跨引用校验：每个考核点只允许引用本课程的目标
        for (AssessmentPoint ap : allAPs) {
            Long apCoId = ap.getCoId();
            if (!validCoIds.contains(apCoId)) {
                crossErrors.add("考核点(ID=" + ap.getApId() + ", 名称=" + ap.getApName()
                        + ") 引用了不属于课程(ID=" + courseId + ") 的课程目标(ID=" + apCoId + ")，跨课程引用不允许");
            }
        }

        // 组装响应
        List<ObjectiveGroup> groups = new ArrayList<>();
        for (CourseObjective co : objectives) {
            List<AssessmentPoint> aps = apMap.getOrDefault(co.getCoId(), List.of());
            float totalFullScore = 0f;
            List<AssessmentPointItem> items = new ArrayList<>();
            for (AssessmentPoint ap : aps) {
                totalFullScore += ap.getFullScore();
                items.add(AssessmentPointItem.builder()
                        .apId(ap.getApId())
                        .apName(ap.getApName())
                        .fullScore(ap.getFullScore())
                        .hasScores(false) // 由前端另行查询成绩
                        .build());
            }
            groups.add(ObjectiveGroup.builder()
                    .coId(co.getCoId())
                    .objectiveCode(co.getObjectiveCode())
                    .coDescription(HtmlUtils.stripHtml(co.getCoDescription()))
                    .assessmentPoints(items)
                    .totalFullScore(totalFullScore)
                    .build());
        }

        // 补充校验：没有课程目标的课程无法使用公式
        if (objectives.isEmpty()) {
            crossErrors.add("课程(ID=" + courseId + ") 下未配置课程目标，公式无法计算");
        }

        boolean valid = crossErrors.isEmpty();

        return AssessmentObjectiveMappingResponse.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .objectives(groups)
                .validation(CrossValidationResult.builder()
                        .valid(valid)
                        .errorCount(crossErrors.size())
                        .errors(crossErrors)
                        .build())
                .build();
    }

    // ==================== 课程目标 → 指标点 映射 ====================

    @Override
    public ObjectiveIndicatorMappingResponse getObjectiveIndicatorMapping(
            ObjectiveIndicatorMappingRequest request,
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
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 获取该课程 + 专业 + 年级版本的 course_major
        CourseMajor courseMajor = courseMajorMapper.selectOne(
                new LambdaQueryWrapper<CourseMajor>()
                        .eq(CourseMajor::getCourseId, courseId)
                        .eq(CourseMajor::getMajorId, majorId)
                        .eq(CourseMajor::getGradeYear, gradeYear)
                        .last("LIMIT 1"));

        List<String> crossErrors = new ArrayList<>();
        if (courseMajor == null) {
            crossErrors.add("课程(ID=" + courseId + ") 未绑定专业(ID=" + majorId + ") "
                    + gradeYear + " 年级培养方案，无法校验课程目标到指标点映射");
        }

        // 合法 coId 集合（该课程下的所有目标）
        Set<Long> validCoIds = new HashSet<>();
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId));
        Map<Long, CourseObjective> coMap = new HashMap<>();
        for (CourseObjective co : objectives) {
            validCoIds.add(co.getCoId());
            coMap.put(co.getCoId(), co);
        }

        // 合法 ipId 集合（该课程 + 年级版本下可关联的指标点）
        Set<Long> validIpIds = new HashSet<>(oicMapper.selectValidIpIds(courseId, majorId, gradeYear));

        // 查询现有内部权重配置
        List<ObjectiveIndicatorContributionResponse>
                allMappings = oicMapper.selectByCourseAndProgram(courseId, majorId, gradeYear);

        // 跨引用校验
        for (ObjectiveIndicatorContributionResponse mapping : allMappings) {
            Long mCoId = mapping.getCoId();
            Long mIpId = mapping.getIpId();

            if (!validCoIds.contains(mCoId)) {
                crossErrors.add("内部权重记录(ID=" + mapping.getOicId() + ") 引用的课程目标(ID=" + mCoId
                        + ") 不属于课程(ID=" + courseId + ")，跨课程引用不允许");
            }
            if (!validIpIds.contains(mIpId)) {
                crossErrors.add("内部权重记录(ID=" + mapping.getOicId() + ") 引用的指标点(ID=" + mIpId
                        + ") 不在课程(ID=" + courseId + ") " + gradeYear + " 年级版本的合法指标点范围内，跨年级/跨专业引用不允许");
            }
        }

        // 按指标点ID分组
        Map<Long, List<ObjectiveIndicatorContributionResponse>>
                ipGroupMap = new HashMap<>();
        // 同时保留指标点信息
        Map<Long, String[]> ipInfoMap = new HashMap<>(); // ipId → [ipCode, ipDescription, grCode]
        for (ObjectiveIndicatorContributionResponse mapping : allMappings) {
            ipGroupMap.computeIfAbsent(mapping.getIpId(), k -> new ArrayList<>()).add(mapping);
            ipInfoMap.putIfAbsent(mapping.getIpId(),
                    new String[]{mapping.getIpCode(), mapping.getIpDescription(), mapping.getGrCode()});
        }

        // 组装响应
        List<IndicatorGroup> indicatorGroups = new ArrayList<>();
        for (Map.Entry<Long, List<ObjectiveIndicatorContributionResponse>>
                entry : ipGroupMap.entrySet()) {
            Long ipId = entry.getKey();
            List<ObjectiveIndicatorContributionResponse> mappings =
                    entry.getValue();
            String[] ipInfo = ipInfoMap.get(ipId);

            double weightSum = 0;
            List<ObjectiveWeight> weights = new ArrayList<>();
            for (ObjectiveIndicatorContributionResponse mapping : mappings) {
                weightSum += mapping.getInternalWeight();
                weights.add(ObjectiveWeight.builder()
                        .coId(mapping.getCoId())
                        .objectiveCode(mapping.getObjectiveCode())
                        .coDescription(HtmlUtils.stripHtml(
                                coMap.containsKey(mapping.getCoId())
                                        ? coMap.get(mapping.getCoId()).getCoDescription()
                                        : mapping.getCoDescription()))
                        .internalWeight(mapping.getInternalWeight())
                        .build());
            }

            boolean weightValid = Math.abs(weightSum - 1.0) < 0.001;
            if (!weightValid) {
                crossErrors.add("指标点(ID=" + ipId + ", 编号=" + ipInfo[0] + ") 的内部权重之和为 "
                        + String.format("%.4f", weightSum) + "，不等于 1.0，公式结果将失真");
            }

            indicatorGroups.add(IndicatorGroup.builder()
                    .ipId(ipId)
                    .ipCode(ipInfo[0])
                    .ipDescription(ipInfo[1])
                    .grCode(ipInfo[2])
                    .contributingObjectives(weights)
                    .weightSum(weightSum)
                    .weightValid(weightValid)
                    .build());
        }

        // 补充校验
        if (objectives.isEmpty()) {
            crossErrors.add("课程(ID=" + courseId + ") 下未配置课程目标");
        }
        if (allMappings.isEmpty()) {
            crossErrors.add("课程(ID=" + courseId + ") 在 " + gradeYear + " 年级版本下未配置内部权重，无法计算 E_k");
        }

        boolean valid = crossErrors.isEmpty();

        return ObjectiveIndicatorMappingResponse.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .gradeYear(gradeYear)
                .majorId(majorId)
                .indicatorPoints(indicatorGroups)
                .validation(ObjectiveIndicatorMappingResponse.CrossValidationResult.builder()
                        .valid(valid)
                        .errorCount(crossErrors.size())
                        .errors(crossErrors)
                        .build())
                .build();
    }

    // ==================== 权限校验 ====================

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行计算数据查询操作");
        }
    }
}
