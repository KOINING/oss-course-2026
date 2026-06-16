package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.trace.AchievementLedgerRow;
import com.oss.osscourse.dto.trace.CourseToObjectiveTraceRequest;
import com.oss.osscourse.dto.trace.CourseToObjectiveTraceResponse;
import com.oss.osscourse.dto.trace.MajorToCourseTraceRequest;
import com.oss.osscourse.dto.trace.MajorToCourseTraceResponse;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceRequest;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.MajorIndicatorAchievement;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AchievementTraceMapper;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseIndicatorSupportMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveAchievementMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorIndicatorAchievementMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.AchievementTraceService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementTraceServiceImpl implements AchievementTraceService {

    private static final int LEDGER_LAST_COLUMN = 18;
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AchievementTraceMapper achievementTraceMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final CourseMapper courseMapper;
    private final IndicatorPointMapper indicatorPointMapper;
    private final CourseIndicatorAchievementMapper ciaMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final CourseObjectiveAchievementMapper coaMapper;
    private final ObjectiveIndicatorContributionMapper oicMapper;
    private final AssessmentPointMapper assessmentPointMapper;
    private final StudentAssessmentScoreMapper sasMapper;
    private final StudentMapper studentMapper;
    private final MajorIndicatorAchievementMapper miaMapper;
    private final CourseIndicatorSupportMapper cisMapper;

    @Override
    public List<MajorToCourseTraceResponse> getMajorToCourseTrace(MajorToCourseTraceRequest request) {
        assertMajorResultReady(request);
        List<AchievementLedgerRow> rows = listLedgerRows(request);
        Map<Long, List<AchievementLedgerRow>> rowsByIp = rows.stream()
                .filter(row -> row.getIpId() != null)
                .collect(Collectors.groupingBy(AchievementLedgerRow::getIpId, LinkedHashMap::new, Collectors.toList()));

        List<MajorToCourseTraceResponse> responses = new ArrayList<>();
        for (List<AchievementLedgerRow> ipRows : rowsByIp.values()) {
            AchievementLedgerRow first = ipRows.get(0);
            Map<String, AchievementLedgerRow> courseRows = ipRows.stream()
                    .filter(row -> row.getCourseId() != null)
                    .collect(Collectors.toMap(
                            row -> row.getCourseId() + ":" + row.getClassId(),
                            row -> row,
                            (left, right) -> left,
                            LinkedHashMap::new));

            List<MajorToCourseTraceResponse.CourseContribution> contributions = courseRows.values().stream()
                    .map(row -> MajorToCourseTraceResponse.CourseContribution.builder()
                            .courseId(row.getCourseId())
                            .courseCode(row.getCourseCode())
                            .courseName(row.getCourseName())
                            .classId(row.getClassId())
                            .classCode(row.getClassCode())
                            .className(row.getClassName())
                            .courseIndicatorAchievement(row.getCourseIndicatorAchievement())
                            .macroWeight(row.getMacroWeight())
                            .weightedContribution(multiply(row.getCourseIndicatorAchievement(), row.getMacroWeight()))
                            .calcStatus(row.getCalcStatus())
                            .build())
                    .toList();

            responses.add(MajorToCourseTraceResponse.builder()
                    .majorId(first.getMajorId())
                    .majorName(first.getMajorName())
                    .gradeYear(first.getGradeYear())
                    .termId(first.getTermId())
                    .termCode(first.getTermCode())
                    .ipId(first.getIpId())
                    .ipCode(first.getIpCode())
                    .ipDescription(first.getIpDescription())
                    .finalAchievement(first.getFinalAchievement())
                    .courseContributions(contributions)
                    .build());
        }
        return responses;
    }

    @Override
    public CourseToObjectiveTraceResponse getCourseToObjectiveTrace(CourseToObjectiveTraceRequest request) {
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        Course course = requireCourse(teachingClass.getCourseId());
        IndicatorPoint indicatorPoint = requireIndicatorPoint(request.getIpId());

        CourseIndicatorAchievement cia = ciaMapper.selectOne(new LambdaQueryWrapper<CourseIndicatorAchievement>()
                .eq(CourseIndicatorAchievement::getClassId, request.getClassId())
                .eq(CourseIndicatorAchievement::getIpId, request.getIpId())
                .last("LIMIT 1"));

        List<CourseObjective> courseObjectives = courseObjectiveMapper.selectList(new LambdaQueryWrapper<CourseObjective>()
                .eq(CourseObjective::getCourseId, course.getCourseId())
                .orderByAsc(CourseObjective::getObjectiveCode));
        List<Long> courseCoIds = courseObjectives.stream()
                .map(CourseObjective::getCoId)
                .filter(Objects::nonNull)
                .toList();
        List<ObjectiveIndicatorContribution> contributions = courseCoIds.isEmpty()
                ? List.of()
                : oicMapper.selectByObjectiveIdsAndContext(
                                courseCoIds,
                                teachingClass.getMajorId(),
                                teachingClass.getGradeYear())
                        .stream()
                        .filter(item -> Objects.equals(item.getIpId(), request.getIpId()))
                        .toList();
        List<Long> coIds = contributions.stream().map(ObjectiveIndicatorContribution::getCoId).filter(Objects::nonNull).toList();
        Map<Long, Float> weightMap = contributions.stream()
                .collect(Collectors.toMap(ObjectiveIndicatorContribution::getCoId, ObjectiveIndicatorContribution::getInternalWeight,
                        (left, right) -> left, LinkedHashMap::new));

        List<CourseObjective> objectives = coIds.isEmpty()
                ? List.of()
                : courseObjectives.stream()
                        .filter(item -> coIds.contains(item.getCoId()))
                        .toList();
        Map<Long, CourseObjectiveAchievement> achievementMap = coaMapper.selectList(
                        new LambdaQueryWrapper<CourseObjectiveAchievement>().eq(CourseObjectiveAchievement::getClassId, request.getClassId()))
                .stream()
                .collect(Collectors.toMap(CourseObjectiveAchievement::getCoId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<CourseToObjectiveTraceResponse.ObjectiveContribution> objectiveContributions = objectives.stream()
                .map(objective -> {
                    Float achievement = achievementMap.containsKey(objective.getCoId())
                            ? achievementMap.get(objective.getCoId()).getAverageAchievement()
                            : null;
                    Float weight = weightMap.get(objective.getCoId());
                    return CourseToObjectiveTraceResponse.ObjectiveContribution.builder()
                            .coId(objective.getCoId())
                            .objectiveCode(objective.getObjectiveCode())
                            .coDescription(objective.getCoDescription())
                            .objectiveAchievement(achievement)
                            .internalWeight(weight)
                            .weightedContribution(multiply(achievement, weight))
                            .build();
                })
                .toList();

        return CourseToObjectiveTraceResponse.builder()
                .classId(teachingClass.getClassId())
                .classCode(teachingClass.getClassCode())
                .className(teachingClass.getClassName())
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .ipId(indicatorPoint.getIpId())
                .ipCode(indicatorPoint.getIpCode())
                .ipDescription(indicatorPoint.getIpDescription())
                .courseIndicatorAchievement(cia == null ? null : cia.getAchievement())
                .objectiveContributions(objectiveContributions)
                .build();
    }

    @Override
    public ObjectiveToScoreTraceResponse getObjectiveToScoreTrace(ObjectiveToScoreTraceRequest request) {
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        Course course = requireCourse(teachingClass.getCourseId());
        CourseObjective objective = courseObjectiveMapper.selectById(request.getCoId());
        if (objective == null || !Objects.equals(objective.getCourseId(), course.getCourseId())) {
            throw new BusinessException(404, "课程目标不存在或不属于当前教学班课程");
        }

        CourseObjectiveAchievement coa = coaMapper.selectOne(new LambdaQueryWrapper<CourseObjectiveAchievement>()
                .eq(CourseObjectiveAchievement::getClassId, request.getClassId())
                .eq(CourseObjectiveAchievement::getCoId, request.getCoId())
                .last("LIMIT 1"));

        LambdaQueryWrapper<AssessmentPoint> apWrapper = new LambdaQueryWrapper<AssessmentPoint>()
                .eq(AssessmentPoint::getCoId, request.getCoId())
                .orderByAsc(AssessmentPoint::getApId);
        if (request.getApId() != null) {
            apWrapper.eq(AssessmentPoint::getApId, request.getApId());
        }
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(apWrapper);
        List<ObjectiveToScoreTraceResponse.AssessmentPointTrace> apTraces = assessmentPoints.stream()
                .map(ap -> buildAssessmentPointTrace(request.getClassId(), ap))
                .toList();

        return ObjectiveToScoreTraceResponse.builder()
                .classId(teachingClass.getClassId())
                .classCode(teachingClass.getClassCode())
                .className(teachingClass.getClassName())
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .coId(objective.getCoId())
                .objectiveCode(objective.getObjectiveCode())
                .coDescription(objective.getCoDescription())
                .objectiveAchievement(coa == null ? null : coa.getAverageAchievement())
                .assessmentPoints(apTraces)
                .build();
    }

    @Override
    public byte[] exportAchievementLedger(MajorToCourseTraceRequest request) {
        assertMajorResultReady(request);
        List<AchievementLedgerRow> rows = listLedgerRows(request);
        if (rows != null) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildMergedLedgerWorkbook(workbook, rows);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "导出追溯台账失败");
        }
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("达成度追溯台账");
            String[] headers = {
                    "专业", "年级", "学期", "毕业要求", "指标点", "Gk专业级达成度",
                    "课程代码", "课程名称", "教学班", "Ek课程级达成度", "W宏观权重",
                    "课程目标", "Cj课程目标达成度", "w内部权重",
                    "考核点", "满分", "学号", "姓名", "原始成绩"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (AchievementLedgerRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row, item);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "导出追溯台账失败");
        }
    }

    private List<AchievementLedgerRow> listLedgerRows(MajorToCourseTraceRequest request) {
        List<AchievementLedgerRow> rows = achievementTraceMapper.selectLedgerRows(
                request.getMajorId(), request.getGradeYear(), request.getTermId(), request.getIpId());
        if (rows.isEmpty()) {
            throw new BusinessException(404, "未查询到可追溯数据，请检查专业、年级、学期或指标点配置");
        }
        return rows;
    }

    private void assertMajorResultReady(MajorToCourseTraceRequest request) {
        assertAllSupportClassesLocked(request.getMajorId(), request.getGradeYear());

        LambdaQueryWrapper<MajorIndicatorAchievement> wrapper = new LambdaQueryWrapper<MajorIndicatorAchievement>()
                .eq(MajorIndicatorAchievement::getMajorId, request.getMajorId())
                .eq(MajorIndicatorAchievement::getGradeYear, request.getGradeYear())
                .isNotNull(MajorIndicatorAchievement::getFinalAchievement);
        if (request.getIpId() != null) {
            wrapper.eq(MajorIndicatorAchievement::getIpId, request.getIpId());
        }
        if (request.getTermId() == null) {
            List<MajorIndicatorAchievement> latestResults = miaMapper.selectList(wrapper
                    .orderByDesc(MajorIndicatorAchievement::getTermId)
                    .last("LIMIT 1"));
            if (latestResults.isEmpty()) {
                throw new BusinessException(400, "当前专业年级尚未生成专业级计算结果，请先完成课程级锁定并执行专业级计算。");
            }
            request.setTermId(latestResults.get(0).getTermId());
            return;
        }
        wrapper.eq(MajorIndicatorAchievement::getTermId, request.getTermId());
        Long count = miaMapper.selectCount(wrapper);
        if (count == null || count == 0) {
            throw new BusinessException(400, "当前专业年级尚未生成专业级计算结果，请先完成课程级锁定并执行专业级计算。");
        }
    }

    private void assertAllSupportClassesLocked(Long majorId, Integer gradeYear) {
        List<Long> supportCourseIds = cisMapper.selectMatrixRelationsByMajor(majorId, gradeYear).stream()
                .map(MatrixRelationResponse::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (supportCourseIds.isEmpty()) {
            throw new BusinessException(400, "当前专业年级尚未配置支撑课程，不能查询专业级穿透台账");
        }
        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(new LambdaQueryWrapper<TeachingClass>()
                .in(TeachingClass::getCourseId, supportCourseIds)
                .eq(TeachingClass::getMajorId, majorId)
                .eq(TeachingClass::getGradeYear, gradeYear));
        boolean aggregationAllowed = !teachingClasses.isEmpty()
                && teachingClasses.stream().allMatch(item -> "locked".equals(item.getCalcStatus()));
        if (!aggregationAllowed) {
            throw new BusinessException(400, "当前专业年级尚未生成专业级计算结果，请先完成课程级锁定并执行专业级计算。");
        }
    }

    private ObjectiveToScoreTraceResponse.AssessmentPointTrace buildAssessmentPointTrace(Long classId, AssessmentPoint ap) {
        List<StudentAssessmentScore> scores = sasMapper.selectList(new LambdaQueryWrapper<StudentAssessmentScore>()
                .eq(StudentAssessmentScore::getClassId, classId)
                .eq(StudentAssessmentScore::getApId, ap.getApId()));
        List<Long> studentIds = scores.stream().map(StudentAssessmentScore::getStudentId).filter(Objects::nonNull).toList();
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Map.of()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<ObjectiveToScoreTraceResponse.StudentScoreTrace> studentScores = scores.stream()
                .map(score -> {
                    Student student = studentMap.get(score.getStudentId());
                    return ObjectiveToScoreTraceResponse.StudentScoreTrace.builder()
                            .studentId(score.getStudentId())
                            .studentNo(student == null ? null : student.getStudentNo())
                            .studentName(student == null ? null : student.getStudentName())
                            .actualScore(score.getActualScore())
                            .build();
                })
                .toList();
        Float averageScore = scores.isEmpty()
                ? null
                : (float) scores.stream().mapToDouble(score -> score.getActualScore() == null ? 0 : score.getActualScore()).average().orElse(0);

        return ObjectiveToScoreTraceResponse.AssessmentPointTrace.builder()
                .apId(ap.getApId())
                .apName(ap.getApName())
                .fullScore(ap.getFullScore())
                .averageScore(averageScore)
                .studentScores(studentScores)
                .build();
    }

    private TeachingClass requireTeachingClass(Long classId) {
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }
        return teachingClass;
    }

    private Course requireCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        return course;
    }

    private IndicatorPoint requireIndicatorPoint(Long ipId) {
        IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(ipId);
        if (indicatorPoint == null) {
            throw new BusinessException(404, "指标点不存在");
        }
        return indicatorPoint;
    }

    private Float multiply(Float left, Float right) {
        if (left == null || right == null) {
            return null;
        }
        return left * right;
    }

    private void buildMergedLedgerWorkbook(XSSFWorkbook workbook, List<AchievementLedgerRow> rows) {
        XSSFSheet sheet = workbook.createSheet("穿透式台账");
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle infoStyle = createInfoStyle(workbook);
        CellStyle majorSectionStyle = createSectionStyle(workbook, IndexedColors.DARK_BLUE, true);
        CellStyle indicatorSectionStyle = createSectionStyle(workbook, IndexedColors.TEAL, true);
        CellStyle courseSectionStyle = createSectionStyle(workbook, IndexedColors.SEA_GREEN, true);
        CellStyle detailSectionStyle = createSectionStyle(workbook, IndexedColors.LIGHT_ORANGE, false);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle detailHeaderStyle = createDetailHeaderStyle(workbook);
        CellStyle bodyStyle = createBodyStyle(workbook, false);
        CellStyle centeredBodyStyle = createBodyStyle(workbook, true);

        AchievementLedgerRow first = rows.get(0);
        String majorName = first.getMajorName() == null ? "" : first.getMajorName();
        String termCode = first.getTermCode() == null ? "-" : first.getTermCode();

        Map<Long, List<AchievementLedgerRow>> rowsByIp = rows.stream()
                .filter(row -> row.getIpId() != null)
                .collect(Collectors.groupingBy(AchievementLedgerRow::getIpId, LinkedHashMap::new, Collectors.toList()));

        int rowIndex = 0;
        rowIndex = writeMergedTextRow(
                sheet,
                rowIndex,
                "穿透式台账 - " + majorName + " " + first.getGradeYear() + "级",
                titleStyle,
                5,
                30
        );
        rowIndex = writeMergedTextRow(
                sheet,
                rowIndex,
                "统计学期：" + termCode + " | 导出时间：" + LocalDateTime.now().format(EXPORT_TIME_FORMATTER),
                infoStyle,
                5,
                22
        );
        rowIndex++;

        rowIndex = writeMergedTextRow(sheet, rowIndex, "一、专业级指标点达成度", majorSectionStyle, 4, 24);
        Row majorHeaderRow = sheet.createRow(rowIndex++);
        String[] majorHeaders = {
                "指标点编号", "指标点描述", "毕业要求", "专业级达成度 Gk", "支撑课程数"
        };
        for (int i = 0; i < majorHeaders.length; i++) {
            writeCell(majorHeaderRow.createCell(i), majorHeaders[i], headerStyle);
        }

        for (List<AchievementLedgerRow> ipRows : rowsByIp.values()) {
            AchievementLedgerRow ipFirst = ipRows.get(0);
            int contributingCourseCount = (int) ipRows.stream()
                    .filter(row -> row.getCourseId() != null && row.getClassId() != null)
                    .map(row -> row.getCourseId() + ":" + row.getClassId())
                    .distinct()
                    .count();

            Row row = sheet.createRow(rowIndex++);
            writeCell(row.createCell(0), ipFirst.getIpCode(), centeredBodyStyle);
            writeCell(row.createCell(1), ipFirst.getIpDescription(), bodyStyle);
            writeCell(row.createCell(2), ipFirst.getGrCode(), centeredBodyStyle);
            writeCell(row.createCell(3), ipFirst.getFinalAchievement(), centeredBodyStyle);
            writeCell(row.createCell(4), contributingCourseCount, centeredBodyStyle);
        }

        rowIndex++;

        for (List<AchievementLedgerRow> ipRows : rowsByIp.values()) {
            AchievementLedgerRow ipFirst = ipRows.get(0);
            rowIndex = writeMergedTextRow(
                    sheet,
                    rowIndex,
                    "二、指标点 " + valueOrEmpty(ipFirst.getIpCode()) + " 支撑课程概览",
                    indicatorSectionStyle,
                    5,
                    24
            );

            Row courseHeaderRow = sheet.createRow(rowIndex++);
            String[] courseHeaders = {
                    "课程代码", "课程名称", "教学班", "课程级达成度 Ek", "宏观权重 W", "加权贡献"
            };
            for (int i = 0; i < courseHeaders.length; i++) {
                writeCell(courseHeaderRow.createCell(i), courseHeaders[i], headerStyle);
            }

            Map<String, AchievementLedgerRow> courseRows = ipRows.stream()
                    .filter(row -> row.getCourseId() != null && row.getClassId() != null)
                    .collect(Collectors.toMap(
                            row -> row.getCourseId() + ":" + row.getClassId(),
                            row -> row,
                            (left, right) -> left,
                            LinkedHashMap::new
                    ));

            for (AchievementLedgerRow courseRow : courseRows.values()) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row.createCell(0), courseRow.getCourseCode(), centeredBodyStyle);
                writeCell(row.createCell(1), courseRow.getCourseName(), bodyStyle);
                writeCell(row.createCell(2), courseRow.getClassName(), bodyStyle);
                writeCell(row.createCell(3), courseRow.getCourseIndicatorAchievement(), centeredBodyStyle);
                writeCell(row.createCell(4), courseRow.getMacroWeight(), centeredBodyStyle);
                writeCell(row.createCell(5), multiply(courseRow.getCourseIndicatorAchievement(), courseRow.getMacroWeight()), centeredBodyStyle);
            }

            rowIndex++;
            for (Map.Entry<String, AchievementLedgerRow> courseEntry : courseRows.entrySet()) {
                AchievementLedgerRow courseFirst = courseEntry.getValue();
                List<AchievementLedgerRow> currentCourseRows = ipRows.stream()
                        .filter(row -> Objects.equals(row.getCourseId(), courseFirst.getCourseId())
                                && Objects.equals(row.getClassId(), courseFirst.getClassId()))
                        .toList();
                Map<Long, List<AchievementLedgerRow>> rowsByObjective = currentCourseRows.stream()
                        .filter(row -> row.getCoId() != null)
                        .collect(Collectors.groupingBy(AchievementLedgerRow::getCoId, LinkedHashMap::new, Collectors.toList()));
                if (rowsByObjective.isEmpty()) {
                    continue;
                }

                rowIndex = writeMergedTextRow(
                        sheet,
                        rowIndex,
                        "三、课程 " + valueOrEmpty(courseFirst.getCourseName())
                                + " | 教学班 " + valueOrEmpty(courseFirst.getClassName()),
                        courseSectionStyle,
                        5,
                        24
                );

                rowIndex = writeHeaderRow(sheet, rowIndex, courseHeaders, headerStyle);
                Row singleCourseRow = sheet.createRow(rowIndex++);
                writeCell(singleCourseRow.createCell(0), courseFirst.getCourseCode(), centeredBodyStyle);
                writeCell(singleCourseRow.createCell(1), courseFirst.getCourseName(), bodyStyle);
                writeCell(singleCourseRow.createCell(2), courseFirst.getClassName(), bodyStyle);
                writeCell(singleCourseRow.createCell(3), courseFirst.getCourseIndicatorAchievement(), centeredBodyStyle);
                writeCell(singleCourseRow.createCell(4), courseFirst.getMacroWeight(), centeredBodyStyle);
                writeCell(singleCourseRow.createCell(5), multiply(courseFirst.getCourseIndicatorAchievement(), courseFirst.getMacroWeight()), centeredBodyStyle);

                rowIndex = writeMergedTextRow(
                        sheet,
                        rowIndex,
                        "3.1 课程目标明细",
                        infoStyle,
                        4,
                        20
                );
                rowIndex = writeHeaderRow(
                        sheet,
                        rowIndex,
                        new String[]{
                                "目标编码", "目标描述", "班级平均达成度",
                                "内部权重", "加权贡献"
                        },
                        headerStyle
                );

                for (List<AchievementLedgerRow> objectiveRows : rowsByObjective.values()) {
                    AchievementLedgerRow objectiveFirst = objectiveRows.get(0);
                    Row objectiveRow = sheet.createRow(rowIndex++);
                    writeCell(objectiveRow.createCell(0), objectiveFirst.getObjectiveCode(), centeredBodyStyle);
                    writeCell(objectiveRow.createCell(1), objectiveFirst.getCoDescription(), bodyStyle);
                    writeCell(objectiveRow.createCell(2), objectiveFirst.getObjectiveAchievement(), centeredBodyStyle);
                    writeCell(objectiveRow.createCell(3), objectiveFirst.getInternalWeight(), centeredBodyStyle);
                    writeCell(objectiveRow.createCell(4), multiply(objectiveFirst.getObjectiveAchievement(), objectiveFirst.getInternalWeight()), centeredBodyStyle);

                    rowIndex = writeMergedTextRow(
                            sheet,
                            rowIndex,
                            "四、目标 " + valueOrEmpty(objectiveFirst.getObjectiveCode()) + " 得分明细",
                            detailSectionStyle,
                            4,
                            22
                    );
                    rowIndex = writeHeaderRow(
                            sheet,
                            rowIndex,
                            new String[]{
                                    "考核点", "满分", "学号", "姓名", "原始成绩"
                            },
                            detailHeaderStyle
                    );

                    for (AchievementLedgerRow detailRow : objectiveRows) {
                        Row scoreRow = sheet.createRow(rowIndex++);
                        writeCell(scoreRow.createCell(0), detailRow.getApName(), bodyStyle);
                        writeCell(scoreRow.createCell(1), detailRow.getFullScore(), centeredBodyStyle);
                        writeCell(scoreRow.createCell(2), detailRow.getStudentNo(), centeredBodyStyle);
                        writeCell(scoreRow.createCell(3), detailRow.getStudentName(), centeredBodyStyle);
                        writeCell(scoreRow.createCell(4), detailRow.getActualScore(), centeredBodyStyle);
                    }
                    rowIndex++;
                }
            }

            rowIndex++;
        }

        sheet.createFreezePane(0, 3);
        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.min(Math.max(sheet.getColumnWidth(i) + 512, 14 * 256), 42 * 256);
            sheet.setColumnWidth(i, width);
        }
    }

    private int writeHeaderRow(XSSFSheet sheet, int rowIndex, String[] headers, CellStyle headerStyle) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            writeCell(row.createCell(i), headers[i], headerStyle);
        }
        return rowIndex + 1;
    }

    private int writeMergedTextRow(XSSFSheet sheet, int rowIndex, String text, CellStyle style, int lastColumn) {
        return writeMergedTextRow(sheet, rowIndex, text, style, lastColumn, 20);
    }

    private int writeMergedTextRow(XSSFSheet sheet, int rowIndex, String text, CellStyle style, int lastColumn, int heightInPoints) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(heightInPoints);
        writeCell(row.createCell(0), text, style);
        for (int column = 1; column <= lastColumn; column++) {
            writeCell(row.createCell(column), "", style);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, lastColumn));
        return rowIndex + 1;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createInfoStyle(XSSFWorkbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createSectionStyle(XSSFWorkbook workbook, IndexedColors fillColor, boolean whiteFont) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFillForegroundColor(fillColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        if (whiteFont) {
            font.setColor(IndexedColors.WHITE.getIndex());
        }
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDetailHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createBodyStyle(XSSFWorkbook workbook, boolean centered) {
        CellStyle style = createBaseStyle(workbook);
        style.setAlignment(centered ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createBaseStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private void mergeLedgerCells(XSSFSheet sheet, List<AchievementLedgerRow> rows, int dataStartRow) {
        mergeColumns(sheet, rows, dataStartRow, new int[]{0, 1, 2}, this::isSameMajorScope);
        mergeColumns(sheet, rows, dataStartRow, new int[]{3}, this::isSameRequirementScope);
        mergeColumns(sheet, rows, dataStartRow, new int[]{4, 5}, this::isSameIndicatorScope);
        mergeColumns(sheet, rows, dataStartRow, new int[]{6, 7, 8, 9, 10}, this::isSameClassScope);
        mergeColumns(sheet, rows, dataStartRow, new int[]{11, 12, 13}, this::isSameObjectiveScope);
        mergeColumns(sheet, rows, dataStartRow, new int[]{14, 15}, this::isSameAssessmentPointScope);
    }

    private void mergeColumns(XSSFSheet sheet,
                              List<AchievementLedgerRow> rows,
                              int dataStartRow,
                              int[] columns,
                              BiPredicate<AchievementLedgerRow, AchievementLedgerRow> sameGroup) {
        if (rows.isEmpty()) {
            return;
        }
        int groupStart = 0;
        for (int index = 1; index <= rows.size(); index++) {
            boolean groupEnded = index == rows.size() || !sameGroup.test(rows.get(index - 1), rows.get(index));
            if (!groupEnded) {
                continue;
            }
            int firstRow = dataStartRow + groupStart;
            int lastRow = dataStartRow + index - 1;
            if (lastRow > firstRow) {
                for (int column : columns) {
                    sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, column, column));
                }
            }
            groupStart = index;
        }
    }

    private boolean isSameMajorScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return Objects.equals(left.getMajorId(), right.getMajorId())
                && Objects.equals(left.getGradeYear(), right.getGradeYear())
                && Objects.equals(left.getTermId(), right.getTermId());
    }

    private boolean isSameRequirementScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return isSameMajorScope(left, right)
                && Objects.equals(left.getGrId(), right.getGrId());
    }

    private boolean isSameIndicatorScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return isSameRequirementScope(left, right)
                && Objects.equals(left.getIpId(), right.getIpId());
    }

    private boolean isSameClassScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return isSameIndicatorScope(left, right)
                && Objects.equals(left.getClassId(), right.getClassId());
    }

    private boolean isSameObjectiveScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return isSameClassScope(left, right)
                && Objects.equals(left.getCoId(), right.getCoId());
    }

    private boolean isSameAssessmentPointScope(AchievementLedgerRow left, AchievementLedgerRow right) {
        return isSameObjectiveScope(left, right)
                && Objects.equals(left.getApId(), right.getApId());
    }

    private void writeMergedLedgerRow(Row row, AchievementLedgerRow item, CellStyle mergedCellStyle, CellStyle bodyStyle) {
        int col = 0;
        writeCell(row.createCell(col++), item.getMajorName(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getGradeYear(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getTermCode(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getGrCode(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getIpCode(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getFinalAchievement(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getCourseCode(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getCourseName(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getClassName(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getCourseIndicatorAchievement(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getMacroWeight(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getObjectiveCode(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getObjectiveAchievement(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getInternalWeight(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getApName(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getFullScore(), mergedCellStyle);
        writeCell(row.createCell(col++), item.getStudentNo(), bodyStyle);
        writeCell(row.createCell(col++), item.getStudentName(), bodyStyle);
        writeCell(row.createCell(col), item.getActualScore(), bodyStyle);
    }

    private void writeRow(Row row, AchievementLedgerRow item) {
        int col = 0;
        writeCell(row.createCell(col++), item.getMajorName());
        writeCell(row.createCell(col++), item.getGradeYear());
        writeCell(row.createCell(col++), item.getTermCode());
        writeCell(row.createCell(col++), item.getGrCode());
        writeCell(row.createCell(col++), item.getIpCode());
        writeCell(row.createCell(col++), item.getFinalAchievement());
        writeCell(row.createCell(col++), item.getCourseCode());
        writeCell(row.createCell(col++), item.getCourseName());
        writeCell(row.createCell(col++), item.getClassName());
        writeCell(row.createCell(col++), item.getCourseIndicatorAchievement());
        writeCell(row.createCell(col++), item.getMacroWeight());
        writeCell(row.createCell(col++), item.getObjectiveCode());
        writeCell(row.createCell(col++), item.getObjectiveAchievement());
        writeCell(row.createCell(col++), item.getInternalWeight());
        writeCell(row.createCell(col++), item.getApName());
        writeCell(row.createCell(col++), item.getFullScore());
        writeCell(row.createCell(col++), item.getStudentNo());
        writeCell(row.createCell(col++), item.getStudentName());
        writeCell(row.createCell(col), item.getActualScore());
    }

    private void writeCell(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);
        writeCell(cell, value);
    }

    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}
