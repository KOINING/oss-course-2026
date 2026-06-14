package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.report.CourseReportRequest;
import com.oss.osscourse.dto.report.CourseReportResponse;
import com.oss.osscourse.dto.report.CourseReportResponse.*;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportResponse;
import com.oss.osscourse.dto.teachercontext.TeacherClassStudentResponse;
import com.oss.osscourse.entity.*;
import com.oss.osscourse.mapper.*;
import com.oss.osscourse.service.CourseReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseReportServiceImpl implements CourseReportService {

    private final CourseMapper courseMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final AssessmentPointMapper assessmentPointMapper;
    private final CourseObjectiveAchievementMapper coaMapper;
    private final CourseIndicatorAchievementMapper ciaMapper;
    private final IndicatorPointMapper indicatorPointMapper;
    private final StudentAssessmentScoreMapper sasMapper;
    private final StudentClassMapper studentClassMapper;
    private final ObjectiveIndicatorContributionMapper oicMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final MajorMapper majorMapper;
    private final GraduationRequirementMapper graduationRequirementMapper;
    private final CourseIndicatorSupportMapper courseIndicatorSupportMapper;
    private final TeacherMapper teacherMapper;

/*
    @Override
    public CourseReportResponse getCourseReport(CourseReportRequest request) {
        // 1. 验证课程存在
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 2. 查询该课程在该年级下的所有教学班
        Long majorId = resolveReportMajorId(request.getCourseId(), request.getGradeYear(), request.getMajorId());
        Major major = majorId == null ? null : majorMapper.selectById(majorId);
        List<IndicatorPoint> scopedIndicators = listScopedIndicators(request.getCourseId(), majorId, request.getGradeYear());
        Set<Long> scopedIndicatorIds = scopedIndicators.stream()
                .map(IndicatorPoint::getIpId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, IndicatorPoint> indicatorPointMap = scopedIndicators.stream()
                .collect(Collectors.toMap(IndicatorPoint::getIpId, item -> item, (left, right) -> left, LinkedHashMap::new));

        LambdaQueryWrapper<TeachingClass> tcWrapper = new LambdaQueryWrapper<>();
        tcWrapper.eq(TeachingClass::getCourseId, request.getCourseId())
                 .eq(TeachingClass::getGradeYear, request.getGradeYear());
        if (request.getTermId() != null) {
            tcWrapper.eq(TeachingClass::getTermId, request.getTermId());
        }
        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(tcWrapper).stream()
                .filter(item -> classMatchesProgram(item.getClassId(), majorId, request.getGradeYear()))
                .collect(Collectors.toList());

        if (teachingClasses.isEmpty()) {
            throw new BusinessException(404, "该课程在该年级下没有教学班");
        }

        // 3. 获取课程目标
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, request.getCourseId()));
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).collect(Collectors.toList());

        // 4. 获取考核点
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>()
                        .in(!coIds.isEmpty(), AssessmentPoint::getCoId, coIds));

        // 5. 构建教学班报表
        List<AssessmentPointHeader> assessmentPointHeaders = assessmentPoints.stream()
                .map(ap -> AssessmentPointHeader.builder()
                        .apId(ap.getApId())
                        .apName(ap.getApName())
                        .fullScore(ap.getFullScore())
                        .build())
                .collect(Collectors.toList());
        List<TeachingClassReport> classReports = new ArrayList<>();
        List<ClassSummary> classSummaries = new ArrayList<>();
        List<ClassScoreSummary> classScoreSummaries = new ArrayList<>();
        Map<Long, List<ClassAchievement>> coAchievementMap = new LinkedHashMap<>();
        Map<Long, List<ClassAchievement>> ipAchievementMap = new LinkedHashMap<>();

        for (TeachingClass tc : teachingClasses) {
            // 获取学生人数
            List<TeacherClassStudentResponse> students = studentClassMapper.selectStudentsByClassId(tc.getClassId());
            int studentCount = students == null ? 0 : students.size();

            // 获取各考核点平均分
            List<AssessmentPointAverage> apAverages = new ArrayList<>();
            Map<Long, Float> apAverageMap = new LinkedHashMap<>();
            for (AssessmentPoint ap : assessmentPoints) {
                // 查询该考核点的所有成绩
                List<StudentAssessmentScore> scores = sasMapper.selectList(
                        new LambdaQueryWrapper<StudentAssessmentScore>()
                                .eq(StudentAssessmentScore::getClassId, tc.getClassId())
                                .eq(StudentAssessmentScore::getApId, ap.getApId()));

                float averageScore = 0;
                if (!scores.isEmpty()) {
                    float sum = scores.stream().map(StudentAssessmentScore::getActualScore)
                            .reduce(0f, Float::sum);
                    averageScore = sum / scores.size();
                }

                float scoreRate = ap.getFullScore() > 0 ? averageScore / ap.getFullScore() : 0;

                apAverages.add(AssessmentPointAverage.builder()
                        .apId(ap.getApId())
                        .apName(ap.getApName())
                        .fullScore(ap.getFullScore())
                        .averageScore(averageScore)
                        .scoreRate(scoreRate)
                        .build());
                apAverageMap.put(ap.getApId(), averageScore);
            }

            // 获取课程目标达成度明细
            List<ObjectiveAchievementDetail> coDetails = new ArrayList<>();
            List<CourseObjectiveAchievement> coaList = coaMapper.selectList(
                    new LambdaQueryWrapper<CourseObjectiveAchievement>()
                            .eq(CourseObjectiveAchievement::getClassId, tc.getClassId()));

            Map<Long, CourseObjectiveAchievement> coaMap = coaList.stream()
                    .collect(Collectors.toMap(CourseObjectiveAchievement::getCoId, c -> c));

            for (CourseObjective co : objectives) {
                CourseObjectiveAchievement coa = coaMap.get(co.getCoId());
                float achievement = coa != null ? coa.getAverageAchievement() : 0;

                ObjectiveAchievementDetail detail = ObjectiveAchievementDetail.builder()
                        .coId(co.getCoId())
                        .objectiveCode(co.getObjectiveCode())
                        .description(co.getCoDescription())
                        .averageAchievement(achievement)
                        .build();
                coDetails.add(detail);

                // 收集用于汇总
                coAchievementMap.computeIfAbsent(co.getCoId(), k -> new ArrayList<>())
                        .add(ClassAchievement.builder()
                                .classId(tc.getClassId())
                                .className(tc.getClassName())
                                .achievement(achievement)
                                .build());
            }

            // 获取课程级指标点达成度
            List<IndicatorAchievementDetail> ipDetails = new ArrayList<>();
            List<CourseIndicatorAchievement> ciaList = ciaMapper.selectList(
                    new LambdaQueryWrapper<CourseIndicatorAchievement>()
                            .eq(CourseIndicatorAchievement::getClassId, tc.getClassId()));

            for (CourseIndicatorAchievement cia : ciaList) {
                if (!scopedIndicatorIds.isEmpty() && !scopedIndicatorIds.contains(cia.getIpId())) {
                    continue;
                }
                IndicatorPoint ip = indicatorPointMap.containsKey(cia.getIpId())
                        ? indicatorPointMap.get(cia.getIpId())
                        : indicatorPointMapper.selectById(cia.getIpId());
                if (ip != null) {
                    indicatorPointMap.putIfAbsent(ip.getIpId(), ip);
                }
                IndicatorAchievementDetail detail = IndicatorAchievementDetail.builder()
                        .ipId(cia.getIpId())
                        .ipCode(ip != null ? ip.getIpCode() : "")
                        .ipDescription(ip != null ? ip.getIpDescription() : "")
                        .achievement(cia.getAchievement())
                        .build();
                ipDetails.add(detail);

                // 收集用于汇总
                ipAchievementMap.computeIfAbsent(cia.getIpId(), k -> new ArrayList<>())
                        .add(ClassAchievement.builder()
                                .classId(tc.getClassId())
                                .className(tc.getClassName())
                                .achievement(cia.getAchievement())
                                .build());
            }

            classReports.add(TeachingClassReport.builder()
                    .classId(tc.getClassId())
                    .classCode(tc.getClassCode())
                    .className(tc.getClassName())
                    .termCode("") // 需要查询学期表
                    .studentCount(studentCount)
                    .calcStatus(tc.getCalcStatus())
                    .assessmentPointAverages(apAverages)
                    .objectiveAchievementDetails(coDetails)
                    .indicatorAchievementDetails(ipDetails)
                    .build());
            classSummaries.add(ClassSummary.builder()
                    .classId(tc.getClassId())
                    .classCode(tc.getClassCode())
                    .className(tc.getClassName())
                    .termCode("")
                    .studentCount(studentCount)
                    .calcStatus(tc.getCalcStatus())
                    .build());
            classScoreSummaries.add(ClassScoreSummary.builder()
                    .classId(tc.getClassId())
                    .classCode(tc.getClassCode())
                    .className(tc.getClassName())
                    .termCode("")
                    .studentCount(studentCount)
                    .calcStatus(tc.getCalcStatus())
                    .apAverages(apAverageMap)
                    .build());
        }

        // 6. 构建课程目标达成度汇总
        List<ObjectiveAchievementSummary> coSummaries = new ArrayList<>();
        for (CourseObjective co : objectives) {
            List<ClassAchievement> classAchievements = coAchievementMap.getOrDefault(co.getCoId(), List.of());
                            .classId(null) // 可以从上下文获取

            float avgAchievement = details.isEmpty() ? 0 :
                    (float) details.stream().mapToDouble(ObjectiveAchievementDetail::getAverageAchievement).average().orElse(0);

            coSummaries.add(ObjectiveAchievementSummary.builder()
                    .coId(co.getCoId())
                    .objectiveCode(co.getObjectiveCode())
                    .description(co.getCoDescription())
                    .classAchievements(classAchievements)
                    .averageAchievement(avgAchievement)
                    .build());
        }

        // 7. 构建课程级指标点达成度汇总
        List<IndicatorAchievementSummary> ipSummaries = new ArrayList<>();
        for (Map.Entry<Long, List<IndicatorAchievementDetail>> entry : ipAchievementMap.entrySet()) {
            Long ipId = entry.getKey();
            List<IndicatorAchievementDetail> details = entry.getValue();

            IndicatorPoint ip = indicatorPointMapper.selectById(ipId);
            List<ClassAchievement> classAchievements = details.stream()
                    .map(d -> ClassAchievement.builder()
                            .className("")
                            .achievement(d.getAchievement())
                            .build())
                    .collect(Collectors.toList());

            float avgAchievement = details.isEmpty() ? 0 :
                    (float) details.stream().mapToDouble(IndicatorAchievementDetail::getAchievement).average().orElse(0);

            ipSummaries.add(IndicatorAchievementSummary.builder()
                    .ipId(ipId)
                    .ipCode(ip != null ? ip.getIpCode() : "")
                    .ipDescription(ip != null ? ip.getIpDescription() : "")
                    .classAchievements(classAchievements)
                    .averageAchievement(avgAchievement)
                    .build());
        }

        return CourseReportResponse.builder()
                .courseId(request.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .gradeYear(request.getGradeYear())
                .credit(course.getCredit())
                .teachingClasses(classReports)
                .objectiveAchievements(coSummaries)
                .indicatorAchievements(ipSummaries)
                .build();
    }

*/

    @Override
    public CourseReportResponse getCourseReport(CourseReportRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }

        Long majorId = resolveReportMajorId(request.getCourseId(), request.getGradeYear(), request.getMajorId());
        Major major = majorId == null ? null : majorMapper.selectById(majorId);
        List<IndicatorPoint> scopedIndicators = listScopedIndicators(request.getCourseId(), majorId, request.getGradeYear());
        Set<Long> scopedIndicatorIds = scopedIndicators.stream()
                .map(IndicatorPoint::getIpId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, IndicatorPoint> indicatorPointMap = scopedIndicators.stream()
                .collect(Collectors.toMap(IndicatorPoint::getIpId, item -> item, (left, right) -> left, LinkedHashMap::new));

        LambdaQueryWrapper<TeachingClass> tcWrapper = new LambdaQueryWrapper<>();
        tcWrapper.eq(TeachingClass::getCourseId, request.getCourseId())
                .eq(TeachingClass::getGradeYear, request.getGradeYear());
        if (request.getTermId() != null) {
            tcWrapper.eq(TeachingClass::getTermId, request.getTermId());
        }
        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(tcWrapper).stream()
                .filter(item -> classMatchesProgram(item.getClassId(), majorId, request.getGradeYear()))
                .collect(Collectors.toList());
        if (teachingClasses.isEmpty()) {
            throw new BusinessException(404, "No teaching class matches the selected course, major and grade");
        }
        String teacherName = teacherMapper.selectBatchIds(
                        teachingClasses.stream()
                                .map(TeachingClass::getTeacherId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList()))
                .stream()
                .map(Teacher::getTeacherName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.joining("、"));

        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, request.getCourseId()));
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).collect(Collectors.toList());
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>()
                        .in(!coIds.isEmpty(), AssessmentPoint::getCoId, coIds));
        List<AssessmentPointHeader> assessmentPointHeaders = assessmentPoints.stream()
                .map(ap -> AssessmentPointHeader.builder()
                        .apId(ap.getApId())
                        .apName(ap.getApName())
                        .fullScore(ap.getFullScore())
                        .build())
                .collect(Collectors.toList());

        List<TeachingClassReport> classReports = new ArrayList<>();
        List<ClassSummary> classSummaries = new ArrayList<>();
        List<ClassScoreSummary> classScoreSummaries = new ArrayList<>();
        Map<Long, List<ClassAchievement>> coAchievementMap = new LinkedHashMap<>();
        Map<Long, List<ClassAchievement>> ipAchievementMap = new LinkedHashMap<>();

        for (TeachingClass teachingClass : teachingClasses) {
            List<TeacherClassStudentResponse> students = studentClassMapper.selectStudentsByClassId(teachingClass.getClassId());
            int studentCount = students == null ? 0 : students.size();

            List<AssessmentPointAverage> apAverages = new ArrayList<>();
            Map<Long, Float> apAverageMap = new LinkedHashMap<>();
            for (AssessmentPoint assessmentPoint : assessmentPoints) {
                List<StudentAssessmentScore> scores = sasMapper.selectList(
                        new LambdaQueryWrapper<StudentAssessmentScore>()
                                .eq(StudentAssessmentScore::getClassId, teachingClass.getClassId())
                                .eq(StudentAssessmentScore::getApId, assessmentPoint.getApId()));
                float averageScore = 0;
                if (!scores.isEmpty()) {
                    float sum = scores.stream()
                            .map(StudentAssessmentScore::getActualScore)
                            .reduce(0f, Float::sum);
                    averageScore = sum / scores.size();
                }
                float scoreRate = assessmentPoint.getFullScore() > 0 ? averageScore / assessmentPoint.getFullScore() : 0;
                apAverages.add(AssessmentPointAverage.builder()
                        .apId(assessmentPoint.getApId())
                        .apName(assessmentPoint.getApName())
                        .fullScore(assessmentPoint.getFullScore())
                        .averageScore(averageScore)
                        .scoreRate(scoreRate)
                        .build());
                apAverageMap.put(assessmentPoint.getApId(), averageScore);
            }

            List<CourseObjectiveAchievement> coaList = coaMapper.selectList(
                    new LambdaQueryWrapper<CourseObjectiveAchievement>()
                            .eq(CourseObjectiveAchievement::getClassId, teachingClass.getClassId()));
            Map<Long, CourseObjectiveAchievement> coaMap = coaList.stream()
                    .collect(Collectors.toMap(CourseObjectiveAchievement::getCoId, item -> item, (left, right) -> right));
            List<ObjectiveAchievementDetail> objectiveDetails = new ArrayList<>();
            for (CourseObjective objective : objectives) {
                CourseObjectiveAchievement achievement = coaMap.get(objective.getCoId());
                float averageAchievement = achievement == null ? 0 : achievement.getAverageAchievement();
                objectiveDetails.add(ObjectiveAchievementDetail.builder()
                        .coId(objective.getCoId())
                        .objectiveCode(objective.getObjectiveCode())
                        .description(objective.getCoDescription())
                        .averageAchievement(averageAchievement)
                        .build());
                coAchievementMap.computeIfAbsent(objective.getCoId(), key -> new ArrayList<>())
                        .add(ClassAchievement.builder()
                                .classId(teachingClass.getClassId())
                                .className(teachingClass.getClassName())
                                .achievement(averageAchievement)
                                .build());
            }

            List<CourseIndicatorAchievement> ciaList = ciaMapper.selectList(
                    new LambdaQueryWrapper<CourseIndicatorAchievement>()
                            .eq(CourseIndicatorAchievement::getClassId, teachingClass.getClassId()));
            List<IndicatorAchievementDetail> indicatorDetails = new ArrayList<>();
            for (CourseIndicatorAchievement achievement : ciaList) {
                if (!scopedIndicatorIds.isEmpty() && !scopedIndicatorIds.contains(achievement.getIpId())) {
                    continue;
                }
                IndicatorPoint indicatorPoint = indicatorPointMap.get(achievement.getIpId());
                if (indicatorPoint == null) {
                    indicatorPoint = indicatorPointMapper.selectById(achievement.getIpId());
                    if (indicatorPoint != null) {
                        indicatorPointMap.put(indicatorPoint.getIpId(), indicatorPoint);
                    }
                }
                indicatorDetails.add(IndicatorAchievementDetail.builder()
                        .ipId(achievement.getIpId())
                        .ipCode(indicatorPoint == null ? "" : indicatorPoint.getIpCode())
                        .ipDescription(indicatorPoint == null ? "" : indicatorPoint.getIpDescription())
                        .achievement(achievement.getAchievement())
                        .build());
                ipAchievementMap.computeIfAbsent(achievement.getIpId(), key -> new ArrayList<>())
                        .add(ClassAchievement.builder()
                                .classId(teachingClass.getClassId())
                                .className(teachingClass.getClassName())
                                .achievement(achievement.getAchievement())
                                .build());
            }

            classReports.add(TeachingClassReport.builder()
                    .classId(teachingClass.getClassId())
                    .classCode(teachingClass.getClassCode())
                    .className(teachingClass.getClassName())
                    .termCode("")
                    .studentCount(studentCount)
                    .calcStatus(teachingClass.getCalcStatus())
                    .assessmentPointAverages(apAverages)
                    .objectiveAchievementDetails(objectiveDetails)
                    .indicatorAchievementDetails(indicatorDetails)
                    .build());
            classSummaries.add(ClassSummary.builder()
                    .classId(teachingClass.getClassId())
                    .classCode(teachingClass.getClassCode())
                    .className(teachingClass.getClassName())
                    .termCode("")
                    .studentCount(studentCount)
                    .calcStatus(teachingClass.getCalcStatus())
                    .build());
            classScoreSummaries.add(ClassScoreSummary.builder()
                    .classId(teachingClass.getClassId())
                    .classCode(teachingClass.getClassCode())
                    .className(teachingClass.getClassName())
                    .termCode("")
                    .studentCount(studentCount)
                    .calcStatus(teachingClass.getCalcStatus())
                    .apAverages(apAverageMap)
                    .build());
        }

        List<ObjectiveAchievementSummary> objectiveSummaries = new ArrayList<>();
        for (CourseObjective objective : objectives) {
            List<ClassAchievement> classAchievements = coAchievementMap.getOrDefault(objective.getCoId(), List.of());
            float courseAverage = classAchievements.isEmpty() ? 0
                    : (float) classAchievements.stream().mapToDouble(ClassAchievement::getAchievement).average().orElse(0);
            objectiveSummaries.add(ObjectiveAchievementSummary.builder()
                    .coId(objective.getCoId())
                    .objectiveCode(objective.getObjectiveCode())
                    .description(objective.getCoDescription())
                    .classAchievements(classAchievements)
                    .courseAverage(courseAverage)
                    .averageAchievement(courseAverage)
                    .build());
        }

        List<IndicatorAchievementSummary> indicatorSummaries = new ArrayList<>();
        Collection<IndicatorPoint> summaryIndicators = scopedIndicators.isEmpty() ? indicatorPointMap.values() : scopedIndicators;
        for (IndicatorPoint indicatorPoint : summaryIndicators) {
            if (indicatorPoint == null || indicatorPoint.getIpId() == null) {
                continue;
            }
            List<ClassAchievement> classAchievements = ipAchievementMap.getOrDefault(indicatorPoint.getIpId(), List.of());
            float courseAchievement = classAchievements.isEmpty() ? 0
                    : (float) classAchievements.stream().mapToDouble(ClassAchievement::getAchievement).average().orElse(0);
            indicatorSummaries.add(IndicatorAchievementSummary.builder()
                    .ipId(indicatorPoint.getIpId())
                    .ipCode(indicatorPoint.getIpCode())
                    .ipDescription(indicatorPoint.getIpDescription())
                    .classAchievements(classAchievements)
                    .courseAchievement(courseAchievement)
                    .averageAchievement(courseAchievement)
                    .build());
        }

        return CourseReportResponse.builder()
                .courseId(request.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .gradeYear(request.getGradeYear())
                .majorId(majorId)
                .credit(course.getCredit())
                .majorName(major == null ? "" : nvl(major.getMajorName()))
                .teacherName(teacherName)
                .classCount(classSummaries.size())
                .classSummaries(classSummaries)
                .assessmentPoints(assessmentPointHeaders)
                .classScoreSummaries(classScoreSummaries)
                .teachingClasses(classReports)
                .objectiveAchievements(objectiveSummaries)
                .indicatorAchievements(indicatorSummaries)
                .build();
    }

    @Override
    public byte[] exportCourseReportExcel(CourseReportRequest request) {
        CourseReportResponse report = getCourseReport(request);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("\u8bfe\u7a0b\u7ea7\u8bc4\u4ef7\u62a5\u8868");
            int lastColumn = 5;

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle sectionStyle = createSectionStyle(workbook);
            CellStyle subSectionStyle = createSubSectionStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoLabelStyle = createInfoLabelStyle(workbook);
            CellStyle infoValueStyle = createInfoValueStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centeredDataStyle = createCenteredDataStyle(workbook);
            CellStyle emphasisStyle = createEmphasisStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeMergedTitleRow(sheet, rowIndex, "\u8bfe\u7a0b\u7ea7\u8bc4\u4ef7\u62a5\u8868", titleStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "\u8bfe\u7a0b\u4ee3\u7801", report.getCourseCode(), "\u8bfe\u7a0b\u540d\u79f0", report.getCourseName(), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "\u5e74\u7ea7", report.getGradeYear(), "\u5b66\u5206", report.getCredit(), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "\u4e13\u4e1a", report.getMajorName(), "\u6d89\u53ca\u6559\u5b66\u73ed", safeSize(report.getTeachingClasses()), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex++;

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "\u4e00\u3001\u5404\u6559\u5b66\u73ed\u660e\u7ec6", sectionStyle, lastColumn);
            for (TeachingClassReport teachingClass : safeList(report.getTeachingClasses())) {
                rowIndex = writeMergedTitleRow(sheet, rowIndex, "\u6559\u5b66\u73ed\uff1a" + nvl(teachingClass.getClassName()) + "    \u5b66\u751f\u6570\uff1a" + nvl(teachingClass.getStudentCount()), subSectionStyle, lastColumn);

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "1. \u8003\u6838\u70b9\u5e73\u5747\u5206", subSectionStyle, lastColumn);
                Row apHeaderRow = sheet.createRow(rowIndex++);
                writeCell(apHeaderRow.createCell(0), "\u8003\u6838\u70b9", headerStyle);
                writeCell(apHeaderRow.createCell(1), "\u6ee1\u5206", headerStyle);
                writeCell(apHeaderRow.createCell(2), "\u5e73\u5747\u5206", headerStyle);
                writeCell(apHeaderRow.createCell(3), "\u5f97\u5206\u7387", headerStyle);
                mergeCells(sheet, apHeaderRow.getRowNum(), apHeaderRow.getRowNum(), 3, 5, headerStyle);
                for (AssessmentPointAverage ap : safeList(teachingClass.getAssessmentPointAverages())) {
                    Row apRow = sheet.createRow(rowIndex++);
                    writeCell(apRow.createCell(0), ap.getApName(), dataStyle);
                    writeCell(apRow.createCell(1), ap.getFullScore(), centeredDataStyle);
                    writeCell(apRow.createCell(2), ap.getAverageScore(), centeredDataStyle);
                    writeCell(apRow.createCell(3), formatPercent(ap.getScoreRate()), centeredDataStyle);
                    mergeCells(sheet, apRow.getRowNum(), apRow.getRowNum(), 3, 5, centeredDataStyle);
                }
                if (safeList(teachingClass.getAssessmentPointAverages()).isEmpty()) {
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "\u6682\u65e0\u8003\u6838\u70b9\u5e73\u5747\u5206\u6570\u636e", dataStyle, lastColumn);
                }
                rowIndex++;

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "2. \u8bfe\u7a0b\u76ee\u6807\u8fbe\u6210\u5ea6", subSectionStyle, lastColumn);
                Row objectiveHeaderRow = sheet.createRow(rowIndex++);
                writeCell(objectiveHeaderRow.createCell(0), "\u76ee\u6807\u7f16\u53f7", headerStyle);
                writeCell(objectiveHeaderRow.createCell(1), "\u76ee\u6807\u540d\u79f0", headerStyle);
                writeCell(objectiveHeaderRow.createCell(2), "\u8fbe\u6210\u5ea6", headerStyle);
                mergeCells(sheet, objectiveHeaderRow.getRowNum(), objectiveHeaderRow.getRowNum(), 2, 5, headerStyle);
                for (ObjectiveAchievementDetail objective : safeList(teachingClass.getObjectiveAchievementDetails())) {
                    Row objectiveRow = sheet.createRow(rowIndex++);
                    writeCell(objectiveRow.createCell(0), objective.getObjectiveCode(), centeredDataStyle);
                    writeCell(objectiveRow.createCell(1), firstNonBlank(objective.getObjectiveName(), objective.getDescription()), dataStyle);
                    writeCell(objectiveRow.createCell(2), formatPercent(objective.getAverageAchievement()), centeredDataStyle);
                    mergeCells(sheet, objectiveRow.getRowNum(), objectiveRow.getRowNum(), 2, 5, centeredDataStyle);
                }
                if (safeList(teachingClass.getObjectiveAchievementDetails()).isEmpty()) {
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "\u6682\u65e0\u8bfe\u7a0b\u76ee\u6807\u8fbe\u6210\u5ea6\u6570\u636e", dataStyle, lastColumn);
                }
                rowIndex++;

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "3. \u8bfe\u7a0b\u7ea7\u6307\u6807\u70b9\u8fbe\u6210\u5ea6", subSectionStyle, lastColumn);
                Row indicatorHeaderRow = sheet.createRow(rowIndex++);
                writeCell(indicatorHeaderRow.createCell(0), "\u6307\u6807\u70b9\u7f16\u53f7", headerStyle);
                writeCell(indicatorHeaderRow.createCell(1), "\u6307\u6807\u70b9\u63cf\u8ff0", headerStyle);
                writeCell(indicatorHeaderRow.createCell(2), "\u8fbe\u6210\u5ea6", headerStyle);
                mergeCells(sheet, indicatorHeaderRow.getRowNum(), indicatorHeaderRow.getRowNum(), 2, 5, headerStyle);
                for (IndicatorAchievementDetail indicator : safeList(teachingClass.getIndicatorAchievementDetails())) {
                    Row indicatorRow = sheet.createRow(rowIndex++);
                    writeCell(indicatorRow.createCell(0), indicator.getIpCode(), centeredDataStyle);
                    writeCell(indicatorRow.createCell(1), indicator.getIpDescription(), dataStyle);
                    writeCell(indicatorRow.createCell(2), formatPercent(indicator.getAchievement()), centeredDataStyle);
                    mergeCells(sheet, indicatorRow.getRowNum(), indicatorRow.getRowNum(), 2, 5, centeredDataStyle);
                }
                if (safeList(teachingClass.getIndicatorAchievementDetails()).isEmpty()) {
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "\u6682\u65e0\u8bfe\u7a0b\u7ea7\u6307\u6807\u70b9\u8fbe\u6210\u5ea6\u6570\u636e", dataStyle, lastColumn);
                }
                rowIndex++;
            }

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "\u4e8c\u3001\u6c47\u603b\u7ed3\u679c", sectionStyle, lastColumn);
            rowIndex = writeMergedTitleRow(sheet, rowIndex, "1. \u8bfe\u7a0b\u76ee\u6807\u8fbe\u6210\u5ea6\u6c47\u603b", subSectionStyle, lastColumn);
            Row objectiveSummaryHeaderRow = sheet.createRow(rowIndex++);
            writeCell(objectiveSummaryHeaderRow.createCell(0), "\u76ee\u6807\u7f16\u53f7", headerStyle);
            writeCell(objectiveSummaryHeaderRow.createCell(1), "\u76ee\u6807\u540d\u79f0", headerStyle);
            writeCell(objectiveSummaryHeaderRow.createCell(2), "\u5e73\u5747\u8fbe\u6210\u5ea6", headerStyle);
            mergeCells(sheet, objectiveSummaryHeaderRow.getRowNum(), objectiveSummaryHeaderRow.getRowNum(), 2, 5, headerStyle);
            for (ObjectiveAchievementSummary objective : safeList(report.getObjectiveAchievements())) {
                Row objectiveRow = sheet.createRow(rowIndex++);
                writeCell(objectiveRow.createCell(0), objective.getObjectiveCode(), centeredDataStyle);
                writeCell(objectiveRow.createCell(1), firstNonBlank(objective.getObjectiveName(), objective.getDescription()), dataStyle);
                writeCell(objectiveRow.createCell(2), formatPercent(firstNonNull(objective.getCourseAverage(), objective.getAverageAchievement())), emphasisStyle);
                mergeCells(sheet, objectiveRow.getRowNum(), objectiveRow.getRowNum(), 2, 5, emphasisStyle);
            }
            if (safeList(report.getObjectiveAchievements()).isEmpty()) {
                rowIndex = writeEmptyHintRow(sheet, rowIndex, "\u6682\u65e0\u8bfe\u7a0b\u76ee\u6807\u6c47\u603b\u6570\u636e", dataStyle, lastColumn);
            }
            rowIndex++;

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "2. \u8bfe\u7a0b\u7ea7\u6307\u6807\u70b9\u8fbe\u6210\u5ea6\u6c47\u603b", subSectionStyle, lastColumn);
            Row indicatorSummaryHeaderRow = sheet.createRow(rowIndex++);
            writeCell(indicatorSummaryHeaderRow.createCell(0), "\u6307\u6807\u70b9\u7f16\u53f7", headerStyle);
            writeCell(indicatorSummaryHeaderRow.createCell(1), "\u6307\u6807\u70b9\u63cf\u8ff0", headerStyle);
            writeCell(indicatorSummaryHeaderRow.createCell(2), "\u5e73\u5747\u8fbe\u6210\u5ea6", headerStyle);
            mergeCells(sheet, indicatorSummaryHeaderRow.getRowNum(), indicatorSummaryHeaderRow.getRowNum(), 2, 5, headerStyle);
            for (IndicatorAchievementSummary indicator : safeList(report.getIndicatorAchievements())) {
                Row indicatorRow = sheet.createRow(rowIndex++);
                writeCell(indicatorRow.createCell(0), indicator.getIpCode(), centeredDataStyle);
                writeCell(indicatorRow.createCell(1), indicator.getIpDescription(), dataStyle);
                writeCell(indicatorRow.createCell(2), formatPercent(firstNonNull(indicator.getCourseAchievement(), indicator.getAverageAchievement())), emphasisStyle);
                mergeCells(sheet, indicatorRow.getRowNum(), indicatorRow.getRowNum(), 2, 5, emphasisStyle);
            }
            if (safeList(report.getIndicatorAchievements()).isEmpty()) {
                rowIndex = writeEmptyHintRow(sheet, rowIndex, "\u6682\u65e0\u8bfe\u7a0b\u7ea7\u6307\u6807\u70b9\u6c47\u603b\u6570\u636e", dataStyle, lastColumn);
            }

            applySheetLayout(sheet);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "\u751f\u6210Excel\u62a5\u8868\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @Override
    public byte[] exportCourseReportPdf(CourseReportRequest request) {
        CourseReportResponse report = getCourseReport(request);

        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            // 加载中文字体（使用TTF格式的Noto Sans SC字体）
            java.io.InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/NotoSansSC-Regular.ttf");
            if (fontStream == null) {
                throw new BusinessException(500, "未找到中文字体文件");
            }

            // 使用PDType0Font.load加载TTF字体
            org.apache.pdfbox.pdmodel.font.PDFont font = org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontStream, false);
            fontStream.close();

            // 粗体使用同一字体（Noto Sans SC是可变字体，这里简化处理）
            org.apache.pdfbox.pdmodel.font.PDFont boldFont = font;

            // 创建第一页
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                    org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            document.addPage(page);

            org.apache.pdfbox.pdmodel.PDPageContentStream contentStream =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            float lineHeight = 22;

            // 标题
            contentStream.setFont(boldFont, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("课程级评价报表");
            contentStream.endText();
            yPosition -= lineHeight;

            // 课程基本信息
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("课程编码：" + report.getCourseCode() + "    课程名称：" + report.getCourseName());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("专业：" + nvl(report.getMajorName()) + "    授课教师：" + nvl(report.getTeacherName()));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("年级：" + report.getGradeYear() + "    学分：" + report.getCredit());
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // 各教学班明细
            for (TeachingClassReport tc : report.getTeachingClasses()) {
                // 检查是否需要新页面
                if (yPosition < 150) {
                    contentStream.close();
                    page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - 50;
                }

                // 教学班标题
                contentStream.setFont(boldFont, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("教学班：" + tc.getClassName());
                contentStream.endText();
                yPosition -= lineHeight;

                // 考核点平均分表头
                float assessmentNameX = margin;
                float fullScoreX = margin + 260;
                float averageScoreX = margin + 340;
                float scoreRateX = margin + 430;
                drawPdfText(contentStream, boldFont, 10, "考核点", assessmentNameX, yPosition);
                drawPdfRightAlignedText(contentStream, boldFont, 10, "满分", fullScoreX, yPosition);
                drawPdfRightAlignedText(contentStream, boldFont, 10, "平均分", averageScoreX, yPosition);
                drawPdfRightAlignedText(contentStream, boldFont, 10, "得分率", scoreRateX, yPosition);
                yPosition -= lineHeight;

                // 考核点数据
                for (AssessmentPointAverage ap : tc.getAssessmentPointAverages()) {
                    String apName = truncatePdfText(ap.getApName(), 20);
                    drawPdfText(contentStream, font, 10, apName, assessmentNameX, yPosition);
                    drawPdfRightAlignedText(contentStream, font, 10,
                            String.format(Locale.ROOT, "%.1f", ap.getFullScore()), fullScoreX, yPosition);
                    drawPdfRightAlignedText(contentStream, font, 10,
                            String.format(Locale.ROOT, "%.1f", ap.getAverageScore()), averageScoreX, yPosition);
                    drawPdfRightAlignedText(contentStream, font, 10,
                            String.format(Locale.ROOT, "%.4f", ap.getScoreRate()), scoreRateX, yPosition);
                    yPosition -= lineHeight;
                }
                yPosition -= lineHeight;

                // 课程目标达成度
                contentStream.setFont(boldFont, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("课程目标达成度：");
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.setFont(font, 10);
                for (ObjectiveAchievementDetail co : tc.getObjectiveAchievementDetails()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    String desc = co.getDescription();
                    if (desc.length() > 40) {
                        desc = desc.substring(0, 37) + "...";
                    }
                    contentStream.showText(String.format("%s - %s：%.4f",
                            co.getObjectiveCode(), desc,
                            co.getAverageAchievement()));
                    contentStream.endText();
                    yPosition -= lineHeight;
                }
                yPosition -= lineHeight;

                // 课程级指标点达成度
                contentStream.setFont(boldFont, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("课程级指标点达成度：");
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.setFont(font, 10);
                for (IndicatorAchievementDetail ip : tc.getIndicatorAchievementDetails()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    String ipDesc = ip.getIpDescription();
                    if (ipDesc.length() > 40) {
                        ipDesc = ipDesc.substring(0, 37) + "...";
                    }
                    contentStream.showText(String.format("%s - %s：%.4f",
                            ip.getIpCode(), ipDesc,
                            ip.getAchievement()));
                    contentStream.endText();
                    yPosition -= lineHeight;
                }
                yPosition -= lineHeight * 2;
            }

            // 汇总部分
            if (yPosition < 200) {
                contentStream.close();
                page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                document.addPage(page);
                contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                yPosition = page.getMediaBox().getHeight() - 50;
            }

            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("汇总");
            contentStream.endText();
            yPosition -= lineHeight * 1.5;

            // 课程目标达成度汇总
            contentStream.setFont(boldFont, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("课程目标达成度汇总：");
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.setFont(font, 10);
            for (ObjectiveAchievementSummary co : report.getObjectiveAchievements()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                String desc = co.getDescription();
                if (desc.length() > 40) {
                    desc = desc.substring(0, 37) + "...";
                }
                contentStream.showText(String.format("%s - %s：%.4f",
                        co.getObjectiveCode(), desc,
                        co.getAverageAchievement()));
                contentStream.endText();
                yPosition -= lineHeight;
            }
            yPosition -= lineHeight;

            // 课程级指标点达成度汇总
            contentStream.setFont(boldFont, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("课程级指标点达成度汇总：");
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.setFont(font, 10);
            for (IndicatorAchievementSummary ip : report.getIndicatorAchievements()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                String ipDesc = ip.getIpDescription();
                if (ipDesc.length() > 40) {
                    ipDesc = ipDesc.substring(0, 37) + "...";
                }
                contentStream.showText(String.format("%s - %s：%.4f",
                        ip.getIpCode(), ipDesc,
                        ip.getAverageAchievement()));
                contentStream.endText();
                yPosition -= lineHeight;
            }

            contentStream.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new BusinessException(500, "生成PDF报表失败：" + e.getMessage());
        }
    }

    private Long resolveReportMajorId(Long courseId, Integer gradeYear, Long requestedMajorId) {
        List<CourseMajor> bindings = courseMajorMapper.selectList(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, courseId)
                .eq(CourseMajor::getGradeYear, gradeYear));
        List<Long> majorIds = bindings.stream()
                .map(CourseMajor::getMajorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (requestedMajorId != null) {
            if (!majorIds.contains(requestedMajorId)) {
                throw new BusinessException(400, "Selected major is not bound to the course in this grade");
            }
            return requestedMajorId;
        }
        if (majorIds.size() == 1) {
            return majorIds.get(0);
        }
        if (majorIds.isEmpty()) {
            throw new BusinessException(400, "No course-major binding found for the selected course and grade");
        }
        throw new BusinessException(400, "Major is required because the selected course and grade map to multiple majors");
    }

    private void drawPdfText(org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
                             org.apache.pdfbox.pdmodel.font.PDFont font,
                             float fontSize,
                             String text,
                             float x,
                             float y) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(nvl(text));
        contentStream.endText();
    }

    private void drawPdfRightAlignedText(org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
                                         org.apache.pdfbox.pdmodel.font.PDFont font,
                                         float fontSize,
                                         String text,
                                         float rightX,
                                         float y) throws IOException {
        String safeText = nvl(text);
        float textWidth = font.getStringWidth(safeText) / 1000 * fontSize;
        drawPdfText(contentStream, font, fontSize, safeText, rightX - textWidth, y);
    }

    private String truncatePdfText(String text, int maxLength) {
        String safeText = nvl(text);
        if (safeText.length() <= maxLength) {
            return safeText;
        }
        if (maxLength <= 3) {
            return safeText.substring(0, maxLength);
        }
        return safeText.substring(0, maxLength - 3) + "...";
    }

    private boolean classMatchesProgram(Long classId, Long majorId, Integer gradeYear) {
        if (majorId == null || gradeYear == null) {
            return true;
        }
        List<TeacherClassStudentResponse> students = studentClassMapper.selectStudentsByClassId(classId);
        if (students == null || students.isEmpty()) {
            return true;
        }
        return students.stream().allMatch(student ->
                Objects.equals(student.getMajorId(), majorId) && Objects.equals(student.getEnrollmentYear(), gradeYear));
    }

    private List<IndicatorPoint> listScopedIndicators(Long courseId, Long majorId, Integer gradeYear) {
        if (courseId == null || majorId == null || gradeYear == null) {
            return List.of();
        }
        return courseIndicatorSupportMapper.selectCourseIndicatorSupports(majorId, gradeYear, courseId, null).stream()
                .collect(Collectors.toMap(
                        CourseIndicatorSupportResponse::getIpId,
                        item -> {
                            IndicatorPoint indicatorPoint = new IndicatorPoint();
                            indicatorPoint.setIpId(item.getIpId());
                            indicatorPoint.setIpCode(item.getIpCode());
                            indicatorPoint.setIpDescription(item.getIpDescription());
                            indicatorPoint.setGrId(item.getGrId());
                            indicatorPoint.setStatus(item.getIpStatus());
                            return indicatorPoint;
                        },
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values()
                .stream()
                .sorted(Comparator.comparing(IndicatorPoint::getIpCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createSectionStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createSubSectionStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createInfoLabelStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createInfoValueStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCenteredDataStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createEmphasisStyle(Workbook workbook) {
        CellStyle style = createCenteredDataStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createBaseStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private int writeMergedTitleRow(Sheet sheet, int rowIndex, String title, CellStyle style, int lastColumn) {
        Row row = sheet.createRow(rowIndex++);
        Cell cell = row.createCell(0);
        cell.setCellValue(title == null ? "" : title);
        mergeCells(sheet, row.getRowNum(), row.getRowNum(), 0, lastColumn, style);
        return rowIndex;
    }

    private int writeInfoRow(Sheet sheet, int rowIndex, String label1, Object value1, String label2, Object value2,
                             CellStyle labelStyle, CellStyle valueStyle, int lastColumn) {
        Row row = sheet.createRow(rowIndex++);
        writeCell(row.createCell(0), label1, labelStyle);
        writeCell(row.createCell(1), value1, valueStyle);
        mergeCells(sheet, row.getRowNum(), row.getRowNum(), 1, 2, valueStyle);
        writeCell(row.createCell(3), label2, labelStyle);
        writeCell(row.createCell(4), value2, valueStyle);
        mergeCells(sheet, row.getRowNum(), row.getRowNum(), 4, lastColumn, valueStyle);
        return rowIndex;
    }

    private int writeEmptyHintRow(Sheet sheet, int rowIndex, String message, CellStyle style, int lastColumn) {
        Row row = sheet.createRow(rowIndex++);
        Cell cell = row.createCell(0);
        cell.setCellValue(message);
        mergeCells(sheet, row.getRowNum(), row.getRowNum(), 0, lastColumn, style);
        return rowIndex;
    }

    private void mergeCells(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, CellStyle style) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            for (int colIndex = firstCol; colIndex <= lastCol; colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                cell.setCellStyle(style);
            }
        }
    }

    private void writeCell(Cell cell, Object value, CellStyle style) {
        if (style != null) {
            cell.setCellStyle(style);
        }
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private String formatPercent(Float value) {
        if (value == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }

    private Float firstNonNull(Float first, Float second) {
        return first != null ? first : second;
    }

    private int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String nvl(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void applySheetLayout(Sheet sheet) {
        int[] widths = {16, 28, 14, 14, 18, 18};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        sheet.createFreezePane(0, 4);
    }
}
