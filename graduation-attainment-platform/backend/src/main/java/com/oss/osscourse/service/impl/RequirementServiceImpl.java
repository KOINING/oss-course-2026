package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.requirement.*;
import com.oss.osscourse.entity.GraduationRequirement;
import com.oss.osscourse.entity.IndicatorPoint;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.GraduationRequirementMapper;
import com.oss.osscourse.mapper.IndicatorPointMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.RequirementService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RequirementServiceImpl implements RequirementService {

    private static final String MANAGE_ROLE = "program_director";
    private static final String MANAGE_PERMISSION = "requirement:write";

    private final GraduationRequirementMapper grMapper;
    private final IndicatorPointMapper ipMapper;
    private final MajorMapper majorMapper;

    public RequirementServiceImpl(GraduationRequirementMapper grMapper,
                                  IndicatorPointMapper ipMapper,
                                  MajorMapper majorMapper) {
        this.grMapper = grMapper;
        this.ipMapper = ipMapper;
        this.majorMapper = majorMapper;
    }

    @Override
    public List<GraduationRequirementResponse> listGraduationRequirements(
            GraduationRequirementQueryRequest request,
            List<String> roles,
            List<String> permissions) {
        assertManagePermission(roles, permissions);
        GraduationRequirementQueryRequest query = request == null ? new GraduationRequirementQueryRequest() : request;
        return grMapper.selectRequirementList(trimToNull(query.getGrCode()), query.getMajorId(), query.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGraduationRequirement(AddGraduationRequirementRequest request,
                                        List<String> roles,
                                        List<String> permissions) {
        assertManagePermission(roles, permissions);

        String grCode = normalizeRequired(request.getGrCode(), "毕业要求编号不能为空");
        String grDescription = normalizeRequired(request.getGrDescription(), "毕业要求描述不能为空");
        Long majorId = request.getMajorId();
        if (majorId == null) {
            throw new BusinessException(400, "所属专业不能为空");
        }
        if (majorMapper.selectById(majorId) == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        if (grMapper.selectOne(new LambdaQueryWrapper<GraduationRequirement>()
                .eq(GraduationRequirement::getMajorId, majorId)
                .eq(GraduationRequirement::getGrCode, grCode)) != null) {
            throw new BusinessException(400, "该专业下已存在相同编号的毕业要求");
        }

        GraduationRequirement entity = new GraduationRequirement();
        entity.setGrCode(grCode);
        entity.setGrDescription(grDescription);
        entity.setMajorId(majorId);
        entity.setStatus(request.getStatus() == null ? 1 : normalizeStatus(request.getStatus()));
        grMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGraduationRequirement(UpdateGraduationRequirementRequest request,
                                           List<String> roles,
                                           List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long grId = request.getGrId();
        if (grId == null) {
            throw new BusinessException(400, "毕业要求ID不能为空");
        }
        String grCode = normalizeRequired(request.getGrCode(), "毕业要求编号不能为空");
        String grDescription = normalizeRequired(request.getGrDescription(), "毕业要求描述不能为空");
        Long majorId = request.getMajorId();
        if (majorId == null) {
            throw new BusinessException(400, "所属专业不能为空");
        }

        GraduationRequirement entity = grMapper.selectById(grId);
        if (entity == null) {
            throw new BusinessException(404, "毕业要求不存在");
        }
        if (majorMapper.selectById(majorId) == null) {
            throw new BusinessException(400, "所选专业不存在");
        }

        GraduationRequirement duplicate = grMapper.selectOne(new LambdaQueryWrapper<GraduationRequirement>()
                .eq(GraduationRequirement::getMajorId, majorId)
                .eq(GraduationRequirement::getGrCode, grCode)
                .ne(GraduationRequirement::getGrId, grId));
        if (duplicate != null) {
            throw new BusinessException(400, "该专业下已存在相同编号的毕业要求");
        }

        entity.setGrCode(grCode);
        entity.setGrDescription(grDescription);
        entity.setMajorId(majorId);
        if (request.getStatus() != null) {
            entity.setStatus(normalizeStatus(request.getStatus()));
        }
        grMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGraduationRequirement(Long grId,
                                           List<String> roles,
                                           List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (grId == null) {
            throw new BusinessException(400, "毕业要求ID不能为空");
        }
        if (grMapper.selectById(grId) == null) {
            throw new BusinessException(404, "毕业要求不存在");
        }

        int ipCount = ipMapper.countByGrId(grId);
        if (ipCount > 0) {
            throw new BusinessException(400, "该毕业要求下存在 " + ipCount + " 个指标点，请先删除指标点后再删除毕业要求");
        }

        try {
            grMapper.deleteById(grId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该毕业要求存在关联数据，无法删除，请改为停用");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGraduationRequirementStatus(UpdateGraduationRequirementStatusRequest request,
                                                  List<String> roles,
                                                  List<String> permissions) {
        assertManagePermission(roles, permissions);

        GraduationRequirement entity = grMapper.selectById(request.getGrId());
        if (entity == null) {
            throw new BusinessException(404, "毕业要求不存在");
        }
        entity.setStatus(normalizeStatus(request.getStatus()));
        grMapper.updateById(entity);
    }

    @Override
    public List<IndicatorPointResponse> listIndicatorPoints(IndicatorPointQueryRequest request,
                                                             List<String> roles,
                                                             List<String> permissions) {
        assertManagePermission(roles, permissions);
        IndicatorPointQueryRequest query = request == null ? new IndicatorPointQueryRequest() : request;
        return ipMapper.selectIndicatorPointList(trimToNull(query.getIpCode()), query.getGrId(), query.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIndicatorPoint(AddIndicatorPointRequest request,
                                 List<String> roles,
                                 List<String> permissions) {
        assertManagePermission(roles, permissions);

        String ipCode = normalizeRequired(request.getIpCode(), "指标点编号不能为空");
        String ipDescription = normalizeRequired(request.getIpDescription(), "指标点描述不能为空");
        Long grId = request.getGrId();
        if (grId == null) {
            throw new BusinessException(400, "所属毕业要求不能为空");
        }
        if (grMapper.selectById(grId) == null) {
            throw new BusinessException(400, "所选毕业要求不存在");
        }

        if (ipMapper.selectOne(new LambdaQueryWrapper<IndicatorPoint>()
                .eq(IndicatorPoint::getGrId, grId)
                .eq(IndicatorPoint::getIpCode, ipCode)) != null) {
            throw new BusinessException(400, "该毕业要求下已存在相同编号的指标点");
        }

        IndicatorPoint entity = new IndicatorPoint();
        entity.setIpCode(ipCode);
        entity.setIpDescription(ipDescription);
        entity.setGrId(grId);
        entity.setStatus(request.getStatus() == null ? 1 : normalizeStatus(request.getStatus()));
        ipMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIndicatorPoint(UpdateIndicatorPointRequest request,
                                    List<String> roles,
                                    List<String> permissions) {
        assertManagePermission(roles, permissions);

        Long ipId = request.getIpId();
        if (ipId == null) {
            throw new BusinessException(400, "指标点ID不能为空");
        }
        String ipCode = normalizeRequired(request.getIpCode(), "指标点编号不能为空");
        String ipDescription = normalizeRequired(request.getIpDescription(), "指标点描述不能为空");
        Long grId = request.getGrId();
        if (grId == null) {
            throw new BusinessException(400, "所属毕业要求不能为空");
        }

        IndicatorPoint entity = ipMapper.selectById(ipId);
        if (entity == null) {
            throw new BusinessException(404, "指标点不存在");
        }
        if (grMapper.selectById(grId) == null) {
            throw new BusinessException(400, "所选毕业要求不存在");
        }

        IndicatorPoint duplicate = ipMapper.selectOne(new LambdaQueryWrapper<IndicatorPoint>()
                .eq(IndicatorPoint::getGrId, grId)
                .eq(IndicatorPoint::getIpCode, ipCode)
                .ne(IndicatorPoint::getIpId, ipId));
        if (duplicate != null) {
            throw new BusinessException(400, "该毕业要求下已存在相同编号的指标点");
        }

        entity.setIpCode(ipCode);
        entity.setIpDescription(ipDescription);
        entity.setGrId(grId);
        if (request.getStatus() != null) {
            entity.setStatus(normalizeStatus(request.getStatus()));
        }
        ipMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndicatorPoint(Long ipId,
                                    List<String> roles,
                                    List<String> permissions) {
        assertManagePermission(roles, permissions);

        if (ipId == null) {
            throw new BusinessException(400, "指标点ID不能为空");
        }
        if (ipMapper.selectById(ipId) == null) {
            throw new BusinessException(404, "指标点不存在");
        }

        try {
            ipMapper.deleteById(ipId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该指标点存在关联数据，无法删除，请改为停用");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIndicatorPointStatus(UpdateIndicatorPointStatusRequest request,
                                           List<String> roles,
                                           List<String> permissions) {
        assertManagePermission(roles, permissions);

        IndicatorPoint entity = ipMapper.selectById(request.getIpId());
        if (entity == null) {
            throw new BusinessException(404, "指标点不存在");
        }
        entity.setStatus(normalizeStatus(request.getStatus()));
        ipMapper.updateById(entity);
    }

    @Override
    public List<Major> listMajors(List<String> roles, List<String> permissions) {
        assertManagePermission(roles, permissions);
        return majorMapper.selectList(new LambdaQueryWrapper<Major>().orderByAsc(Major::getMajorId));
    }

    private void assertManagePermission(List<String> roles, List<String> permissions) {
        boolean hasRole = roles != null && roles.contains(MANAGE_ROLE);
        boolean hasPermission = permissions != null && permissions.contains(MANAGE_PERMISSION);
        if (!hasRole && !hasPermission) {
            throw new BusinessException(403, "无权执行毕业要求与指标点管理操作");
        }
    }

    private String normalizeRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(400, message);
        }
        return trimmed;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为0或1");
        }
        return status;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
