package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.report.MajorReportRequest;
import com.oss.osscourse.dto.report.MajorReportResponse;
import com.oss.osscourse.dto.report.MajorReportResponse.ContributingCourse;
import com.oss.osscourse.dto.report.MajorReportResponse.DataSourceSummary;
import com.oss.osscourse.dto.report.MajorReportResponse.IndicatorReportRow;
import com.oss.osscourse.dto.supportmatrix.MatrixIndicatorPointResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.MajorIndicatorAchievement;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseIndicatorSupportMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.MajorIndicatorAchievementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.MajorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorReportServiceImpl implements MajorReportService {

    private final MajorMapper majorMapper;
    private final AcademicTermMapper academicTermMapper;
    private final CourseIndicatorSupportMapper cisMapper;
    private final MajorIndicatorAchievementMapper miaMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final CourseMapper courseMapper;
    private final CourseIndicatorAchievementMapper ciaMapper;

    @Override
    public MajorReportResponse assembleMajorReport(MajorReportRequest request) {
        // 1. 校验专业存在
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        // 2. 获取该专业+年级下所有启用的指标点
        List<MatrixIndicatorPointResponse> indicatorPoints = cisMapper.selectIndicatorPointsByMajor(
                request.getMajorId(), request.getGradeYear());
        if (indicatorPoints.isEmpty()) {
            return MajorReportResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .reportGeneratedAt(LocalDateTime.now())
                    .resultReady(false)
                    .message("当前专业在该年级下未配置毕业要求与指标点")
                    .indicatorAchievements(List.of())
                    .dataSourceSummary(DataSourceSummary.builder()
                            .sourceTable("major_indicator_achievement")
                            .supportCourseCount(0)
                            .lockedClassCount(0)
                            .remark("无可用数据")
                            .build())
                    .build();
        }

        // 3. 获取宏观支撑矩阵关系（course → ip）
        List<MatrixRelationResponse> matrixRelations = cisMapper.selectMatrixRelationsByMajor(
                request.getMajorId(), request.getGradeYear());
        Map<Long, List<MatrixRelationResponse>> ipRelationMap = matrixRelations.stream()
                .collect(Collectors.groupingBy(MatrixRelationResponse::getIpId));

        // 4. 确定学期并读取专业级结果
        Long targetTermId = resolveTermId(request);
        List<MajorIndicatorAchievement> majorResults = listMajorResults(
                request.getMajorId(), request.getGradeYear(), targetTermId);

        Map<Long, MajorIndicatorAchievement> majorResultMap = majorResults.stream()
                .collect(Collectors.toMap(
                        MajorIndicatorAchievement::getIpId,
                        item -> item,
                        (left, right) -> right,
                        LinkedHashMap::new));

        // 如果当前请求没有结果
        if (majorResults.isEmpty() && targetTermId == null) {
            return buildEmptyReport(major, request.getGradeYear(), indicatorPoints);
        }

        Long finalTermId = targetTermId != null
                ? targetTermId
                : (majorResults.isEmpty() ? null : majorResults.get(0).getTermId());

        // 5. 获取支撑课程信息和教学班
        Set<Long> supportCourseIds = matrixRelations.stream()
                .map(MatrixRelationResponse::getCourseId)
                .collect(Collectors.toSet());

        Map<Long, Course> courseMap = supportCourseIds.isEmpty()
                ? Map.of()
                : courseMapper.selectBatchIds(supportCourseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, item -> item));

        // 6. 获取这些课程在指定年级下的教学班（已锁定）
        List<TeachingClass> teachingClasses = listLockedTeachingClasses(supportCourseIds, request.getGradeYear());
        Map<Long, TeachingClass> classByCourseMap = new LinkedHashMap<>();
        for (TeachingClass tc : teachingClasses) {
            classByCourseMap.putIfAbsent(tc.getCourseId(), tc);
        }

        // 7. 获取课程级指标点达成度
        List<Long> classIds = teachingClasses.stream()
                .map(TeachingClass::getClassId)
                .toList();
        List<CourseIndicatorAchievement> courseAchievements = classIds.isEmpty()
                ? List.of()
                : ciaMapper.selectList(new LambdaQueryWrapper<CourseIndicatorAchievement>()
                .in(CourseIndicatorAchievement::getClassId, classIds));

        // 按 (classId, ipId) 组织课程级结果
        Map<String, CourseIndicatorAchievement> ciaMap = new LinkedHashMap<>();
        for (CourseIndicatorAchievement cia : courseAchievements) {
            String key = cia.getClassId() + "_" + cia.getIpId();
            ciaMap.put(key, cia);
        }

        // 8. 组装每个指标点的报告行
        List<IndicatorReportRow> rows = new ArrayList<>();
        for (MatrixIndicatorPointResponse ip : indicatorPoints) {
            MajorIndicatorAchievement mia = majorResultMap.get(ip.getIpId());
            List<MatrixRelationResponse> relations = ipRelationMap.getOrDefault(ip.getIpId(), List.of());

            float weightSum = 0f;
            List<ContributingCourse> contributingCourses = new ArrayList<>();

            for (MatrixRelationResponse relation : relations) {
                weightSum += relation.getTotalWeight();

                Course course = courseMap.get(relation.getCourseId());
                TeachingClass tc = classByCourseMap.get(relation.getCourseId());

                Float ek = null;
                if (tc != null) {
                    String ciaKey = tc.getClassId() + "_" + ip.getIpId();
                    CourseIndicatorAchievement cia = ciaMap.get(ciaKey);
                    ek = cia != null ? cia.getAchievement() : null;
                }

                float weightedContribution = (ek != null) ? ek * relation.getTotalWeight() : 0f;

                contributingCourses.add(ContributingCourse.builder()
                        .courseId(relation.getCourseId())
                        .courseCode(course != null ? course.getCourseCode() : null)
                        .courseName(course != null ? course.getCourseName() : null)
                        .classId(tc != null ? tc.getClassId() : null)
                        .className(tc != null ? tc.getClassName() : null)
                        .courseAchievement(ek)
                        .totalWeight(relation.getTotalWeight())
                        .weightedContribution(weightedContribution)
                        .build());
            }

            rows.add(IndicatorReportRow.builder()
                    .ipId(ip.getIpId())
                    .ipCode(ip.getIpCode())
                    .ipDescription(ip.getIpDescription())
                    .grCode(ip.getGrCode())
                    .finalAchievement(mia != null ? mia.getFinalAchievement() : null)
                    .contributingCourseCount(contributingCourses.size())
                    .totalWeightSum(weightSum)
                    .contributingCourses(contributingCourses)
                    .build());
        }

        // 9. 数据源摘要
        String termCode = finalTermId != null ? resolveTermCode(finalTermId) : null;
        int lockedCount = teachingClasses.size();
        DataSourceSummary summary = DataSourceSummary.builder()
                .sourceTable("major_indicator_achievement")
                .supportCourseCount(supportCourseIds.size())
                .lockedClassCount(lockedCount)
                .snapshotTermId(finalTermId)
                .remark(lockedCount < supportCourseIds.size()
                        ? "部分支撑课程尚未锁定，其课程级达成度可能缺失"
                        : "所有支撑课程均已锁定")
                .build();

        return MajorReportResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(finalTermId)
                .termCode(termCode)
                .reportGeneratedAt(LocalDateTime.now())
                .resultReady(!rows.isEmpty() && rows.stream().anyMatch(r -> r.getFinalAchievement() != null))
                .message(rows.isEmpty() || rows.stream().noneMatch(r -> r.getFinalAchievement() != null)
                        ? "当前年级尚未生成专业级汇总结果，请先执行专业级计算"
                        : null)
                .indicatorAchievements(rows)
                .dataSourceSummary(summary)
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 确定目标学期：优先使用请求中的 termId，否则取最新
     */
    private Long resolveTermId(MajorReportRequest request) {
        if (request.getTermId() != null) {
            return request.getTermId();
        }
        // 取该 major+gradeYear 下最新一条结果的 termId
        List<MajorIndicatorAchievement> latest = miaMapper.selectList(
                new LambdaQueryWrapper<MajorIndicatorAchievement>()
                        .eq(MajorIndicatorAchievement::getMajorId, request.getMajorId())
                        .eq(MajorIndicatorAchievement::getGradeYear, request.getGradeYear())
                        .orderByDesc(MajorIndicatorAchievement::getTermId)
                        .last("LIMIT 1"));
        return latest.isEmpty() ? null : latest.get(0).getTermId();
    }

    /**
     * 读取专业级结果
     */
    private List<MajorIndicatorAchievement> listMajorResults(Long majorId, Integer gradeYear, Long termId) {
        LambdaQueryWrapper<MajorIndicatorAchievement> wrapper = new LambdaQueryWrapper<MajorIndicatorAchievement>()
                .eq(MajorIndicatorAchievement::getMajorId, majorId)
                .eq(MajorIndicatorAchievement::getGradeYear, gradeYear);
        if (termId != null) {
            wrapper.eq(MajorIndicatorAchievement::getTermId, termId);
        } else {
            wrapper.orderByDesc(MajorIndicatorAchievement::getTermId);
        }
        wrapper.orderByAsc(MajorIndicatorAchievement::getIpId);

        List<MajorIndicatorAchievement> results = miaMapper.selectList(wrapper);
        if (termId == null && !results.isEmpty()) {
            // 取最新学期的那一批结果
            Long latestTermId = results.get(0).getTermId();
            results = results.stream()
                    .filter(item -> Objects.equals(item.getTermId(), latestTermId))
                    .collect(Collectors.toList());
        }
        return results;
    }

    /**
     * 获取指定课程集合在指定年级下已锁定的教学班
     */
    private List<TeachingClass> listLockedTeachingClasses(Set<Long> courseIds, Integer gradeYear) {
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return teachingClassMapper.selectList(new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getGradeYear, gradeYear)
                        .eq(TeachingClass::getCalcStatus, "locked")
                        .orderByAsc(TeachingClass::getCourseId)
                        .orderByAsc(TeachingClass::getClassCode));
    }

    /**
     * 解析学期编码
     */
    private String resolveTermCode(Long termId) {
        if (termId == null) {
            return null;
        }
        AcademicTerm term = academicTermMapper.selectById(termId);
        return term == null ? null : term.getTermCode();
    }

    /**
     * 构建无结果时的空报告
     */
    private MajorReportResponse buildEmptyReport(Major major, Integer gradeYear,
                                                  List<MatrixIndicatorPointResponse> indicatorPoints) {
        List<IndicatorReportRow> emptyRows = indicatorPoints.stream()
                .map(ip -> IndicatorReportRow.builder()
                        .ipId(ip.getIpId())
                        .ipCode(ip.getIpCode())
                        .ipDescription(ip.getIpDescription())
                        .grCode(ip.getGrCode())
                        .finalAchievement(null)
                        .contributingCourseCount(0)
                        .totalWeightSum(null)
                        .contributingCourses(List.of())
                        .build())
                .toList();

        return MajorReportResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(gradeYear)
                .reportGeneratedAt(LocalDateTime.now())
                .resultReady(false)
                .message("当前年级尚未生成专业级汇总结果，请先执行专业级计算")
                .indicatorAchievements(emptyRows)
                .dataSourceSummary(DataSourceSummary.builder()
                        .sourceTable("major_indicator_achievement")
                        .supportCourseCount(0)
                        .lockedClassCount(0)
                        .remark("无专业级计算结果")
                        .build())
                .build();
    }
}
