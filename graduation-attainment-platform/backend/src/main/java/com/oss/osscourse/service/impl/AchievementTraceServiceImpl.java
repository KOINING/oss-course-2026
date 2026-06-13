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
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AchievementTraceMapper;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveAchievementMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.AchievementTraceService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementTraceServiceImpl implements AchievementTraceService {

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

    @Override
    public List<MajorToCourseTraceResponse> getMajorToCourseTrace(MajorToCourseTraceRequest request) {
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

        List<ObjectiveIndicatorContribution> contributions = oicMapper.selectList(
                new LambdaQueryWrapper<ObjectiveIndicatorContribution>().eq(ObjectiveIndicatorContribution::getIpId, request.getIpId()));
        List<Long> coIds = contributions.stream().map(ObjectiveIndicatorContribution::getCoId).filter(Objects::nonNull).toList();
        Map<Long, Float> weightMap = contributions.stream()
                .collect(Collectors.toMap(ObjectiveIndicatorContribution::getCoId, ObjectiveIndicatorContribution::getInternalWeight,
                        (left, right) -> left, LinkedHashMap::new));

        List<CourseObjective> objectives = coIds.isEmpty()
                ? List.of()
                : courseObjectiveMapper.selectList(new LambdaQueryWrapper<CourseObjective>()
                .eq(CourseObjective::getCourseId, course.getCourseId())
                .in(CourseObjective::getCoId, coIds)
                .orderByAsc(CourseObjective::getObjectiveCode));
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
        List<AchievementLedgerRow> rows = listLedgerRows(request);
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
