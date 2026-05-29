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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            if (hasText(request.getCourseCode())) {
                wrapper.like(Course::getCourseCode, request.getCourseCode().trim());
            }
            if (hasText(request.getCourseName())) {
                wrapper.like(Course::getCourseName, request.getCourseName().trim());
            }
            if (request.getStatus() != null) {
                wrapper.eq(Course::getStatus, request.getStatus());
            }
            if (request.getMajorId() != null) {
                List<Long> courseIds = courseMajorMapper.selectList(
                                new LambdaQueryWrapper<CourseMajor>()
                                        .eq(CourseMajor::getMajorId, request.getMajorId()))
                        .stream()
                        .map(CourseMajor::getCourseId)
                        .distinct()
                        .toList();
                if (courseIds.isEmpty()) {
                    return List.of();
                }
                wrapper.in(Course::getCourseId, courseIds);
            }
        }

        wrapper.orderByAsc(Course::getCourseCode);
        List<Course> courses = courseMapper.selectList(wrapper);
        return buildCourseResponses(courses);
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "courseId is required");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "course not found");
        }

        return buildCourseResponses(List.of(course)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCourse(CourseCreateRequest request) {
        String courseCode = request.getCourseCode().trim();
        if (courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, courseCode)) != null) {
            throw new BusinessException(400, "courseCode already exists");
        }

        List<Long> normalizedMajorIds = normalizeMajorIds(request.getMajorIds());
        validateMajorIdsExist(normalizedMajorIds);

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(request.getCourseName().trim());
        course.setCredit(request.getCredit());
        course.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        courseMapper.insert(course);

        saveCourseMajorRelations(course.getCourseId(), normalizedMajorIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCourse(CourseSaveRequest request) {
        if (request.getCourseId() == null) {
            CourseCreateRequest createRequest = new CourseCreateRequest();
            createRequest.setCourseCode(request.getCourseCode());
            createRequest.setCourseName(request.getCourseName());
            createRequest.setCredit(request.getCredit());
            createRequest.setMajorIds(request.getMajorIds());
            createRequest.setStatus(request.getStatus());
            createCourse(createRequest);
            return;
        }

        CourseUpdateRequest updateRequest = new CourseUpdateRequest();
        updateRequest.setCourseId(request.getCourseId());
        updateRequest.setCourseCode(request.getCourseCode());
        updateRequest.setCourseName(request.getCourseName());
        updateRequest.setCredit(request.getCredit());
        updateRequest.setMajorIds(request.getMajorIds());
        updateRequest.setStatus(request.getStatus());
        updateCourse(updateRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseUpdateRequest request) {
        if (request.getCourseId() == null) {
            throw new BusinessException(400, "courseId is required");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "course not found");
        }

        if (hasText(request.getCourseCode())) {
            String courseCode = request.getCourseCode().trim();
            Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, courseCode)
                    .ne(Course::getCourseId, request.getCourseId()));
            if (existing != null) {
                throw new BusinessException(400, "courseCode already exists");
            }
            course.setCourseCode(courseCode);
        }

        if (hasText(request.getCourseName())) {
            course.setCourseName(request.getCourseName().trim());
        }

        if (request.getCredit() != null) {
            course.setCredit(request.getCredit());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        courseMapper.updateById(course);

        if (request.getMajorIds() != null) {
            List<Long> normalizedMajorIds = normalizeMajorIds(request.getMajorIds());
            validateMajorIdsExist(normalizedMajorIds);
            syncCourseMajorRelations(course.getCourseId(), normalizedMajorIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(CourseStatusRequest request) {
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "status must be 0 or 1");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "course not found");
        }

        course.setStatus(request.getStatus());
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "courseId is required");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "course not found");
        }

        Long teachingClassRefCount = courseMapper.countTeachingClassReferences(courseId);
        if (teachingClassRefCount != null && teachingClassRefCount > 0) {
            throw new BusinessException(400, "该课程已被教学班引用，无法删除");
        }

        Long supportRefCount = courseMapper.countIndicatorSupportReferences(courseId);
        if (supportRefCount != null && supportRefCount > 0) {
            throw new BusinessException(400, "该课程已被课程支撑关系引用，无法删除");
        }

        Long objectiveRefCount = courseMapper.countCourseObjectiveReferences(courseId);
        if (objectiveRefCount != null && objectiveRefCount > 0) {
            throw new BusinessException(400, "该课程已被课程目标或考核点数据引用，无法删除");
        }

        try {
            courseMapper.deleteById(courseId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该课程存在关联数据，无法删除");
        }
    }

    private List<CourseResponse> buildCourseResponses(List<Course> courses) {
        if (courses.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = courses.stream()
                .map(Course::getCourseId)
                .toList();

        List<CourseMajor> relations = courseMajorMapper.selectList(
                new LambdaQueryWrapper<CourseMajor>()
                        .in(CourseMajor::getCourseId, courseIds)
                        .orderByAsc(CourseMajor::getCmId));

        Map<Long, List<CourseMajor>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(
                        CourseMajor::getCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        Set<Long> majorIdSet = relations.stream()
                .map(CourseMajor::getMajorId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Major> majorMap = majorIdSet.isEmpty()
                ? Map.of()
                : majorMapper.selectBatchIds(majorIdSet).stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));

        return courses.stream()
                .map(course -> toResponse(course, relationMap.getOrDefault(course.getCourseId(), List.of()), majorMap))
                .toList();
    }

    private CourseResponse toResponse(Course course, List<CourseMajor> relations, Map<Long, Major> majorMap) {
        List<Long> majorIds = new ArrayList<>(relations.size());
        List<String> majorNames = new ArrayList<>(relations.size());

        for (CourseMajor relation : relations) {
            majorIds.add(relation.getMajorId());
            Major major = majorMap.get(relation.getMajorId());
            if (major != null) {
                majorNames.add(major.getMajorName());
            }
        }

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credit(course.getCredit())
                .majorIds(majorIds)
                .majorNames(majorNames)
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private void saveCourseMajorRelations(Long courseId, Collection<Long> majorIds) {
        for (Long majorId : majorIds) {
            CourseMajor relation = new CourseMajor();
            relation.setCourseId(courseId);
            relation.setMajorId(majorId);
            courseMajorMapper.insert(relation);
        }
    }

    private void syncCourseMajorRelations(Long courseId, List<Long> majorIds) {
        courseMajorMapper.delete(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getCourseId, courseId));
        saveCourseMajorRelations(courseId, majorIds);
    }

    private void validateMajorIdsExist(List<Long> majorIds) {
        if (majorIds.isEmpty()) {
            throw new BusinessException(400, "majorIds cannot be empty");
        }

        List<Major> majors = majorMapper.selectBatchIds(majorIds);
        if (majors.size() != majorIds.size()) {
            throw new BusinessException(400, "selected major does not exist");
        }
    }

    private List<Long> normalizeMajorIds(List<Long> majorIds) {
        if (majorIds == null) {
            return Collections.emptyList();
        }

        return majorIds.stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
