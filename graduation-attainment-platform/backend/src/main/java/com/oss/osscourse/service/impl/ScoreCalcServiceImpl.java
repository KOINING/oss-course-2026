package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.achievement.CourseCalcRequest;
import com.oss.osscourse.dto.achievement.CourseCalcResponse;
import com.oss.osscourse.dto.achievement.CourseCalcStatusResponse;
import com.oss.osscourse.dto.achievement.CourseObjectiveDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcRequest;
import com.oss.osscourse.dto.achievement.MajorCalcResponse;
import com.oss.osscourse.dto.achievement.UnlockRequestCreateRequest;
import com.oss.osscourse.dto.score.ScoreImportPreviewResponse;
import com.oss.osscourse.dto.score.ScoreImportRequest;
import com.oss.osscourse.dto.score.ScoreSaveRequest;
import com.oss.osscourse.dto.score.ScoreTemplatePreviewResponse;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.CourseIndicatorSupport;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import com.oss.osscourse.entity.GraduationRequirement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.MajorIndicatorAchievement;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.StudentObjectiveAchievement;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.entity.UnlockAuditLog;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseIndicatorSupportMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveAchievementMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.GraduationRequirementMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorIndicatorAchievementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.mapper.StudentObjectiveAchievementMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.UnlockAuditLogMapper;
import com.oss.osscourse.service.ScoreCalcService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreCalcServiceImpl implements ScoreCalcService {

    private static final int HEADER_ROW_INDEX = 0;
    private static final int FULL_SCORE_ROW_INDEX = 1;
    private static final int OBJECTIVE_ROW_INDEX = 2;
    private static final int DATA_START_ROW_INDEX = 3;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

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
    private final GraduationRequirementMapper graduationRequirementMapper;
    private final IndicatorPointMapper indicatorPointMapper;
    private final TeacherMapper teacherMapper;
    private final UnlockAuditLogMapper unlockAuditLogMapper;

    @Override
    public ScoreTemplatePreviewResponse previewTemplate(Long classId) {
        TeachingClass teachingClass = requireTeachingClass(classId);
        List<StudentClass> studentClasses = listStudentsInClass(classId);
        if (studentClasses.isEmpty()) {
            throw new BusinessException(400, "当前教学班没有学生名单，请先导入学生");
        }

        List<Long> studentIds = studentClasses.stream().map(StudentClass::getStudentId).toList();
        Map<Long, Student> studentMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, student -> student));

        List<CourseObjective> objectives = listCourseObjectives(teachingClass.getCourseId());
        if (objectives.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置课程目标，请先配置课程目标");
        }

        List<AssessmentPoint> assessmentPoints = listAssessmentPointsByObjectives(objectives);
        if (assessmentPoints.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置考核点，请先配置考核点");
        }

        ensureInternalWeightsConfigured(objectives, teachingClass);

        Map<Long, CourseObjective> objectiveMap = objectives.stream()
                .collect(Collectors.toMap(CourseObjective::getCoId, objective -> objective));
        Map<Long, Map<Long, Float>> savedScoreMap = loadSavedScoreMap(classId);

        List<ScoreTemplatePreviewResponse.AssessmentPointHeader> dynamicHeaders = assessmentPoints.stream()
                .map(assessmentPoint -> {
                    CourseObjective objective = objectiveMap.get(assessmentPoint.getCoId());
                    return ScoreTemplatePreviewResponse.AssessmentPointHeader.builder()
                            .apId(assessmentPoint.getApId())
                            .apName(assessmentPoint.getApName())
                            .fullScore(assessmentPoint.getFullScore())
                            .objectiveCode(objective == null ? null : objective.getObjectiveCode())
                            .build();
                })
                .toList();

        List<ScoreTemplatePreviewResponse.StudentScoreRow> rows = new ArrayList<>();
        for (Long studentId : studentIds) {
            Student student = studentMap.get(studentId);
            if (student == null) {
                continue;
            }
            List<Float> scores = new ArrayList<>();
            Map<Long, Float> studentSavedScores = savedScoreMap.getOrDefault(studentId, Map.of());
            for (AssessmentPoint assessmentPoint : assessmentPoints) {
                scores.add(studentSavedScores.get(assessmentPoint.getApId()));
            }
            rows.add(ScoreTemplatePreviewResponse.StudentScoreRow.builder()
                    .studentId(studentId)
                    .studentNo(student.getStudentNo())
                    .studentName(student.getStudentName())
                    .scores(scores)
                    .build());
        }

        Course course = courseMapper.selectById(teachingClass.getCourseId());
        return ScoreTemplatePreviewResponse.builder()
                .classId(classId)
                .className(teachingClass.getClassName())
                .courseName(course == null ? null : course.getCourseName())
                .studentCount(rows.size())
                .assessmentPointCount(assessmentPoints.size())
                .fixedHeaders(List.of("学号", "姓名"))
                .dynamicHeaders(dynamicHeaders)
                .rows(rows)
                .build();
    }

    @Override
    public byte[] downloadTemplate(Long classId) {
        ScoreTemplatePreviewResponse preview = previewTemplate(classId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("成绩模板");

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            XSSFRow headerRow = sheet.createRow(HEADER_ROW_INDEX);
            int cellIndex = 0;
            for (String header : preview.getFixedHeaders()) {
                XSSFCell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }
            for (ScoreTemplatePreviewResponse.AssessmentPointHeader header : preview.getDynamicHeaders()) {
                XSSFCell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(header.getApName());
                cell.setCellStyle(headerStyle);
            }

            XSSFRow fullScoreRow = sheet.createRow(FULL_SCORE_ROW_INDEX);
            cellIndex = 0;
            XSSFCell labelCell = fullScoreRow.createCell(cellIndex++);
            labelCell.setCellValue("满分");
            labelCell.setCellStyle(headerStyle);
            XSSFCell emptyCell = fullScoreRow.createCell(cellIndex++);
            emptyCell.setCellValue("");
            emptyCell.setCellStyle(headerStyle);
            for (ScoreTemplatePreviewResponse.AssessmentPointHeader header : preview.getDynamicHeaders()) {
                XSSFCell cell = fullScoreRow.createCell(cellIndex++);
                cell.setCellValue(header.getFullScore());
                cell.setCellStyle(headerStyle);
            }

            XSSFRow objectiveRow = sheet.createRow(OBJECTIVE_ROW_INDEX);
            cellIndex = 0;
            XSSFCell objectiveLabelCell = objectiveRow.createCell(cellIndex++);
            objectiveLabelCell.setCellValue("课程目标");
            objectiveLabelCell.setCellStyle(headerStyle);
            XSSFCell objectiveEmptyCell = objectiveRow.createCell(cellIndex++);
            objectiveEmptyCell.setCellValue("");
            objectiveEmptyCell.setCellStyle(headerStyle);
            for (ScoreTemplatePreviewResponse.AssessmentPointHeader header : preview.getDynamicHeaders()) {
                XSSFCell cell = objectiveRow.createCell(cellIndex++);
                cell.setCellValue(header.getObjectiveCode() == null ? "" : header.getObjectiveCode());
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = DATA_START_ROW_INDEX;
            for (ScoreTemplatePreviewResponse.StudentScoreRow studentRow : preview.getRows()) {
                XSSFRow dataRow = sheet.createRow(rowIndex++);
                cellIndex = 0;
                dataRow.createCell(cellIndex++).setCellValue(studentRow.getStudentNo());
                dataRow.createCell(cellIndex++).setCellValue(studentRow.getStudentName());
                for (int i = 0; i < preview.getDynamicHeaders().size(); i++) {
                    dataRow.createCell(cellIndex++);
                }
            }

            for (int i = 0; i < preview.getDynamicHeaders().size() + 2; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "生成 Excel 模板失败: " + e.getMessage());
        }
    }

    @Override
    public ScoreImportPreviewResponse importScorePreview(ScoreImportRequest request) {
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        if ("locked".equals(teachingClass.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班已锁定，无法导入成绩");
        }

        List<StudentClass> studentClasses = listStudentsInClass(request.getClassId());
        if (studentClasses.isEmpty()) {
            throw new BusinessException(400, "当前教学班没有学生名单，无法导入成绩");
        }

        List<Long> studentIds = studentClasses.stream().map(StudentClass::getStudentId).toList();
        Map<String, Student> studentNoMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentNo, student -> student));

        List<CourseObjective> objectives = listCourseObjectives(teachingClass.getCourseId());
        if (objectives.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置课程目标，无法导入成绩");
        }

        List<AssessmentPoint> assessmentPoints = listAssessmentPointsByObjectives(objectives);
        if (assessmentPoints.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置考核点，无法导入成绩");
        }

        ensureInternalWeightsConfigured(objectives, teachingClass);

        Map<Long, CourseObjective> objectiveMap = objectives.stream()
                .collect(Collectors.toMap(CourseObjective::getCoId, objective -> objective));
        Map<String, AssessmentPoint> apNameMap = assessmentPoints.stream()
                .collect(Collectors.toMap(ap -> normalize(ap.getApName()), ap -> ap, (left, right) -> left, LinkedHashMap::new));

        List<List<String>> table = readImportTable(request);
        validateTemplateMetadata(table, apNameMap, objectiveMap);

        Map<Integer, AssessmentPoint> columnAssessmentPoints = mapAssessmentPointColumns(table.get(HEADER_ROW_INDEX), apNameMap);
        List<ScoreImportPreviewResponse.SuccessRow> successRows = new ArrayList<>();
        List<ScoreImportPreviewResponse.FailRow> failRows = new ArrayList<>();
        List<ScoreSaveRequest.ScoreItem> scoreItems = new ArrayList<>();
        Set<String> seenStudentNos = new HashSet<>();
        int totalRows = 0;

        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex < table.size(); rowIndex++) {
            List<String> row = table.get(rowIndex);
            if (isEmptyDataRow(row)) {
                continue;
            }
            totalRows++;

            String studentNo = valueAt(row, 0);
            String studentName = valueAt(row, 1);
            List<String> errors = new ArrayList<>();

            if (isBlank(studentNo)) {
                errors.add("学号为空");
            }
            if (isBlank(studentName)) {
                errors.add("姓名为空");
            }
            if (!isBlank(studentNo) && !seenStudentNos.add(studentNo)) {
                errors.add("文件内存在重复学号");
            }

            Student student = null;
            if (!isBlank(studentNo)) {
                student = studentNoMap.get(studentNo);
                if (student == null) {
                    errors.add("学号不属于当前教学班");
                } else if (!isBlank(studentName) && !Objects.equals(normalize(student.getStudentName()), normalize(studentName))) {
                    errors.add("姓名与教学班学生名单不一致");
                }
            }

            List<ScoreSaveRequest.ScoreItem> rowScoreItems = new ArrayList<>();
            for (Map.Entry<Integer, AssessmentPoint> entry : columnAssessmentPoints.entrySet()) {
                Integer columnIndex = entry.getKey();
                AssessmentPoint assessmentPoint = entry.getValue();
                String rawScore = valueAt(row, columnIndex);
                if (isBlank(rawScore)) {
                    errors.add("考核点“" + assessmentPoint.getApName() + "”成绩为空");
                    continue;
                }
                Float actualScore;
                try {
                    actualScore = Float.parseFloat(rawScore);
                } catch (NumberFormatException ex) {
                    errors.add("考核点“" + assessmentPoint.getApName() + "”成绩不是合法数字");
                    continue;
                }
                if (actualScore < 0 || actualScore > assessmentPoint.getFullScore()) {
                    errors.add("考核点“" + assessmentPoint.getApName() + "”成绩超出满分范围");
                    continue;
                }
                if (student != null) {
                    ScoreSaveRequest.ScoreItem item = new ScoreSaveRequest.ScoreItem();
                    item.setStudentId(student.getStudentId());
                    item.setApId(assessmentPoint.getApId());
                    item.setActualScore(actualScore);
                    rowScoreItems.add(item);
                }
            }

            if (errors.isEmpty()) {
                successRows.add(ScoreImportPreviewResponse.SuccessRow.builder()
                        .rowIndex(rowIndex + 1)
                        .studentNo(studentNo)
                        .studentName(studentName)
                        .studentId(student.getStudentId())
                        .scoreCount(rowScoreItems.size())
                        .build());
                scoreItems.addAll(rowScoreItems);
            } else {
                failRows.add(ScoreImportPreviewResponse.FailRow.builder()
                        .rowIndex(rowIndex + 1)
                        .studentNo(studentNo)
                        .studentName(studentName)
                        .errorMessage(String.join("；", errors))
                        .build());
            }
        }

        return ScoreImportPreviewResponse.builder()
                .totalRows(totalRows)
                .successCount(successRows.size())
                .failCount(failRows.size())
                .canSave(totalRows > 0 && failRows.isEmpty())
                .successRows(successRows)
                .failRows(failRows)
                .scoreItems(scoreItems)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScores(ScoreSaveRequest request) {
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        if ("locked".equals(teachingClass.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班已锁定，无法保存成绩");
        }
        if (request.getScores() == null || request.getScores().isEmpty()) {
            throw new BusinessException(400, "没有可保存的成绩数据");
        }

        for (ScoreSaveRequest.ScoreItem item : request.getScores()) {
            Long scCount = studentClassMapper.selectCount(
                    new LambdaQueryWrapper<StudentClass>()
                            .eq(StudentClass::getClassId, request.getClassId())
                            .eq(StudentClass::getStudentId, item.getStudentId()));
            if (scCount == null || scCount == 0) {
                throw new BusinessException(400, "学生不属于当前教学班");
            }

            AssessmentPoint assessmentPoint = assessmentPointMapper.selectById(item.getApId());
            if (assessmentPoint == null) {
                throw new BusinessException(400, "考核点不存在");
            }

            if (item.getActualScore() == null
                    || item.getActualScore() < 0
                    || item.getActualScore() > assessmentPoint.getFullScore()) {
                throw new BusinessException(400, "成绩超出满分范围");
            }

            StudentAssessmentScore existing = sasMapper.selectOne(
                    new LambdaQueryWrapper<StudentAssessmentScore>()
                            .eq(StudentAssessmentScore::getStudentId, item.getStudentId())
                            .eq(StudentAssessmentScore::getApId, item.getApId())
                            .eq(StudentAssessmentScore::getClassId, request.getClassId()));

            if (existing != null) {
                existing.setActualScore(item.getActualScore());
                sasMapper.updateById(existing);
            } else {
                StudentAssessmentScore score = new StudentAssessmentScore();
                score.setStudentId(item.getStudentId());
                score.setApId(item.getApId());
                score.setClassId(request.getClassId());
                score.setActualScore(item.getActualScore());
                sasMapper.insert(score);
            }
        }

        teachingClass.setCalcStatus("score_imported");
        teachingClassMapper.updateById(teachingClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseCalcResponse calcCourseAchievement(CourseCalcRequest request) {
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        if ("locked".equals(teachingClass.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班已锁定，不能重复计算");
        }

        Long scoreCount = sasMapper.selectCount(
                new LambdaQueryWrapper<StudentAssessmentScore>()
                        .eq(StudentAssessmentScore::getClassId, request.getClassId()));
        if (scoreCount == null || scoreCount == 0) {
            throw new BusinessException(400, "当前教学班没有成绩数据，请先导入成绩");
        }

        List<CourseObjective> objectives = listCourseObjectives(teachingClass.getCourseId());
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).toList();
        List<AssessmentPoint> assessmentPoints = listAssessmentPointsByObjectives(objectives);
        List<ObjectiveIndicatorContribution> internalWeights = listInternalWeights(
                coIds,
                teachingClass.getMajorId(),
                teachingClass.getGradeYear());
        if (internalWeights.isEmpty()) {
            throw new BusinessException(400, "当前课程未配置内部权重 w，不能执行课程级计算");
        }

        validateInternalWeightSums(internalWeights);

        List<StudentClass> studentClasses = listStudentsInClass(request.getClassId());
        List<Long> studentIds = studentClasses.stream().map(StudentClass::getStudentId).toList();
        List<StudentAssessmentScore> allScores = sasMapper.selectList(
                new LambdaQueryWrapper<StudentAssessmentScore>()
                        .eq(StudentAssessmentScore::getClassId, request.getClassId()));
        ensureAllScoresCompleted(studentIds, assessmentPoints, allScores);

        Map<Long, List<StudentAssessmentScore>> studentScoresMap = allScores.stream()
                .collect(Collectors.groupingBy(StudentAssessmentScore::getStudentId));

        Map<Long, Map<Long, Float>> studentObjectiveAchievement = new HashMap<>();
        for (Long studentId : studentIds) {
            Map<Long, Float> coAchievement = new HashMap<>();
            List<StudentAssessmentScore> studentScores = studentScoresMap.getOrDefault(studentId, List.of());

            for (CourseObjective objective : objectives) {
                List<AssessmentPoint> objectiveAssessmentPoints = assessmentPoints.stream()
                        .filter(ap -> ap.getCoId().equals(objective.getCoId()))
                        .toList();
                if (objectiveAssessmentPoints.isEmpty()) {
                    coAchievement.put(objective.getCoId(), 0f);
                    continue;
                }

                float totalActual = 0f;
                float totalFull = 0f;
                for (AssessmentPoint assessmentPoint : objectiveAssessmentPoints) {
                    totalFull += assessmentPoint.getFullScore();
                    Optional<StudentAssessmentScore> score = studentScores.stream()
                            .filter(item -> item.getApId().equals(assessmentPoint.getApId()))
                            .findFirst();
                    if (score.isPresent()) {
                        totalActual += score.get().getActualScore();
                    }
                }
                float achievement = totalFull > 0 ? totalActual / totalFull : 0f;
                coAchievement.put(objective.getCoId(), Math.min(achievement, 1.0f));
            }

            studentObjectiveAchievement.put(studentId, coAchievement);
            for (Map.Entry<Long, Float> entry : coAchievement.entrySet()) {
                Long coId = entry.getKey();
                Float achievement = entry.getValue();
                StudentObjectiveAchievement existing = soaMapper.selectOne(
                        new LambdaQueryWrapper<StudentObjectiveAchievement>()
                                .eq(StudentObjectiveAchievement::getStudentId, studentId)
                                .eq(StudentObjectiveAchievement::getClassId, request.getClassId())
                                .eq(StudentObjectiveAchievement::getCoId, coId));
                if (existing != null) {
                    existing.setAchievement(achievement);
                    soaMapper.updateById(existing);
                } else {
                    StudentObjectiveAchievement entity = new StudentObjectiveAchievement();
                    entity.setStudentId(studentId);
                    entity.setClassId(request.getClassId());
                    entity.setCoId(coId);
                    entity.setAchievement(achievement);
                    soaMapper.insert(entity);
                }
            }
        }

        List<CourseCalcResponse.ObjectiveAchievement> objectiveAchievements = new ArrayList<>();
        Map<Long, Float> classObjectiveAchievement = new HashMap<>();
        for (CourseObjective objective : objectives) {
            float sum = 0f;
            int count = 0;
            for (Long studentId : studentIds) {
                Map<Long, Float> coMap = studentObjectiveAchievement.get(studentId);
                if (coMap != null && coMap.containsKey(objective.getCoId())) {
                    sum += coMap.get(objective.getCoId());
                    count++;
                }
            }
            float average = count > 0 ? sum / count : 0f;
            classObjectiveAchievement.put(objective.getCoId(), average);
            objectiveAchievements.add(CourseCalcResponse.ObjectiveAchievement.builder()
                    .coId(objective.getCoId())
                    .objectiveCode(objective.getObjectiveCode())
                    .description(objective.getCoDescription())
                    .averageAchievement(average)
                    .build());

            CourseObjectiveAchievement existing = coaMapper.selectOne(
                    new LambdaQueryWrapper<CourseObjectiveAchievement>()
                            .eq(CourseObjectiveAchievement::getClassId, request.getClassId())
                            .eq(CourseObjectiveAchievement::getCoId, objective.getCoId()));
            if (existing != null) {
                existing.setAverageAchievement(average);
                coaMapper.updateById(existing);
            } else {
                CourseObjectiveAchievement entity = new CourseObjectiveAchievement();
                entity.setClassId(request.getClassId());
                entity.setCoId(objective.getCoId());
                entity.setAverageAchievement(average);
                coaMapper.insert(entity);
            }
        }

        Map<String, CourseCalcResponse.IndicatorAchievement> indicatorAchievementMap = new LinkedHashMap<>();
        Map<Long, List<ObjectiveIndicatorContribution>> ipOicMap = internalWeights.stream()
                .collect(Collectors.groupingBy(ObjectiveIndicatorContribution::getIpId));
        for (Map.Entry<Long, List<ObjectiveIndicatorContribution>> entry : ipOicMap.entrySet()) {
            Long ipId = entry.getKey();
            float ek = 0f;
            for (ObjectiveIndicatorContribution contribution : entry.getValue()) {
                Float cj = classObjectiveAchievement.get(contribution.getCoId());
                if (cj != null) {
                    ek += cj * contribution.getInternalWeight();
                }
            }
            ek = Math.min(ek, 1.0f);

            IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(ipId);
            CourseCalcResponse.IndicatorAchievement indicatorAchievement = CourseCalcResponse.IndicatorAchievement.builder()
                    .ipId(ipId)
                    .ipCode(indicatorPoint == null ? "" : indicatorPoint.getIpCode())
                    .ipDescription(indicatorPoint == null ? "" : indicatorPoint.getIpDescription())
                    .achievement(ek)
                    .build();
            indicatorAchievementMap.putIfAbsent(indicatorDisplayKey(indicatorPoint), indicatorAchievement);

            CourseIndicatorAchievement existing = ciaMapper.selectOne(
                    new LambdaQueryWrapper<CourseIndicatorAchievement>()
                            .eq(CourseIndicatorAchievement::getClassId, request.getClassId())
                            .eq(CourseIndicatorAchievement::getIpId, ipId));
            if (existing != null) {
                existing.setAchievement(ek);
                existing.setIsLocked(true);
                ciaMapper.updateById(existing);
            } else {
                CourseIndicatorAchievement entity = new CourseIndicatorAchievement();
                entity.setClassId(request.getClassId());
                entity.setIpId(ipId);
                entity.setAchievement(ek);
                entity.setIsLocked(true);
                ciaMapper.insert(entity);
            }
        }
        List<CourseCalcResponse.IndicatorAchievement> indicatorAchievements = indicatorAchievementMap.values().stream()
                .sorted((left, right) -> compareIndicatorCode(left.getIpCode(), right.getIpCode()))
                .toList();

        teachingClass.setCalcStatus("locked");
        teachingClassMapper.updateById(teachingClass);

        Course course = courseMapper.selectById(teachingClass.getCourseId());
        return CourseCalcResponse.builder()
                .classId(request.getClassId())
                .className(teachingClass.getClassName())
                .courseName(course == null ? null : course.getCourseName())
                .studentCount(studentIds.size())
                .objectiveAchievements(objectiveAchievements)
                .indicatorAchievements(indicatorAchievements)
                .isLocked(true)
                .build();
    }

    @Override
    public CourseObjectiveDashboardResponse getCourseObjectiveDashboard(Long classId) {
        TeachingClass teachingClass = requireTeachingClass(classId);
        Course course = courseMapper.selectById(teachingClass.getCourseId());

        List<CourseObjective> objectives = listCourseObjectives(teachingClass.getCourseId());
        Map<Long, Float> averageMap = coaMapper.selectList(
                        new LambdaQueryWrapper<CourseObjectiveAchievement>()
                                .eq(CourseObjectiveAchievement::getClassId, classId))
                .stream()
                .collect(Collectors.toMap(
                        CourseObjectiveAchievement::getCoId,
                        CourseObjectiveAchievement::getAverageAchievement,
                        (left, right) -> right,
                        LinkedHashMap::new));

        List<CourseObjectiveDashboardResponse.ObjectiveSummary> objectiveSummaries = objectives.stream()
                .map(objective -> CourseObjectiveDashboardResponse.ObjectiveSummary.builder()
                        .coId(objective.getCoId())
                        .objectiveCode(objective.getObjectiveCode())
                        .description(objective.getCoDescription())
                        .averageAchievement(averageMap.get(objective.getCoId()))
                        .build())
                .toList();

        List<CourseIndicatorAchievement> courseIndicatorAchievements = ciaMapper.selectList(
                        new LambdaQueryWrapper<CourseIndicatorAchievement>()
                                .eq(CourseIndicatorAchievement::getClassId, classId));
        List<Long> indicatorIds = courseIndicatorAchievements.stream()
                .map(CourseIndicatorAchievement::getIpId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, IndicatorPoint> indicatorPointMap = indicatorIds.isEmpty()
                ? Map.of()
                : indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getIpId, item -> item));

        Map<String, CourseIndicatorAchievement> latestIndicatorAchievementMap = courseIndicatorAchievements.stream()
                .filter(item -> item.getIpId() != null)
                .collect(Collectors.toMap(
                        item -> indicatorDisplayKey(indicatorPointMap.get(item.getIpId())),
                        item -> item,
                        (left, right) -> compareNullableLong(left.getCiaId(), right.getCiaId()) >= 0 ? left : right,
                        LinkedHashMap::new));

        List<CourseObjectiveDashboardResponse.IndicatorAchievement> indicatorAchievements = latestIndicatorAchievementMap.values()
                .stream()
                .sorted((left, right) -> compareIndicatorCode(
                        indicatorPointMap.get(left.getIpId()),
                        indicatorPointMap.get(right.getIpId())))
                .map(item -> {
                    IndicatorPoint indicatorPoint = indicatorPointMap.get(item.getIpId());
                    return CourseObjectiveDashboardResponse.IndicatorAchievement.builder()
                            .ipId(item.getIpId())
                            .ipCode(indicatorPoint == null ? "" : indicatorPoint.getIpCode())
                            .ipDescription(indicatorPoint == null ? "" : indicatorPoint.getIpDescription())
                            .achievement(item.getAchievement())
                            .locked(Boolean.TRUE.equals(item.getIsLocked()))
                            .build();
                })
                .toList();

        UnlockAuditLog pendingUnlockRequest = findPendingUnlockRequest(classId);

        List<StudentClass> studentClasses = listStudentsInClass(classId);
        List<Long> studentIds = studentClasses.stream().map(StudentClass::getStudentId).toList();
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Map.of()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, student -> student));

        Map<Long, Map<Long, Float>> studentAchievementMap = soaMapper.selectList(
                        new LambdaQueryWrapper<StudentObjectiveAchievement>()
                                .eq(StudentObjectiveAchievement::getClassId, classId))
                .stream()
                .collect(Collectors.groupingBy(
                        StudentObjectiveAchievement::getStudentId,
                        Collectors.toMap(
                                StudentObjectiveAchievement::getCoId,
                                StudentObjectiveAchievement::getAchievement,
                                (left, right) -> right,
                                LinkedHashMap::new)));

        List<CourseObjectiveDashboardResponse.StudentObjectiveRow> studentRows = studentClasses.stream()
                .map(studentClass -> {
                    Student student = studentMap.get(studentClass.getStudentId());
                    if (student == null) {
                        return null;
                    }
                    Map<Long, Float> rowMap = studentAchievementMap.getOrDefault(student.getStudentId(), Map.of());
                    List<Float> achievements = objectives.stream()
                            .map(objective -> rowMap.get(objective.getCoId()))
                            .toList();
                    return CourseObjectiveDashboardResponse.StudentObjectiveRow.builder()
                            .studentId(student.getStudentId())
                            .studentNo(student.getStudentNo())
                            .studentName(student.getStudentName())
                            .achievements(achievements)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return CourseObjectiveDashboardResponse.builder()
                .classId(classId)
                .className(teachingClass.getClassName())
                .courseName(course == null ? null : course.getCourseName())
                .calcStatus(teachingClass.getCalcStatus())
                .locked("locked".equals(teachingClass.getCalcStatus()))
                .unlockRequested(pendingUnlockRequest != null)
                .unlockRequestReason(pendingUnlockRequest == null ? null : pendingUnlockRequest.getReason())
                .resultReady(!averageMap.isEmpty() || !studentAchievementMap.isEmpty() || !indicatorAchievements.isEmpty())
                .objectiveSummaries(objectiveSummaries)
                .indicatorAchievements(indicatorAchievements)
                .studentRows(studentRows)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestUnlock(UnlockRequestCreateRequest request, Long userId, List<String> roles) {
        Teacher teacher = resolveCurrentTeacher(userId, roles);
        TeachingClass teachingClass = requireTeachingClass(request.getClassId());
        if (!teacher.getId().equals(teachingClass.getTeacherId())) {
            throw new BusinessException(403, "只能对当前教师负责的教学班提交解锁申请");
        }
        if (!"locked".equals(teachingClass.getCalcStatus())) {
            throw new BusinessException(400, "当前教学班尚未锁定，无需申请解锁");
        }
        if (findPendingUnlockRequest(request.getClassId()) != null) {
            throw new BusinessException(400, "当前教学班已存在待处理的解锁申请");
        }

        UnlockAuditLog entity = new UnlockAuditLog();
        entity.setClassId(request.getClassId());
        entity.setRequestBy(teacher.getId());
        entity.setApprovedBy(0L);
        entity.setReason(request.getReason().trim());
        unlockAuditLogMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MajorCalcResponse calcMajorAchievement(MajorCalcRequest request) {
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        Long snapshotTermId = resolveMajorSnapshotTermId(request);
        AcademicTerm term = snapshotTermId == null ? null : academicTermMapper.selectById(snapshotTermId);
        if (term == null) {
            throw new BusinessException(404, "学期不存在");
        }

        List<CourseMajor> courseMajors = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>()
                        .eq(CourseMajor::getMajorId, request.getMajorId())
                        .eq(CourseMajor::getGradeYear, request.getGradeYear()));
        List<Long> courseIds = courseMajors.stream().map(CourseMajor::getCourseId).toList();
        if (courseIds.isEmpty()) {
            throw new BusinessException(400, "当前专业在该年级下没有配置支撑课程");
        }

        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getMajorId, request.getMajorId())
                        .eq(TeachingClass::getGradeYear, request.getGradeYear())
                        .eq(TeachingClass::getTermId, snapshotTermId));
        if (teachingClasses.isEmpty()) {
            throw new BusinessException(400, "当前专业在该年级和学期下没有教学班数据");
        }

        for (TeachingClass teachingClass : teachingClasses) {
            if (!"locked".equals(teachingClass.getCalcStatus())) {
                throw new BusinessException(400, "存在未完成计算的教学班：" + teachingClass.getClassName());
            }
        }

        List<Long> classIds = teachingClasses.stream().map(TeachingClass::getClassId).toList();
        List<CourseIndicatorAchievement> courseIndicatorAchievements = ciaMapper.selectList(
                new LambdaQueryWrapper<CourseIndicatorAchievement>()
                        .in(CourseIndicatorAchievement::getClassId, classIds));
        List<CourseIndicatorSupport> courseIndicatorSupports = cisMapper.selectList(
                new LambdaQueryWrapper<CourseIndicatorSupport>()
                        .in(CourseIndicatorSupport::getCourseId, courseIds));
        List<Long> configuredIpIds = listConfiguredIndicators(request.getMajorId(), request.getGradeYear()).stream()
                .map(IndicatorPoint::getIpId)
                .filter(Objects::nonNull)
                .toList();
        if (!configuredIpIds.isEmpty()) {
            courseIndicatorSupports = courseIndicatorSupports.stream()
                    .filter(item -> configuredIpIds.contains(item.getIpId()))
                    .toList();
        }

        Map<Long, Float> ipFinalAchievement = new HashMap<>();
        Map<Long, List<CourseIndicatorAchievement>> ipCiaMap = courseIndicatorAchievements.stream()
                .collect(Collectors.groupingBy(CourseIndicatorAchievement::getIpId));
        for (Map.Entry<Long, List<CourseIndicatorAchievement>> entry : ipCiaMap.entrySet()) {
            Long ipId = entry.getKey();
            float gk = 0f;
            for (CourseIndicatorAchievement courseIndicatorAchievement : entry.getValue()) {
                TeachingClass teachingClass = teachingClasses.stream()
                        .filter(item -> item.getClassId().equals(courseIndicatorAchievement.getClassId()))
                        .findFirst()
                        .orElse(null);
                if (teachingClass == null) {
                    continue;
                }
                Optional<CourseIndicatorSupport> support = courseIndicatorSupports.stream()
                        .filter(item -> item.getCourseId().equals(teachingClass.getCourseId()) && item.getIpId().equals(ipId))
                        .findFirst();
                if (support.isPresent()) {
                    gk += courseIndicatorAchievement.getAchievement() * support.get().getTotalWeight();
                }
            }
            ipFinalAchievement.put(ipId, Math.min(gk, 1.0f));
        }

        List<MajorCalcResponse.IndicatorAchievement> indicatorAchievements = new ArrayList<>();
        for (Map.Entry<Long, Float> entry : ipFinalAchievement.entrySet()) {
            Long ipId = entry.getKey();
            Float finalAchievement = entry.getValue();

            MajorIndicatorAchievement existing = miaMapper.selectOne(
                    new LambdaQueryWrapper<MajorIndicatorAchievement>()
                            .eq(MajorIndicatorAchievement::getMajorId, request.getMajorId())
                            .eq(MajorIndicatorAchievement::getGradeYear, request.getGradeYear())
                            .eq(MajorIndicatorAchievement::getTermId, snapshotTermId)
                            .eq(MajorIndicatorAchievement::getIpId, ipId));
            if (existing != null) {
                existing.setFinalAchievement(finalAchievement);
                miaMapper.updateById(existing);
            } else {
                MajorIndicatorAchievement entity = new MajorIndicatorAchievement();
                entity.setMajorId(request.getMajorId());
                entity.setGradeYear(request.getGradeYear());
                entity.setTermId(snapshotTermId);
                entity.setIpId(ipId);
                entity.setFinalAchievement(finalAchievement);
                miaMapper.insert(entity);
            }

            IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(ipId);
            indicatorAchievements.add(MajorCalcResponse.IndicatorAchievement.builder()
                    .ipId(ipId)
                    .ipCode(indicatorPoint == null ? "" : indicatorPoint.getIpCode())
                    .ipDescription(indicatorPoint == null ? "" : indicatorPoint.getIpDescription())
                    .finalAchievement(finalAchievement)
                    .build());
        }

        return MajorCalcResponse.builder()
                .majorId(request.getMajorId())
                .majorName(major.getMajorName())
                .gradeYear(request.getGradeYear())
                .termId(snapshotTermId)
                .termCode(term == null ? null : term.getTermCode())
                .indicatorAchievements(indicatorAchievements)
                .build();
    }

    @Override
    public CourseCalcStatusResponse getCourseCalcStatus(Long majorId, Long termId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        AcademicTerm term = academicTermMapper.selectById(termId);
        if (term == null) {
            throw new BusinessException(404, "学期不存在");
        }

        List<CourseMajor> courseMajors = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getMajorId, majorId));
        List<Long> courseIds = courseMajors.stream().map(CourseMajor::getCourseId).toList();

        List<TeachingClass> teachingClasses = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .in(TeachingClass::getCourseId, courseIds)
                        .eq(TeachingClass::getMajorId, majorId)
                        .eq(TeachingClass::getTermId, termId));

        List<CourseCalcStatusResponse.CourseStatus> courseStatuses = new ArrayList<>();
        boolean allLocked = true;
        for (TeachingClass teachingClass : teachingClasses) {
            Course course = courseMapper.selectById(teachingClass.getCourseId());
            boolean isLocked = "locked".equals(teachingClass.getCalcStatus());
            if (!isLocked) {
                allLocked = false;
            }
            courseStatuses.add(CourseCalcStatusResponse.CourseStatus.builder()
                    .courseId(teachingClass.getCourseId())
                    .courseCode(course == null ? null : course.getCourseCode())
                    .courseName(course == null ? null : course.getCourseName())
                    .classId(teachingClass.getClassId())
                    .className(teachingClass.getClassName())
                    .calcStatus(teachingClass.getCalcStatus())
                    .isLocked(isLocked)
                    .build());
        }

        return CourseCalcStatusResponse.builder()
                .majorId(majorId)
                .majorName(major.getMajorName())
                .termId(termId)
                .termCode(term.getTermCode())
                .canCalcMajor(allLocked)
                .blockReason(allLocked ? null : "存在未完成计算的教学班")
                .courseStatuses(courseStatuses)
                .build();
    }

    private TeachingClass requireTeachingClass(Long classId) {
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }
        return teachingClass;
    }

    private Teacher resolveCurrentTeacher(Long userId, List<String> roles) {
        if (userId == null) {
            throw new BusinessException(401, "当前登录信息缺少用户ID");
        }
        if (roles == null || roles.stream().noneMatch("instructor"::equals)) {
            throw new BusinessException(403, "当前账号不是课程主讲教师");
        }
        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId)
                .eq(Teacher::getStatus, 1));
        if (teacher == null) {
            throw new BusinessException(403, "当前登录用户未绑定启用状态的教师身份");
        }
        return teacher;
    }

    private UnlockAuditLog findPendingUnlockRequest(Long classId) {
        return unlockAuditLogMapper.selectOne(new LambdaQueryWrapper<UnlockAuditLog>()
                .eq(UnlockAuditLog::getClassId, classId)
                .eq(UnlockAuditLog::getApprovedBy, 0L)
                .orderByDesc(UnlockAuditLog::getUlogId)
                .last("LIMIT 1"));
    }

    private List<StudentClass> listStudentsInClass(Long classId) {
        return studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, classId));
    }

    private List<CourseObjective> listCourseObjectives(Long courseId) {
        return courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>().eq(CourseObjective::getCourseId, courseId));
    }

    private List<AssessmentPoint> listAssessmentPointsByObjectives(List<CourseObjective> objectives) {
        if (objectives.isEmpty()) {
            return List.of();
        }
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).toList();
        return assessmentPointMapper.selectList(
                new LambdaQueryWrapper<AssessmentPoint>()
                        .in(AssessmentPoint::getCoId, coIds)
                        .orderByAsc(AssessmentPoint::getApId));
    }

    private List<ObjectiveIndicatorContribution> listInternalWeights(List<Long> coIds, Long majorId, Integer gradeYear) {
        if (coIds.isEmpty()) {
            return List.of();
        }
        if (majorId == null || gradeYear == null) {
            throw new BusinessException(400, "当前教学班缺少专业或年级信息，不能执行课程级计算");
        }
        return oicMapper.selectByObjectiveIdsAndContext(coIds, majorId, gradeYear);
    }

    private Map<Long, Map<Long, Float>> loadSavedScoreMap(Long classId) {
        return sasMapper.selectList(
                        new LambdaQueryWrapper<StudentAssessmentScore>()
                                .eq(StudentAssessmentScore::getClassId, classId))
                .stream()
                .collect(Collectors.groupingBy(
                        StudentAssessmentScore::getStudentId,
                        Collectors.toMap(
                                StudentAssessmentScore::getApId,
                                StudentAssessmentScore::getActualScore,
                                (left, right) -> right,
                                LinkedHashMap::new)));
    }

    private Map<Long, Long> loadStudentCountMap(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Map.of();
        }
        return studentClassMapper.selectList(new LambdaQueryWrapper<StudentClass>()
                        .in(StudentClass::getClassId, classIds))
                .stream()
                .collect(Collectors.groupingBy(
                        StudentClass::getClassId,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private Map<String, Float> calculateCourseIndicatorAverageMap(List<CourseIndicatorAchievement> achievements,
                                                                  Map<Long, TeachingClass> classMap,
                                                                  Map<Long, Long> studentCountMap) {
        Map<String, Float> result = new LinkedHashMap<>();
        Map<String, Float> weightedSumMap = new LinkedHashMap<>();
        Map<String, Long> totalStudentMap = new LinkedHashMap<>();
        for (CourseIndicatorAchievement achievement : achievements) {
            TeachingClass teachingClass = classMap.get(achievement.getClassId());
            if (teachingClass == null || achievement.getIpId() == null || achievement.getAchievement() == null) {
                continue;
            }
            long studentCount = studentCountMap.getOrDefault(achievement.getClassId(), 0L);
            if (studentCount <= 0) {
                continue;
            }
            String key = courseIndicatorKey(teachingClass.getCourseId(), achievement.getIpId());
            weightedSumMap.merge(key, achievement.getAchievement() * studentCount, Float::sum);
            totalStudentMap.merge(key, studentCount, Long::sum);
        }
        for (Map.Entry<String, Float> entry : weightedSumMap.entrySet()) {
            long totalStudents = totalStudentMap.getOrDefault(entry.getKey(), 0L);
            if (totalStudents > 0) {
                result.put(entry.getKey(), entry.getValue() / totalStudents);
            }
        }
        return result;
    }

    private String courseIndicatorKey(Long courseId, Long ipId) {
        return courseId + "_" + ipId;
    }

    private Long resolveMajorSnapshotTermId(MajorCalcRequest request) {
        if (request.getTermId() != null) {
            return request.getTermId();
        }
        return teachingClassMapper.selectList(new LambdaQueryWrapper<TeachingClass>()
                        .eq(TeachingClass::getMajorId, request.getMajorId())
                        .eq(TeachingClass::getGradeYear, request.getGradeYear()))
                .stream()
                .map(TeachingClass::getTermId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
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

    private void validateInternalWeightSums(List<ObjectiveIndicatorContribution> internalWeights) {
        Map<Long, Float> weightSums = new LinkedHashMap<>();
        for (ObjectiveIndicatorContribution contribution : internalWeights) {
            weightSums.merge(contribution.getIpId(), contribution.getInternalWeight(), Float::sum);
        }

        List<Long> invalidIpIds = weightSums.entrySet().stream()
                .filter(entry -> Math.abs(entry.getValue() - 1.0f) > 0.001f)
                .map(Map.Entry::getKey)
                .toList();
        if (invalidIpIds.isEmpty()) {
            return;
        }

        List<String> invalidCodes = indicatorPointMapper.selectBatchIds(invalidIpIds).stream()
                .map(IndicatorPoint::getIpCode)
                .filter(Objects::nonNull)
                .toList();
        throw new BusinessException(400,
                "当前课程内部权重 w 未满足同一指标点列权重和为 1.00，异常指标点："
                        + String.join("、", invalidCodes));
    }

    private void ensureAllScoresCompleted(List<Long> studentIds,
                                          List<AssessmentPoint> assessmentPoints,
                                          List<StudentAssessmentScore> allScores) {
        if (studentIds.isEmpty() || assessmentPoints.isEmpty()) {
            throw new BusinessException(400, "当前教学班缺少学生名单或考核点配置，不能执行课程级计算");
        }

        Set<String> savedKeys = allScores.stream()
                .map(score -> score.getStudentId() + "_" + score.getApId())
                .collect(Collectors.toSet());

        for (Long studentId : studentIds) {
            for (AssessmentPoint assessmentPoint : assessmentPoints) {
                String key = studentId + "_" + assessmentPoint.getApId();
                if (!savedKeys.contains(key)) {
                    throw new BusinessException(400, "当前教学班仍有未录入的考核点成绩，必须补齐全部学生成绩后才能执行课程级计算");
                }
            }
        }
    }

    private void ensureInternalWeightsConfigured(List<CourseObjective> objectives, TeachingClass teachingClass) {
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).toList();
        if (listInternalWeights(coIds, teachingClass.getMajorId(), teachingClass.getGradeYear()).isEmpty()) {
            throw new BusinessException(400, "当前课程未配置内部权重 w，请先完成内部权重配置");
        }
    }

    private List<List<String>> readImportTable(ScoreImportRequest request) {
        byte[] fileBytes = decodeFileBytes(request.getFileBase64());
        String lowerName = request.getFileName().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".csv")) {
            return parseCsvRows(new String(fileBytes, StandardCharsets.UTF_8));
        }
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            return parseSheetRows(sheet);
        } catch (IOException e) {
            throw new BusinessException(400, "读取成绩文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(400, "成绩文件格式不正确，请使用系统模板重新导入");
        }
    }

    private byte[] decodeFileBytes(String fileBase64) {
        String encoded = fileBase64;
        int commaIndex = encoded.indexOf(',');
        if (commaIndex >= 0) {
            encoded = encoded.substring(commaIndex + 1);
        }
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "成绩文件内容不是合法的 Base64 数据");
        }
    }

    private List<List<String>> parseSheetRows(Sheet sheet) {
        List<List<String>> rows = new ArrayList<>();
        if (sheet == null) {
            return rows;
        }
        int lastRowNum = sheet.getLastRowNum();
        for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            int lastCellNum = row == null ? 0 : Math.max(row.getLastCellNum(), 0);
            List<String> values = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                Cell cell = row == null ? null : row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                values.add(cell == null ? "" : DATA_FORMATTER.formatCellValue(cell).trim());
            }
            rows.add(values);
        }
        return rows;
    }

    private List<List<String>> parseCsvRows(String content) {
        List<List<String>> rows = new ArrayList<>();
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < normalized.length() && normalized.charAt(i + 1) == '"') {
                    currentCell.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                currentRow.add(currentCell.toString().trim());
                currentCell.setLength(0);
            } else if (ch == '\n' && !inQuotes) {
                currentRow.add(currentCell.toString().trim());
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
                currentCell.setLength(0);
            } else {
                currentCell.append(ch);
            }
        }
        currentRow.add(currentCell.toString().trim());
        if (!currentRow.isEmpty() && !(currentRow.size() == 1 && currentRow.get(0).isEmpty() && rows.isEmpty())) {
            rows.add(new ArrayList<>(currentRow));
        }
        return rows;
    }

    private void validateTemplateMetadata(List<List<String>> table,
                                          Map<String, AssessmentPoint> apNameMap,
                                          Map<Long, CourseObjective> objectiveMap) {
        if (table.size() <= OBJECTIVE_ROW_INDEX) {
            throw new BusinessException(400, "成绩文件缺少模板头部，请使用系统模板重新导入");
        }
        List<String> headerRow = table.get(HEADER_ROW_INDEX);
        if (!Objects.equals(valueAt(headerRow, 0), "学号") || !Objects.equals(valueAt(headerRow, 1), "姓名")) {
            throw new BusinessException(400, "模板固定列必须为“学号、姓名”");
        }

        Map<Integer, AssessmentPoint> columnMapping = mapAssessmentPointColumns(headerRow, apNameMap);
        if (columnMapping.size() != apNameMap.size()) {
            throw new BusinessException(400, "模板中的考核点列与当前课程配置不一致，请重新下载模板");
        }

        List<String> fullScoreRow = table.get(FULL_SCORE_ROW_INDEX);
        if (!Objects.equals(valueAt(fullScoreRow, 0), "满分")) {
            throw new BusinessException(400, "模板第二行必须为满分行");
        }
        for (Map.Entry<Integer, AssessmentPoint> entry : columnMapping.entrySet()) {
            String rawFullScore = valueAt(fullScoreRow, entry.getKey());
            if (isBlank(rawFullScore)) {
                throw new BusinessException(400, "模板满分行缺少考核点“" + entry.getValue().getApName() + "”的满分配置");
            }
            float actualFullScore;
            try {
                actualFullScore = Float.parseFloat(rawFullScore);
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "模板满分行存在非法分值，请重新下载模板");
            }
            if (Math.abs(actualFullScore - entry.getValue().getFullScore()) > 0.001f) {
                throw new BusinessException(400, "考核点“" + entry.getValue().getApName() + "”的满分与当前配置不一致");
            }
        }

        List<String> objectiveRow = table.get(OBJECTIVE_ROW_INDEX);
        if (!Objects.equals(valueAt(objectiveRow, 0), "课程目标")) {
            throw new BusinessException(400, "模板第三行必须为课程目标行");
        }
        for (Map.Entry<Integer, AssessmentPoint> entry : columnMapping.entrySet()) {
            AssessmentPoint assessmentPoint = entry.getValue();
            CourseObjective objective = objectiveMap.get(assessmentPoint.getCoId());
            String expectedCode = objective == null ? "" : safeString(objective.getObjectiveCode());
            String actualCode = valueAt(objectiveRow, entry.getKey());
            if (!Objects.equals(normalize(expectedCode), normalize(actualCode))) {
                throw new BusinessException(400, "考核点“" + assessmentPoint.getApName() + "”所属课程目标与当前配置不一致");
            }
        }
    }

    private Map<Integer, AssessmentPoint> mapAssessmentPointColumns(List<String> headerRow,
                                                                    Map<String, AssessmentPoint> apNameMap) {
        Map<Integer, AssessmentPoint> columnMapping = new LinkedHashMap<>();
        Set<Long> mappedApIds = new HashSet<>();
        for (int columnIndex = 2; columnIndex < headerRow.size(); columnIndex++) {
            String headerName = valueAt(headerRow, columnIndex);
            if (isBlank(headerName)) {
                continue;
            }
            AssessmentPoint assessmentPoint = apNameMap.get(normalize(headerName));
            if (assessmentPoint == null) {
                throw new BusinessException(400, "模板中存在未配置的考核点列：" + headerName);
            }
            if (!mappedApIds.add(assessmentPoint.getApId())) {
                throw new BusinessException(400, "模板中存在重复的考核点列：" + headerName);
            }
            columnMapping.put(columnIndex, assessmentPoint);
        }
        return columnMapping;
    }

    private boolean isEmptyDataRow(List<String> row) {
        if (row == null || row.isEmpty()) {
            return true;
        }
        for (String cellValue : row) {
            if (!isBlank(cellValue)) {
                return false;
            }
        }
        return true;
    }

    private String valueAt(List<String> row, int index) {
        if (row == null || index < 0 || index >= row.size()) {
            return "";
        }
        return safeString(row.get(index)).trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.ROOT);
    }

    private int compareNullableLong(Long left, Long right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return Long.compare(left, right);
    }

    private int compareIndicatorCode(IndicatorPoint left, IndicatorPoint right) {
        String leftCode = left == null ? "" : left.getIpCode();
        String rightCode = right == null ? "" : right.getIpCode();
        return compareIndicatorCode(leftCode, rightCode);
    }

    private int compareIndicatorCode(String leftCode, String rightCode) {
        List<Integer> leftNumbers = extractNumbers(leftCode);
        List<Integer> rightNumbers = extractNumbers(rightCode);
        int maxSize = Math.max(leftNumbers.size(), rightNumbers.size());
        for (int i = 0; i < maxSize; i++) {
            int leftNumber = i < leftNumbers.size() ? leftNumbers.get(i) : -1;
            int rightNumber = i < rightNumbers.size() ? rightNumbers.get(i) : -1;
            if (leftNumber != rightNumber) {
                return Integer.compare(leftNumber, rightNumber);
            }
        }
        return safeString(leftCode).compareTo(safeString(rightCode));
    }

    private String indicatorDisplayKey(IndicatorPoint indicatorPoint) {
        if (indicatorPoint == null) {
            return "";
        }
        return normalize(indicatorPoint.getIpCode()) + "|" + normalize(indicatorPoint.getIpDescription());
    }

    private List<Integer> extractNumbers(String value) {
        List<Integer> numbers = new ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(safeString(value));
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        return numbers;
    }
}



