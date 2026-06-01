package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.course.CourseCreateRequest;
import com.oss.osscourse.dto.course.CourseImportResult;
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
import com.oss.osscourse.util.ImportSheetReader;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

    @Override
    public CourseImportResult importCourses(MultipartFile file) {
        List<CourseImportResult.FailedItem> failedItems = new ArrayList<>();
        int successCount = 0;
        int totalCount = 0;
        Set<String> courseCodesInBatch = new HashSet<>();

        List<ImportSheetReader.ImportRowData> rows = ImportSheetReader.readDataRows(file);
        for (ImportSheetReader.ImportRowData row : rows) {
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            try {
                String error = validateAndImportCourseRow(row, courseCodesInBatch);
                if (error != null) {
                    failedItems.add(CourseImportResult.FailedItem.builder()
                            .rowNumber(row.getRowNumber())
                            .reason(error)
                            .build());
                } else {
                    successCount++;
                }
            } catch (Exception e) {
                failedItems.add(CourseImportResult.FailedItem.builder()
                        .rowNumber(row.getRowNumber())
                        .reason("系统错误: " + e.getMessage())
                        .build());
            }
        }

        return CourseImportResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failedItems.size())
                .failedItems(failedItems)
                .build();
    }

    private String validateAndImportCourseRow(ImportSheetReader.ImportRowData row, Set<String> courseCodesInBatch) {
        String majorCode = row.getCell(0);
        String courseCode = row.getCell(1);
        String courseName = row.getCell(2);
        String creditStr = row.getCell(3);
        String statusStr = row.getCell(4);

        // 所属专业代码必须已存在
        if (majorCode == null || majorCode.isEmpty()) {
            return "所属专业代码不能为空";
        }
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return "所属专业代码不存在: " + majorCode;
        }

        // 课程代码不能为空
        if (courseCode == null || courseCode.isEmpty()) {
            return "课程代码不能为空";
        }

        // 课程名称不能为空
        if (courseName == null || courseName.isEmpty()) {
            return "课程名称不能为空";
        }

        // 学分必须为合法数值
        Float credit;
        if (creditStr == null || creditStr.isEmpty()) {
            return "学分不能为空";
        }
        try {
            credit = Float.parseFloat(creditStr);
            if (credit < 0) {
                return "学分不能为负数: " + creditStr;
            }
        } catch (NumberFormatException e) {
            return "学分必须为合法数值: " + creditStr;
        }

        // 状态
        Integer status = 1;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Integer.parseInt(statusStr);
                if (status != 0 && status != 1) {
                    return "状态值必须为0或1: " + statusStr;
                }
            } catch (NumberFormatException e) {
                return "状态值必须为合法整数: " + statusStr;
            }
        }

        // 课程代码在同一模板中不能重复
        if (!courseCodesInBatch.add(courseCode)) {
            return "课程代码在导入模板中重复: " + courseCode;
        }

        // 检查数据库中是否已存在相同课程代码
        Course existingInDb = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCourseCode, courseCode));
        if (existingInDb != null) {
            return "课程代码已存在: " + courseCode;
        }

        // 创建课程
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCredit(credit);
        course.setStatus(status);
        courseMapper.insert(course);

        // 创建课程-专业关联
        CourseMajor relation = new CourseMajor();
        relation.setCourseId(course.getCourseId());
        relation.setMajorId(major.getMajorId());
        courseMajorMapper.insert(relation);

        return null;
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
