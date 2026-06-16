package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.student.StudentImportResult;
import com.oss.osscourse.dto.student.StudentQueryRequest;
import com.oss.osscourse.dto.student.StudentResponse;
import com.oss.osscourse.dto.student.StudentSaveRequest;
import com.oss.osscourse.dto.student.StudentStatusRequest;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.service.StudentService;
import com.oss.osscourse.util.ImportSheetReader;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final MajorMapper majorMapper;
    private final StudentClassMapper studentClassMapper;

    @Override
    public List<StudentResponse> listStudents(StudentQueryRequest request) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (hasText(request.getStudentNo())) {
                wrapper.like(Student::getStudentNo, request.getStudentNo().trim());
            }
            if (hasText(request.getStudentName())) {
                wrapper.like(Student::getStudentName, request.getStudentName().trim());
            }
            if (request.getMajorId() != null) {
                wrapper.eq(Student::getMajorId, request.getMajorId());
            }
            if (request.getEnrollmentYear() != null) {
                wrapper.eq(Student::getEnrollmentYear, request.getEnrollmentYear());
            }
            if (request.getStatus() != null) {
                wrapper.eq(Student::getStatus, request.getStatus());
            }
        }

        wrapper.orderByAsc(Student::getStudentNo);
        return toResponseList(studentMapper.selectList(wrapper));
    }

    @Override
    public List<StudentResponse> listStudentsForSelect() {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStatus, 1).orderByAsc(Student::getStudentNo);
        return toResponseList(studentMapper.selectList(wrapper));
    }

    @Override
    public List<Integer> listEnrollmentYears() {
        return studentMapper.selectEnrollmentYears();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentImportResult importStudents(MultipartFile file) {
        List<StudentImportResult.FailedItem> failedItems = new ArrayList<>();
        int successCount = 0;
        int totalCount = 0;
        Set<String> processedStudentNos = new HashSet<>();

        List<ImportSheetReader.ImportRowData> rows = ImportSheetReader.readDataRows(file);
        for (ImportSheetReader.ImportRowData row : rows) {
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            try {
                String error = validateAndImportStudentRow(row, processedStudentNos);
                if (error == null) {
                    successCount++;
                } else {
                    failedItems.add(StudentImportResult.FailedItem.builder()
                            .rowNumber(row.getRowNumber())
                            .reason(error)
                            .build());
                }
            } catch (Exception e) {
                failedItems.add(StudentImportResult.FailedItem.builder()
                        .rowNumber(row.getRowNumber())
                        .reason("系统错误: " + e.getMessage())
                        .build());
            }
        }

        return StudentImportResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failedItems.size())
                .failedItems(failedItems)
                .build();
    }

    @Override
    public StudentResponse getStudentById(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生ID不能为空");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        return toResponse(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStudent(StudentSaveRequest request) {
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        if (request.getStudentId() == null) {
            createStudent(request);
        } else {
            updateStudent(request);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentStatus(StudentStatusRequest request) {
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        student.setStatus(request.getStatus());
        studentMapper.updateById(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生ID不能为空");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        Long classCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getStudentId, studentId));
        if (classCount != null && classCount > 0) {
            throw new BusinessException(400, "该学生已关联到教学班，无法删除");
        }

        try {
            studentMapper.deleteById(studentId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该学生存在关联数据，无法删除");
        }
    }

    private void createStudent(StudentSaveRequest request) {
        Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, request.getStudentNo()));
        if (existing != null) {
            throw new BusinessException(400, "学号已存在");
        }

        Student student = new Student();
        student.setStudentNo(request.getStudentNo().trim());
        student.setStudentName(request.getStudentName().trim());
        student.setMajorId(request.getMajorId());
        student.setEnrollmentYear(request.getEnrollmentYear());
        student.setUserId(request.getUserId());
        student.setStatus(request.getStatus());
        studentMapper.insert(student);
    }

    private void updateStudent(StudentSaveRequest request) {
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        if (hasText(request.getStudentNo())) {
            Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                    .eq(Student::getStudentNo, request.getStudentNo().trim())
                    .ne(Student::getStudentId, request.getStudentId()));
            if (existing != null) {
                throw new BusinessException(400, "学号已存在");
            }
            student.setStudentNo(request.getStudentNo().trim());
        }

        if (hasText(request.getStudentName())) {
            student.setStudentName(request.getStudentName().trim());
        }
        if (request.getMajorId() != null) {
            student.setMajorId(request.getMajorId());
        }
        if (request.getEnrollmentYear() != null) {
            student.setEnrollmentYear(request.getEnrollmentYear());
        }
        if (request.getUserId() != null) {
            student.setUserId(request.getUserId());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }

        studentMapper.updateById(student);
    }

    private String validateAndImportStudentRow(ImportSheetReader.ImportRowData row, Set<String> processedStudentNos) {
        String studentNo = row.getCell(0);
        String studentName = row.getCell(1);
        String majorCode = row.getCell(2);
        String enrollmentYear = row.getCell(3);
        String status = row.getCell(4);

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
        if (!hasText(status)) {
            return "学籍状态不能为空";
        }

        studentNo = studentNo.trim();
        studentName = studentName.trim();
        majorCode = majorCode.trim();
        enrollmentYear = enrollmentYear.trim();
        status = status.trim();

        if (!processedStudentNos.add(studentNo)) {
            return "学号在导入模板中重复: " + studentNo;
        }

        Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo));
        if (existing != null) {
            return "学号已存在: " + studentNo;
        }

        Major major = majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return "专业代码不存在: " + majorCode;
        }

        int parsedEnrollmentYear;
        try {
            parsedEnrollmentYear = Integer.parseInt(enrollmentYear);
        } catch (NumberFormatException e) {
            return "入学年份必须为合法年份: " + enrollmentYear;
        }
        if (parsedEnrollmentYear < 2000 || parsedEnrollmentYear > 2100) {
            return "入学年份必须为合法年份: " + enrollmentYear;
        }

        int parsedStatus;
        try {
            parsedStatus = Integer.parseInt(status);
        } catch (NumberFormatException e) {
            return "学籍状态必须为 0-3 的合法值: " + status;
        }
        if (parsedStatus < 0 || parsedStatus > 3) {
            return "学籍状态必须为 0-3 的合法值: " + status;
        }

        Student student = new Student();
        student.setStudentNo(studentNo);
        student.setStudentName(studentName);
        student.setMajorId(major.getMajorId());
        student.setEnrollmentYear(parsedEnrollmentYear);
        student.setStatus(parsedStatus);
        studentMapper.insert(student);
        return null;
    }

    private List<StudentResponse> toResponseList(List<Student> students) {
        if (students.isEmpty()) {
            return List.of();
        }

        Set<Long> majorIds = students.stream().map(Student::getMajorId).collect(Collectors.toSet());
        Map<Long, Major> majorMap = majorMapper.selectBatchIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));

        return students.stream()
                .map(student -> {
                    Major major = majorMap.get(student.getMajorId());
                    return StudentResponse.builder()
                            .studentId(student.getStudentId())
                            .studentNo(student.getStudentNo())
                            .studentName(student.getStudentName())
                            .majorId(student.getMajorId())
                            .majorName(major != null ? major.getMajorName() : null)
                            .majorCode(major != null ? major.getMajorCode() : null)
                            .enrollmentYear(student.getEnrollmentYear())
                            .userId(student.getUserId())
                            .status(student.getStatus())
                            .statusText(getStatusText(student.getStatus()))
                            .createdAt(student.getCreatedAt())
                            .updatedAt(student.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private StudentResponse toResponse(Student student) {
        return toResponseList(List.of(student)).get(0);
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "退学";
            case 1 -> "在读";
            case 2 -> "毕业";
            case 3 -> "休学";
            default -> "未知";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
