package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.course.*;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final MajorMapper majorMapper;

    public CourseServiceImpl(CourseMapper courseMapper, MajorMapper majorMapper) {
        this.courseMapper = courseMapper;
        this.majorMapper = majorMapper;
    }

    @Override
    public List<CourseVO> listCourses(CourseQueryRequest request) {
        CourseQueryRequest query = request == null ? new CourseQueryRequest() : request;
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .like(query.getCourseCode() != null, Course::getCourseCode, trimToNull(query.getCourseCode()))
                .like(query.getCourseName() != null, Course::getCourseName, trimToNull(query.getCourseName()))
                .eq(query.getMajorId() != null, Course::getMajorId, query.getMajorId())
                .orderByAsc(Course::getCourseId);

        List<Course> courses = courseMapper.selectList(wrapper);
        if (courses.isEmpty()) {
            return List.of();
        }

        Map<Long, String> majorNameMap = buildMajorNameMap(courses);
        return courses.stream()
                .map(c -> CourseVO.builder()
                        .courseId(c.getCourseId())
                        .courseCode(c.getCourseCode())
                        .courseName(c.getCourseName())
                        .credit(c.getCredit())
                        .majorId(c.getMajorId())
                        .majorName(majorNameMap.getOrDefault(c.getMajorId(), ""))
                        .status(c.getStatus())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public CourseVO getCourse(Long courseId) {
        Course course = requireCourse(courseId);
        Major major = majorMapper.selectById(course.getMajorId());
        return CourseVO.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credit(course.getCredit())
                .majorId(course.getMajorId())
                .majorName(major != null ? major.getMajorName() : "")
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCourse(CourseSaveRequest request) {
        validateStatus(request.getStatus());

        String code = normalizeRequired(request.getCourseCode(), "课程编码不能为空");
        String name = normalizeRequired(request.getCourseName(), "课程名称不能为空");
        requireMajorExists(request.getMajorId());

        if (request.getCourseId() == null) {
            if (courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, code)) != null) {
                throw new BusinessException(400, "课程编码已存在");
            }
            Course course = new Course();
            course.setCourseCode(code);
            course.setCourseName(name);
            course.setCredit(request.getCredit());
            course.setMajorId(request.getMajorId());
            course.setStatus(request.getStatus());
            courseMapper.insert(course);
        } else {
            Course course = requireCourse(request.getCourseId());
            Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, code)
                    .ne(Course::getCourseId, request.getCourseId()));
            if (existing != null) {
                throw new BusinessException(400, "课程编码已存在");
            }
            course.setCourseCode(code);
            course.setCourseName(name);
            course.setCredit(request.getCredit());
            course.setMajorId(request.getMajorId());
            course.setStatus(request.getStatus());
            courseMapper.updateById(course);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(CourseStatusRequest request) {
        validateStatus(request.getStatus());
        Course course = requireCourse(request.getCourseId());
        course.setStatus(request.getStatus());
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        requireCourse(courseId);
        try {
            courseMapper.deleteById(courseId);
        } catch (Exception e) {
            throw new BusinessException(400, "该课程下存在关联数据（教学班级、课程目标等），无法删除。请先停用该课程");
        }
    }

    private Course requireCourse(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        return course;
    }

    private void requireMajorExists(Long majorId) {
        if (majorId == null || majorMapper.selectById(majorId) == null) {
            throw new BusinessException(400, "所选专业不存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为0或1");
        }
    }

    private Map<Long, String> buildMajorNameMap(List<Course> courses) {
        List<Long> majorIds = courses.stream()
                .map(Course::getMajorId)
                .distinct()
                .toList();
        if (majorIds.isEmpty()) {
            return Map.of();
        }
        return majorMapper.selectBatchIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, Major::getMajorName));
    }

    private String normalizeRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(400, message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
