package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.college.*;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeMapper collegeMapper;

    @Override
    public List<CollegeResponse> listColleges(CollegeQueryRequest request) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (request.getCollegeCode() != null && !request.getCollegeCode().trim().isEmpty()) {
                wrapper.like(College::getCollegeCode, request.getCollegeCode().trim());
            }
            if (request.getCollegeName() != null && !request.getCollegeName().trim().isEmpty()) {
                wrapper.like(College::getCollegeName, request.getCollegeName().trim());
            }
        }

        wrapper.orderByAsc(College::getCollegeCode);

        List<College> colleges = collegeMapper.selectList(wrapper);
        return colleges.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CollegeResponse getCollegeById(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException(400, "学院ID不能为空");
        }

        College college = collegeMapper.selectById(collegeId);
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }

        return toResponse(college);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCollege(CollegeCreateRequest request) {
        // 检查学院编码是否已存在
        if (collegeMapper.selectOne(new LambdaQueryWrapper<College>()
                .eq(College::getCollegeCode, request.getCollegeCode())) != null) {
            throw new BusinessException(400, "学院编码已存在");
        }

        College college = new College();
        college.setCollegeCode(request.getCollegeCode());
        college.setCollegeName(request.getCollegeName());

        collegeMapper.insert(college);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCollege(CollegeUpdateRequest request) {
        if (request.getCollegeId() == null) {
            throw new BusinessException(400, "学院ID不能为空");
        }

        College college = collegeMapper.selectById(request.getCollegeId());
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }

        // 检查学院编码是否已存在（排除自身）
        if (request.getCollegeCode() != null && !request.getCollegeCode().isEmpty()) {
            College existing = collegeMapper.selectOne(new LambdaQueryWrapper<College>()
                    .eq(College::getCollegeCode, request.getCollegeCode())
                    .ne(College::getCollegeId, request.getCollegeId()));
            if (existing != null) {
                throw new BusinessException(400, "学院编码已存在");
            }
            college.setCollegeCode(request.getCollegeCode());
        }

        if (request.getCollegeName() != null && !request.getCollegeName().isEmpty()) {
            college.setCollegeName(request.getCollegeName());
        }

        collegeMapper.updateById(college);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCollege(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException(400, "学院ID不能为空");
        }

        College college = collegeMapper.selectById(collegeId);
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }

        // 检查是否有关联的专业
        // TODO: 检查Major表是否有引用

        collegeMapper.deleteById(collegeId);
    }

    private CollegeResponse toResponse(College college) {
        return CollegeResponse.builder()
                .collegeId(college.getCollegeId())
                .collegeCode(college.getCollegeCode())
                .collegeName(college.getCollegeName())
                .createdAt(college.getCreatedAt())
                .updatedAt(college.getUpdatedAt())
                .build();
    }
}
