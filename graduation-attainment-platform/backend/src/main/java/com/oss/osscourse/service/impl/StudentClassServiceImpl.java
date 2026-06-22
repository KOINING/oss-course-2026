package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.teachingclass.StudentClassImportResult;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.StudentClassService;
import com.oss.osscourse.util.ImportSheetReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentClassServiceImpl implements StudentClassService {

    private final StudentClassMapper studentClassMapper;
    private final StudentMapper studentMapper;
    private final MajorMapper majorMapper;
    private final TeachingClassMapper teachingClassMapper;

    @Override
    public StudentClassImportResult importStudentClasses(MultipartFile file) {
        List<StudentClassImportResult.FailedItem> failedItems = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int totalCount = 0;
        Set<String> processedRecords = new HashSet<>();

        List<ImportSheetReader.ImportRowData> rows = ImportSheetReader.readDataRows(file);
        for (ImportSheetReader.ImportRowData row : rows) {
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            try {
                RowImportResult result = validateAndImportRow(row, processedRecords);
                if (result.success()) {
                    successCount++;
                } else if (result.skipped()) {
                    skippedCount++;
                } else {
                    failedItems.add(StudentClassImportResult.FailedItem.builder()
                            .rowNumber(row.getRowNumber())
                            .reason(result.message())
                            .build());
                }
            } catch (Exception e) {
                failedItems.add(StudentClassImportResult.FailedItem.builder()
                        .rowNumber(row.getRowNumber())
                        .reason("系统错误: " + e.getMessage())
                        .build());
            }
        }

        return StudentClassImportResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failureCount(failedItems.size())
                .failedItems(failedItems)
                .build();
    }

    private RowImportResult validateAndImportRow(ImportSheetReader.ImportRowData row, Set<String> processedRecords) {
        String studentNo = row.getCell(0);
        String studentName = row.getCell(1);
        String majorCode = row.getCell(2);
        String enrollmentYear = row.getCell(3);
        String teachingClassCode = row.getCell(4);

        if (!hasText(studentNo)) {
            return RowImportResult.failed("学号不能为空");
        }
        if (!hasText(studentName)) {
            return RowImportResult.failed("姓名不能为空");
        }
        if (!hasText(majorCode)) {
            return RowImportResult.failed("专业代码不能为空");
        }
        if (!hasText(enrollmentYear)) {
            return RowImportResult.failed("入学年份不能为空");
        }
        if (!hasText(teachingClassCode)) {
            return RowImportResult.failed("教学班编号不能为空");
        }

        studentNo = studentNo.trim();
        studentName = studentName.trim();
        majorCode = majorCode.trim();
        enrollmentYear = enrollmentYear.trim();
        teachingClassCode = teachingClassCode.trim();

        int parsedEnrollmentYear;
        try {
            parsedEnrollmentYear = Integer.parseInt(enrollmentYear);
        } catch (NumberFormatException e) {
            return RowImportResult.failed("入学年份必须为合法数值: " + enrollmentYear);
        }
        if (parsedEnrollmentYear < 2000 || parsedEnrollmentYear > 2100) {
            return RowImportResult.failed("入学年份不合法: " + enrollmentYear);
        }

        Major major = majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return RowImportResult.failed("专业代码不存在: " + majorCode);
        }

        TeachingClass teachingClass = teachingClassMapper.selectOne(new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getClassCode, teachingClassCode));
        if (teachingClass == null) {
            return RowImportResult.failed("教学班编号不存在: " + teachingClassCode);
        }
        if (teachingClass.getMajorId() != null && !teachingClass.getMajorId().equals(major.getMajorId())) {
            return RowImportResult.failed("导入专业与教学班所属专业不匹配: " + teachingClassCode);
        }
        if (teachingClass.getGradeYear() != null && !teachingClass.getGradeYear().equals(parsedEnrollmentYear)) {
            return RowImportResult.failed("入学年份与教学班适用年级不匹配: " + teachingClassCode);
        }

        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo));
        if (student == null) {
            student = createSupplementStudent(studentNo, studentName, major.getMajorId(), parsedEnrollmentYear);
        } else {
            RowImportResult checkResult = validateExistingStudent(student, studentNo, studentName, major.getMajorId(),
                    parsedEnrollmentYear);
            if (!checkResult.success()) {
                return checkResult;
            }
        }

        String duplicateKey = student.getStudentId() + "-" + teachingClass.getClassId();
        if (!processedRecords.add(duplicateKey)) {
            return RowImportResult.skippedRow("学生 " + studentNo + " 在同一批次中重复导入到教学班 " + teachingClassCode);
        }

        Long existingCount = studentClassMapper.selectCount(new LambdaQueryWrapper<StudentClass>()
                .eq(StudentClass::getStudentId, student.getStudentId())
                .eq(StudentClass::getClassId, teachingClass.getClassId()));
        if (existingCount != null && existingCount > 0) {
            return RowImportResult.skippedRow("学生 " + studentNo + " 已存在于教学班 " + teachingClassCode + " 中");
        }

        StudentClass studentClass = new StudentClass();
        studentClass.setStudentId(student.getStudentId());
        studentClass.setClassId(teachingClass.getClassId());
        studentClassMapper.insert(studentClass);
        return RowImportResult.imported();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentClassImportResult generateStudentClasses(Long teachingClassId) {
        if (teachingClassId == null) {
            throw new BusinessException(400, "教学班ID不能为空");
        }

        TeachingClass teachingClass = teachingClassMapper.selectById(teachingClassId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }
        if (teachingClass.getMajorId() == null || teachingClass.getGradeYear() == null) {
            throw new BusinessException(400, "教学班缺少专业或年级，无法自动生成学生名单");
        }

        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getMajorId, teachingClass.getMajorId())
                .eq(Student::getEnrollmentYear, teachingClass.getGradeYear())
                .eq(Student::getStatus, 1)
                .orderByAsc(Student::getStudentNo));
        if (students.isEmpty()) {
            return StudentClassImportResult.builder()
                    .totalCount(0)
                    .successCount(0)
                    .skippedCount(0)
                    .failureCount(0)
                    .failedItems(List.of())
                    .build();
        }

        Set<Long> existingStudentIds = studentClassMapper.selectList(new LambdaQueryWrapper<StudentClass>()
                        .eq(StudentClass::getClassId, teachingClassId))
                .stream()
                .map(StudentClass::getStudentId)
                .collect(java.util.stream.Collectors.toSet());

        int successCount = 0;
        int skippedCount = 0;
        for (Student student : students) {
            if (existingStudentIds.contains(student.getStudentId())) {
                skippedCount++;
                continue;
            }

            StudentClass studentClass = new StudentClass();
            studentClass.setStudentId(student.getStudentId());
            studentClass.setClassId(teachingClassId);
            studentClassMapper.insert(studentClass);
            successCount++;
        }

        return StudentClassImportResult.builder()
                .totalCount(students.size())
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failureCount(0)
                .failedItems(List.of())
                .build();
    }

    @Override
    public List<StudentClass> listByTeachingClassId(Long teachingClassId) {
        if (teachingClassId == null) {
            throw new BusinessException(400, "教学班ID不能为空");
        }
        return studentClassMapper.selectList(new LambdaQueryWrapper<StudentClass>()
                .eq(StudentClass::getClassId, teachingClassId)
                .orderByAsc(StudentClass::getScId));
    }

    @Override
    public List<StudentClass> listByStudentId(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生ID不能为空");
        }
        return studentClassMapper.selectList(new LambdaQueryWrapper<StudentClass>()
                .eq(StudentClass::getStudentId, studentId)
                .orderByAsc(StudentClass::getScId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudentFromClass(Long scId) {
        if (scId == null) {
            throw new BusinessException(400, "关联ID不能为空");
        }
        StudentClass studentClass = studentClassMapper.selectById(scId);
        if (studentClass == null) {
            throw new BusinessException(404, "学生-教学班关联不存在");
        }
        studentClassMapper.deleteById(scId);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean parsedEnrollmentYearEquals(Student student, int parsedEnrollmentYear) {
        return student.getEnrollmentYear() != null && student.getEnrollmentYear() == parsedEnrollmentYear;
    }

    private Student createSupplementStudent(String studentNo, String studentName, Long majorId, int enrollmentYear) {
        Student student = new Student();
        student.setStudentNo(studentNo);
        student.setStudentName(studentName);
        student.setMajorId(majorId);
        student.setEnrollmentYear(enrollmentYear);
        student.setStatus(1);
        studentMapper.insert(student);
        return student;
    }

    private RowImportResult validateExistingStudent(Student student, String studentNo, String studentName, Long majorId,
                                                    int enrollmentYear) {
        if (!studentName.equals(student.getStudentName())) {
            return RowImportResult.failed("学生姓名与学号不匹配: " + studentNo);
        }
        if (!parsedEnrollmentYearEquals(student, enrollmentYear)) {
            return RowImportResult.failed("入学年份与学生记录不匹配: " + studentNo);
        }
        if (!majorId.equals(student.getMajorId())) {
            return RowImportResult.failed("学生专业与导入专业代码不匹配: " + studentNo);
        }
        return RowImportResult.imported();
    }

    private record RowImportResult(boolean success, boolean skipped, String message) {

        private static RowImportResult imported() {
            return new RowImportResult(true, false, null);
        }

        private static RowImportResult skippedRow(String message) {
            return new RowImportResult(false, true, message);
        }

        private static RowImportResult failed(String message) {
            return new RowImportResult(false, false, message);
        }
    }
}
