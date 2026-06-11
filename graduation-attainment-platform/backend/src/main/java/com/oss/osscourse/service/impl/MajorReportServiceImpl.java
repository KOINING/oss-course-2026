package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.report.MajorRadarResponse;
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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public byte[] exportMajorReport(MajorReportRequest request) {
        // 与 assembleMajorReport 共用同一结果源
        MajorReportResponse report = assembleMajorReport(request);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("专业级达成度评价报告");

            // 预定义样式
            XSSFCellStyle titleStyle = createTitleStyle(workbook);
            XSSFCellStyle infoStyle = createInfoStyle(workbook);
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);
            XSSFCellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            XSSFCellStyle remarkStyle = createRemarkStyle(workbook);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            int rowIdx = 0;

            // ========== 报告标题 ==========
            XSSFRow titleRow = sheet.createRow(rowIdx++);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("专业级毕业要求指标点达成度评价报告");
            titleCell.setCellStyle(titleStyle);
            // 合并标题行（跨 7 列）
            sheet.addMergedRegion(
                    new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));

            // 空行
            rowIdx++;

            // ========== 基本信息区 ==========
            String majorInfo = "专业：" + nvl(report.getMajorName()) + "（ID=" + report.getMajorId() + "）";
            createInfoRow(sheet, rowIdx++, majorInfo, infoStyle);

            String gradeInfo = "年级：" + report.getGradeYear() + " 级";
            if (report.getTermCode() != null) {
                gradeInfo += "    统计学期：" + report.getTermCode();
            }
            createInfoRow(sheet, rowIdx++, gradeInfo, infoStyle);

            String timeInfo = "报告生成时间：" + (report.getReportGeneratedAt() != null
                    ? report.getReportGeneratedAt().format(dtf) : "");
            createInfoRow(sheet, rowIdx++, timeInfo, infoStyle);

            String statusInfo = "计算状态："
                    + (Boolean.TRUE.equals(report.getResultReady()) ? "结果已就绪" : "结果未就绪");
            if (report.getMessage() != null) {
                statusInfo += "（" + report.getMessage() + "）";
            }
            createInfoRow(sheet, rowIdx++, statusInfo, infoStyle);

            // 空行
            rowIdx++;

            // ========== 指标点达成度汇总表 ==========
            // 表头
            XSSFRow summaryHeaderRow = sheet.createRow(rowIdx++);
            String[] summaryHeaders = {"序号", "指标点编码", "指标点描述", "所属毕业要求",
                    "专业级达成度(G_k)", "支撑课程数", "宏观权重和"};
            for (int i = 0; i < summaryHeaders.length; i++) {
                XSSFCell cell = summaryHeaderRow.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            List<IndicatorReportRow> rows = report.getIndicatorAchievements() != null
                    ? report.getIndicatorAchievements()
                    : List.of();

            int seq = 1;
            for (IndicatorReportRow row : rows) {
                XSSFRow dataRow = sheet.createRow(rowIdx++);
                fillCell(dataRow, 0, String.valueOf(seq++), dataStyle);
                fillCell(dataRow, 1, nvl(row.getIpCode()), dataStyle);
                fillCell(dataRow, 2, nvl(row.getIpDescription()), dataStyle);
                fillCell(dataRow, 3, nvl(row.getGrCode()), dataStyle);
                fillCell(dataRow, 4, formatAchievement(row.getFinalAchievement()), dataStyle);
                fillCell(dataRow, 5, row.getContributingCourseCount() != null
                        ? String.valueOf(row.getContributingCourseCount()) : "0", dataStyle);
                fillCell(dataRow, 6, row.getTotalWeightSum() != null
                        ? String.format("%.4f", row.getTotalWeightSum()) : "-", dataStyle);
            }

            // ========== 支撑课程明细区 ==========
            if (!rows.isEmpty()) {
                rowIdx++; // 空行
                XSSFRow detailTitleRow = sheet.createRow(rowIdx++);
                XSSFCell detailTitleCell = detailTitleRow.createCell(0);
                detailTitleCell.setCellValue("支撑课程贡献明细");
                detailTitleCell.setCellStyle(subHeaderStyle);
                sheet.addMergedRegion(
                        new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));

                for (IndicatorReportRow row : rows) {
                    // 每个指标点一个小节
                    rowIdx++; // 空行
                    XSSFRow ipHeaderRow = sheet.createRow(rowIdx++);
                    XSSFCell ipHeaderCell = ipHeaderRow.createCell(0);
                    ipHeaderCell.setCellValue("▶ 指标点 " + nvl(row.getIpCode())
                            + "（" + nvl(row.getIpDescription()) + "）  G_k = "
                            + formatAchievement(row.getFinalAchievement()));
                    ipHeaderCell.setCellStyle(subHeaderStyle);
                    sheet.addMergedRegion(
                            new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));

                    // 明细表头
                    XSSFRow detailHeaderRow = sheet.createRow(rowIdx++);
                    String[] detailHeaders = {"序号", "课程代码", "课程名称", "教学班",
                            "课程级达成度(E_k)", "宏观权重(W)", "加权贡献(E_k×W)"};
                    for (int i = 0; i < detailHeaders.length; i++) {
                        XSSFCell cell = detailHeaderRow.createCell(i);
                        cell.setCellValue(detailHeaders[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    // 明细数据
                    List<ContributingCourse> courses = row.getContributingCourses() != null
                            ? row.getContributingCourses()
                            : List.of();

                    int detailSeq = 1;
                    float ekSum = 0f;
                    int ekCount = 0;
                    for (ContributingCourse cc : courses) {
                        XSSFRow detailRow = sheet.createRow(rowIdx++);
                        fillCell(detailRow, 0, String.valueOf(detailSeq++), dataStyle);
                        fillCell(detailRow, 1, nvl(cc.getCourseCode()), dataStyle);
                        fillCell(detailRow, 2, nvl(cc.getCourseName()), dataStyle);
                        fillCell(detailRow, 3, nvl(cc.getClassName()), dataStyle);
                        fillCell(detailRow, 4, formatAchievement(cc.getCourseAchievement()), dataStyle);
                        fillCell(detailRow, 5, cc.getTotalWeight() != null
                                ? String.format("%.4f", cc.getTotalWeight()) : "-", dataStyle);
                        fillCell(detailRow, 6, formatWeightedContribution(cc.getWeightedContribution()), dataStyle);

                        if (cc.getCourseAchievement() != null) {
                            ekSum += cc.getCourseAchievement();
                            ekCount++;
                        }
                    }

                    // 校准提示：如果课程级E_k的加权和与专业级G_k差异较大
                    if (row.getFinalAchievement() != null && ekCount > 0) {
                        float ekAvg = ekSum / ekCount;
                        float diff = Math.abs(row.getFinalAchievement() - ekAvg);
                        if (diff > 0.05f && ekCount >= 2) {
                            XSSFRow noteRow = sheet.createRow(rowIdx++);
                            XSSFCell noteCell = noteRow.createCell(0);
                            noteCell.setCellValue("注：该指标点下支撑课程 E_k 均值为 "
                                    + String.format("%.4f", ekAvg) + "，与 G_k（"
                                    + formatAchievement(row.getFinalAchievement())
                                    + "）存在差异，系宏观权重 W 加权所致，属正常现象");
                            noteCell.setCellStyle(remarkStyle);
                            sheet.addMergedRegion(
                                    new org.apache.poi.ss.util.CellRangeAddress(
                                            rowIdx - 1, rowIdx - 1, 0, 6));
                        }
                    }
                }
            }

            // ========== 数据源说明 ==========
            rowIdx++;
            XSSFRow dsTitleRow = sheet.createRow(rowIdx++);
            XSSFCell dsTitleCell = dsTitleRow.createCell(0);
            dsTitleCell.setCellValue("数据源说明");
            dsTitleCell.setCellStyle(subHeaderStyle);
            sheet.addMergedRegion(
                    new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));

            DataSourceSummary ds = report.getDataSourceSummary() != null
                    ? report.getDataSourceSummary()
                    : DataSourceSummary.builder().build();

            String[] dsLabels = {
                    "数据来源表：" + nvl(ds.getSourceTable()),
                    "支撑课程总数：" + (ds.getSupportCourseCount() != null
                            ? ds.getSupportCourseCount() : 0),
                    "已锁定教学班数：" + (ds.getLockedClassCount() != null
                            ? ds.getLockedClassCount() : 0),
                    "备注：" + nvl(ds.getRemark())
            };
            for (String label : dsLabels) {
                createInfoRow(sheet, rowIdx++, label, infoStyle);
            }

            // ========== 调整列宽 ==========
            int[] colWidths = {2000, 4000, 12000, 4000, 5000, 3500, 4500};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), colWidths[i]));
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "生成专业级评价报告 Excel 失败: " + e.getMessage());
        }
    }

    @Override
    public MajorRadarResponse getMajorRadar(MajorReportRequest request) {
        // 与 assembleMajorReport 共用同一结果源
        MajorReportResponse report = assembleMajorReport(request);

        // 提取指标点达成度列表
        List<IndicatorReportRow> rows = report.getIndicatorAchievements() != null
                ? report.getIndicatorAchievements()
                : List.of();

        // 构建轴标签和值
        List<MajorRadarResponse.AxisIndicator> axisIndicators = new ArrayList<>();
        List<Float> seriesData = new ArrayList<>();

        for (IndicatorReportRow row : rows) {
            axisIndicators.add(MajorRadarResponse.AxisIndicator.builder()
                    .ipId(row.getIpId())
                    .ipCode(row.getIpCode())
                    .ipDescription(row.getIpDescription())
                    .grCode(row.getGrCode())
                    .build());
            seriesData.add(row.getFinalAchievement());
        }

        MajorRadarResponse.RadarData radarData = MajorRadarResponse.RadarData.builder()
                .indicators(axisIndicators)
                .series(List.of(MajorRadarResponse.SeriesItem.builder()
                        .name("专业级达成度")
                        .data(seriesData)
                        .build()))
                .maxValue(1.0f)
                .referenceLines(List.of(
                        MajorRadarResponse.ReferenceLine.builder()
                                .value(0.7f)
                                .name("合格线")
                                .build(),
                        MajorRadarResponse.ReferenceLine.builder()
                                .value(0.8f)
                                .name("良好线")
                                .build()))
                .build();

        return MajorRadarResponse.builder()
                .majorId(report.getMajorId())
                .majorName(report.getMajorName())
                .gradeYear(report.getGradeYear())
                .termId(report.getTermId())
                .termCode(report.getTermCode())
                .reportGeneratedAt(LocalDateTime.now())
                .resultReady(report.getResultReady())
                .message(report.getMessage())
                .radar(radarData)
                .dataSource("major_indicator_achievement（专业+" + report.getGradeYear() + " 级）")
                .build();
    }

    // ==================== Excel 样式工厂 ====================

    private XSSFCellStyle createTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createInfoStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private XSSFCellStyle createSubHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle createRemarkStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // ==================== Excel 辅助写方法 ====================

    private void fillCell(XSSFRow row, int col, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createInfoRow(XSSFSheet sheet, int rowIdx, String value, XSSFCellStyle style) {
        XSSFRow row = sheet.createRow(rowIdx);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 6));
    }

    private String formatAchievement(Float value) {
        return value != null ? String.format("%.4f", value) : "-";
    }

    private String formatWeightedContribution(Float value) {
        return value != null ? String.format("%.4f", value) : "-";
    }

    private String nvl(String value) {
        return value == null || value.isEmpty() ? "-" : value;
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
