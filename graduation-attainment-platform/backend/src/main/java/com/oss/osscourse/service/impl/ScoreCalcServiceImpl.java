package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.achievement.*;
import com.oss.osscourse.dto.score.*;
import com.oss.osscourse.entity.*;
import com.oss.osscourse.mapper.*;
import com.oss.osscourse.service.ScoreCalcService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreCalcServiceImpl implements ScoreCalcService {

    private final TeachingClassMapper teachingClassMapper;
    private final StudentClassMapper studentClassMapper;
    private final StudentMapper studentMapper;
    private final AssessmentPointMapper assessmentPointMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final ObjectiveIndicatorContributionMapper oicMapper;
    private final CourseIndicatorSupportMapper cisMapper;
    private final StudentAssessmentScoreMapper sasMapper;
    private final CourseObjectiveAchievementMapper coaMapper;
    private final CourseIndicatorAchievementMapper ciaMapper;
    private final MajorIndicatorAchievementMapper miaMapper;
    private final StudentObjectiveAchievementMapper soaMapper;
    private final CourseMapper courseMapper;
    private final MajorMapper majorMapper;
    private final AcademicTermMapper academicTermMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final IndicatorPointMapper indicatorPointMapper;

    @Override
    public ScoreTemplatePreviewResponse previewTemplate(Long classId) {
        // 1. 验证教学班存在
        TeachingClass tc = teachingClassMapper.selectById(classId);
        if (tc == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 2. 获取教学班学生名单
        List<StudentClass> scList = studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, classId));
        if (scList.isEmpty()) {
            throw new BusinessException(400, "当前教学班没有学生名单，请先导入学生");
        }

        List<Long> studentIds = scList.stream().map(StudentClass::getStudentId).collect(Collectors.toList());
        Map<Long, Student> studentMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s));

        // 3. 获取课程目标
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>().eq(CourseObjective::getCourseId, tc.getCourseId()));
        if (objectives.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置课程目标，请先配置课程目标");
        }

        // 4. 获取考核点
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).collect(Collectors.toList());
        List<AssessmentPoint> apList = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>().in(AssessmentPoint::getCoId, coIds));
        if (apList.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置考核点，请先配置考核点");
        }

        // 5. 构建课程目标映射
        Map<Long, CourseObjective> objectiveMap = objectives.stream()
                .collect(Collectors.toMap(CourseObjective::getCoId, o -> o));

        // 6. 构建固定列头
        List<String> fixedHeaders = List.of("学号", "姓名");

        // 7. 构建动态列头
        List<ScoreTemplatePreviewResponse.AssessmentPointHeader> dynamicHeaders = apList.stream()
                .map(ap -> {
                    CourseObjective co = objectiveMap.get(ap.getCoId());
                    return ScoreTemplatePreviewResponse.AssessmentPointHeader.builder()
                            .apId(ap.getApId())
                            .apName(ap.getApName())
                            .fullScore(ap.getFullScore())
                            .objectiveCode(co != null ? co.getObjectiveCode() : null)
                            .build();
                })
                .collect(Collectors.toList());

        // 8. 构建学生数据行
        List<ScoreTemplatePreviewResponse.StudentScoreRow> rows = new ArrayList<>();
        for (Long studentId : studentIds) {
            Student student = studentMap.get(studentId);
            if (student == null) continue;

            List<Float> scores = new ArrayList<>();
            for (int i = 0; i < apList.size(); i++) {
                scores.add(null); // 初始为空
            }

            rows.add(ScoreTemplatePreviewResponse.StudentScoreRow.builder()
                    .studentId(studentId)
                    .studentNo(student.getStudentNo())
                    .studentName(student.getStudentName())
                    .scores(scores)
                    .build());
        }

        // 9. 获取课程信息
        Course course = courseMapper.selectById(tc.getCourseId());

        return ScoreTemplatePreviewResponse.builder()
                .classId(classId)
                .className(tc.getClassName())
                .courseName(course != null ? course.getCourseName() : null)
                .studentCount(rows.size())
                .assessmentPointCount(apList.size())
                .fixedHeaders(fixedHeaders)
                .dynamicHeaders(dynamicHeaders)
                .rows(rows)
                .build();
    }

    @Override
    public byte[] downloadTemplate(Long classId) {
        // 1. 获取模板预览数据
        ScoreTemplatePreviewResponse preview = previewTemplate(classId);

        // 2. 创建 Excel 工作簿
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("成绩模板");

            // 3. 创建表头样式
            org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            org.apache.poi.xssf.usermodel.XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 4. 创建第一行：固定列头 + 动态列头（考核点名称）
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            int cellIndex = 0;

            // 固定列头
            for (String header : preview.getFixedHeaders()) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }

            // 动态列头（考核点名称）
            for (ScoreTemplatePreviewResponse.AssessmentPointHeader apHeader : preview.getDynamicHeaders()) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(apHeader.getApName());
                cell.setCellStyle(headerStyle);
            }

            // 5. 创建第二行：满分信息
            org.apache.poi.xssf.usermodel.XSSFRow scoreRow = sheet.createRow(1);
            cellIndex = 0;

            // 固定列显示"满分"
            org.apache.poi.xssf.usermodel.XSSFCell labelCell = scoreRow.createCell(cellIndex++);
            labelCell.setCellValue("满分");
            labelCell.setCellStyle(headerStyle);

            org.apache.poi.xssf.usermodel.XSSFCell emptyCell = scoreRow.createCell(cellIndex++);
            emptyCell.setCellValue("");
            emptyCell.setCellStyle(headerStyle);

            // 动态列显示满分值
            for (ScoreTemplatePreviewResponse.AssessmentPointHeader apHeader : preview.getDynamicHeaders()) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = scoreRow.createCell(cellIndex++);
                cell.setCellValue(apHeader.getFullScore());
                cell.setCellStyle(headerStyle);
            }

            // 6. 创建第三行：课程目标信息
            org.apache.poi.xssf.usermodel.XSSFRow coRow = sheet.createRow(2);
            cellIndex = 0;

            org.apache.poi.xssf.usermodel.XSSFCell coLabelCell = coRow.createCell(cellIndex++);
            coLabelCell.setCellValue("课程目标");
            coLabelCell.setCellStyle(headerStyle);

            org.apache.poi.xssf.usermodel.XSSFCell coEmptyCell = coRow.createCell(cellIndex++);
            coEmptyCell.setCellValue("");
            coEmptyCell.setCellStyle(headerStyle);

            for (ScoreTemplatePreviewResponse.AssessmentPointHeader apHeader : preview.getDynamicHeaders()) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = coRow.createCell(cellIndex++);
                cell.setCellValue(apHeader.getObjectiveCode() != null ? apHeader.getObjectiveCode() : "");
                cell.setCellStyle(headerStyle);
            }

            // 7. 填充学生数据行
            int rowIndex = 3;
            for (ScoreTemplatePreviewResponse.StudentScoreRow studentRow : preview.getRows()) {
                org.apache.poi.xssf.usermodel.XSSFRow dataRow = sheet.createRow(rowIndex++);
                cellIndex = 0;

                // 学号
                org.apache.poi.xssf.usermodel.XSSFCell studentNoCell = dataRow.createCell(cellIndex++);
                studentNoCell.setCellValue(studentRow.getStudentNo());

                // 姓名
                org.apache.poi.xssf.usermodel.XSSFCell studentNameCell = dataRow.createCell(cellIndex++);
                studentNameCell.setCellValue(studentRow.getStudentName());

                // 成绩列（留空供填写）
                for (int i = 0; i < preview.getDynamicHeaders().size(); i++) {
                    dataRow.createCell(cellIndex++);
                }
            }

            // 8. 自动调整列宽
            for (int i = 0; i < cellIndex; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            // 9. 转换为字节数组
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (java.io.IOException e) {
            throw new BusinessException(500, "生成Excel模板失败：" + e.getMessage());
        }
    }

    @Override
    public ScoreImportPreviewResponse importScorePreview(ScoreImportRequest request) {
        // 1. 验证教学班存在
        TeachingClass tc = teachingClassMapper.selectById(request.getClassId());
        if (tc == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 2. 检查计算状态
        if ("locked".equals(tc.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班已锁定，无法导入成绩");
        }

        // 3. 获取教学班学生名单
        List<StudentClass> scList = studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, request.getClassId()));
        Map<String, Student> studentNoMap = new HashMap<>();
        if (!scList.isEmpty()) {
            List<Long> studentIds = scList.stream().map(StudentClass::getStudentId).collect(Collectors.toList());
            studentNoMap = studentMapper.selectBatchIds(studentIds).stream()
                    .collect(Collectors.toMap(Student::getStudentNo, s -> s));
        }

        // 4. 获取考核点
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>().eq(CourseObjective::getCourseId, tc.getCourseId()));
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).collect(Collectors.toList());
        List<AssessmentPoint> apList = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>().in(AssessmentPoint::getCoId, coIds));
        Map<String, AssessmentPoint> apNameMap = apList.stream()
                .collect(Collectors.toMap(AssessmentPoint::getApName, ap -> ap));

        // 5. 解析导入数据（简化处理，实际应解析Excel）
        // 这里假设 jsonData 是一个简化的 JSON 格式
        List<ScoreImportPreviewResponse.SuccessRow> successRows = new ArrayList<>();
        List<ScoreImportPreviewResponse.FailRow> failRows = new ArrayList<>();

        // 简化示例：假设数据格式正确
        // 实际实现需要解析 Excel 并进行详细校验

        return ScoreImportPreviewResponse.builder()
                .totalRows(successRows.size() + failRows.size())
                .successCount(successRows.size())
                .failCount(failRows.size())
                .canSave(failRows.isEmpty())
                .successRows(successRows)
                .failRows(failRows)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScores(ScoreSaveRequest request) {
        // 1. 验证教学班存在
        TeachingClass tc = teachingClassMapper.selectById(request.getClassId());
        if (tc == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 2. 检查计算状态
        if ("locked".equals(tc.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班已锁定，无法保存成绩");
        }

        // 3. 保存成绩
        for (ScoreSaveRequest.ScoreItem item : request.getScores()) {
            // 验证学生属于当前教学班
            Long scCount = studentClassMapper.selectCount(
                    new LambdaQueryWrapper<StudentClass>()
                            .eq(StudentClass::getClassId, request.getClassId())
                            .eq(StudentClass::getStudentId, item.getStudentId()));
            if (scCount == null || scCount == 0) {
                throw new BusinessException(400, "学生不属于当前教学班");
            }

            // 验证考核点存在
            AssessmentPoint ap = assessmentPointMapper.selectById(item.getApId());
            if (ap == null) {
                throw new BusinessException(400, "考核点不存在");
            }

            // 验证成绩范围
            if (item.getActualScore() < 0 || item.getActualScore() > ap.getFullScore()) {
                throw new BusinessException(400, "成绩超出满分范围");
            }

            // 保存或更新成绩
            StudentAssessmentScore existing = sasMapper.selectOne(
                    new LambdaQueryWrapper<StudentAssessmentScore>()
                            .eq(StudentAssessmentScore::getStudentId, item.getStudentId())
                            .eq(StudentAssessmentScore::getApId, item.getApId())
                            .eq(StudentAssessmentScore::getClassId, request.getClassId()));

            if (existing != null) {
                existing.setActualScore(item.getActualScore());
                sasMapper.updateById(existing);
            } else {
                StudentAssessmentScore sas = new StudentAssessmentScore();
                sas.setStudentId(item.getStudentId());
                sas.setApId(item.getApId());
                sas.setClassId(request.getClassId());
                sas.setActualScore(item.getActualScore());
                sasMapper.insert(sas);
            }
        }

        // 4. 更新教学班状态
        tc.setCalcStatus("score_imported");
        teachingClassMapper.updateById(tc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseCalcResponse calcCourseAchievement(CourseCalcRequest request) {
        // 1. 验证教学班存在
        TeachingClass tc = teachingClassMapper.selectById(request.getClassId());
        if (tc == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 2. 检查是否有成绩
        Long scoreCount = sasMapper.selectCount(
                new LambdaQueryWrapper<StudentAssessmentScore>()
                        .eq(StudentAssessmentScore::getClassId, request.getClassId()));
        if (scoreCount == null || scoreCount == 0) {
            throw new BusinessException(400, "当前教学班没有成绩数据，请先导入成绩");
        }

        // 3. 获取课程目标和考核点
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>().eq(CourseObjective::getCourseId, tc.getCourseId()));
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).collect(Collectors.toList());
        List<AssessmentPoint> apList = assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>().in(AssessmentPoint::getCoId, coIds));

        // 4. 获取学生名单
        List<StudentClass> scList = studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, request.getClassId()));
        List<Long> studentIds = scList.stream().map(StudentClass::getStudentId).collect(Collectors.toList());

        // 5. 获取所有成绩
        List<StudentAssessmentScore> allScores = sasMapper.selectList(
                new LambdaQueryWrapper<StudentAssessmentScore>()
                        .eq(StudentAssessmentScore::getClassId, request.getClassId()));

        // 按学生分组
        Map<Long, List<StudentAssessmentScore>> studentScoresMap = allScores.stream()
                .collect(Collectors.groupingBy(StudentAssessmentScore::getStudentId));

        // 按考核点分组
        Map<Long, List<StudentAssessmentScore>> apScoresMap = allScores.stream()
                .collect(Collectors.groupingBy(StudentAssessmentScore::getApId));

        // 6. 计算学生-课程目标达成度 Cij
        // Cij = Σ(支撑目标j的考核点实际得分) / Σ(支撑目标j的考核点满分)
        Map<Long, Map<Long, Float>> studentObjectiveAchievement = new HashMap<>(); // studentId -> coId -> achievement

        for (Long studentId : studentIds) {
            Map<Long, Float> coAchievement = new HashMap<>();
            List<StudentAssessmentScore> studentScores = studentScoresMap.getOrDefault(studentId, List.of());

            for (CourseObjective co : objectives) {
                // 获取支撑该课程目标的考核点
                List<AssessmentPoint> coApList = apList.stream()
                        .filter(ap -> ap.getCoId().equals(co.getCoId()))
                        .collect(Collectors.toList());

                if (coApList.isEmpty()) {
                    coAchievement.put(co.getCoId(), 0f);
                    continue;
                }

                float totalActual = 0;
                float totalFull = 0;

                for (AssessmentPoint ap : coApList) {
                    totalFull += ap.getFullScore();
                    Optional<StudentAssessmentScore> scoreOpt = studentScores.stream()
                            .filter(s -> s.getApId().equals(ap.getApId()))
                            .findFirst();
                    if (scoreOpt.isPresent()) {
                        totalActual += scoreOpt.get().getActualScore();
                    }
                }

                float achievement = totalFull > 0 ? totalActual / totalFull : 0;
                coAchievement.put(co.getCoId(), Math.min(achievement, 1.0f));
            }

            studentObjectiveAchievement.put(studentId, coAchievement);

            // 保存学生-课程目标达成度到数据库
            for (Map.Entry<Long, Float> entry : coAchievement.entrySet()) {
                Long coId = entry.getKey();
                Float achievement = entry.getValue();

                StudentObjectiveAchievement existingSoa = soaMapper.selectOne(
                        new LambdaQueryWrapper<StudentObjectiveAchievement>()
                                .eq(StudentObjectiveAchievement::getStudentId, studentId)
                                .eq(StudentObjectiveAchievement::getClassId, request.getClassId())
                                .eq(StudentObjectiveAchievement::getCoId, coId));
                if (existingSoa != null) {
                    existingSoa.setAchievement(achievement);
                    soaMapper.updateById(existingSoa);
                } else {
                    StudentObjectiveAchievement soa = new StudentObjectiveAchievement();
                    soa.setStudentId(studentId);
                    soa.setClassId(request.getClassId());
                    soa.setCoId(coId);
                    soa.setAchievement(achievement);
                    soaMapper.insert(soa);
                }
            }
        }

        // 7. 计算班级课程目标平均达成度 C̄j
        // C̄j = 全班 Cij 的算术平均
        List<CourseCalcResponse.ObjectiveAchievement> objectiveAchievements = new ArrayList<>();
        Map<Long, Float> classObjectiveAchievement = new HashMap<>(); // coId -> averageAchievement

        for (CourseObjective co : objectives) {
            float sum = 0;
            int count = 0;
            for (Long studentId : studentIds) {
                Map<Long, Float> coMap = studentObjectiveAchievement.get(studentId);
                if (coMap != null && coMap.containsKey(co.getCoId())) {
                    sum += coMap.get(co.getCoId());
                    count++;
                }
            }
            float average = count > 0 ? sum / count : 0;
            classObjectiveAchievement.put(co.getCoId(), average);

            objectiveAchievements.add(CourseCalcResponse.ObjectiveAchievement.builder()
                    .coId(co.getCoId())
                    .objectiveCode(co.getObjectiveCode())
                    .description(co.getCoDescription())
                    .averageAchievement(average)
                    .build());

            // 保存到数据库
            CourseObjectiveAchievement existing = coaMapper.selectOne(
                    new LambdaQueryWrapper<CourseObjectiveAchievement>()
                            .eq(CourseObjectiveAchievement::getClassId, request.getClassId())
                            .eq(CourseObjectiveAchievement::getCoId, co.getCoId()));
            if (existing != null) {
                existing.setAverageAchievement(average);
                coaMapper.updateById(existing);
            } else {
                CourseObjectiveAchievement coa = new CourseObjectiveAchievement();
                coa.setClassId(request.getClassId());
                coa.setCoId(co.getCoId());
                coa.setAverageAchievement(average);
                coaMapper.insert(coa);
            }
        }

        // 8. 计算课程级指标点达成度 Ek
        // Ek = Σ(C̄j × wjk)
        List<CourseCalcResponse.IndicatorAchievement> indicatorAchievements = new ArrayList<>();

        // 获取内部权重 w
        List<ObjectiveIndicatorContribution> oicList = oicMapper.selectList(
                new LambdaQueryWrapper<ObjectiveIndicatorContribution>()
                        .in(ObjectiveIndicatorContribution::getCoId, coIds));

        // 按指标点分组
        Map<Long, List<ObjectiveIndicatorContribution>> ipOicMap = oicList.stream()
                .collect(Collectors.groupingBy(ObjectiveIndicatorContribution::getIpId));

        for (Map.Entry<Long, List<ObjectiveIndicatorContribution>> entry : ipOicMap.entrySet()) {
            Long ipId = entry.getKey();
            List<ObjectiveIndicatorContribution> oics = entry.getValue();

            float ek = 0;
            for (ObjectiveIndicatorContribution oic : oics) {
                Float cj = classObjectiveAchievement.get(oic.getCoId());
                if (cj != null) {
                    ek += cj * oic.getInternalWeight();
                }
            }
            ek = Math.min(ek, 1.0f);

            // 获取指标点信息
            IndicatorPoint ip = indicatorPointMapper.selectById(ipId);

            indicatorAchievements.add(CourseCalcResponse.IndicatorAchievement.builder()
                    .ipId(ipId)
                    .ipCode(ip != null ? ip.getIpCode() : "")
                    .ipDescription(ip != null ? ip.getIpDescription() : "")
                    .achievement(ek)
                    .build());

            // 保存到数据库
            CourseIndicatorAchievement existing = ciaMapper.selectOne(
                    new LambdaQueryWrapper<CourseIndicatorAchievement>()
                            .eq(CourseIndicatorAchievement::getClassId, request.getClassId())
                            .eq(CourseIndicatorAchievement::getIpId, ipId));
            if (existing != null) {
                existing.setAchievement(ek);
                ciaMapper.updateById(existing);
            } else {
                CourseIndicatorAchievement cia = new CourseIndicatorAchievement();
                cia.setClassId(request.getClassId());
                cia.setIpId(ipId);
                cia.setAchievement(ek);
                cia.setIsLocked(false);
                ciaMapper.insert(cia);
            }
        }

        // 9. 更新教学班状态
        tc.setCalcStatus("calculating");
        teachingClassMapper.updateById(tc);

        // 10. 获取课程信息
        Course course = courseMapper.selectById(tc.getCourseId());

        return CourseCalcResponse.builder()
                .classId(request.getClassId())
                .className(tc.getClassName())
                .courseName(course != null ? course.getCourseName() : null)
                .studentCount(studentIds.size())
                .objectiveAchievements(objectiveAchievements)
                .indicatorAchievements(indicatorAchievements)
                .isLocked(false)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MajorCalcResponse calcMajorAchievement(MajorCalcRequest request) {
        // 1. 验证专业和学期存在
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        AcademicTerm term = academicTermMapper.selectById(request.getTermId());
        if (term == null) {
            throw new BusinessException(404, "学期不存在");
        }

        // 2. 前置校验：检查所有支撑课程是否已完成课程级计算
        // 获取该专业在该学期的所有课程
        List<CourseMajor> cmList = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getMajorId, request.getMajorId()));
        List<Long> courseIds = cmList.stream().map(CourseMajor::getCourseId).collect(Collectors.toList());

        // 获取这些课程在该学期的教学班
        List<TeachingClass> tcList = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getTermId, request.getTermId()));

        // 检查是否所有教学班都已完成计算
        for (TeachingClass tc : tcList) {
            if (!"locked".equals(tc.getCalcStatus())) {
                throw new BusinessException(400, "存在未完成计算的教学班：" + tc.getClassName());
            }
        }

        // 3. 获取所有课程级指标点达成度
        List<Long> classIds = tcList.stream().map(TeachingClass::getClassId).collect(Collectors.toList());
        List<CourseIndicatorAchievement> ciaList = ciaMapper.selectList(
                new LambdaQueryWrapper<CourseIndicatorAchievement>()
                        .in(CourseIndicatorAchievement::getClassId, classIds));

        // 4. 获取宏观支撑权重 W
        List<CourseIndicatorSupport> cisList = cisMapper.selectList(
                new LambdaQueryWrapper<CourseIndicatorSupport>()
                        .in(CourseIndicatorSupport::getCourseId, courseIds));

        // 5. 计算专业级指标点达成度 Gk
        // Gk = Σ(Eck × Wck)
        Map<Long, Float> ipFinalAchievement = new HashMap<>(); // ipId -> finalAchievement

        // 按指标点分组
        Map<Long, List<CourseIndicatorAchievement>> ipCiaMap = ciaList.stream()
                .collect(Collectors.groupingBy(CourseIndicatorAchievement::getIpId));

        for (Map.Entry<Long, List<CourseIndicatorAchievement>> entry : ipCiaMap.entrySet()) {
            Long ipId = entry.getKey();
            List<CourseIndicatorAchievement> cias = entry.getValue();

            float gk = 0;
            for (CourseIndicatorAchievement cia : cias) {
                // 找到对应的教学班
                TeachingClass tc = tcList.stream()
                        .filter(t -> t.getClassId().equals(cia.getClassId()))
                        .findFirst().orElse(null);
                if (tc == null) continue;

                // 找到对应的支撑权重 W
                Optional<CourseIndicatorSupport> cisOpt = cisList.stream()
                        .filter(c -> c.getCourseId().equals(tc.getCourseId()) && c.getIpId().equals(ipId))
                        .findFirst();
                if (cisOpt.isPresent()) {
                    gk += cia.getAchievement() * cisOpt.get().getTotalWeight();
                }
            }
            gk = Math.min(gk, 1.0f);
            ipFinalAchievement.put(ipId, gk);
        }

        // 6. 保存专业级达成度
        List<MajorCalcResponse.IndicatorAchievement> indicatorAchievements = new ArrayList<>();
        for (Map.Entry<Long, Float> entry : ipFinalAchievement.entrySet()) {
            Long ipId = entry.getKey();
            Float finalAch = entry.getValue();

            MajorIndicatorAchievement existing = miaMapper.selectOne(
                    new LambdaQueryWrapper<MajorIndicatorAchievement>()
                            .eq(MajorIndicatorAchievement::getMajorId, request.getMajorId())
                            .eq(MajorIndicatorAchievement::getTermId, request.getTermId())
                            .eq(MajorIndicatorAchievement::getIpId, ipId));
            if (existing != null) {
                existing.setFinalAchievement(finalAch);
                miaMapper.updateById(existing);
            } else {
                MajorIndicatorAchievement mia = new MajorIndicatorAchievement();
                mia.setMajorId(request.getMajorId());
                mia.setTermId(request.getTermId());
                mia.setIpId(ipId);
                mia.setFinalAchievement(finalAch);
                miaMapper.insert(mia);
            }

            IndicatorPoint ip = indicatorPointMapper.selectById(ipId);
            indicatorAchievements.add(MajorCalcResponse.IndicatorAchievement.builder()
                    .ipId(ipId)
                    .ipCode(ip != null ? ip.getIpCode() : "")
                    .ipDescription(ip != null ? ip.getIpDescription() : "")
                    .finalAchievement(finalAch)
                    .build());
        }

        return MajorCalcResponse.builder()
                .majorId(request.getMajorId())
                .majorName(major.getMajorName())
                .termId(request.getTermId())
                .termCode(term.getTermCode())
                .indicatorAchievements(indicatorAchievements)
                .build();
    }

    @Override
    public CourseCalcStatusResponse getCourseCalcStatus(Long majorId, Long termId) {
        // 1. 验证专业和学期存在
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        AcademicTerm term = academicTermMapper.selectById(termId);
        if (term == null) {
            throw new BusinessException(404, "学期不存在");
        }

        // 2. 获取该专业在该学期的所有课程
        List<CourseMajor> cmList = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getMajorId, majorId));
        List<Long> courseIds = cmList.stream().map(CourseMajor::getCourseId).collect(Collectors.toList());

        // 3. 获取这些课程在该学期的教学班
        List<TeachingClass> tcList = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getTermId, termId));

        // 4. 构建课程状态列表
        List<CourseCalcStatusResponse.CourseStatus> courseStatuses = new ArrayList<>();
        boolean allLocked = true;

        for (TeachingClass tc : tcList) {
            Course course = courseMapper.selectById(tc.getCourseId());
            boolean isLocked = "locked".equals(tc.getCalcStatus());

            if (!isLocked) {
                allLocked = false;
            }

            courseStatuses.add(CourseCalcStatusResponse.CourseStatus.builder()
                    .courseId(tc.getCourseId())
                    .courseCode(course != null ? course.getCourseCode() : null)
                    .courseName(course != null ? course.getCourseName() : null)
                    .classId(tc.getClassId())
                    .className(tc.getClassName())
                    .calcStatus(tc.getCalcStatus())
                    .isLocked(isLocked)
                    .build());
        }

        // 5. 判断是否满足专业级汇总前置条件
        String blockReason = null;
        if (!allLocked) {
            blockReason = "存在未完成计算的教学班";
        }

        return CourseCalcStatusResponse.builder()
                .majorId(majorId)
                .majorName(major.getMajorName())
                .termId(termId)
                .termCode(term.getTermCode())
                .canCalcMajor(allLocked)
                .blockReason(blockReason)
                .courseStatuses(courseStatuses)
                .build();
    }
}
