package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.teachingclass.StudentClassImportResult;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.SysUser;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.SysUserMapper;
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
    private final SysUserMapper sysUserMapper;
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
                int excelRowNum = i + 1;

                try {
                    String error = validateAndImportRow(row, processedRecords);
                    if (error != null) {
                        failedItems.add(StudentClassImportResult.FailedItem.builder()
                                .rowNumber(excelRowNum)
                                .reason(error)
                                .build());
                    } else {
                        successCount++;
                    }
                } catch (Exception e) {
                    failedItems.add(StudentClassImportResult.FailedItem.builder()
                            .rowNumber(excelRowNum)
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

        // 学号不能为空
        if (studentNo == null || studentNo.isEmpty()) {
            return "学号不能为空";
        }

        // 姓名不能为空
        if (studentName == null || studentName.isEmpty()) {
            return "姓名不能为空";
        }

        // 入学年份必须为合法年份
        if (enrollmentYear == null || enrollmentYear.isEmpty()) {
            return "入学年份不能为空";
        }
        int year;
        try {
            year = Integer.parseInt(enrollmentYear);
            if (year < 2000 || year > 2100) {
                return "入学年份不合法: " + enrollmentYear;
            }
        } catch (NumberFormatException e) {
            return "入学年份必须为合法数值: " + enrollmentYear;
        }

        // 查找学生
        SysUser student = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, studentNo));
        if (student == null) {
            return "学号不存在: " + studentNo;
        }

        // 专业代码必须已存在
        if (majorCode == null || majorCode.isEmpty()) {
            return "专业代码不能为空";
        }
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return "专业代码不存在: " + majorCode;
        }

        // 教学班编号必须存在并能唯一定位教学班
        if (teachingClassCode == null || teachingClassCode.isEmpty()) {
            return "教学班编号不能为空";
        }
        Long classId;
        try {
            classId = Long.parseLong(teachingClassCode);
        } catch (NumberFormatException e) {
            return "教学班编号必须为合法数值: " + teachingClassCode;
        }
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            return "教学班编号不存在: " + teachingClassCode;
        }

        // 同一学生不能重复导入到同一教学班
        String duplicateKey = student.getId() + "-" + teachingClass.getClassId();
        if (!processedRecords.add(duplicateKey)) {
            return "学生 " + studentNo + " 在同一批次中重复导入到教学班 " + teachingClassCode;
        }

        // 检查是否已存在该关联
        Long existingCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<StudentClass>()
                        .eq(StudentClass::getStudentId, student.getId())
                        .eq(StudentClass::getClassId, teachingClass.getClassId()));
        if (existingCount != null && existingCount > 0) {
            return "学生 " + studentNo + " 已存在于教学班 " + teachingClassCode + " 中";
        }

        // 创建关联
        StudentClass sc = new StudentClass();
        sc.setStudentId(student.getId());
        sc.setClassId(teachingClass.getClassId());
        studentClassMapper.insert(sc);

        return null;
    }

    @Override
    public List<StudentClass> listByTeachingClassId(Long teachingClassId) {
        if (teachingClassId == null) {
            throw new BusinessException(400, "教学班ID不能为空");
        }
        return studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>()
                        .eq(StudentClass::getClassId, teachingClassId)
                        .orderByAsc(StudentClass::getScId));
    }

    @Override
    public List<StudentClass> listByStudentId(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生ID不能为空");
        }
        return studentClassMapper.selectList(
                new LambdaQueryWrapper<StudentClass>()
                        .eq(StudentClass::getStudentId, studentId)
                        .orderByAsc(StudentClass::getScId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudentFromClass(Long scId) {
        if (scId == null) {
            throw new BusinessException(400, "关联ID不能为空");
        }
        StudentClass sc = studentClassMapper.selectById(scId);
        if (sc == null) {
            throw new BusinessException(404, "学生-教学班关联不存在");
        }
        studentClassMapper.deleteById(scId);
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val) && !Double.isInfinite(val)) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        }
        String value = cell.getStringCellValue();
        return value != null ? value.trim() : null;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && getCellStringValue(cell) != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
