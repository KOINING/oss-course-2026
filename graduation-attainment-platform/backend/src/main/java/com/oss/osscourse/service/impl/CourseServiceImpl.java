package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.course.*;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CourseMajorMapper;
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
    private final CourseMajorMapper courseMajorMapper;
    private final MajorMapper majorMapper;

    @Override
    public List<CourseResponse> listCourses(CourseQueryRequest request) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (request.getCourseCode() != null && !request.getCourseCode().trim().isEmpty()) {
                wrapper.like(Course::getCourseCode, request.getCourseCode().trim());
            }
            if (request.getCourseName() != null && !request.getCourseName().trim().isEmpty()) {
                wrapper.like(Course::getCourseName, request.getCourseName().trim());
            }
            if (request.getStatus() != null) {
                wrapper.eq(Course::getStatus, request.getStatus());
            }
        }

        wrapper.orderByAsc(Course::getCourseCode);

        List<Course> courses = courseMapper.selectList(wrapper);

        // 构建 courseId -> majorId 映射
        List<Long> courseIds = courses.stream().map(Course::getCourseId).collect(Collectors.toList());
        Map<Long, Long> courseMajorMap = Map.of();
        Map<Long, Major> majorMap = Map.of();

        if (!courseIds.isEmpty()) {
            List<CourseMajor> courseMajors = courseMajorMapper.selectList(
                    new LambdaQueryWrapper<CourseMajor>().in(CourseMajor::getCourseId, courseIds));
            courseMajorMap = courseMajors.stream()
                    .collect(Collectors.toMap(CourseMajor::getCourseId, CourseMajor::getMajorId, (a, b) -> a));

            List<Long> majorIds = courseMajors.stream()
                    .map(CourseMajor::getMajorId).distinct().collect(Collectors.toList());
            if (!majorIds.isEmpty()) {
                majorMap = majorMapper.selectBatchIds(majorIds).stream()
                        .collect(Collectors.toMap(Major::getMajorId, m -> m));
            }
        }

        // 按 majorId 过滤
        if (request != null && request.getMajorId() != null) {
            final Map<Long, Long> finalCourseMajorMap = courseMajorMap;
            courses = courses.stream()
                    .filter(c -> finalCourseMajorMap.getOrDefault(c.getCourseId(), -1L).equals(request.getMajorId()))
                    .collect(Collectors.toList());
        }

        final Map<Long, Long> finalCourseMajorMap = courseMajorMap;
        final Map<Long, Major> finalMajorMap = majorMap;
        return courses.stream()
                .map(c -> {
                    Long majorId = finalCourseMajorMap.get(c.getCourseId());
                    String majorName = null;
                    if (majorId != null && finalMajorMap.containsKey(majorId)) {
                        majorName = finalMajorMap.get(majorId).getMajorName();
                    }
                    return toResponse(c, majorId, majorName);
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

        Long majorId = null;
        String majorName = null;
        CourseMajor courseMajor = courseMajorMapper.selectOne(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getCourseId, courseId));
        if (courseMajor != null) {
            majorId = courseMajor.getMajorId();
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
        if (majorMapper.selectById(request.getMajorId()) == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCredit(request.getCredit());
        course.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        courseMapper.insert(course);

        CourseMajor courseMajor = new CourseMajor();
        courseMajor.setCourseId(course.getCourseId());
        courseMajor.setMajorId(request.getMajorId());
        courseMajorMapper.insert(courseMajor);
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
        } else {
            CourseUpdateRequest updateRequest = new CourseUpdateRequest();
            updateRequest.setCourseId(request.getCourseId());
            updateRequest.setCourseCode(request.getCourseCode());
            updateRequest.setCourseName(request.getCourseName());
            updateRequest.setCredit(request.getCredit());
            updateRequest.setMajorId(request.getMajorId());
            updateRequest.setStatus(request.getStatus());
            updateCourse(updateRequest);
        }
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

        if (request.getCourseCode() != null && !request.getCourseCode().isEmpty()) {
            Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, request.getCourseCode())
                    .ne(Course::getCourseId, request.getCourseId()));
            if (existing != null) {
                throw new BusinessException(400, "课程编码已存在");
            }
            course.setCourseCode(request.getCourseCode());
        }

        if (request.getCourseName() != null && !request.getCourseName().isEmpty()) {
            course.setCourseName(request.getCourseName());
        }

        if (request.getCredit() != null) {
            course.setCredit(request.getCredit());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        courseMapper.updateById(course);

        if (request.getMajorId() != null) {
            if (majorMapper.selectById(request.getMajorId()) == null) {
                throw new BusinessException(400, "所选专业不存在");
            }
            CourseMajor courseMajor = courseMajorMapper.selectOne(
                    new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getCourseId, request.getCourseId()));
            if (courseMajor != null) {
                courseMajor.setMajorId(request.getMajorId());
                courseMajorMapper.updateById(courseMajor);
            } else {
                courseMajor = new CourseMajor();
                courseMajor.setCourseId(request.getCourseId());
                courseMajor.setMajorId(request.getMajorId());
                courseMajorMapper.insert(courseMajor);
            }
        }
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

        courseMajorMapper.delete(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, courseId));
        try {
            courseMapper.deleteById(courseId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该课程下存在关联数据（教学班级、课程目标等），无法删除。请先停用该课程");
        }
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
