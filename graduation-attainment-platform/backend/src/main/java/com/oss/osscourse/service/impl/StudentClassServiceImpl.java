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
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        int totalCount = 0;
        Set<String> processedRecords = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                throw new BusinessException(400, "Excel文件无数据行");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                totalCount++;
                int rowNumber = i + 1;

                try {
                    String error = validateAndImportRow(row, processedRecords);
                    if (error == null) {
                        successCount++;
                    } else {
                        failedItems.add(StudentClassImportResult.FailedItem.builder()
                                .rowNumber(rowNumber)
                                .reason(error)
                                .build());
                    }
                } catch (Exception e) {
                    failedItems.add(StudentClassImportResult.FailedItem.builder()
                            .rowNumber(rowNumber)
                            .reason("系统错误: " + e.getMessage())
                            .build());
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "Excel文件解析失败: " + e.getMessage());
        }

        return StudentClassImportResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failedItems.size())
                .failedItems(failedItems)
                .build();
    }

    private String validateAndImportRow(Row row, Set<String> processedRecords) {
        String studentNo = getCellStringValue(row.getCell(0));
        String studentName = getCellStringValue(row.getCell(1));
        String majorCode = getCellStringValue(row.getCell(2));
        String enrollmentYear = getCellStringValue(row.getCell(3));
        String teachingClassCode = getCellStringValue(row.getCell(4));

        if (!hasText(studentNo)) {
            return "学号不能为空";
        }
        if (!hasText(studentName)) {
            return "姓名不能为空";
        }
        if (!hasText(majorCode)) {
            return "专业代码不能为空";
        }
        if (!hasText(enrollmentYear)) {
            return "入学年份不能为空";
        }
        if (!hasText(teachingClassCode)) {
            return "教学班编号不能为空";
        }

        int parsedEnrollmentYear;
        try {
            parsedEnrollmentYear = Integer.parseInt(enrollmentYear);
        } catch (NumberFormatException e) {
            return "入学年份必须为合法数值: " + enrollmentYear;
        }
        if (parsedEnrollmentYear < 2000 || parsedEnrollmentYear > 2100) {
            return "入学年份不合法: " + enrollmentYear;
        }

        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo));
        if (student == null) {
            return "学号不存在: " + studentNo;
        }
        if (!studentName.equals(student.getStudentName())) {
            return "学生姓名与学号不匹配: " + studentNo;
        }
        if (!parsedEnrollmentYearEquals(student, parsedEnrollmentYear)) {
            return "入学年份与学生记录不匹配: " + studentNo;
        }

        Major major = majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return "专业代码不存在: " + majorCode;
        }
        if (!major.getMajorId().equals(student.getMajorId())) {
            return "学生专业与导入专业代码不匹配: " + studentNo;
        }

        TeachingClass teachingClass = teachingClassMapper.selectOne(new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getClassCode, teachingClassCode));
        if (teachingClass == null) {
            return "教学班编号不存在: " + teachingClassCode;
        }

        String duplicateKey = student.getStudentId() + "-" + teachingClass.getClassId();
        if (!processedRecords.add(duplicateKey)) {
            return "学生 " + studentNo + " 在同一批次中重复导入到教学班 " + teachingClassCode;
        }

        Long existingCount = studentClassMapper.selectCount(new LambdaQueryWrapper<StudentClass>()
                .eq(StudentClass::getStudentId, student.getStudentId())
                .eq(StudentClass::getClassId, teachingClass.getClassId()));
        if (existingCount != null && existingCount > 0) {
            return "学生 " + studentNo + " 已存在于教学班 " + teachingClassCode + " 中";
        }

        StudentClass studentClass = new StudentClass();
        studentClass.setStudentId(student.getStudentId());
        studentClass.setClassId(teachingClass.getClassId());
        studentClassMapper.insert(studentClass);
        return null;
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

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
    }

    private boolean isRowEmpty(Row row) {
        for (int index = 0; index < row.getLastCellNum(); index++) {
            String value = getCellStringValue(row.getCell(index));
            if (hasText(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean parsedEnrollmentYearEquals(Student student, int parsedEnrollmentYear) {
        return student.getEnrollmentYear() != null && student.getEnrollmentYear() == parsedEnrollmentYear;
    }
}
