package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.achievement.AssessmentFilterOptionsResponse;
import com.oss.osscourse.dto.achievement.MacroDashboardRequest;
import com.oss.osscourse.dto.achievement.MacroDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResultResponse;
import com.oss.osscourse.dto.achievement.UnlockRequestApproveRequest;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import com.oss.osscourse.entity.GraduationRequirement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.MajorIndicatorAchievement;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.StudentObjectiveAchievement;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.entity.UnlockAuditLog;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveAchievementMapper;
import com.oss.osscourse.mapper.GraduationRequirementMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorIndicatorAchievementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentObjectiveAchievementMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.mapper.UnlockAuditLogMapper;
import com.oss.osscourse.service.AssessmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentQueryServiceImpl implements AssessmentQueryService {

    private final MajorMapper majorMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final AcademicTermMapper academicTermMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final StudentClassMapper studentClassMapper;
    private final StudentAssessmentScoreMapper sasMapper;
    private final MajorIndicatorAchievementMapper miaMapper;
    private final GraduationRequirementMapper graduationRequirementMapper;
    private final IndicatorPointMapper indicatorPointMapper;
    private final CourseObjectiveAchievementMapper coaMapper;
    private final CourseIndicatorAchievementMapper ciaMapper;
    private final StudentObjectiveAchievementMapper soaMapper;
    private final UnlockAuditLogMapper unlockAuditLogMapper;

    @Override
    public AssessmentFilterOptionsResponse listMajorGradeYearTerms() {
        List<Major> activeMajors = majorMapper.selectList(
                new LambdaQueryWrapper<Major>().eq(Major::getStatus, 1).orderByAsc(Major::getMajorName));
        List<CourseMajor> courseMajors = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>().orderByAsc(CourseMajor::getMajorId).orderByDesc(CourseMajor::getGradeYear));

        List<Integer> globalGradeYears = courseMajors.stream()
                .map(CourseMajor::getGradeYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<AssessmentFilterOptionsResponse.MajorOption> majors = activeMajors.stream()
                .map(major -> {
                    List<Integer> gradeYears = courseMajors.stream()
                            .filter(item -> Objects.equals(item.getMajorId(), major.getMajorId()))
                            .map(CourseMajor::getGradeYear)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted(Comparator.reverseOrder())
                            .toList();

                    List<AssessmentFilterOptionsResponse.GradeYearScope> gradeYearScopes = gradeYears.stream()
                            .map(year -> AssessmentFilterOptionsResponse.GradeYearScope.builder()
                                    .gradeYear(year)
                                    .terms(List.of())
                                    .build())
                            .toList();

                    return AssessmentFilterOptionsResponse.MajorOption.builder()
                            .majorId(major.getMajorId())
                            .majorName(major.getMajorName())
                            .gradeYearScopes(gradeYearScopes)
                            .build();
                })
                .toList();

        return AssessmentFilterOptionsResponse.builder()
                .majors(majors)
                .gradeYears(globalGradeYears)
                .terms(List.of())
                .build();
    }

    @Override
    public MacroDashboardResponse getMacroDashboard(MacroDashboardRequest request) {
        Major major = requireMajor(request.getMajorId());
        List<Long> courseIds = listSupportCourseIds(request.getMajorId(), request.getGradeYear());

        if (courseIds.isEmpty()) {
            return MacroDashboardResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .termId(null)
                    .termCode(null)
                    .aggregationAllowed(false)
                    .unlockedWarning(true)
                    .blockReason("当前专业在该年级下没有配置支撑课程")
                    .majorResultExists(false)
                    .courses(List.of())
                    .build();
        }

        List<TeachingClass> teachingClasses = listSupportTeachingClasses(request.getMajorId(), request.getGradeYear(), courseIds);
        if (teachingClasses.isEmpty()) {
            return MacroDashboardResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .termId(null)
                    .termCode(null)
                    .aggregationAllowed(false)
                    .unlockedWarning(true)
                    .blockReason("当前专业在该年级下没有教学班数据")
                    .majorResultExists(false)
                    .courses(List.of())
                    .build();
        }

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, item -> item, (left, right) -> left, HashMap::new));
        Map<Long, Teacher> teacherMap = loadTeacherMap(teachingClasses);
        Map<Long, UnlockAuditLog> pendingUnlockMap = loadPendingUnlockMap(teachingClasses);
        Map<Long, Teacher> requestTeacherMap = loadRequestTeacherMap(new ArrayList<>(pendingUnlockMap.values()));

        List<MacroDashboardResponse.CourseRow> courses = teachingClasses.stream()
                .map(teachingClass -> {
                    Long studentCount = studentClassMapper.selectCount(
                            new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, teachingClass.getClassId()));
                    Long scoreCount = sasMapper.selectCount(
                            new LambdaQueryWrapper<StudentAssessmentScore>().eq(StudentAssessmentScore::getClassId, teachingClass.getClassId()));
                    Course course = courseMap.get(teachingClass.getCourseId());
                    Teacher teacher = teacherMap.get(teachingClass.getTeacherId());
                    String calcStatus = teachingClass.getCalcStatus();
                    UnlockAuditLog pendingUnlock = pendingUnlockMap.get(teachingClass.getClassId());
                    Teacher requestTeacher = pendingUnlock == null ? null : requestTeacherMap.get(pendingUnlock.getRequestBy());
                    return MacroDashboardResponse.CourseRow.builder()
                            .courseId(teachingClass.getCourseId())
                            .courseCode(course == null ? null : course.getCourseCode())
                            .courseName(course == null ? null : course.getCourseName())
                            .classId(teachingClass.getClassId())
                            .classCode(teachingClass.getClassCode())
                            .className(teachingClass.getClassName())
                            .teacherName(teacher == null ? "-" : teacher.getTeacherName())
                            .studentCount(studentCount == null ? 0 : studentCount)
                            .scoreCount(scoreCount == null ? 0 : scoreCount)
                            .calcStatus(calcStatus)
                            .blockReason(resolveCourseBlockReason(calcStatus))
                            .unlockRequested(pendingUnlock != null)
                            .unlockReason(pendingUnlock == null ? null : pendingUnlock.getReason())
                            .unlockRequestedBy(requestTeacher == null ? null : requestTeacher.getTeacherName())
                            .build();
                })
                .toList();

        Long latestTermId = teachingClasses.stream()
                .map(TeachingClass::getTermId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);

        boolean aggregationAllowed = !courses.isEmpty()
                && courses.stream().allMatch(item -> "locked".equals(item.getCalcStatus()));

        return MacroDashboardResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(latestTermId)
                .termCode(resolveTermCode(latestTermId))
                .aggregationAllowed(aggregationAllowed)
                .unlockedWarning(!aggregationAllowed)
                .blockReason(aggregationAllowed ? null : "存在未完成计算或未锁定的教学班")
                .majorResultExists(hasMajorResult(request.getMajorId(), request.getGradeYear()))
                .courses(courses)
                .build();
    }

    @Override
    public MajorCalcResultResponse getMajorCalcResult(MacroDashboardRequest request) {
        Major major = requireMajor(request.getMajorId());
        List<Long> courseIds = listSupportCourseIds(request.getMajorId(), request.getGradeYear());
        List<TeachingClass> teachingClasses = listSupportTeachingClasses(request.getMajorId(), request.getGradeYear(), courseIds);
        boolean aggregationAllowed = !teachingClasses.isEmpty()
                && teachingClasses.stream().allMatch(item -> "locked".equals(item.getCalcStatus()));

        if (!aggregationAllowed) {
            return MajorCalcResultResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .resultReady(false)
                    .message("当前专业年级尚未生成专业级计算结果，请先完成课程级锁定并执行专业级计算。")
                    .indicatorAchievements(List.of())
                    .build();
        }

        List<IndicatorPoint> configuredIndicators = listConfiguredIndicators(request.getMajorId(), request.getGradeYear());
        List<MajorIndicatorAchievement> snapshotResults = listLatestMajorResults(request.getMajorId(), request.getGradeYear());
        Map<Long, MajorIndicatorAchievement> resultMap = snapshotResults.stream()
                .collect(Collectors.toMap(MajorIndicatorAchievement::getIpId, item -> item, (left, right) -> right, LinkedHashMap::new));

        List<MajorCalcResponse.IndicatorAchievement> indicatorAchievements = configuredIndicators.stream()
                .map(indicatorPoint -> {
                    MajorIndicatorAchievement result = resultMap.get(indicatorPoint.getIpId());
                    return MajorCalcResponse.IndicatorAchievement.builder()
                            .ipId(indicatorPoint.getIpId())
                            .ipCode(indicatorPoint.getIpCode())
                            .ipDescription(indicatorPoint.getIpDescription())
                            .finalAchievement(result == null ? null : result.getFinalAchievement())
                            .build();
                })
                .toList();

        Long snapshotTermId = snapshotResults.isEmpty() ? null : snapshotResults.get(0).getTermId();
        return MajorCalcResultResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(snapshotTermId)
                .termCode(resolveTermCode(snapshotTermId))
                .resultReady(indicatorAchievements.stream().anyMatch(item -> item.getFinalAchievement() != null))
                .message(indicatorAchievements.stream().anyMatch(item -> item.getFinalAchievement() != null)
                        ? "专业级汇总结果已生成"
                        : "当前专业年级尚未生成专业级计算结果，请先完成课程级锁定并执行专业级计算。")
                .indicatorAchievements(indicatorAchievements)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveUnlock(UnlockRequestApproveRequest request, Long userId) {
        TeachingClass teachingClass = teachingClassMapper.selectById(request.getClassId());
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }
        if (!"locked".equals(teachingClass.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班不是锁定状态，不能执行解锁");
        }

        UnlockAuditLog pendingUnlock = unlockAuditLogMapper.selectOne(new LambdaQueryWrapper<UnlockAuditLog>()
                .eq(UnlockAuditLog::getClassId, request.getClassId())
                .eq(UnlockAuditLog::getApprovedBy, 0L)
                .orderByDesc(UnlockAuditLog::getUlogId)
                .last("LIMIT 1"));
        if (pendingUnlock == null) {
            throw new BusinessException(400, "当前教学班没有待处理的解锁申请");
        }

        pendingUnlock.setApprovedBy(userId);
        unlockAuditLogMapper.updateById(pendingUnlock);

        teachingClass.setCalcStatus("score_imported");
        teachingClassMapper.updateById(teachingClass);

        List<Long> affectedIpIds = ciaMapper.selectList(new LambdaQueryWrapper<CourseIndicatorAchievement>()
                        .eq(CourseIndicatorAchievement::getClassId, request.getClassId()))
                .stream()
                .map(CourseIndicatorAchievement::getIpId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        soaMapper.delete(new LambdaQueryWrapper<StudentObjectiveAchievement>()
                .eq(StudentObjectiveAchievement::getClassId, request.getClassId()));
        coaMapper.delete(new LambdaQueryWrapper<CourseObjectiveAchievement>()
                .eq(CourseObjectiveAchievement::getClassId, request.getClassId()));
        ciaMapper.delete(new LambdaQueryWrapper<CourseIndicatorAchievement>()
                .eq(CourseIndicatorAchievement::getClassId, request.getClassId()));

        if (teachingClass.getMajorId() != null
                && teachingClass.getGradeYear() != null
                && teachingClass.getTermId() != null
                && !affectedIpIds.isEmpty()) {
            miaMapper.delete(new LambdaQueryWrapper<MajorIndicatorAchievement>()
                    .eq(MajorIndicatorAchievement::getMajorId, teachingClass.getMajorId())
                    .eq(MajorIndicatorAchievement::getGradeYear, teachingClass.getGradeYear())
                    .eq(MajorIndicatorAchievement::getTermId, teachingClass.getTermId())
                    .in(MajorIndicatorAchievement::getIpId, affectedIpIds));
        }
    }

    private Major requireMajor(Long majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        return major;
    }

    private String resolveTermCode(Long termId) {
        if (termId == null) {
            return null;
        }
        AcademicTerm term = academicTermMapper.selectById(termId);
        return term == null ? null : term.getTermCode();
    }

    private Map<Long, Teacher> loadTeacherMap(List<TeachingClass> teachingClasses) {
        List<Long> teacherIds = teachingClasses.stream()
                .map(TeachingClass::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (teacherIds.isEmpty()) {
            return Map.of();
        }
        return teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, item -> item, (left, right) -> left, HashMap::new));
    }

    private Map<Long, UnlockAuditLog> loadPendingUnlockMap(List<TeachingClass> teachingClasses) {
        List<Long> classIds = teachingClasses.stream()
                .map(TeachingClass::getClassId)
                .filter(Objects::nonNull)
                .toList();
        if (classIds.isEmpty()) {
            return Map.of();
        }
        return unlockAuditLogMapper.selectList(new LambdaQueryWrapper<UnlockAuditLog>()
                        .in(UnlockAuditLog::getClassId, classIds)
                        .eq(UnlockAuditLog::getApprovedBy, 0L)
                        .orderByDesc(UnlockAuditLog::getUlogId))
                .stream()
                .collect(Collectors.toMap(
                        UnlockAuditLog::getClassId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private Map<Long, Teacher> loadRequestTeacherMap(List<UnlockAuditLog> requests) {
        List<Long> teacherIds = requests.stream()
                .map(UnlockAuditLog::getRequestBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (teacherIds.isEmpty()) {
            return Map.of();
        }
        return teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private boolean hasMajorResult(Long majorId, Integer gradeYear) {
        Long count = miaMapper.selectCount(new LambdaQueryWrapper<MajorIndicatorAchievement>()
                .eq(MajorIndicatorAchievement::getMajorId, majorId)
                .eq(MajorIndicatorAchievement::getGradeYear, gradeYear));
        return count != null && count > 0;
    }

    private List<Long> listSupportCourseIds(Long majorId, Integer gradeYear) {
        return courseMajorMapper.selectList(new LambdaQueryWrapper<CourseMajor>()
                        .eq(CourseMajor::getMajorId, majorId)
                        .eq(CourseMajor::getGradeYear, gradeYear))
                .stream()
                .map(CourseMajor::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<TeachingClass> listSupportTeachingClasses(Long majorId, Integer gradeYear, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return teachingClassMapper.selectList(new LambdaQueryWrapper<TeachingClass>()
                .in(TeachingClass::getCourseId, courseIds)
                .eq(TeachingClass::getMajorId, majorId)
                .eq(TeachingClass::getGradeYear, gradeYear)
                .orderByAsc(TeachingClass::getTermId)
                .orderByAsc(TeachingClass::getCourseId)
                .orderByAsc(TeachingClass::getClassCode));
    }

    private List<IndicatorPoint> listConfiguredIndicators(Long majorId, Integer gradeYear) {
        List<Long> grIds = graduationRequirementMapper.selectList(new LambdaQueryWrapper<GraduationRequirement>()
                        .eq(GraduationRequirement::getMajorId, majorId)
                        .eq(GraduationRequirement::getGradeYear, gradeYear)
                        .eq(GraduationRequirement::getStatus, 1)
                        .orderByAsc(GraduationRequirement::getGrId))
                .stream()
                .map(GraduationRequirement::getGrId)
                .toList();
        if (grIds.isEmpty()) {
            return List.of();
        }
        return indicatorPointMapper.selectList(new LambdaQueryWrapper<IndicatorPoint>()
                .in(IndicatorPoint::getGrId, grIds)
                .eq(IndicatorPoint::getStatus, 1)
                .orderByAsc(IndicatorPoint::getGrId)
                .orderByAsc(IndicatorPoint::getIpCode));
    }

    private List<MajorIndicatorAchievement> listLatestMajorResults(Long majorId, Integer gradeYear) {
        List<MajorIndicatorAchievement> allResults = miaMapper.selectList(new LambdaQueryWrapper<MajorIndicatorAchievement>()
                .eq(MajorIndicatorAchievement::getMajorId, majorId)
                .eq(MajorIndicatorAchievement::getGradeYear, gradeYear)
                .orderByDesc(MajorIndicatorAchievement::getUpdatedAt)
                .orderByDesc(MajorIndicatorAchievement::getTermId)
                .orderByAsc(MajorIndicatorAchievement::getIpId));
        if (allResults.isEmpty()) {
            return List.of();
        }
        Long snapshotTermId = allResults.get(0).getTermId();
        return allResults.stream()
                .filter(item -> Objects.equals(item.getTermId(), snapshotTermId))
                .sorted(Comparator.comparing(MajorIndicatorAchievement::getIpId))
                .toList();
    }

    private String resolveCourseBlockReason(String calcStatus) {
        return switch (calcStatus == null ? "unsubmitted" : calcStatus) {
            case "locked" -> null;
            case "calculating" -> "课程级结果尚未锁定";
            case "score_imported" -> "成绩已保存但尚未完成课程级计算";
            default -> "尚未提交成绩";
        };
    }
}
