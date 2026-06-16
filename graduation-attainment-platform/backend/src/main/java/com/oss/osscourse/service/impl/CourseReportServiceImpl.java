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
                .eq(TeachingClass::getMajorId, majorId)
                .eq(TeachingClass::getGradeYear, request.getGradeYear());
        if (request.getTermId() != null) {
            tcWrapper.eq(TeachingClass::getTermId, request.getTermId());
        }
        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(tcWrapper);
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
            Sheet sheet = workbook.createSheet("课程级评价报表");
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
            rowIndex = writeMergedTitleRow(sheet, rowIndex, "课程级评价报表", titleStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "课程代码", report.getCourseCode(), "课程名称", report.getCourseName(), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "年级", report.getGradeYear(), "学分", report.getCredit(), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex = writeInfoRow(sheet, rowIndex, "专业", report.getMajorName(), "涉及教学班", safeSize(report.getTeachingClasses()), infoLabelStyle, infoValueStyle, lastColumn);
            rowIndex++;

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "一、各教学班明细", sectionStyle, lastColumn);
            for (TeachingClassReport teachingClass : safeList(report.getTeachingClasses())) {
                rowIndex = writeMergedTitleRow(sheet, rowIndex, "教学班：" + nvl(teachingClass.getClassName()) + "    学生数：" + nvl(teachingClass.getStudentCount()), subSectionStyle, lastColumn);

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "1. 考核点平均分", subSectionStyle, lastColumn);
                Row apHeaderRow = sheet.createRow(rowIndex++);
                writeCell(apHeaderRow.createCell(0), "考核点", headerStyle);
                writeCell(apHeaderRow.createCell(1), "满分", headerStyle);
                writeCell(apHeaderRow.createCell(2), "平均分", headerStyle);
                writeCell(apHeaderRow.createCell(3), "得分率", headerStyle);
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
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "暂无考核点平均分数据", dataStyle, lastColumn);
                }
                rowIndex++;

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "2. 课程目标达成度", subSectionStyle, lastColumn);
                Row objectiveHeaderRow = sheet.createRow(rowIndex++);
                writeCell(objectiveHeaderRow.createCell(0), "目标编号", headerStyle);
                writeCell(objectiveHeaderRow.createCell(1), "目标名称", headerStyle);
                writeCell(objectiveHeaderRow.createCell(2), "达成度", headerStyle);
                mergeCells(sheet, objectiveHeaderRow.getRowNum(), objectiveHeaderRow.getRowNum(), 2, 5, headerStyle);
                for (ObjectiveAchievementDetail objective : safeList(teachingClass.getObjectiveAchievementDetails())) {
                    Row objectiveRow = sheet.createRow(rowIndex++);
                    writeCell(objectiveRow.createCell(0), objective.getObjectiveCode(), centeredDataStyle);
                    writeCell(objectiveRow.createCell(1), firstNonBlank(objective.getObjectiveName(), objective.getDescription()), dataStyle);
                    writeCell(objectiveRow.createCell(2), formatPercent(objective.getAverageAchievement()), centeredDataStyle);
                    mergeCells(sheet, objectiveRow.getRowNum(), objectiveRow.getRowNum(), 2, 5, centeredDataStyle);
                }
                if (safeList(teachingClass.getObjectiveAchievementDetails()).isEmpty()) {
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "暂无课程目标达成度数据", dataStyle, lastColumn);
                }
                rowIndex++;

                rowIndex = writeMergedTitleRow(sheet, rowIndex, "3. 课程级指标点达成度", subSectionStyle, lastColumn);
                Row indicatorHeaderRow = sheet.createRow(rowIndex++);
                writeCell(indicatorHeaderRow.createCell(0), "指标点编号", headerStyle);
                writeCell(indicatorHeaderRow.createCell(1), "指标点描述", headerStyle);
                writeCell(indicatorHeaderRow.createCell(2), "达成度", headerStyle);
                mergeCells(sheet, indicatorHeaderRow.getRowNum(), indicatorHeaderRow.getRowNum(), 2, 5, headerStyle);
                for (IndicatorAchievementDetail indicator : safeList(teachingClass.getIndicatorAchievementDetails())) {
                    Row indicatorRow = sheet.createRow(rowIndex++);
                    writeCell(indicatorRow.createCell(0), indicator.getIpCode(), centeredDataStyle);
                    writeCell(indicatorRow.createCell(1), indicator.getIpDescription(), dataStyle);
                    writeCell(indicatorRow.createCell(2), formatPercent(indicator.getAchievement()), centeredDataStyle);
                    mergeCells(sheet, indicatorRow.getRowNum(), indicatorRow.getRowNum(), 2, 5, centeredDataStyle);
                }
                if (safeList(teachingClass.getIndicatorAchievementDetails()).isEmpty()) {
                    rowIndex = writeEmptyHintRow(sheet, rowIndex, "暂无课程级指标点达成度数据", dataStyle, lastColumn);
                }
                rowIndex++;
            }

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "二、汇总结果", sectionStyle, lastColumn);
            rowIndex = writeMergedTitleRow(sheet, rowIndex, "1. 课程目标达成度汇总", subSectionStyle, lastColumn);
            Row objectiveSummaryHeaderRow = sheet.createRow(rowIndex++);
            writeCell(objectiveSummaryHeaderRow.createCell(0), "目标编号", headerStyle);
            writeCell(objectiveSummaryHeaderRow.createCell(1), "目标名称", headerStyle);
            writeCell(objectiveSummaryHeaderRow.createCell(2), "平均达成度", headerStyle);
            mergeCells(sheet, objectiveSummaryHeaderRow.getRowNum(), objectiveSummaryHeaderRow.getRowNum(), 2, 5, headerStyle);
            for (ObjectiveAchievementSummary objective : safeList(report.getObjectiveAchievements())) {
                Row objectiveRow = sheet.createRow(rowIndex++);
                writeCell(objectiveRow.createCell(0), objective.getObjectiveCode(), centeredDataStyle);
                writeCell(objectiveRow.createCell(1), firstNonBlank(objective.getObjectiveName(), objective.getDescription()), dataStyle);
                writeCell(objectiveRow.createCell(2), formatPercent(firstNonNull(objective.getCourseAverage(), objective.getAverageAchievement())), emphasisStyle);
                mergeCells(sheet, objectiveRow.getRowNum(), objectiveRow.getRowNum(), 2, 5, emphasisStyle);
            }
            if (safeList(report.getObjectiveAchievements()).isEmpty()) {
                rowIndex = writeEmptyHintRow(sheet, rowIndex, "暂无课程目标汇总数据", dataStyle, lastColumn);
            }
            rowIndex++;

            rowIndex = writeMergedTitleRow(sheet, rowIndex, "2. 课程级指标点达成度汇总", subSectionStyle, lastColumn);
            Row indicatorSummaryHeaderRow = sheet.createRow(rowIndex++);
            writeCell(indicatorSummaryHeaderRow.createCell(0), "指标点编号", headerStyle);
            writeCell(indicatorSummaryHeaderRow.createCell(1), "指标点描述", headerStyle);
            writeCell(indicatorSummaryHeaderRow.createCell(2), "平均达成度", headerStyle);
            mergeCells(sheet, indicatorSummaryHeaderRow.getRowNum(), indicatorSummaryHeaderRow.getRowNum(), 2, 5, headerStyle);
            for (IndicatorAchievementSummary indicator : safeList(report.getIndicatorAchievements())) {
                Row indicatorRow = sheet.createRow(rowIndex++);
                writeCell(indicatorRow.createCell(0), indicator.getIpCode(), centeredDataStyle);
                writeCell(indicatorRow.createCell(1), indicator.getIpDescription(), dataStyle);
                writeCell(indicatorRow.createCell(2), formatPercent(firstNonNull(indicator.getCourseAchievement(), indicator.getAverageAchievement())), emphasisStyle);
                mergeCells(sheet, indicatorRow.getRowNum(), indicatorRow.getRowNum(), 2, 5, emphasisStyle);
            }
            if (safeList(report.getIndicatorAchievements()).isEmpty()) {
                rowIndex = writeEmptyHintRow(sheet, rowIndex, "暂无课程级指标点汇总数据", dataStyle, lastColumn);
            }

            applySheetLayout(sheet);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "生成Excel报表失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] exportCourseReportPdf(CourseReportRequest request) {
        CourseReportResponse report = getCourseReport(request);

        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            java.io.InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/NotoSansSC-Regular.ttf");
            if (fontStream == null) {
                throw new BusinessException(500, "未找到中文字体文件");
            }

            org.apache.pdfbox.pdmodel.font.PDFont font = org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontStream, false);
            fontStream.close();

            org.apache.pdfbox.pdmodel.font.PDFont boldFont = font;

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
