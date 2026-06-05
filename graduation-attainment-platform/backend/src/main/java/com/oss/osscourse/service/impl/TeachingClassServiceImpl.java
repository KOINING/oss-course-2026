package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.teachingclass.TeachingClassQueryRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassResponse;
import com.oss.osscourse.dto.teachingclass.TeachingClassSaveRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassStatusRequest;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
            if (hasText(request.getClassCode())) {
                wrapper.like(TeachingClass::getClassCode, request.getClassCode().trim());
            }
            if (hasText(request.getClassName())) {
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
            if (hasText(request.getCalcStatus())) {
                wrapper.eq(TeachingClass::getCalcStatus, request.getCalcStatus().trim());
            }
        }

        wrapper.orderByAsc(TeachingClass::getClassCode)
                .orderByDesc(TeachingClass::getCreatedAt);
        return toResponseList(teachingClassMapper.selectList(wrapper));
    }

    @Override
    public List<TeachingClassResponse> listTeachingClassesForSelect() {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(TeachingClass::getCalcStatus, "locked")
                .orderByAsc(TeachingClass::getClassCode);
        return toResponseList(teachingClassMapper.selectList(wrapper));
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
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(400, "所选课程不存在");
        }

        AcademicTerm term = academicTermMapper.selectById(request.getTermId());
        if (term == null) {
            throw new BusinessException(400, "所选学期不存在");
        }

        Teacher teacher = teacherMapper.selectById(request.getTeacherId());
        if (teacher == null) {
            throw new BusinessException(400, "所选教师不存在");
        }

        if (request.getClassId() == null) {
            createTeachingClass(request);
            return;
        }
        updateTeachingClass(request);
    }

    private void createTeachingClass(TeachingClassSaveRequest request) {
        validateClassCodeUnique(request.getClassCode().trim(), null);
        validateClassNameUnique(request.getCourseId(), request.getTermId(), request.getClassName().trim(), null);

        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setClassCode(request.getClassCode().trim());
        teachingClass.setClassName(request.getClassName().trim());
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

        Long studentCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, request.getClassId()));
        if (studentCount != null && studentCount > 0) {
            if (!teachingClass.getCourseId().equals(request.getCourseId())
                    || !teachingClass.getTermId().equals(request.getTermId())) {
                throw new BusinessException(400, "该教学班已有学生关联，不允许修改所属课程或学期");
            }
        }

        validateClassCodeUnique(request.getClassCode().trim(), request.getClassId());
        validateClassNameUnique(request.getCourseId(), request.getTermId(), request.getClassName().trim(), request.getClassId());

        teachingClass.setClassCode(request.getClassCode().trim());
        teachingClass.setClassName(request.getClassName().trim());
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

    private void validateClassCodeUnique(String classCode, Long currentClassId) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getClassCode, classCode);
        if (currentClassId != null) {
            wrapper.ne(TeachingClass::getClassId, currentClassId);
        }
        if (teachingClassMapper.selectOne(wrapper) != null) {
            throw new BusinessException(400, "教学班编号已存在");
        }
    }

    private void validateClassNameUnique(Long courseId, Long termId, String className, Long currentClassId) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getCourseId, courseId)
                .eq(TeachingClass::getTermId, termId)
                .eq(TeachingClass::getClassName, className);
        if (currentClassId != null) {
            wrapper.ne(TeachingClass::getClassId, currentClassId);
        }
        if (teachingClassMapper.selectOne(wrapper) != null) {
            throw new BusinessException(400, "同一课程同一学期内教学班名称已存在");
        }
    }

    private List<TeachingClassResponse> toResponseList(List<TeachingClass> classes) {
        if (classes.isEmpty()) {
            return List.of();
        }

        Set<Long> courseIds = classes.stream().map(TeachingClass::getCourseId).collect(Collectors.toSet());
        Set<Long> termIds = classes.stream().map(TeachingClass::getTermId).collect(Collectors.toSet());
        Set<Long> teacherIds = classes.stream().map(TeachingClass::getTeacherId).collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course));
        Map<Long, AcademicTerm> termMap = academicTermMapper.selectBatchIds(termIds).stream()
                .collect(Collectors.toMap(AcademicTerm::getTermId, term -> term));
        Map<Long, Teacher> teacherMap = teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, teacher -> teacher));

        return classes.stream()
                .map(item -> TeachingClassResponse.builder()
                        .classId(item.getClassId())
                        .classCode(item.getClassCode())
                        .className(item.getClassName())
                        .courseId(item.getCourseId())
                        .courseName(courseMap.get(item.getCourseId()) != null ? courseMap.get(item.getCourseId()).getCourseName() : null)
                        .courseCode(courseMap.get(item.getCourseId()) != null ? courseMap.get(item.getCourseId()).getCourseCode() : null)
                        .termId(item.getTermId())
                        .termCode(termMap.get(item.getTermId()) != null ? termMap.get(item.getTermId()).getTermCode() : null)
                        .teacherId(item.getTeacherId())
                        .teacherName(teacherMap.get(item.getTeacherId()) != null ? teacherMap.get(item.getTeacherId()).getTeacherName() : null)
                        .calcStatus(item.getCalcStatus())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();
    }

    private TeachingClassResponse toResponse(TeachingClass teachingClass) {
        return toResponseList(List.of(teachingClass)).get(0);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
