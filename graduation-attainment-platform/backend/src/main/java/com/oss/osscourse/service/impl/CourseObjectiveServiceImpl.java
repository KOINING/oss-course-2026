package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveCreateRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveQueryRequest;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveResponse;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveUpdateRequest;
import com.oss.osscourse.entity.CourseObjective;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.CourseObjectiveMapper;
import com.oss.osscourse.service.CourseObjectiveService;
import com.oss.osscourse.util.HtmlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseObjectiveServiceImpl implements CourseObjectiveService {

    private static final String MANAGE_ROLE = "instructor";
    private static final String MANAGE_PERMISSION = "objective:write";

    private final CourseObjectiveMapper courseObjectiveMapper;
    private final CourseMapper courseMapper;

    @Override
    public List<CourseObjectiveResponse> list(CourseObjectiveQueryRequest request,
                                               List<String> roles,
                                               List<String> permissions) {
        assertManagePermission(roles, permissions);

        CourseObjectiveQueryRequest query = request == null ? new CourseObjectiveQueryRequest() : request;
        List<CourseObjectiveResponse> list = courseObjectiveMapper.selectListWithCourse(
                trimToNull(query.getObjectiveCode()), query.getCourseId());

        // 列表：description 字段当前从 DB 读取的是原始值，需转为纯文本
        // descriptionRich 不返回（列表保持轻量）
        return list.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseObjectiveResponse getById(Long coId,
                                            List<String> roles,
                                            List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (coId == null) {
            throw new BusinessException(400, "课程目标ID不能为空");
        }

        CourseObjectiveResponse response = courseObjectiveMapper.selectByIdWithCourse(coId);
        if (response == null) {
            throw new BusinessException(404, "课程目标不存在");
        }

        // 详情：description=纯文本，descriptionRich=原始DB值
        String rawDescription = response.getDescription(); // DB原始值
        response.setDescriptionRich(rawDescription);
        response.setDescription(HtmlUtils.stripHtml(rawDescription));

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CourseObjectiveCreateRequest request,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        String objectiveCode = normalizeRequired(request.getObjectiveCode(), "课程目标编号不能为空");
        String description = normalizeRequired(request.getDescription(), "课程目标纯文本描述不能为空");
        String descriptionRich = trimToNull(request.getDescriptionRich());
        Long courseId = request.getCourseId();
        if (courseId == null) {
            throw new BusinessException(400, "所属课程不能为空");
        }
        if (courseMapper.selectById(courseId) == null) {
            throw new BusinessException(400, "所选课程不存在");
        }

        // 同一课程下编号唯一
        if (courseObjectiveMapper.selectOne(new LambdaQueryWrapper<CourseObjective>()
                .eq(CourseObjective::getCourseId, courseId)
                .eq(CourseObjective::getObjectiveCode, objectiveCode)) != null) {
            throw new BusinessException(400, "该课程下已存在相同编号的课程目标");
        }

        CourseObjective entity = new CourseObjective();
        entity.setObjectiveCode(objectiveCode);
        // 存储：优先富文本，否则纯文本
        entity.setCoDescription(descriptionRich != null ? descriptionRich : description);
        entity.setCourseId(courseId);
        courseObjectiveMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseObjectiveUpdateRequest request,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long coId = request.getCoId();
        if (coId == null) {
            throw new BusinessException(400, "课程目标ID不能为空");
        }
        String objectiveCode = normalizeRequired(request.getObjectiveCode(), "课程目标编号不能为空");
        String description = normalizeRequired(request.getDescription(), "课程目标纯文本描述不能为空");
        String descriptionRich = trimToNull(request.getDescriptionRich());
        Long courseId = request.getCourseId();
        if (courseId == null) {
            throw new BusinessException(400, "所属课程不能为空");
        }

        CourseObjective entity = courseObjectiveMapper.selectById(coId);
        if (entity == null) {
            throw new BusinessException(404, "课程目标不存在");
        }
        if (courseMapper.selectById(courseId) == null) {
            throw new BusinessException(400, "所选课程不存在");
        }

        // 同一课程下编号唯一（排除自身）
        CourseObjective duplicate = courseObjectiveMapper.selectOne(new LambdaQueryWrapper<CourseObjective>()
                .eq(CourseObjective::getCourseId, courseId)
                .eq(CourseObjective::getObjectiveCode, objectiveCode)
                .ne(CourseObjective::getCoId, coId));
        if (duplicate != null) {
            throw new BusinessException(400, "该课程下已存在相同编号的课程目标");
        }

        entity.setObjectiveCode(objectiveCode);
        // 存储：优先富文本，否则纯文本
        entity.setCoDescription(descriptionRich != null ? descriptionRich : description);
        entity.setCourseId(courseId);
        courseObjectiveMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long coId,
                        List<String> roles,
                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (coId == null) {
            throw new BusinessException(400, "课程目标ID不能为空");
        }
        if (courseObjectiveMapper.selectById(coId) == null) {
            throw new BusinessException(404, "课程目标不存在");
        }

        // 删除前校验：是否被考核点引用
        int apRefCount = courseObjectiveMapper.countAssessmentPointRefs(coId);
        if (apRefCount > 0) {
            throw new BusinessException(400, "该课程目标下存在 " + apRefCount + " 个考核点，请先删除考核点后再删除课程目标");
        }

        // 删除前校验：是否被内部权重引用
        int oicRefCount = courseObjectiveMapper.countObjectiveIndicatorContributionRefs(coId);
        if (oicRefCount > 0) {
            throw new BusinessException(400, "该课程目标存在 " + oicRefCount + " 条内部权重关联，请先解除关联后再删除");
        }

        try {
            courseObjectiveMapper.deleteById(coId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该课程目标存在关联数据，无法删除");
        }
    }

    // ==================== 权限校验 ====================

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行课程目标管理操作");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 将列表查询结果转为列表响应：description 转为纯文本，descriptionRich 置 null。
     */
    private CourseObjectiveResponse toListResponse(CourseObjectiveResponse response) {
        String rawDescription = response.getDescription(); // DB原始值
        return CourseObjectiveResponse.builder()
                .coId(response.getCoId())
                .objectiveCode(response.getObjectiveCode())
                .description(HtmlUtils.stripHtml(rawDescription))
                .descriptionRich(null) // 列表不返回富文本
                .courseId(response.getCourseId())
                .courseCode(response.getCourseCode())
                .courseName(response.getCourseName())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
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
