package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.teachingclass.*;
import com.oss.osscourse.entity.*;
import com.oss.osscourse.mapper.*;
import com.oss.osscourse.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingClassServiceImpl implements TeachingClassService {

    private final TeachingClassMapper teachingClassMapper;
    private final CourseMapper courseMapper;
    private final AcademicTermMapper academicTermMapper;
    private final TeacherMapper teacherMapper;
    private final StudentClassMapper studentClassMapper;

    @Override
    public List<TeachingClassResponse> listTeachingClasses(TeachingClassQueryRequest request) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (request.getClassName() != null && !request.getClassName().trim().isEmpty()) {
                wrapper.like(TeachingClass::getClassName, request.getClassName().trim());
            }
            if (request.getCourseId() != null) {
                wrapper.eq(TeachingClass::getCourseId, request.getCourseId());
            }
            if (request.getTermId() != null) {
                wrapper.eq(TeachingClass::getTermId, request.getTermId());
            }
            if (request.getTeacherId() != null) {
                wrapper.eq(TeachingClass::getTeacherId, request.getTeacherId());
            }
            if (request.getCalcStatus() != null && !request.getCalcStatus().trim().isEmpty()) {
                wrapper.eq(TeachingClass::getCalcStatus, request.getCalcStatus().trim());
            }
        }

        wrapper.orderByDesc(TeachingClass::getCreatedAt);

        List<TeachingClass> classes = teachingClassMapper.selectList(wrapper);
        return toResponseList(classes);
    }

    @Override
    public List<TeachingClassResponse> listTeachingClassesForSelect() {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(TeachingClass::getCalcStatus, "locked")
               .orderByDesc(TeachingClass::getCreatedAt);
        List<TeachingClass> classes = teachingClassMapper.selectList(wrapper);
        return toResponseList(classes);
    }

    @Override
    public TeachingClassResponse getTeachingClassById(Long classId) {
        if (classId == null) {
            throw new BusinessException(400, "教学班ID不能为空");
        }

        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        return toResponse(teachingClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTeachingClass(TeachingClassSaveRequest request) {
        // 验证课程是否存在
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(400, "所选课程不存在");
        }

        // 验证学期是否存在
        AcademicTerm term = academicTermMapper.selectById(request.getTermId());
        if (term == null) {
            throw new BusinessException(400, "所选学期不存在");
        }

        // 验证教师是否存在
        Teacher teacher = teacherMapper.selectById(request.getTeacherId());
        if (teacher == null) {
            throw new BusinessException(400, "所选教师不存在");
        }

        if (request.getClassId() == null) {
            // 新增
            createTeachingClass(request);
        } else {
            // 更新
            updateTeachingClass(request);
        }
    }

    private void createTeachingClass(TeachingClassSaveRequest request) {
        // 检查同一课程同一学期内班级名称是否重复
        TeachingClass existing = teachingClassMapper.selectOne(new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getCourseId, request.getCourseId())
                .eq(TeachingClass::getTermId, request.getTermId())
                .eq(TeachingClass::getClassName, request.getClassName()));
        if (existing != null) {
            throw new BusinessException(400, "同一课程同一学期内班级名称已存在");
        }

        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setClassName(request.getClassName());
        teachingClass.setCourseId(request.getCourseId());
        teachingClass.setTermId(request.getTermId());
        teachingClass.setTeacherId(request.getTeacherId());
        teachingClass.setCalcStatus("unsubmitted");

        teachingClassMapper.insert(teachingClass);
    }

    private void updateTeachingClass(TeachingClassSaveRequest request) {
        TeachingClass teachingClass = teachingClassMapper.selectById(request.getClassId());
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 检查是否有关联的学生
        Long studentCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, request.getClassId()));
        if (studentCount != null && studentCount > 0) {
            // 如果有学生关联，不允许修改课程和学期
            if (!teachingClass.getCourseId().equals(request.getCourseId()) ||
                !teachingClass.getTermId().equals(request.getTermId())) {
                throw new BusinessException(400, "该教学班已有关联学生，不允许修改所属课程或学期");
            }
        }

        // 检查班级名称是否重复（排除自身）
        TeachingClass existing = teachingClassMapper.selectOne(new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getCourseId, request.getCourseId())
                .eq(TeachingClass::getTermId, request.getTermId())
                .eq(TeachingClass::getClassName, request.getClassName())
                .ne(TeachingClass::getClassId, request.getClassId()));
        if (existing != null) {
            throw new BusinessException(400, "同一课程同一学期内班级名称已存在");
        }

        teachingClass.setClassName(request.getClassName());
        teachingClass.setCourseId(request.getCourseId());
        teachingClass.setTermId(request.getTermId());
        teachingClass.setTeacherId(request.getTeacherId());

        teachingClassMapper.updateById(teachingClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeachingClassStatus(TeachingClassStatusRequest request) {
        TeachingClass teachingClass = teachingClassMapper.selectById(request.getClassId());
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        teachingClass.setCalcStatus(request.getCalcStatus());
        teachingClassMapper.updateById(teachingClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeachingClass(Long classId) {
        if (classId == null) {
            throw new BusinessException(400, "教学班ID不能为空");
        }

        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        // 检查是否有关联的学生
        Long studentCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, classId));
        if (studentCount != null && studentCount > 0) {
            throw new BusinessException(400, "该教学班下存在学生，无法删除");
        }

        try {
            teachingClassMapper.deleteById(classId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该教学班存在关联数据，无法删除");
        }
    }

    private List<TeachingClassResponse> toResponseList(List<TeachingClass> classes) {
        if (classes.isEmpty()) {
            return List.of();
        }

        // 批量查询关联数据
        Set<Long> courseIds = classes.stream().map(TeachingClass::getCourseId).collect(Collectors.toSet());
        Set<Long> termIds = classes.stream().map(TeachingClass::getTermId).collect(Collectors.toSet());
        Set<Long> teacherIds = classes.stream().map(TeachingClass::getTeacherId).collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        Map<Long, AcademicTerm> termMap = academicTermMapper.selectBatchIds(termIds).stream()
                .collect(Collectors.toMap(AcademicTerm::getTermId, t -> t));
        Map<Long, Teacher> teacherMap = teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, t -> t));

        return classes.stream()
                .map(tc -> {
                    Course course = courseMap.get(tc.getCourseId());
                    AcademicTerm term = termMap.get(tc.getTermId());
                    Teacher teacher = teacherMap.get(tc.getTeacherId());

                    return TeachingClassResponse.builder()
                            .classId(tc.getClassId())
                            .className(tc.getClassName())
                            .courseId(tc.getCourseId())
                            .courseName(course != null ? course.getCourseName() : null)
                            .courseCode(course != null ? course.getCourseCode() : null)
                            .termId(tc.getTermId())
                            .termCode(term != null ? term.getTermCode() : null)
                            .teacherId(tc.getTeacherId())
                            .teacherName(teacher != null ? teacher.getTeacherName() : null)
                            .calcStatus(tc.getCalcStatus())
                            .createdAt(tc.getCreatedAt())
                            .updatedAt(tc.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private TeachingClassResponse toResponse(TeachingClass teachingClass) {
        return toResponseList(List.of(teachingClass)).get(0);
    }
}
