package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.report.CourseReportRequest;
import com.oss.osscourse.dto.report.CourseReportResponse;
import com.oss.osscourse.dto.report.CourseReportResponse.*;
import com.oss.osscourse.entity.*;
import com.oss.osscourse.mapper.*;
import com.oss.osscourse.service.CourseReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
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

    @Override
    public CourseReportResponse getCourseReport(CourseReportRequest request) {
        // 1. 验证课程存在
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 2. 查询该课程在该年级下的所有教学班
        LambdaQueryWrapper<TeachingClass> tcWrapper = new LambdaQueryWrapper<>();
        tcWrapper.eq(TeachingClass::getCourseId, request.getCourseId())
                 .eq(TeachingClass::getGradeYear, request.getGradeYear());
        if (request.getTermId() != null) {
            tcWrapper.eq(TeachingClass::getTermId, request.getTermId());
        }
        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(tcWrapper);

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
                        .in(AssessmentPoint::getCoId, coIds));

        // 5. 构建教学班报表
        List<TeachingClassReport> classReports = new ArrayList<>();
        Map<Long, List<ObjectiveAchievementDetail>> coAchievementMap = new HashMap<>();
        Map<Long, List<IndicatorAchievementDetail>> ipAchievementMap = new HashMap<>();

        for (TeachingClass tc : teachingClasses) {
            // 获取学生人数
            Long studentCount = studentClassMapper.selectCount(
                    new LambdaQueryWrapper<StudentClass>()
                            .eq(StudentClass::getClassId, tc.getClassId()));

            // 获取各考核点平均分
            List<AssessmentPointAverage> apAverages = new ArrayList<>();
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
                        .add(detail);
            }

            // 获取课程级指标点达成度
            List<IndicatorAchievementDetail> ipDetails = new ArrayList<>();
            List<CourseIndicatorAchievement> ciaList = ciaMapper.selectList(
                    new LambdaQueryWrapper<CourseIndicatorAchievement>()
                            .eq(CourseIndicatorAchievement::getClassId, tc.getClassId()));

            for (CourseIndicatorAchievement cia : ciaList) {
                IndicatorPoint ip = indicatorPointMapper.selectById(cia.getIpId());
                IndicatorAchievementDetail detail = IndicatorAchievementDetail.builder()
                        .ipId(cia.getIpId())
                        .ipCode(ip != null ? ip.getIpCode() : "")
                        .ipDescription(ip != null ? ip.getIpDescription() : "")
                        .achievement(cia.getAchievement())
                        .build();
                ipDetails.add(detail);

                // 收集用于汇总
                ipAchievementMap.computeIfAbsent(cia.getIpId(), k -> new ArrayList<>())
                        .add(detail);
            }

            classReports.add(TeachingClassReport.builder()
                    .classId(tc.getClassId())
                    .classCode(tc.getClassCode())
                    .className(tc.getClassName())
                    .termCode("") // 需要查询学期表
                    .studentCount(studentCount != null ? studentCount.intValue() : 0)
                    .calcStatus(tc.getCalcStatus())
                    .assessmentPointAverages(apAverages)
                    .objectiveAchievementDetails(coDetails)
                    .indicatorAchievementDetails(ipDetails)
                    .build());
        }

        // 6. 构建课程目标达成度汇总
        List<ObjectiveAchievementSummary> coSummaries = new ArrayList<>();
        for (CourseObjective co : objectives) {
            List<ObjectiveAchievementDetail> details = coAchievementMap.getOrDefault(co.getCoId(), List.of());
            List<ClassAchievement> classAchievements = details.stream()
                    .map(d -> ClassAchievement.builder()
                            .classId(null) // 可以从上下文获取
                            .className("")
                            .achievement(d.getAverageAchievement())
                            .build())
                    .collect(Collectors.toList());

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
                            .classId(null)
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

    @Override
    public byte[] exportCourseReportExcel(CourseReportRequest request) {
        CourseReportResponse report = getCourseReport(request);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课程级评价报表");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowIndex = 0;

            // 第一部分：课程基本信息
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("课程级评价报表");
            titleCell.setCellStyle(headerStyle);

            Row infoRow1 = sheet.createRow(rowIndex++);
            infoRow1.createCell(0).setCellValue("课程编码");
            infoRow1.createCell(1).setCellValue(report.getCourseCode());
            infoRow1.createCell(2).setCellValue("课程名称");
            infoRow1.createCell(3).setCellValue(report.getCourseName());

            Row infoRow2 = sheet.createRow(rowIndex++);
            infoRow2.createCell(0).setCellValue("年级");
            infoRow2.createCell(1).setCellValue(report.getGradeYear());
            infoRow2.createCell(2).setCellValue("学分");
            infoRow2.createCell(3).setCellValue(report.getCredit());

            rowIndex++; // 空行

            // 第二部分：各教学班明细
            for (TeachingClassReport tc : report.getTeachingClasses()) {
                Row tcTitleRow = sheet.createRow(rowIndex++);
                tcTitleRow.createCell(0).setCellValue("教学班：" + tc.getClassName());
                tcTitleRow.getCell(0).setCellStyle(headerStyle);

                // 考核点平均分
                Row apHeaderRow = sheet.createRow(rowIndex++);
                apHeaderRow.createCell(0).setCellValue("考核点");
                apHeaderRow.createCell(1).setCellValue("满分");
                apHeaderRow.createCell(2).setCellValue("平均分");
                apHeaderRow.createCell(3).setCellValue("得分率");

                for (AssessmentPointAverage ap : tc.getAssessmentPointAverages()) {
                    Row apRow = sheet.createRow(rowIndex++);
                    apRow.createCell(0).setCellValue(ap.getApName());
                    apRow.createCell(1).setCellValue(ap.getFullScore());
                    apRow.createCell(2).setCellValue(ap.getAverageScore());
                    apRow.createCell(3).setCellValue(String.format("%.1f%%", ap.getScoreRate() * 100));
                }

                rowIndex++; // 空行

                // 课程目标达成度
                Row coHeaderRow = sheet.createRow(rowIndex++);
                coHeaderRow.createCell(0).setCellValue("课程目标");
                coHeaderRow.createCell(1).setCellValue("描述");
                coHeaderRow.createCell(2).setCellValue("达成度");

                for (ObjectiveAchievementDetail co : tc.getObjectiveAchievementDetails()) {
                    Row coRow = sheet.createRow(rowIndex++);
                    coRow.createCell(0).setCellValue(co.getObjectiveCode());
                    coRow.createCell(1).setCellValue(co.getDescription());
                    coRow.createCell(2).setCellValue(String.format("%.1f%%", co.getAverageAchievement() * 100));
                }

                rowIndex++; // 空行
            }

            // 第三部分：汇总
            Row summaryTitleRow = sheet.createRow(rowIndex++);
            summaryTitleRow.createCell(0).setCellValue("课程目标达成度汇总");
            summaryTitleRow.getCell(0).setCellStyle(headerStyle);

            for (ObjectiveAchievementSummary co : report.getObjectiveAchievements()) {
                Row coRow = sheet.createRow(rowIndex++);
                coRow.createCell(0).setCellValue(co.getObjectiveCode());
                coRow.createCell(1).setCellValue(co.getDescription());
                coRow.createCell(2).setCellValue(String.format("%.1f%%", co.getAverageAchievement() * 100));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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
            yPosition -= lineHeight * 2;

            // 课程基本信息
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("课程编码：" + report.getCourseCode() + "    课程名称：" + report.getCourseName());
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
                contentStream.setFont(boldFont, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("考核点                    满分    平均分    得分率");
                contentStream.endText();
                yPosition -= lineHeight;

                // 考核点数据
                contentStream.setFont(font, 10);
                for (AssessmentPointAverage ap : tc.getAssessmentPointAverages()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    String apName = ap.getApName();
                    if (apName.length() > 20) {
                        apName = apName.substring(0, 17) + "...";
                    }
                    contentStream.showText(String.format("%-20s %6.1f %8.1f %6.1f%%",
                            apName, ap.getFullScore(), ap.getAverageScore(),
                            ap.getScoreRate() * 100));
                    contentStream.endText();
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
                    contentStream.showText(String.format("%s - %s：%.1f%%",
                            co.getObjectiveCode(), desc,
                            co.getAverageAchievement() * 100));
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
                    contentStream.showText(String.format("%s - %s：%.1f%%",
                            ip.getIpCode(), ipDesc,
                            ip.getAchievement() * 100));
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
                contentStream.showText(String.format("%s - %s：%.1f%%",
                        co.getObjectiveCode(), desc,
                        co.getAverageAchievement() * 100));
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
                contentStream.showText(String.format("%s - %s：%.1f%%",
                        ip.getIpCode(), ipDesc,
                        ip.getAverageAchievement() * 100));
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

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }
}
