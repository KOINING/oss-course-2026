package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.PageQueryUtils;
import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.college.CollegeCreateRequest;
import com.oss.osscourse.dto.college.CollegeQueryRequest;
import com.oss.osscourse.dto.college.CollegeResponse;
import com.oss.osscourse.dto.college.CollegeUpdateRequest;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    @Override
    public List<CollegeResponse> listColleges(CollegeQueryRequest request) {
        LambdaQueryWrapper<College> wrapper = buildQueryWrapper(request);
        List<College> colleges = collegeMapper.selectList(wrapper);
        return colleges.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CollegeResponse> listCollegesByPage(CollegeQueryRequest request) {
        int pageNum = PageQueryUtils.normalizePageNum(request != null ? request.getPageNum() : null);
        int pageSize = PageQueryUtils.normalizePageSize(request != null ? request.getPageSize() : null);

        Page<College> page = collegeMapper.selectPage(new Page<>(pageNum, pageSize), buildQueryWrapper(request));
        List<CollegeResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
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

        Long majorRefCount = majorMapper.selectCount(
                new LambdaQueryWrapper<Major>().eq(Major::getCollegeId, collegeId));
        if (majorRefCount != null && majorRefCount > 0) {
            throw new BusinessException(400, "该学院下存在专业数据，无法删除");
        }

        try {
            collegeMapper.deleteById(collegeId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该学院存在关联数据，无法删除");
        }
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

    private LambdaQueryWrapper<College> buildQueryWrapper(CollegeQueryRequest request) {
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
        return wrapper;
    }
}
