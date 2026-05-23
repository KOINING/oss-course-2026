package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.course.CourseCreateRequest;
import com.oss.osscourse.dto.course.CourseQueryRequest;
import com.oss.osscourse.dto.course.CourseResponse;
import com.oss.osscourse.dto.course.CourseSaveRequest;
import com.oss.osscourse.dto.course.CourseStatusRequest;
import com.oss.osscourse.dto.course.CourseUpdateRequest;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final MajorMapper majorMapper;

    @Override
    public List<CourseResponse> listCourses(CourseQueryRequest request) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (hasText(request.getCourseCode())) {
                wrapper.like(Course::getCourseCode, request.getCourseCode().trim());
            }
            if (hasText(request.getCourseName())) {
                wrapper.like(Course::getCourseName, request.getCourseName().trim());
            }
            if (request.getMajorId() != null) {
                wrapper.eq(Course::getMajorId, request.getMajorId());
            }
            if (request.getStatus() != null) {
                wrapper.eq(Course::getStatus, request.getStatus());
            }
        }

        wrapper.orderByAsc(Course::getCourseCode);

        List<Course> courses = courseMapper.selectList(wrapper);
        Map<Long, Major> majorMap = buildMajorMap(courses);

        return courses.stream()
                .map(course -> {
                    Long majorId = course.getMajorId();
                    String majorName = null;
                    if (majorId != null) {
                        Major major = majorMap.get(majorId);
                        if (major != null) {
                            majorName = major.getMajorName();
                        }
                    }
                    return toResponse(course, majorId, majorName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        Long majorId = course.getMajorId();
        String majorName = null;
        if (majorId != null) {
            Major major = majorMapper.selectById(majorId);
            if (major != null) {
                majorName = major.getMajorName();
            }
        }

        return toResponse(course, majorId, majorName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCourse(CourseCreateRequest request) {
        if (courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, request.getCourseCode())) != null) {
            throw new BusinessException(400, "课程编码已存在");
        }
        validateMajorExists(request.getMajorId());

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCredit(request.getCredit());
        course.setMajorId(request.getMajorId());
        course.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        courseMapper.insert(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCourse(CourseSaveRequest request) {
        if (request.getCourseId() == null) {
            CourseCreateRequest createRequest = new CourseCreateRequest();
            createRequest.setCourseCode(request.getCourseCode());
            createRequest.setCourseName(request.getCourseName());
            createRequest.setCredit(request.getCredit());
            createRequest.setMajorId(request.getMajorId());
            createRequest.setStatus(request.getStatus());
            createCourse(createRequest);
            return;
        }

        CourseUpdateRequest updateRequest = new CourseUpdateRequest();
        updateRequest.setCourseId(request.getCourseId());
        updateRequest.setCourseCode(request.getCourseCode());
        updateRequest.setCourseName(request.getCourseName());
        updateRequest.setCredit(request.getCredit());
        updateRequest.setMajorId(request.getMajorId());
        updateRequest.setStatus(request.getStatus());
        updateCourse(updateRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseUpdateRequest request) {
        if (request.getCourseId() == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        if (hasText(request.getCourseCode())) {
            Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, request.getCourseCode())
                    .ne(Course::getCourseId, request.getCourseId()));
            if (existing != null) {
                throw new BusinessException(400, "课程编码已存在");
            }
            course.setCourseCode(request.getCourseCode());
        }

        if (hasText(request.getCourseName())) {
            course.setCourseName(request.getCourseName());
        }

        if (request.getCredit() != null) {
            course.setCredit(request.getCredit());
        }

        if (request.getMajorId() != null) {
            validateMajorExists(request.getMajorId());
            course.setMajorId(request.getMajorId());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(CourseStatusRequest request) {
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态值必须为0或1");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        course.setStatus(request.getStatus());
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        try {
            courseMapper.deleteById(courseId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该课程下存在关联数据，无法删除，请先停用该课程");
        }
    }

    private Map<Long, Major> buildMajorMap(List<Course> courses) {
        List<Long> majorIds = courses.stream()
                .map(Course::getMajorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (majorIds.isEmpty()) {
            return Map.of();
        }

        return majorMapper.selectBatchIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));
    }

    private void validateMajorExists(Long majorId) {
        if (majorId == null || majorMapper.selectById(majorId) == null) {
            throw new BusinessException(400, "所选专业不存在");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private CourseResponse toResponse(Course course, Long majorId, String majorName) {
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credit(course.getCredit())
                .majorId(majorId)
                .majorName(majorName)
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
