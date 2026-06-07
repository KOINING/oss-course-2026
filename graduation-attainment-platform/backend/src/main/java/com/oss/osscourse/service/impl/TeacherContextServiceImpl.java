package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.teachercontext.ScoreImportContextResponse;
import com.oss.osscourse.dto.teachercontext.TeacherClassRequest;
import com.oss.osscourse.dto.teachercontext.TeacherClassStudentResponse;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassQueryRequest;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassResponse;
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import com.oss.osscourse.entity.Teacher;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.mapper.ObjectiveIndicatorContributionMapper;
import com.oss.osscourse.mapper.StudentClassMapper;
import com.oss.osscourse.mapper.TeacherMapper;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.TeacherContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherContextServiceImpl implements TeacherContextService {
    private static final String INSTRUCTOR_ROLE = "instructor";

    private final TeacherMapper teacherMapper;
    private final TeachingClassMapper teachingClassMapper;
    private final StudentClassMapper studentClassMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final AssessmentPointMapper assessmentPointMapper;
    private final ObjectiveIndicatorContributionMapper oicMapper;

    @Override
    public List<TeacherTeachingClassResponse> listMyTeachingClasses(TeacherTeachingClassQueryRequest request,
                                                                    Long userId,
                                                                    List<String> roles) {
        Teacher teacher = resolveCurrentTeacher(userId, roles);
        TeacherTeachingClassQueryRequest query = request == null ? new TeacherTeachingClassQueryRequest() : request;
        List<TeacherTeachingClassResponse> rows = teachingClassMapper.selectTeacherTeachingClasses(
                teacher.getId(),
                query.getCourseId(),
                query.getTermId(),
                query.getGradeYear(),
                trimToNull(query.getClassCode()),
                trimToNull(query.getCalcStatus()));
        rows.forEach(row -> fillProgramMatch(row, teacher));
        return rows;
    }

    @Override
    public List<TeacherClassStudentResponse> listMyClassStudents(TeacherClassRequest request,
                                                                 Long userId,
                                                                 List<String> roles) {
        Teacher teacher = resolveCurrentTeacher(userId, roles);
        assertClassOwnedByTeacher(request.getClassId(), teacher);
        return studentClassMapper.selectStudentsByClassId(request.getClassId());
    }

    @Override
    public ScoreImportContextResponse getScoreImportContext(TeacherClassRequest request,
                                                            Long userId,
                                                            List<String> roles) {
        Teacher teacher = resolveCurrentTeacher(userId, roles);
        TeachingClass teachingClass = assertClassOwnedByTeacher(request.getClassId(), teacher);
        TeacherTeachingClassResponse context = teachingClassMapper.selectTeachingClassContext(request.getClassId());
        fillProgramMatch(context, teacher);

        boolean hasInstructorRole = hasInstructorRole(roles);
        boolean ownsClass = teacher.getId().equals(teachingClass.getTeacherId());
        List<String> blockReasons = new ArrayList<>();
        if (!hasInstructorRole) {
            blockReasons.add("当前账号缺少课程主讲教师角色");
        }
        if (!Boolean.TRUE.equals(context.getProgramMatched())) {
            blockReasons.add(context.getBlockReason());
        }

        Long studentCount = studentClassMapper.selectCount(
                new LambdaQueryWrapper<com.oss.osscourse.entity.StudentClass>()
                        .eq(com.oss.osscourse.entity.StudentClass::getClassId, request.getClassId()));
        Long objectiveCount = courseObjectiveMapper.selectCount(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, teachingClass.getCourseId()));
        Long assessmentPointCount = countAssessmentPoints(teachingClass.getCourseId());
        Long internalWeightCount = countInternalWeights(teachingClass.getCourseId());

        if (studentCount == null || studentCount == 0) {
            blockReasons.add("当前教学班暂无学生名单，不能生成成绩模板或导入成绩");
        }
        if (objectiveCount == null || objectiveCount == 0) {
            blockReasons.add("当前课程未配置课程目标，不能生成成绩模板或导入成绩");
        }
        if (assessmentPointCount == null || assessmentPointCount == 0) {
            blockReasons.add("当前课程未配置考核点，不能生成成绩模板或导入成绩");
        }
        if (internalWeightCount == null || internalWeightCount == 0) {
            blockReasons.add("当前课程未配置内部权重 w，不能生成成绩模板或导入成绩");
        }
        if ("locked".equals(teachingClass.getCalcStatus())) {
            blockReasons.add("当前课程级计算状态为 locked，不能导入成绩");
        }

        boolean canGenerateTemplate = ownsClass
                && hasInstructorRole
                && Boolean.TRUE.equals(context.getProgramMatched())
                && studentCount != null && studentCount > 0
                && objectiveCount != null && objectiveCount > 0
                && assessmentPointCount != null && assessmentPointCount > 0
                && internalWeightCount != null && internalWeightCount > 0;
        boolean canImportScore = canGenerateTemplate && !"locked".equals(teachingClass.getCalcStatus());

        String permissionBlockReason = null;
        if (!hasInstructorRole) {
            permissionBlockReason = "当前账号缺少课程主讲教师角色";
        } else if (!ownsClass) {
            permissionBlockReason = "当前教师不是该教学班主讲教师";
        }

        return ScoreImportContextResponse.builder()
                .permission(ScoreImportContextResponse.PermissionResult.builder()
                        .userId(userId)
                        .teacherId(teacher.getId())
                        .teacherName(teacher.getTeacherName())
                        .hasInstructorRole(hasInstructorRole)
                        .ownsTeachingClass(ownsClass)
                        .canOperate(ownsClass && hasInstructorRole)
                        .blockReason(permissionBlockReason)
                        .build())
                .teachingClass(context)
                .studentCount(studentCount == null ? 0 : studentCount)
                .courseObjectiveCount(objectiveCount == null ? 0 : objectiveCount)
                .assessmentPointCount(assessmentPointCount == null ? 0 : assessmentPointCount)
                .internalWeightCount(internalWeightCount == null ? 0 : internalWeightCount)
                .calcStatus(teachingClass.getCalcStatus())
                .canGenerateTemplate(canGenerateTemplate)
                .canImportScore(canImportScore)
                .blockReasons(blockReasons)
                .build();
    }

    private Teacher resolveCurrentTeacher(Long userId, List<String> roles) {
        if (userId == null) {
            throw new BusinessException(401, "当前登录信息缺少用户ID");
        }
        if (!hasInstructorRole(roles)) {
            throw new BusinessException(403, "当前账号不是课程主讲教师，无法访问教师端上下文接口");
        }
        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId)
                .eq(Teacher::getStatus, 1));
        if (teacher == null) {
            throw new BusinessException(403, "当前登录用户未绑定启用状态的教师身份");
        }
        return teacher;
    }

    private TeachingClass assertClassOwnedByTeacher(Long classId, Teacher teacher) {
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }
        if (!teacher.getId().equals(teachingClass.getTeacherId())) {
            throw new BusinessException(403, "只能操作当前教师负责的课程和教学班");
        }
        return teachingClass;
    }

    private void fillProgramMatch(TeacherTeachingClassResponse context, Teacher teacher) {
        if (context == null) {
            return;
        }
        Long teacherMajorId = teacher.getMajorId();
        Integer gradeYear = context.getGradeYear();
        if (teacherMajorId == null) {
            context.setProgramMatched(false);
            context.setBlockReason("当前教师未绑定所属专业，无法匹配培养方案上下文");
            return;
        }
        if (gradeYear == null) {
            context.setProgramMatched(false);
            context.setBlockReason("当前教学班未配置培养方案年级，无法匹配专业+年级上下文");
            return;
        }
        Long count = courseMajorMapper.selectCount(new LambdaQueryWrapper<CourseMajor>()
                .eq(CourseMajor::getCourseId, context.getCourseId())
                .eq(CourseMajor::getMajorId, teacherMajorId)
                .eq(CourseMajor::getGradeYear, gradeYear));
        boolean matched = count != null && count > 0;
        context.setProgramMatched(matched);
        if (!matched) {
            context.setBlockReason("教学班所属课程与当前教师专业+年级培养方案不匹配，接口已阻断后续操作");
        } else {
            context.setBlockReason(null);
        }
    }

    private Long countAssessmentPoints(Long courseId) {
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId));
        if (objectives.isEmpty()) {
            return 0L;
        }
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).toList();
        return assessmentPointMapper.selectCount(new LambdaQueryWrapper<AssessmentPoint>()
                .in(AssessmentPoint::getCoId, coIds));
    }

    private Long countInternalWeights(Long courseId) {
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId));
        if (objectives.isEmpty()) {
            return 0L;
        }
        List<Long> coIds = objectives.stream().map(CourseObjective::getCoId).toList();
        return oicMapper.selectCount(new LambdaQueryWrapper<ObjectiveIndicatorContribution>()
                .in(ObjectiveIndicatorContribution::getCoId, coIds));
    }

    private boolean hasInstructorRole(List<String> roles) {
        return roles != null && roles.contains(INSTRUCTOR_ROLE);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
