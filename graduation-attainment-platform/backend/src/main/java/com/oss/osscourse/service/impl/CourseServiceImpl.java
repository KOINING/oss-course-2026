package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.PageQueryUtils;
import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.course.CourseCreateRequest;
import com.oss.osscourse.dto.course.CourseImportResult;
import com.oss.osscourse.dto.course.CourseMajorGradeYearBindingRequest;
import com.oss.osscourse.dto.course.CourseMajorGradeYearBindingResponse;
import com.oss.osscourse.dto.course.CourseQueryRequest;
import com.oss.osscourse.dto.course.CourseResponse;
import com.oss.osscourse.dto.course.CourseSaveRequest;
import com.oss.osscourse.dto.course.CourseStatusRequest;
import com.oss.osscourse.dto.course.CourseUpdateRequest;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.GraduationRequirement;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.Student;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.GraduationRequirementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
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
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final MajorMapper majorMapper;
    private final GraduationRequirementMapper graduationRequirementMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final StudentMapper studentMapper;

    @Override
    public PageResult<CourseResponse> listCoursesByPage(CourseQueryRequest request) {
        int pageNum = PageQueryUtils.normalizePageNum(request != null ? request.getPageNum() : null);
        Integer pageSize = request != null && request.getPageSize() != null
                ? PageQueryUtils.normalizePageSize(request.getPageSize())
                : null;

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
                List<Long> courseIds = selectCourseIdsByPlanFilters(request.getMajorId(), request.getGradeYear());
                if (courseIds.isEmpty()) {
                    return PageResult.of(List.of(), 0, pageNum, pageSize != null ? pageSize : 0);
                }
                wrapper.in(Course::getCourseId, courseIds);
            } else if (request.getGradeYear() != null) {
                List<Long> courseIds = selectCourseIdsByPlanFilters(null, request.getGradeYear());
                if (courseIds.isEmpty()) {
                    return PageResult.of(List.of(), 0, pageNum, pageSize != null ? pageSize : 0);
                }
                wrapper.in(Course::getCourseId, courseIds);
            }
        }

        wrapper.orderByAsc(Course::getCourseCode);

        long total = courseMapper.selectCount(wrapper);

        if (pageSize != null) {
            int offset = PageQueryUtils.offset(pageNum, pageSize);
            wrapper.last("LIMIT " + offset + "," + pageSize);
        }

        List<Course> courses = courseMapper.selectList(wrapper);
        List<CourseResponse> records = buildCourseResponses(courses);
        int actualPageSize = pageSize != null ? pageSize : courses.size();
        return PageResult.of(records, total, pageNum, actualPageSize);
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(400, "courseId不能为空");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        return buildCourseResponses(List.of(course)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCourse(CourseCreateRequest request) {
        String courseCode = request.getCourseCode().trim();
        if (courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, courseCode)) != null) {
            throw new BusinessException(400, "课程代码已存在");
        }

        List<CourseMajorBindingRow> bindings = normalizeCourseBindings(request.getMajorGradeYearBindings(), request.getMajorIds());
        validateBindings(bindings);

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(request.getCourseName().trim());
        course.setCredit(request.getCredit());
        course.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        courseMapper.insert(course);

        saveCourseMajorRelations(course.getCourseId(), bindings);
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
            createRequest.setMajorGradeYearBindings(request.getMajorGradeYearBindings());
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
        updateRequest.setMajorGradeYearBindings(request.getMajorGradeYearBindings());
        updateRequest.setStatus(request.getStatus());
        updateCourse(updateRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseUpdateRequest request) {
        if (request.getCourseId() == null) {
            throw new BusinessException(400, "courseId不能为空");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        if (hasText(request.getCourseCode())) {
            String courseCode = request.getCourseCode().trim();
            Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, courseCode)
                    .ne(Course::getCourseId, request.getCourseId()));
            if (existing != null) {
                throw new BusinessException(400, "课程代码已存在");
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

        if (request.getMajorIds() != null || request.getMajorGradeYearBindings() != null) {
            List<CourseMajorBindingRow> bindings = normalizeCourseBindings(request.getMajorGradeYearBindings(), request.getMajorIds());
            validateBindings(bindings);
            syncCourseMajorRelations(course.getCourseId(), bindings);
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
            throw new BusinessException(400, "courseId不能为空");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
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
        List<CourseImportResult.SkippedItem> skippedItems = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int totalCount = 0;
        Set<String> courseCodesInBatch = new HashSet<>();

        List<ImportSheetReader.ImportRowData> rows = ImportSheetReader.readDataRows(file);
        for (ImportSheetReader.ImportRowData row : rows) {
            if (row.isEmpty()) {
                continue;
            }

            totalCount++;
            try {
                CourseImportRowResult result = validateAndImportCourseRow(row, courseCodesInBatch);
                if (result.status() == CourseImportRowStatus.FAILURE) {
                    failedItems.add(CourseImportResult.FailedItem.builder()
                            .rowNumber(row.getRowNumber())
                            .reason(result.reason())
                            .build());
                } else if (result.status() == CourseImportRowStatus.SKIPPED) {
                    skippedCount++;
                    skippedItems.add(CourseImportResult.SkippedItem.builder()
                            .rowNumber(row.getRowNumber())
                            .reason(result.reason())
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
                .skippedCount(skippedCount)
                .failureCount(failedItems.size())
                .skippedItems(skippedItems)
                .failedItems(failedItems)
                .build();
    }

    private CourseImportRowResult validateAndImportCourseRow(ImportSheetReader.ImportRowData row, Set<String> courseCodesInBatch) {
        String majorCode = row.getCell(0);
        String gradeYearStr = row.getCell(1);
        String courseCode = row.getCell(2);
        String courseName = row.getCell(3);
        String creditStr = row.getCell(4);
        String statusStr = row.getCell(5);

        if (majorCode == null || majorCode.isEmpty()) {
            return CourseImportRowResult.failure("所属专业代码不能为空");
        }
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getMajorCode, majorCode));
        if (major == null) {
            return CourseImportRowResult.failure("所属专业代码不存在: " + majorCode);
        }

        Integer gradeYear;
        if (gradeYearStr == null || gradeYearStr.isEmpty()) {
            return CourseImportRowResult.failure("适用年级不能为空");
        }
        try {
            gradeYear = Integer.parseInt(gradeYearStr);
        } catch (NumberFormatException e) {
            return CourseImportRowResult.failure("适用年级必须为合法年份: " + gradeYearStr);
        }
        if (gradeYear < 2000 || gradeYear > 2100) {
            return CourseImportRowResult.failure("适用年级必须为合法年份: " + gradeYearStr);
        }

        if (courseCode == null || courseCode.isEmpty()) {
            return CourseImportRowResult.failure("课程代码不能为空");
        }

        if (courseName == null || courseName.isEmpty()) {
            return CourseImportRowResult.failure("课程名称不能为空");
        }

        Float credit;
        if (creditStr == null || creditStr.isEmpty()) {
            return CourseImportRowResult.failure("学分不能为空");
        }
        try {
            credit = Float.parseFloat(creditStr);
            if (credit < 0) {
                return CourseImportRowResult.failure("学分不能为负数: " + creditStr);
            }
        } catch (NumberFormatException e) {
            return CourseImportRowResult.failure("学分必须为合法数值: " + creditStr);
        }

        Integer status = 1;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Integer.parseInt(statusStr);
                if (status != 0 && status != 1) {
                    return CourseImportRowResult.failure("状态值必须为0或1: " + statusStr);
                }
            } catch (NumberFormatException e) {
                return CourseImportRowResult.failure("状态值必须为合法整数: " + statusStr);
            }
        }

        String importKey = majorCode + "_" + gradeYear + "_" + courseCode;
        if (!courseCodesInBatch.add(importKey)) {
            return CourseImportRowResult.skipped("同一导入文件中已处理相同的专业、年级和课程代码，已跳过");
        }

        Course existingInDb = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCourseCode, courseCode));
        Course course;
        if (existingInDb == null) {
            course = new Course();
            course.setCourseCode(courseCode);
            course.setCourseName(courseName);
            course.setCredit(credit);
            course.setStatus(status);
            courseMapper.insert(course);
        } else {
            if (!courseName.equals(existingInDb.getCourseName())) {
                return CourseImportRowResult.failure("课程代码已存在且课程名称不一致: " + courseCode);
            }
            if (Float.compare(credit, existingInDb.getCredit()) != 0) {
                return CourseImportRowResult.failure("课程代码已存在且学分不一致: " + courseCode);
            }
            course = existingInDb;
        }

        Long relationCount = courseMajorMapper.selectCount(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, course.getCourseId())
                .eq(CourseMajor::getMajorId, major.getMajorId())
                .eq(CourseMajor::getGradeYear, gradeYear));
        if (relationCount != null && relationCount > 0) {
            return CourseImportRowResult.skipped("该课程已存在于当前专业和年级的培养方案中，已跳过");
        }

        if (existingInDb != null && !status.equals(existingInDb.getStatus())) {
            existingInDb.setStatus(status);
            courseMapper.updateById(existingInDb);
        }

        CourseMajor relation = new CourseMajor();
        relation.setCourseId(course.getCourseId());
        relation.setMajorId(major.getMajorId());
        relation.setGradeYear(gradeYear);
        courseMajorMapper.insert(relation);

        return CourseImportRowResult.success();
    }

    private enum CourseImportRowStatus {
        SUCCESS,
        SKIPPED,
        FAILURE
    }

    private record CourseImportRowResult(CourseImportRowStatus status, String reason) {

        private static CourseImportRowResult success() {
            return new CourseImportRowResult(CourseImportRowStatus.SUCCESS, null);
        }

        private static CourseImportRowResult skipped(String reason) {
            return new CourseImportRowResult(CourseImportRowStatus.SKIPPED, reason);
        }

        private static CourseImportRowResult failure(String reason) {
            return new CourseImportRowResult(CourseImportRowStatus.FAILURE, reason);
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
        Map<Long, NavigableSet<Integer>> gradeYearMap = new LinkedHashMap<>();

        for (CourseMajor relation : relations) {
            if (!majorIds.contains(relation.getMajorId())) {
                majorIds.add(relation.getMajorId());
            }
            Major major = majorMap.get(relation.getMajorId());
            if (major != null && !majorNames.contains(major.getMajorName())) {
                majorNames.add(major.getMajorName());
            }
            gradeYearMap
                    .computeIfAbsent(relation.getMajorId(), key -> new TreeSet<>())
                    .add(relation.getGradeYear());
        }

        List<CourseMajorGradeYearBindingResponse> bindings = gradeYearMap.entrySet().stream()
                .map(entry -> CourseMajorGradeYearBindingResponse.builder()
                        .majorId(entry.getKey())
                        .majorName(majorMap.get(entry.getKey()) == null ? null : majorMap.get(entry.getKey()).getMajorName())
                        .gradeYears(new ArrayList<>(entry.getValue()))
                        .build())
                .toList();

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credit(course.getCredit())
                .majorIds(majorIds)
                .majorNames(majorNames)
                .majorGradeYearBindings(bindings)
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private void saveCourseMajorRelations(Long courseId, Collection<CourseMajorBindingRow> bindings) {
        for (CourseMajorBindingRow binding : bindings) {
            CourseMajor relation = new CourseMajor();
            relation.setCourseId(courseId);
            relation.setMajorId(binding.majorId());
            relation.setGradeYear(binding.gradeYear());
            courseMajorMapper.insert(relation);
        }
    }

    private void syncCourseMajorRelations(Long courseId, List<CourseMajorBindingRow> bindings) {
        courseMajorMapper.delete(
                new LambdaQueryWrapper<CourseMajor>().eq(CourseMajor::getCourseId, courseId));
        saveCourseMajorRelations(courseId, bindings);
    }

    private void validateBindings(List<CourseMajorBindingRow> bindings) {
        if (bindings.isEmpty()) {
            throw new BusinessException(400, "至少需要配置一个专业-年级绑定");
        }

        List<Long> majorIds = bindings.stream().map(CourseMajorBindingRow::majorId).distinct().toList();
        List<Major> majors = majorMapper.selectBatchIds(majorIds);
        if (majors.size() != majorIds.size()) {
            throw new BusinessException(400, "所选专业不存在");
        }
    }

    private List<CourseMajorBindingRow> normalizeCourseBindings(List<CourseMajorGradeYearBindingRequest> bindingRequests,
                                                                List<Long> legacyMajorIds) {
        List<CourseMajorBindingRow> bindings = new ArrayList<>();
        if (bindingRequests != null && !bindingRequests.isEmpty()) {
            for (CourseMajorGradeYearBindingRequest bindingRequest : bindingRequests) {
                if (bindingRequest == null || bindingRequest.getMajorId() == null || bindingRequest.getGradeYears() == null) {
                    continue;
                }
                for (Integer gradeYear : bindingRequest.getGradeYears()) {
                    validateGradeYear(gradeYear);
                    bindings.add(new CourseMajorBindingRow(bindingRequest.getMajorId(), gradeYear));
                }
            }
        }
        if (!bindings.isEmpty()) {
            return bindings.stream().distinct().toList();
        }
        if (legacyMajorIds == null) {
            return Collections.emptyList();
        }
        return legacyMajorIds.stream()
                .filter(id -> id != null)
                .distinct()
                .map(majorId -> new CourseMajorBindingRow(majorId, 2022))
                .toList();
    }

    private List<Long> selectCourseIdsByPlanFilters(Long majorId, Integer gradeYear) {
        LambdaQueryWrapper<CourseMajor> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(CourseMajor::getCourseId)
                .eq(majorId != null, CourseMajor::getMajorId, majorId)
                .eq(gradeYear != null, CourseMajor::getGradeYear, gradeYear);
        return courseMajorMapper.selectList(wrapper).stream()
                .map(CourseMajor::getCourseId)
                .collect(Collectors.toList());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void validateGradeYear(Integer gradeYear) {
        if (gradeYear == null || gradeYear < 2000 || gradeYear > 2100) {
            throw new BusinessException(400, "年级必须在2000到2100之间");
        }
    }

    @Override
    public List<Integer> listGradeYears() {
        NavigableSet<Integer> years = new TreeSet<>((left, right) -> Integer.compare(right, left));
        years.addAll(graduationRequirementMapper.selectList(
                        new LambdaQueryWrapper<GraduationRequirement>()
                                .select(GraduationRequirement::getGradeYear))
                .stream()
                .map(GraduationRequirement::getGradeYear)
                .filter(year -> year != null)
                .collect(Collectors.toSet()));
        years.addAll(courseMajorMapper.selectList(
                        new LambdaQueryWrapper<CourseMajor>()
                                .select(CourseMajor::getGradeYear))
                .stream()
                .map(CourseMajor::getGradeYear)
                .filter(year -> year != null)
                .collect(Collectors.toSet()));
        years.addAll(teachingClassMapper.selectList(
                        new LambdaQueryWrapper<TeachingClass>()
                                .select(TeachingClass::getGradeYear))
                .stream()
                .map(TeachingClass::getGradeYear)
                .filter(year -> year != null)
                .collect(Collectors.toSet()));
        years.addAll(studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .select(Student::getEnrollmentYear))
                .stream()
                .map(Student::getEnrollmentYear)
                .filter(year -> year != null)
                .collect(Collectors.toSet()));
        years.add(2022);
        return new ArrayList<>(years);
    }

    private record CourseMajorBindingRow(Long majorId, Integer gradeYear) {
    }
}

