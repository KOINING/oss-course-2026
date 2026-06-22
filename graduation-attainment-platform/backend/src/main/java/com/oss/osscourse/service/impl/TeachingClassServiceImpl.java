package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.PageQueryUtils;
import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.teachingclass.TeachingClassQueryRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassResponse;
import com.oss.osscourse.dto.teachingclass.TeachingClassSaveRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassStatusRequest;
import com.oss.osscourse.dto.teachingclass.TeachingClassStudentCountResponse;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.entity.StudentAssessmentScore;
import com.oss.osscourse.entity.StudentClass;
import com.oss.osscourse.entity.StudentObjectiveAchievement;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.entity.UnlockAuditLog;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseIndicatorAchievementMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveAchievementMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.mapper.StudentAssessmentScoreMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.StudentObjectiveAchievementMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.mapper.UnlockAuditLogMapper;
import com.oss.osscourse.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingClassServiceImpl implements TeachingClassService {

    private final TeachingClassMapper teachingClassMapper;
    private final CourseMapper courseMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final MajorMapper majorMapper;
    private final AcademicTermMapper academicTermMapper;
    private final TeacherMapper teacherMapper;
    private final StudentClassMapper studentClassMapper;
    private final StudentAssessmentScoreMapper studentAssessmentScoreMapper;
    private final StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;
    private final CourseObjectiveAchievementMapper courseObjectiveAchievementMapper;
    private final CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;
    private final UnlockAuditLogMapper unlockAuditLogMapper;

    @Override
    public List<TeachingClassResponse> listTeachingClasses(TeachingClassQueryRequest request) {
        LambdaQueryWrapper<TeachingClass> wrapper = buildQueryWrapper(request);
        wrapper.orderByAsc(TeachingClass::getClassCode)
                .orderByDesc(TeachingClass::getCreatedAt);
        return toResponseList(teachingClassMapper.selectList(wrapper));
    }

    @Override
    public PageResult<TeachingClassResponse> listTeachingClassesByPage(TeachingClassQueryRequest request) {
        LambdaQueryWrapper<TeachingClass> wrapper = buildQueryWrapper(request);
        wrapper.orderByAsc(TeachingClass::getClassCode)
                .orderByDesc(TeachingClass::getCreatedAt);

        int pageNum = PageQueryUtils.normalizePageNum(request != null ? request.getPageNum() : null);
        int pageSize = PageQueryUtils.normalizePageSize(request != null ? request.getPageSize() : null);

        Page<TeachingClass> page = teachingClassMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        List<TeachingClassResponse> records = toResponseList(page.getRecords());
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
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

        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        validateCourseMajorBinding(request.getCourseId(), request.getMajorId(), request.getGradeYear());

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
        validateMajorGradeCourseUnique(request.getMajorId(), request.getGradeYear(), request.getCourseId(), null);

        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setClassCode(request.getClassCode().trim());
        teachingClass.setClassName(request.getClassName().trim());
        teachingClass.setCourseId(request.getCourseId());
        teachingClass.setMajorId(request.getMajorId());
        teachingClass.setGradeYear(request.getGradeYear());
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
                    || !teachingClass.getMajorId().equals(request.getMajorId())
                    || !teachingClass.getGradeYear().equals(request.getGradeYear())
                    || !teachingClass.getTermId().equals(request.getTermId())) {
                throw new BusinessException(400, "该教学班已有学生关联，不允许修改所属专业、年级、课程或学期");
            }
        }

        validateClassCodeUnique(request.getClassCode().trim(), request.getClassId());
        validateMajorGradeCourseUnique(request.getMajorId(), request.getGradeYear(), request.getCourseId(), request.getClassId());

        teachingClass.setClassCode(request.getClassCode().trim());
        teachingClass.setClassName(request.getClassName().trim());
        teachingClass.setCourseId(request.getCourseId());
        teachingClass.setMajorId(request.getMajorId());
        teachingClass.setGradeYear(request.getGradeYear());
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

        List<String> blockingReasons = listDeleteBlockingReasons(classId);
        if (!blockingReasons.isEmpty()) {
            throw new BusinessException(400, "该教学班存在" + String.join("、", blockingReasons) + "，无法删除");
        }

        try {
            studentClassMapper.delete(new LambdaQueryWrapper<StudentClass>().eq(StudentClass::getClassId, classId));
            teachingClassMapper.deleteById(classId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该教学班存在关联数据，无法删除");
        }
    }

    private List<String> listDeleteBlockingReasons(Long classId) {
        List<String> reasons = new ArrayList<>();
        addDeleteBlockingReason(reasons, "原始成绩", studentAssessmentScoreMapper.selectCount(
                new LambdaQueryWrapper<StudentAssessmentScore>().eq(StudentAssessmentScore::getClassId, classId)));
        addDeleteBlockingReason(reasons, "学生课程目标达成度", studentObjectiveAchievementMapper.selectCount(
                new LambdaQueryWrapper<StudentObjectiveAchievement>().eq(StudentObjectiveAchievement::getClassId, classId)));
        addDeleteBlockingReason(reasons, "课程目标达成度", courseObjectiveAchievementMapper.selectCount(
                new LambdaQueryWrapper<CourseObjectiveAchievement>().eq(CourseObjectiveAchievement::getClassId, classId)));
        addDeleteBlockingReason(reasons, "课程指标点达成度", courseIndicatorAchievementMapper.selectCount(
                new LambdaQueryWrapper<CourseIndicatorAchievement>().eq(CourseIndicatorAchievement::getClassId, classId)));
        addDeleteBlockingReason(reasons, "解锁审核记录", unlockAuditLogMapper.selectCount(
                new LambdaQueryWrapper<UnlockAuditLog>().eq(UnlockAuditLog::getClassId, classId)));
        return reasons;
    }

    private void addDeleteBlockingReason(List<String> reasons, String label, Long count) {
        if (count != null && count > 0) {
            reasons.add(label + count + "条");
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

    private void validateCourseMajorBinding(Long courseId, Long majorId, Integer gradeYear) {
        Long count = courseMajorMapper.selectCount(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, courseId)
                .eq(CourseMajor::getMajorId, majorId)
                .eq(CourseMajor::getGradeYear, gradeYear));
        if (count == null || count == 0) {
            throw new BusinessException(400, "该课程未绑定到所选专业和年级培养方案");
        }
    }

    private void validateMajorGradeCourseUnique(Long majorId, Integer gradeYear, Long courseId, Long currentClassId) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<TeachingClass>()
                .eq(TeachingClass::getMajorId, majorId)
                .eq(TeachingClass::getGradeYear, gradeYear)
                .eq(TeachingClass::getCourseId, courseId);
        if (currentClassId != null) {
            wrapper.ne(TeachingClass::getClassId, currentClassId);
        }
        if (teachingClassMapper.selectOne(wrapper) != null) {
            throw new BusinessException(400, "同一专业同一年级同一课程已存在教学班");
        }
    }

    private List<TeachingClassResponse> toResponseList(List<TeachingClass> classes) {
        if (classes.isEmpty()) {
            return List.of();
        }

        Set<Long> courseIds = classes.stream().map(TeachingClass::getCourseId).collect(Collectors.toSet());
        Set<Long> majorIds = classes.stream().map(TeachingClass::getMajorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> termIds = classes.stream().map(TeachingClass::getTermId).collect(Collectors.toSet());
        Set<Long> teacherIds = classes.stream().map(TeachingClass::getTeacherId).collect(Collectors.toSet());
        Set<Long> classIds = classes.stream().map(TeachingClass::getClassId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course));
        Map<Long, Major> majorMap = majorIds.isEmpty()
                ? Map.of()
                : majorMapper.selectBatchIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));
        Map<Long, AcademicTerm> termMap = academicTermMapper.selectBatchIds(termIds).stream()
                .collect(Collectors.toMap(AcademicTerm::getTermId, term -> term));
        Map<Long, Teacher> teacherMap = teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, teacher -> teacher));
        Map<Long, Long> studentCountMap = classIds.isEmpty()
                ? Map.of()
                : studentClassMapper.countStudentsByClassIds(new ArrayList<>(classIds)).stream()
                .collect(Collectors.toMap(
                        TeachingClassStudentCountResponse::getClassId,
                        item -> item.getStudentCount() == null ? 0L : item.getStudentCount()));

        return classes.stream()
                .map(item -> TeachingClassResponse.builder()
                        .classId(item.getClassId())
                        .classCode(item.getClassCode())
                        .className(item.getClassName())
                        .courseId(item.getCourseId())
                        .courseName(courseMap.get(item.getCourseId()) != null ? courseMap.get(item.getCourseId()).getCourseName() : null)
                        .courseCode(courseMap.get(item.getCourseId()) != null ? courseMap.get(item.getCourseId()).getCourseCode() : null)
                        .majorId(item.getMajorId())
                        .majorName(majorMap.get(item.getMajorId()) != null ? majorMap.get(item.getMajorId()).getMajorName() : null)
                        .gradeYear(item.getGradeYear())
                        .termId(item.getTermId())
                        .termCode(termMap.get(item.getTermId()) != null ? termMap.get(item.getTermId()).getTermCode() : null)
                        .teacherId(item.getTeacherId())
                        .teacherName(teacherMap.get(item.getTeacherId()) != null ? teacherMap.get(item.getTeacherId()).getTeacherName() : null)
                        .studentCount(studentCountMap.getOrDefault(item.getClassId(), 0L))
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

    private LambdaQueryWrapper<TeachingClass> buildQueryWrapper(TeachingClassQueryRequest request) {
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
            if (request.getMajorId() != null) {
                wrapper.eq(TeachingClass::getMajorId, request.getMajorId());
            }
            if (request.getGradeYear() != null) {
                wrapper.eq(TeachingClass::getGradeYear, request.getGradeYear());
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
        return wrapper;
    }
}
