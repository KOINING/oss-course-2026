package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.supportmatrix.AddCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportListRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportResponse;
import com.oss.osscourse.dto.supportmatrix.DeleteCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.MatrixAcademicTermResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixCourseOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixGraduationRequirementResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixIndicatorPointResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixMajorOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.dto.supportmatrix.ResetSupportMatrixRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixGetRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixMajorFilterRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixRowRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixSaveRequest;
import com.oss.osscourse.dto.supportmatrix.UpdateCourseIndicatorSupportRequest;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.Course;
import com.oss.osscourse.entity.CourseIndicatorSupport;
import com.oss.osscourse.entity.CourseMajor;
import com.oss.osscourse.entity.GraduationRequirement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CourseIndicatorSupportMapper;
import com.oss.osscourse.mapper.CourseMajorMapper;
import com.oss.osscourse.mapper.CourseMapper;
import com.oss.osscourse.mapper.GraduationRequirementMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.SupportMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportMatrixServiceImpl implements SupportMatrixService {
    private static final String MANAGE_ROLE = "program_director";
    private static final String MANAGE_PERMISSION = "requirement:write";
    private static final float WEIGHT_EPSILON = 0.0001f;

    private final CourseIndicatorSupportMapper courseIndicatorSupportMapper;
    private final MajorMapper majorMapper;
    private final CourseMapper courseMapper;
    private final CourseMajorMapper courseMajorMapper;
    private final IndicatorPointMapper indicatorPointMapper;
    private final GraduationRequirementMapper graduationRequirementMapper;
    private final AcademicTermMapper academicTermMapper;

    @Override
    public List<MatrixMajorOptionResponse> listMajors(SupportMatrixMajorFilterRequest request,
                                                       List<String> roles,
                                                       List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = request == null ? null : request.getMajorId();
        return courseIndicatorSupportMapper.selectMajorOptions(majorId);
    }

    @Override
    public List<Integer> listGradeYears(SupportMatrixMajorFilterRequest request,
                                        List<String> roles,
                                        List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = request == null ? null : request.getMajorId();
        return new ArrayList<>(courseIndicatorSupportMapper.selectGradeYears(majorId));
    }

    @Override
    public List<MatrixCourseOptionResponse> listCourses(SupportMatrixMajorFilterRequest request,
                                                        List<String> roles,
                                                        List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = extractMajorId(request);
        Integer gradeYear = extractGradeYear(request);
        assertMajorExists(majorId);
        return courseIndicatorSupportMapper.selectCourseOptionsByMajor(majorId, gradeYear);
    }

    @Override
    public List<MatrixAcademicTermResponse> listAcademicTerms(List<String> roles, List<String> permissions) {
        assertManagePermission(roles, permissions);
        List<AcademicTerm> terms = academicTermMapper.selectList(
                new LambdaQueryWrapper<AcademicTerm>()
                        .orderByDesc(AcademicTerm::getAcademicYear)
                        .orderByDesc(AcademicTerm::getSemester));

        List<MatrixAcademicTermResponse> result = new ArrayList<>(terms.size());
        for (AcademicTerm term : terms) {
            MatrixAcademicTermResponse item = new MatrixAcademicTermResponse();
            item.setTermId(term.getTermId());
            item.setTermCode(term.getTermCode());
            item.setAcademicYear(term.getAcademicYear());
            item.setSemester(term.getSemester());
            item.setTermName(buildTermName(term));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<MatrixGraduationRequirementResponse> listGraduationRequirements(SupportMatrixMajorFilterRequest request,
                                                                                List<String> roles,
                                                                                List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = extractMajorId(request);
        Integer gradeYear = extractGradeYear(request);
        assertMajorExists(majorId);
        return courseIndicatorSupportMapper.selectGraduationRequirementsByMajor(majorId, gradeYear);
    }

    @Override
    public List<MatrixIndicatorPointResponse> listIndicatorPoints(SupportMatrixMajorFilterRequest request,
                                                                  List<String> roles,
                                                                  List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = extractMajorId(request);
        Integer gradeYear = extractGradeYear(request);
        assertMajorExists(majorId);
        return courseIndicatorSupportMapper.selectIndicatorPointsByMajor(majorId, gradeYear);
    }

    @Override
    public List<MatrixRelationResponse> getSupportMatrix(SupportMatrixGetRequest request,
                                                         List<String> roles,
                                                         List<String> permissions) {
        assertManagePermission(roles, permissions);
        assertMajorExists(request.getMajorId());
        validateGradeYear(request.getGradeYear());
        return courseIndicatorSupportMapper.selectMatrixRelationsByMajor(request.getMajorId(), request.getGradeYear());
    }

    @Override
    public List<CourseIndicatorSupportResponse> listCourseIndicatorSupports(CourseIndicatorSupportListRequest request,
                                                                            List<String> roles,
                                                                            List<String> permissions) {
        assertManagePermission(roles, permissions);
        CourseIndicatorSupportListRequest query = request == null ? new CourseIndicatorSupportListRequest() : request;
        if (query.getMajorId() != null) {
            assertMajorExists(query.getMajorId());
        }
        return courseIndicatorSupportMapper.selectCourseIndicatorSupports(
                query.getMajorId(), query.getGradeYear(), query.getCourseId(), query.getIpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCourseIndicatorSupport(AddCourseIndicatorSupportRequest request,
                                          List<String> roles,
                                          List<String> permissions) {
        assertManagePermission(roles, permissions);
        validateCourseAndIndicatorRelation(request.getCourseId(), request.getIpId(), null, null);
        float weight = normalizeWeight(request.getTotalWeight());

        CourseIndicatorSupport duplicate = courseIndicatorSupportMapper.selectOne(
                new LambdaQueryWrapper<CourseIndicatorSupport>()
                        .eq(CourseIndicatorSupport::getCourseId, request.getCourseId())
                        .eq(CourseIndicatorSupport::getIpId, request.getIpId()));
        if (duplicate != null) {
            throw new BusinessException(400, "同一 courseId + ipId 组合不能重复");
        }

        CourseIndicatorSupport entity = new CourseIndicatorSupport();
        entity.setCourseId(request.getCourseId());
        entity.setIpId(request.getIpId());
        entity.setTotalWeight(weight);
        courseIndicatorSupportMapper.insert(entity);
        validateSingleIndicatorWeightSum(request.getIpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseIndicatorSupport(UpdateCourseIndicatorSupportRequest request,
                                             List<String> roles,
                                             List<String> permissions) {
        assertManagePermission(roles, permissions);
        CourseIndicatorSupport exists = courseIndicatorSupportMapper.selectById(request.getCisId());
        if (exists == null) {
            throw new BusinessException(404, "支撑关系不存在");
        }

        validateCourseAndIndicatorRelation(request.getCourseId(), request.getIpId(), null, null);
        float weight = normalizeWeight(request.getTotalWeight());
        CourseIndicatorSupport duplicate = courseIndicatorSupportMapper.selectOne(
                new LambdaQueryWrapper<CourseIndicatorSupport>()
                        .eq(CourseIndicatorSupport::getCourseId, request.getCourseId())
                        .eq(CourseIndicatorSupport::getIpId, request.getIpId())
                        .ne(CourseIndicatorSupport::getCisId, request.getCisId()));
        if (duplicate != null) {
            throw new BusinessException(400, "同一 courseId + ipId 组合不能重复");
        }

        exists.setCourseId(request.getCourseId());
        exists.setIpId(request.getIpId());
        exists.setTotalWeight(weight);
        courseIndicatorSupportMapper.updateById(exists);
        validateSingleIndicatorWeightSum(request.getIpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseIndicatorSupport(DeleteCourseIndicatorSupportRequest request,
                                             List<String> roles,
                                             List<String> permissions) {
        assertManagePermission(roles, permissions);
        CourseIndicatorSupport exists = courseIndicatorSupportMapper.selectById(request.getCisId());
        if (exists == null) {
            throw new BusinessException(404, "支撑关系不存在");
        }
        Long ipId = exists.getIpId();
        courseIndicatorSupportMapper.deleteById(request.getCisId());
        validateSingleIndicatorWeightSum(ipId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSupportMatrix(SupportMatrixSaveRequest request, List<String> roles, List<String> permissions) {
        assertManagePermission(roles, permissions);
        Long majorId = request.getMajorId();
        Integer gradeYear = request.getGradeYear();
        validateGradeYear(gradeYear);
        assertMajorExists(majorId);

        List<MatrixCourseOptionResponse> courses = courseIndicatorSupportMapper.selectCourseOptionsByMajor(majorId, gradeYear);
        List<MatrixIndicatorPointResponse> indicatorPoints = courseIndicatorSupportMapper.selectIndicatorPointsByMajor(majorId, gradeYear);
        if (indicatorPoints.isEmpty()) {
            throw new BusinessException(400, "当前专业当前年级下没有可用的指标点，无法保存支撑矩阵");
        }

        Set<Long> validCourseIds = courses.stream().map(MatrixCourseOptionResponse::getCourseId).collect(Collectors.toSet());
        Set<Long> validIpIds = indicatorPoints.stream().map(MatrixIndicatorPointResponse::getIpId).collect(Collectors.toSet());
        Map<Long, String> ipCodeMap = indicatorPoints.stream()
                .collect(Collectors.toMap(MatrixIndicatorPointResponse::getIpId, MatrixIndicatorPointResponse::getIpCode));

        Map<Long, Float> ipWeightSumMap = new HashMap<>();
        Set<String> pairSet = new HashSet<>();
        for (SupportMatrixRowRequest row : request.getRows()) {
            if (row.getCourseId() == null) {
                throw new BusinessException(400, "courseId不能为空");
            }
            if (row.getIpId() == null) {
                throw new BusinessException(400, "ipId不能为空");
            }
            Float rawWeight = row.getTotalWeight() != null ? row.getTotalWeight() : row.getWeight();
            float weight = normalizeWeight(rawWeight);
            row.setTotalWeight(weight);
            row.setWeight(weight);

            if (!validCourseIds.contains(row.getCourseId())) {
                throw new BusinessException(400, "课程ID " + row.getCourseId() + " 不属于当前专业或课程已停用");
            }
            if (!validIpIds.contains(row.getIpId())) {
                throw new BusinessException(400, "指标点ID " + row.getIpId() + " 不属于当前专业或指标点已停用");
            }
            String key = row.getCourseId() + "_" + row.getIpId();
            if (!pairSet.add(key)) {
                throw new BusinessException(400, "同一 courseId + ipId 组合不能重复");
            }
            ipWeightSumMap.merge(row.getIpId(), weight, Float::sum);
        }

        List<String> invalidMessages = new ArrayList<>();
        for (Long ipId : validIpIds) {
            float sum = ipWeightSumMap.getOrDefault(ipId, 0f);
            if (Math.abs(sum - 1.0f) > WEIGHT_EPSILON) {
                String ipCode = ipCodeMap.getOrDefault(ipId, String.valueOf(ipId));
                invalidMessages.add("指标点 " + ipCode + " 校验失败：当前权重和=" + formatWeight(sum) + "，要求为1.00");
            }
        }
        if (!invalidMessages.isEmpty()) {
            throw new BusinessException(400, String.join("；", invalidMessages));
        }

        courseIndicatorSupportMapper.deleteByMajorId(majorId, gradeYear);
        for (SupportMatrixRowRequest row : request.getRows()) {
            CourseIndicatorSupport entity = new CourseIndicatorSupport();
            entity.setCourseId(row.getCourseId());
            entity.setIpId(row.getIpId());
            entity.setTotalWeight(row.getTotalWeight());
            courseIndicatorSupportMapper.insert(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupportMatrix(ResetSupportMatrixRequest request, List<String> roles, List<String> permissions) {
        assertManagePermission(roles, permissions);
        validateGradeYear(request.getGradeYear());
        assertMajorExists(request.getMajorId());
        courseIndicatorSupportMapper.deleteByMajorId(request.getMajorId(), request.getGradeYear());
    }

    private void validateCourseAndIndicatorRelation(Long courseId, Long ipId, Long expectedMajorId, Integer expectedGradeYear) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (course.getStatus() != null && course.getStatus() == 0) {
            throw new BusinessException(400, "课程已停用，不能配置支撑关系");
        }

        IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(ipId);
        if (indicatorPoint == null) {
            throw new BusinessException(404, "指标点不存在");
        }
        if (indicatorPoint.getStatus() != null && indicatorPoint.getStatus() == 0) {
            throw new BusinessException(400, "指标点已停用，不能配置支撑关系");
        }

        GraduationRequirement requirement = graduationRequirementMapper.selectById(indicatorPoint.getGrId());
        if (requirement == null) {
            throw new BusinessException(400, "指标点对应的毕业要求不存在");
        }
        if (requirement.getStatus() != null && requirement.getStatus() == 0) {
            throw new BusinessException(400, "指标点所属毕业要求已停用，不能配置支撑关系");
        }

        if (expectedMajorId != null && !expectedMajorId.equals(requirement.getMajorId())) {
            throw new BusinessException(400, "指标点不属于当前所选专业");
        }
        if (expectedGradeYear != null && !expectedGradeYear.equals(requirement.getGradeYear())) {
            throw new BusinessException(400, "指标点不属于当前所选年级");
        }

        Long relationCount = courseMajorMapper.selectCount(
                new LambdaQueryWrapper<CourseMajor>()
                        .eq(CourseMajor::getCourseId, courseId)
                        .eq(CourseMajor::getMajorId, requirement.getMajorId())
                        .eq(expectedGradeYear != null, CourseMajor::getGradeYear, expectedGradeYear != null ? expectedGradeYear : requirement.getGradeYear()));
        if (relationCount == null || relationCount == 0) {
            throw new BusinessException(400, "课程与指标点不属于同一专业同一年级，无法建立支撑关系");
        }
    }

    private void validateSingleIndicatorWeightSum(Long ipId) {
        IndicatorPoint indicatorPoint = indicatorPointMapper.selectById(ipId);
        if (indicatorPoint == null) {
            throw new BusinessException(400, "指标点不存在");
        }
        float sum = 0f;
        List<CourseIndicatorSupport> rows = courseIndicatorSupportMapper.selectList(
                new LambdaQueryWrapper<CourseIndicatorSupport>()
                        .eq(CourseIndicatorSupport::getIpId, ipId));
        for (CourseIndicatorSupport row : rows) {
            if (row.getTotalWeight() != null) {
                sum += row.getTotalWeight();
            }
        }
        if (Math.abs(sum - 1.0f) > WEIGHT_EPSILON) {
            throw new BusinessException(400, "指标点 " + indicatorPoint.getIpCode()
                    + " 校验失败：当前权重和=" + formatWeight(sum) + "，要求为1.00");
        }
    }

    private Long extractMajorId(SupportMatrixMajorFilterRequest request) {
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        return request.getMajorId();
    }

    private Integer extractGradeYear(SupportMatrixMajorFilterRequest request) {
        if (request == null || request.getGradeYear() == null) {
            throw new BusinessException(400, "年级不能为空");
        }
        validateGradeYear(request.getGradeYear());
        return request.getGradeYear();
    }

    private void assertMajorExists(Long majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        if (major.getStatus() != null && major.getStatus() == 0) {
            throw new BusinessException(400, "专业已停用，不能配置支撑矩阵");
        }
    }

    private float normalizeWeight(Float weight) {
        if (weight == null || !Float.isFinite(weight)) {
            throw new BusinessException(400, "totalWeight 必须为合法数字");
        }
        if (weight < 0f || weight > 1f) {
            throw new BusinessException(400, "totalWeight 必须在 0 到 1 之间");
        }
        return Math.round(weight * 10000f) / 10000f;
    }

    private void validateGradeYear(Integer gradeYear) {
        if (gradeYear == null) {
            throw new BusinessException(400, "年级不能为空");
        }
        if (gradeYear < 2000 || gradeYear > 2100) {
            throw new BusinessException(400, "年级必须在2000到2100之间");
        }
    }

    private String buildTermName(AcademicTerm term) {
        if (term.getAcademicYear() != null && term.getSemester() != null) {
            return term.getAcademicYear() + "-" + (term.getAcademicYear() + 1) + " 学年 第" + term.getSemester() + "学期";
        }
        return term.getTermCode() != null ? term.getTermCode() : "学期" + term.getTermId();
    }

    private String formatWeight(float value) {
        return String.format("%.2f", value);
    }

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行支撑矩阵管理操作");
        }
    }
}
