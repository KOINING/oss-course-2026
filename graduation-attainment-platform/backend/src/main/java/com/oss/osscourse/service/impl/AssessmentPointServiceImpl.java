package com.oss.osscourse.service.impl;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointCreateRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointQueryRequest;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointResponse;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointUpdateRequest;
import com.oss.osscourse.entity.AssessmentPoint;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.mapper.AssessmentPointMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.service.AssessmentPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentPointServiceImpl implements AssessmentPointService {

    private static final String MANAGE_ROLE = "instructor";
    private static final String MANAGE_PERMISSION = "point:write";
    private static final float MAX_TOTAL_FULL_SCORE = 100.0f;

    private final AssessmentPointMapper apMapper;
    private final CourseObjectiveMapper courseObjectiveMapper;

    @Override
    public List<AssessmentPointResponse> list(AssessmentPointQueryRequest request,
                                               List<String> roles,
                                               List<String> permissions) {
        assertManagePermission(roles, permissions);

        AssessmentPointQueryRequest query = request == null ? new AssessmentPointQueryRequest() : request;
        return apMapper.selectListWithDetails(
                trimToNull(query.getApName()), query.getCourseId(), query.getCoId());
    }

    @Override
    public AssessmentPointResponse getById(Long apId,
                                            List<String> roles,
                                            List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (apId == null) {
            throw new BusinessException(400, "考核点ID不能为空");
        }

        AssessmentPointResponse response = apMapper.selectByIdWithDetails(apId);
        if (response == null) {
            throw new BusinessException(404, "考核点不存在");
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AssessmentPointCreateRequest request,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        String apName = normalizeRequired(request.getApName(), "考核点名称不能为空");
        Float fullScore = request.getFullScore();
        Long coId = request.getCoId();

        // 1. 校验绑定目标：coId 必须有效
        if (coId == null) {
            throw new BusinessException(400, "绑定的课程目标不能为空");
        }
        CourseObjective objective = courseObjectiveMapper.selectById(coId);
        if (objective == null) {
            throw new BusinessException(400, "绑定的课程目标(ID=" + coId + ") 不存在");
        }
        Long courseId = objective.getCourseId();

        // 2. 校验满分 > 0
        if (fullScore == null) {
            throw new BusinessException(400, "满分不能为空");
        }
        if (fullScore <= 0) {
            throw new BusinessException(400, "满分必须大于 0，当前值为 " + fullScore);
        }

        // 3. 校验同一课程下考核点名称不能无意义重复
        int nameCount = apMapper.countByNameInCourse(courseId, apName, null);
        if (nameCount > 0) {
            throw new BusinessException(400, "课程(ID=" + courseId + ") 下已存在名称为「" + apName + "」的考核点，请使用不同的名称");
        }

        validateCourseTotalFullScore(courseId, fullScore, null);

        AssessmentPoint entity = new AssessmentPoint();
        entity.setApName(apName);
        entity.setFullScore(fullScore);
        entity.setCoId(coId);
        apMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AssessmentPointUpdateRequest request,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long apId = request.getApId();
        String apName = normalizeRequired(request.getApName(), "考核点名称不能为空");
        Float fullScore = request.getFullScore();
        Long coId = request.getCoId();

        // 1. 考核点必须存在
        if (apId == null) {
            throw new BusinessException(400, "考核点ID不能为空");
        }
        AssessmentPoint entity = apMapper.selectById(apId);
        if (entity == null) {
            throw new BusinessException(404, "考核点不存在");
        }

        // 2. 校验绑定目标：coId 必须有效
        if (coId == null) {
            throw new BusinessException(400, "绑定的课程目标不能为空");
        }
        CourseObjective objective = courseObjectiveMapper.selectById(coId);
        if (objective == null) {
            throw new BusinessException(400, "绑定的课程目标(ID=" + coId + ") 不存在");
        }
        Long courseId = objective.getCourseId();

        // 3. 校验满分 > 0
        if (fullScore == null) {
            throw new BusinessException(400, "满分不能为空");
        }
        if (fullScore <= 0) {
            throw new BusinessException(400, "满分必须大于 0，当前值为 " + fullScore);
        }

        // 4. 校验同一课程下考核点名称不能无意义重复（排除自身）
        int nameCount = apMapper.countByNameInCourse(courseId, apName, apId);
        if (nameCount > 0) {
            throw new BusinessException(400, "课程(ID=" + courseId + ") 下已存在名称为「" + apName + "」的考核点，请使用不同的名称");
        }

        validateCourseTotalFullScore(courseId, fullScore, apId);

        entity.setApName(apName);
        entity.setFullScore(fullScore);
        entity.setCoId(coId);
        apMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long apId,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (apId == null) {
            throw new BusinessException(400, "考核点ID不能为空");
        }
        if (apMapper.selectById(apId) == null) {
            throw new BusinessException(404, "考核点不存在");
        }

        // 删除前校验：是否被学生成绩引用
        int scoreRefCount = apMapper.countScoreRefs(apId);
        if (scoreRefCount > 0) {
            throw new BusinessException(400, "该考核点存在 " + scoreRefCount + " 条学生成绩记录，请先删除相关成绩后再删除考核点");
        }

        try {
            apMapper.deleteById(apId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该考核点存在关联数据，无法删除");
        }
    }

    // ==================== 权限校验 ====================

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行考核点管理操作");
        }
    }

    // ==================== 工具方法 ====================

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

    private void validateCourseTotalFullScore(Long courseId, Float currentFullScore, Long excludeApId) {
        Float existingTotal = apMapper.sumFullScoreByCourse(courseId, excludeApId);
        float safeExistingTotal = existingTotal == null ? 0.0f : existingTotal;
        float nextTotal = safeExistingTotal + currentFullScore;
        if (nextTotal > MAX_TOTAL_FULL_SCORE) {
            throw new BusinessException(
                    400,
                    "当前课程下考核点总满分不能超过 100，现有总满分为 " + safeExistingTotal + "，本次提交后将达到 " + nextTotal
            );
        }
    }
}
