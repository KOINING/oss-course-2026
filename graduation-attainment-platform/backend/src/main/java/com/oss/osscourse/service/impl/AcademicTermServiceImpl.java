package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.academicterm.*;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.service.AcademicTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicTermServiceImpl implements AcademicTermService {

    private final AcademicTermMapper academicTermMapper;

    @Override
    public List<AcademicTermResponse> listAcademicTerms(AcademicTermQueryRequest request) {
        LambdaQueryWrapper<AcademicTerm> wrapper = new LambdaQueryWrapper<>();

        if (request != null) {
            if (request.getTermCode() != null && !request.getTermCode().trim().isEmpty()) {
                wrapper.like(AcademicTerm::getTermCode, request.getTermCode().trim());
            }
            if (request.getAcademicYear() != null) {
                wrapper.eq(AcademicTerm::getAcademicYear, request.getAcademicYear());
            }
            if (request.getSemester() != null) {
                wrapper.eq(AcademicTerm::getSemester, request.getSemester());
            }
        }

        wrapper.orderByDesc(AcademicTerm::getAcademicYear)
               .orderByAsc(AcademicTerm::getSemester);

        List<AcademicTerm> terms = academicTermMapper.selectList(wrapper);
        return terms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AcademicTermResponse getAcademicTermById(Long termId) {
        if (termId == null) {
            throw new BusinessException(400, "学期ID不能为空");
        }

        AcademicTerm term = academicTermMapper.selectById(termId);
        if (term == null) {
            throw new BusinessException(404, "学年学期不存在");
        }

        return toResponse(term);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAcademicTerm(AcademicTermCreateRequest request) {
        // 检查学期编码是否已存在
        if (academicTermMapper.selectOne(new LambdaQueryWrapper<AcademicTerm>()
                .eq(AcademicTerm::getTermCode, request.getTermCode())) != null) {
            throw new BusinessException(400, "学期编码已存在");
        }

        // 验证学期序号
        if (request.getSemester() != 1 && request.getSemester() != 2) {
            throw new BusinessException(400, "学期序号必须为1或2");
        }

        // 验证日期
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        AcademicTerm term = new AcademicTerm();
        term.setTermCode(request.getTermCode());
        term.setAcademicYear(request.getAcademicYear());
        term.setSemester(request.getSemester());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());

        academicTermMapper.insert(term);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAcademicTerm(AcademicTermUpdateRequest request) {
        if (request.getTermId() == null) {
            throw new BusinessException(400, "学期ID不能为空");
        }

        AcademicTerm term = academicTermMapper.selectById(request.getTermId());
        if (term == null) {
            throw new BusinessException(404, "学年学期不存在");
        }

        // 检查学期编码是否已存在（排除自身）
        if (request.getTermCode() != null && !request.getTermCode().isEmpty()) {
            AcademicTerm existing = academicTermMapper.selectOne(new LambdaQueryWrapper<AcademicTerm>()
                    .eq(AcademicTerm::getTermCode, request.getTermCode())
                    .ne(AcademicTerm::getTermId, request.getTermId()));
            if (existing != null) {
                throw new BusinessException(400, "学期编码已存在");
            }
            term.setTermCode(request.getTermCode());
        }

        if (request.getAcademicYear() != null) {
            term.setAcademicYear(request.getAcademicYear());
        }

        if (request.getSemester() != null) {
            if (request.getSemester() != 1 && request.getSemester() != 2) {
                throw new BusinessException(400, "学期序号必须为1或2");
            }
            term.setSemester(request.getSemester());
        }

        if (request.getStartDate() != null) {
            term.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            term.setEndDate(request.getEndDate());
        }

        // 验证日期
        if (term.getStartDate().isAfter(term.getEndDate())) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        academicTermMapper.updateById(term);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAcademicTerm(Long termId) {
        if (termId == null) {
            throw new BusinessException(400, "学期ID不能为空");
        }

        AcademicTerm term = academicTermMapper.selectById(termId);
        if (term == null) {
            throw new BusinessException(404, "学年学期不存在");
        }

        // 检查是否有关联的教学班级
        // TODO: 检查TeachingClass表是否有引用

        academicTermMapper.deleteById(termId);
    }

    private AcademicTermResponse toResponse(AcademicTerm term) {
        return AcademicTermResponse.builder()
                .termId(term.getTermId())
                .termCode(term.getTermCode())
                .academicYear(term.getAcademicYear())
                .semester(term.getSemester())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .createdAt(term.getCreatedAt())
                .updatedAt(term.getUpdatedAt())
                .build();
    }
}
