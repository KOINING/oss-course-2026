package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.achievement.AssessmentFilterOptionsResponse;
import com.oss.osscourse.dto.achievement.MacroDashboardRequest;
import com.oss.osscourse.dto.achievement.MacroDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResultResponse;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.MajorIndicatorAchievement;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorIndicatorAchievementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.AssessmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    private final IndicatorPointMapper indicatorPointMapper;

    @Override
    public AssessmentFilterOptionsResponse listMajorGradeYearTerms() {
        List<AssessmentFilterOptionsResponse.MajorOption> majors = majorMapper.selectList(
                        new LambdaQueryWrapper<Major>().eq(Major::getStatus, 1).orderByAsc(Major::getMajorName))
                .stream()
                .map(major -> AssessmentFilterOptionsResponse.MajorOption.builder()
                        .majorId(major.getMajorId())
                        .majorName(major.getMajorName())
                        .build())
                .toList();

        List<Integer> gradeYears = courseMajorMapper.selectList(
                        new LambdaQueryWrapper<CourseMajor>()
                                .select(CourseMajor::getGradeYear)
                                .groupBy(CourseMajor::getGradeYear)
                                .orderByDesc(CourseMajor::getGradeYear))
                .stream()
                .map(CourseMajor::getGradeYear)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<AssessmentFilterOptionsResponse.TermOption> terms = academicTermMapper.selectList(
                        new LambdaQueryWrapper<AcademicTerm>().orderByDesc(AcademicTerm::getTermCode))
                .stream()
                .map(term -> AssessmentFilterOptionsResponse.TermOption.builder()
                        .termId(term.getTermId())
                        .termCode(term.getTermCode())
                        .build())
                .toList();

        return AssessmentFilterOptionsResponse.builder()
                .majors(majors)
                .gradeYears(gradeYears)
                .terms(terms)
                .build();
    }

    @Override
    public MacroDashboardResponse getMacroDashboard(MacroDashboardRequest request) {
        Major major = requireMajor(request.getMajorId());
        AcademicTerm term = requireTerm(request.getTermId());

        List<CourseMajor> courseMajors = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>()
                        .eq(CourseMajor::getMajorId, request.getMajorId())
                        .eq(CourseMajor::getGradeYear, request.getGradeYear()));
        List<Long> courseIds = courseMajors.stream().map(CourseMajor::getCourseId).toList();

        if (courseIds.isEmpty()) {
            return MacroDashboardResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .termId(term.getTermId())
                    .termCode(term.getTermCode())
                    .aggregationAllowed(false)
                    .unlockedWarning(true)
                    .blockReason("当前专业在该年级下没有配置支撑课程")
                    .majorResultExists(false)
                    .courses(List.of())
                    .build();
        }

        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getGradeYear, request.getGradeYear())
                        .eq(TeachingClass::getTermId, request.getTermId())
                        .orderByAsc(TeachingClass::getCourseId)
                        .orderByAsc(TeachingClass::getClassCode));

        if (teachingClasses.isEmpty()) {
            return MacroDashboardResponse.builder()
                    .majorId(major.getMajorId())
                    .majorName(major.getMajorName())
                    .gradeYear(request.getGradeYear())
                    .termId(term.getTermId())
                    .termCode(term.getTermCode())
                    .aggregationAllowed(false)
                    .unlockedWarning(true)
                    .blockReason("当前筛选条件下没有教学班数据")
                    .majorResultExists(false)
                    .courses(List.of())
                    .build();
        }

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course));
        Map<Long, Teacher> teacherMap = loadTeacherMap(teachingClasses);

        List<MacroDashboardResponse.CourseRow> courses = teachingClasses.stream()
                .map(teachingClass -> {
                    Long studentCount = studentClassMapper.selectCount(
                            new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, teachingClass.getClassId()));
                    Long scoreCount = sasMapper.selectCount(
                            new LambdaQueryWrapper<StudentAssessmentScore>().eq(StudentAssessmentScore::getClassId, teachingClass.getClassId()));
                    Course course = courseMap.get(teachingClass.getCourseId());
                    Teacher teacher = teacherMap.get(teachingClass.getTeacherId());
                    String calcStatus = teachingClass.getCalcStatus();
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
                            .build();
                })
                .toList();

        boolean aggregationAllowed = !courses.isEmpty()
                && courses.stream().allMatch(course -> "locked".equals(course.getCalcStatus()));
        return MacroDashboardResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(term.getTermId())
                .termCode(term.getTermCode())
                .aggregationAllowed(aggregationAllowed)
                .unlockedWarning(!aggregationAllowed)
                .blockReason(aggregationAllowed ? null : "存在未完成计算或未锁定的教学班")
                .majorResultExists(hasMajorResult(request.getMajorId(), request.getGradeYear(), request.getTermId()))
                .courses(courses)
                .build();
    }

    @Override
    public MajorCalcResultResponse getMajorCalcResult(MacroDashboardRequest request) {
        Major major = requireMajor(request.getMajorId());
        AcademicTerm term = requireTerm(request.getTermId());

        List<MajorIndicatorAchievement> results = miaMapper.selectList(
                new LambdaQueryWrapper<MajorIndicatorAchievement>()
                        .eq(MajorIndicatorAchievement::getMajorId, request.getMajorId())
                        .eq(MajorIndicatorAchievement::getGradeYear, request.getGradeYear())
                        .eq(MajorIndicatorAchievement::getTermId, request.getTermId())
                        .orderByAsc(MajorIndicatorAchievement::getIpId));

        List<MajorCalcResponse.IndicatorAchievement> indicatorAchievements = results.stream()
                .map(item -> {
                    IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(item.getIpId());
                    return MajorCalcResponse.IndicatorAchievement.builder()
                            .ipId(item.getIpId())
                            .ipCode(indicatorPoint == null ? "" : indicatorPoint.getIpCode())
                            .ipDescription(indicatorPoint == null ? "" : indicatorPoint.getIpDescription())
                            .finalAchievement(item.getFinalAchievement())
                            .build();
                })
                .toList();

        return MajorCalcResultResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(term.getTermId())
                .termCode(term.getTermCode())
                .resultReady(!indicatorAchievements.isEmpty())
                .message(indicatorAchievements.isEmpty() ? "当前筛选条件下暂无专业级汇总结果" : "专业级汇总结果已生成")
                .indicatorAchievements(indicatorAchievements)
                .build();
    }

    private Major requireMajor(Long majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        return major;
    }

    private AcademicTerm requireTerm(Long termId) {
        AcademicTerm term = academicTermMapper.selectById(termId);
        if (term == null) {
            throw new BusinessException(404, "学期不存在");
        }
        return term;
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
                .collect(Collectors.toMap(Teacher::getId, teacher -> teacher, (left, right) -> left, HashMap::new));
    }

    private boolean hasMajorResult(Long majorId, Integer gradeYear, Long termId) {
        Long count = miaMapper.selectCount(new LambdaQueryWrapper<MajorIndicatorAchievement>()
                .eq(MajorIndicatorAchievement::getMajorId, majorId)
                .eq(MajorIndicatorAchievement::getGradeYear, gradeYear)
                .eq(MajorIndicatorAchievement::getTermId, termId));
        return count != null && count > 0;
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
