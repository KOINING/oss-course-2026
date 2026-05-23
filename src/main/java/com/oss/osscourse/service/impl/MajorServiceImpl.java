package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.major.*;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.MajorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MajorServiceImpl implements MajorService {

    private final MajorMapper majorMapper;
    private final CollegeMapper collegeMapper;

    public MajorServiceImpl(MajorMapper majorMapper, CollegeMapper collegeMapper) {
        this.majorMapper = majorMapper;
        this.collegeMapper = collegeMapper;
    }

    @Override
    public List<MajorVO> listMajors(MajorQueryRequest request) {
        MajorQueryRequest query = request == null ? new MajorQueryRequest() : request;
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .like(query.getMajorCode() != null, Major::getMajorCode, trimToNull(query.getMajorCode()))
                .like(query.getMajorName() != null, Major::getMajorName, trimToNull(query.getMajorName()))
                .eq(query.getCollegeId() != null, Major::getCollegeId, query.getCollegeId())
                .orderByAsc(Major::getMajorId);

        List<Major> majors = majorMapper.selectList(wrapper);
        if (majors.isEmpty()) {
            return List.of();
        }

        Map<Long, String> collegeMap = buildCollegeNameMap(majors);
        return majors.stream()
                .map(m -> MajorVO.builder()
                        .majorId(m.getMajorId())
                        .majorCode(m.getMajorCode())
                        .majorName(m.getMajorName())
                        .collegeId(m.getCollegeId())
                        .collegeName(collegeMap.getOrDefault(m.getCollegeId(), ""))
                        .status(m.getStatus())
                        .createdAt(m.getCreatedAt())
                        .updatedAt(m.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public MajorVO getMajor(Long majorId) {
        Major major = requireMajor(majorId);
        College college = collegeMapper.selectById(major.getCollegeId());
        return MajorVO.builder()
                .majorId(major.getMajorId())
                .majorCode(major.getMajorCode())
                .majorName(major.getMajorName())
                .collegeId(major.getCollegeId())
                .collegeName(college != null ? college.getCollegeName() : "")
                .status(major.getStatus())
                .createdAt(major.getCreatedAt())
                .updatedAt(major.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMajor(MajorSaveRequest request) {
        validateStatus(request.getStatus());

        String code = normalizeRequired(request.getMajorCode(), "专业编码不能为空");
        String name = normalizeRequired(request.getMajorName(), "专业名称不能为空");
        requireCollegeExists(request.getCollegeId());

        if (request.getMajorId() == null) {
            if (majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                    .eq(Major::getMajorCode, code)) != null) {
                throw new BusinessException(400, "专业编码已存在");
            }
            Major major = new Major();
            major.setMajorCode(code);
            major.setMajorName(name);
            major.setCollegeId(request.getCollegeId());
            major.setStatus(request.getStatus());
            majorMapper.insert(major);
        } else {
            Major major = requireMajor(request.getMajorId());
            Major existing = majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                    .eq(Major::getMajorCode, code)
                    .ne(Major::getMajorId, request.getMajorId()));
            if (existing != null) {
                throw new BusinessException(400, "专业编码已存在");
            }
            major.setMajorCode(code);
            major.setMajorName(name);
            major.setCollegeId(request.getCollegeId());
            major.setStatus(request.getStatus());
            majorMapper.updateById(major);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMajorStatus(MajorStatusRequest request) {
        validateStatus(request.getStatus());
        Major major = requireMajor(request.getMajorId());
        major.setStatus(request.getStatus());
        majorMapper.updateById(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMajor(Long majorId) {
        requireMajor(majorId);
        try {
            majorMapper.deleteById(majorId);
        } catch (Exception e) {
            throw new BusinessException(400, "该专业下存在关联数据（课程、毕业要求等），无法删除。请先停用该专业");
        }
    }

    private Major requireMajor(Long majorId) {
        if (majorId == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        return major;
    }

    private void requireCollegeExists(Long collegeId) {
        if (collegeId == null || collegeMapper.selectById(collegeId) == null) {
            throw new BusinessException(400, "所选学院不存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为0或1");
        }
    }

    private Map<Long, String> buildCollegeNameMap(List<Major> majors) {
        List<Long> collegeIds = majors.stream()
                .map(Major::getCollegeId)
                .distinct()
                .toList();
        if (collegeIds.isEmpty()) {
            return Map.of();
        }
        return collegeMapper.selectBatchIds(collegeIds).stream()
                .collect(Collectors.toMap(College::getCollegeId, College::getCollegeName));
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
