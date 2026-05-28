package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.student.*;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
            if (request.getStudentNo() != null && !request.getStudentNo().trim().isEmpty()) {
                wrapper.like(Student::getStudentNo, request.getStudentNo().trim());
            }
            if (request.getStudentName() != null && !request.getStudentName().trim().isEmpty()) {
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

        List<Student> students = studentMapper.selectList(wrapper);
        return toResponseList(students);
    }

    @Override
    public List<StudentResponse> listStudentsForSelect() {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStatus, 1)  // 只查询在读学生
               .orderByAsc(Student::getStudentNo);
        List<Student> students = studentMapper.selectList(wrapper);
        return toResponseList(students);
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
        // 验证专业是否存在
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        if (request.getStudentId() == null) {
            // 新增
            createStudent(request);
        } else {
            // 更新
            updateStudent(request);
        }
    }

    private void createStudent(StudentSaveRequest request) {
        // 检查学号是否重复
        Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, request.getStudentNo()));
        if (existing != null) {
            throw new BusinessException(400, "学号已存在");
        }

        Student student = new Student();
        student.setStudentNo(request.getStudentNo());
        student.setStudentName(request.getStudentName());
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

        // 检查学号是否重复（排除自身）
        if (request.getStudentNo() != null && !request.getStudentNo().isEmpty()) {
            Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                    .eq(Student::getStudentNo, request.getStudentNo())
                    .ne(Student::getStudentId, request.getStudentId()));
            if (existing != null) {
                throw new BusinessException(400, "学号已存在");
            }
            student.setStudentNo(request.getStudentNo());
        }

        if (request.getStudentName() != null && !request.getStudentName().isEmpty()) {
            student.setStudentName(request.getStudentName());
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

        // 检查是否有关联的教学班
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

    private List<StudentResponse> toResponseList(List<Student> students) {
        if (students.isEmpty()) {
            return List.of();
        }

        // 批量查询专业信息
        Set<Long> majorIds = students.stream().map(Student::getMajorId).collect(Collectors.toSet());
        Map<Long, Major> majorMap = majorMapper.selectBatchIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, m -> m));

        return students.stream()
                .map(s -> {
                    Major major = majorMap.get(s.getMajorId());
                    return StudentResponse.builder()
                            .studentId(s.getStudentId())
                            .studentNo(s.getStudentNo())
                            .studentName(s.getStudentName())
                            .majorId(s.getMajorId())
                            .majorName(major != null ? major.getMajorName() : null)
                            .majorCode(major != null ? major.getMajorCode() : null)
                            .enrollmentYear(s.getEnrollmentYear())
                            .userId(s.getUserId())
                            .status(s.getStatus())
                            .statusText(getStatusText(s.getStatus()))
                            .createdAt(s.getCreatedAt())
                            .updatedAt(s.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private StudentResponse toResponse(Student student) {
        return toResponseList(List.of(student)).get(0);
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "退学";
            case 1 -> "在读";
            case 2 -> "毕业";
            case 3 -> "休学";
            default -> "未知";
        };
    }
}
