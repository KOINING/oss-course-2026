package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.PageQueryUtils;
import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.major.*;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.entity.Major;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.mapper.MajorMapper;
import com.oss.osscourse.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private final MajorMapper majorMapper;
    private final CollegeMapper collegeMapper;

    @Override
    public List<MajorResponse> listMajors(MajorQueryRequest request) {
        LambdaQueryWrapper<Major> wrapper = buildQueryWrapper(request);
        List<Major> majors = majorMapper.selectList(wrapper);
        Map<Long, String> collegeNameMap = buildCollegeNameMap(majors);

        return majors.stream()
                .map(m -> toResponse(m, collegeNameMap.get(m.getCollegeId())))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<MajorResponse> listMajorsByPage(MajorQueryRequest request) {
        int pageNum = PageQueryUtils.normalizePageNum(request != null ? request.getPageNum() : null);
        int pageSize = PageQueryUtils.normalizePageSize(request != null ? request.getPageSize() : null);

        Page<Major> page = majorMapper.selectPage(new Page<>(pageNum, pageSize), buildQueryWrapper(request));
        List<Major> majors = page.getRecords();
        Map<Long, String> collegeNameMap = buildCollegeNameMap(majors);
        List<MajorResponse> records = majors.stream()
                .map(m -> toResponse(m, collegeNameMap.get(m.getCollegeId())))
                .collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<MajorResponse> listMajorsForSelect() {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getStatus, 1).orderByAsc(Major::getMajorCode);
        List<Major> majors = majorMapper.selectList(wrapper);
        Map<Long, String> collegeNameMap = buildCollegeNameMap(majors);

        return majors.stream()
                .map(m -> toResponse(m, collegeNameMap.get(m.getCollegeId())))
                .collect(Collectors.toList());
    }

    @Override
    public MajorResponse getMajorById(Long majorId) {
        if (majorId == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        College college = collegeMapper.selectById(major.getCollegeId());
        String collegeName = college != null ? college.getCollegeName() : null;
        return toResponse(major, collegeName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMajor(MajorCreateRequest request) {
        if (majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                .eq(Major::getMajorCode, request.getMajorCode())) != null) {
            throw new BusinessException(400, "专业编码已存在");
        }
        if (collegeMapper.selectById(request.getCollegeId()) == null) {
            throw new BusinessException(400, "所选学院不存在");
        }

        Major major = new Major();
        major.setMajorCode(request.getMajorCode());
        major.setMajorName(request.getMajorName());
        major.setCollegeId(request.getCollegeId());
        major.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        majorMapper.insert(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMajor(MajorSaveRequest request) {
        if (request.getMajorId() == null) {
            MajorCreateRequest createRequest = new MajorCreateRequest();
            createRequest.setMajorCode(request.getMajorCode());
            createRequest.setMajorName(request.getMajorName());
            createRequest.setCollegeId(request.getCollegeId());
            createRequest.setStatus(request.getStatus());
            createMajor(createRequest);
        } else {
            MajorUpdateRequest updateRequest = new MajorUpdateRequest();
            updateRequest.setMajorId(request.getMajorId());
            updateRequest.setMajorCode(request.getMajorCode());
            updateRequest.setMajorName(request.getMajorName());
            updateRequest.setCollegeId(request.getCollegeId());
            updateRequest.setStatus(request.getStatus());
            updateMajor(updateRequest);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMajor(MajorUpdateRequest request) {
        if (request.getMajorId() == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }

        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        if (request.getMajorCode() != null && !request.getMajorCode().isEmpty()) {
            Major existing = majorMapper.selectOne(new LambdaQueryWrapper<Major>()
                    .eq(Major::getMajorCode, request.getMajorCode())
                    .ne(Major::getMajorId, request.getMajorId()));
            if (existing != null) {
                throw new BusinessException(400, "专业编码已存在");
            }
            major.setMajorCode(request.getMajorCode());
        }

        if (request.getMajorName() != null && !request.getMajorName().isEmpty()) {
            major.setMajorName(request.getMajorName());
        }

        if (request.getCollegeId() != null) {
            if (collegeMapper.selectById(request.getCollegeId()) == null) {
                throw new BusinessException(400, "所选学院不存在");
            }
            major.setCollegeId(request.getCollegeId());
        }

        if (request.getStatus() != null) {
            major.setStatus(request.getStatus());
        }

        majorMapper.updateById(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMajorStatus(MajorStatusRequest request) {
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(400, "状态值必须为0或1");
        }
        Major major = majorMapper.selectById(request.getMajorId());
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        major.setStatus(request.getStatus());
        majorMapper.updateById(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMajor(Long majorId) {
        if (majorId == null) {
            throw new BusinessException(400, "专业ID不能为空");
        }
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }
        try {
            majorMapper.deleteById(majorId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "该专业下存在关联数据（课程、毕业要求等），无法删除。请先停用该专业");
        }
    }

    private Map<Long, String> buildCollegeNameMap(List<Major> majors) {
        List<Long> collegeIds = majors.stream()
                .map(Major::getCollegeId)
                .distinct()
                .collect(Collectors.toList());
        if (collegeIds.isEmpty()) {
            return Map.of();
        }
        return collegeMapper.selectBatchIds(collegeIds).stream()
                .collect(Collectors.toMap(College::getCollegeId, College::getCollegeName));
    }

    private LambdaQueryWrapper<Major> buildQueryWrapper(MajorQueryRequest request) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (request.getMajorCode() != null && !request.getMajorCode().trim().isEmpty()) {
                wrapper.like(Major::getMajorCode, request.getMajorCode().trim());
            }
            if (request.getMajorName() != null && !request.getMajorName().trim().isEmpty()) {
                wrapper.like(Major::getMajorName, request.getMajorName().trim());
            }
            if (request.getCollegeId() != null) {
                wrapper.eq(Major::getCollegeId, request.getCollegeId());
            }
            if (request.getStatus() != null) {
                wrapper.eq(Major::getStatus, request.getStatus());
            }
        }

        wrapper.orderByAsc(Major::getMajorCode);
        return wrapper;
    }

    private MajorResponse toResponse(Major major, String collegeName) {
        return MajorResponse.builder()
                .majorId(major.getMajorId())
                .majorCode(major.getMajorCode())
                .majorName(major.getMajorName())
                .collegeId(major.getCollegeId())
                .collegeName(collegeName)
                .status(major.getStatus())
                .createdAt(major.getCreatedAt())
                .updatedAt(major.getUpdatedAt())
                .build();
    }
}
