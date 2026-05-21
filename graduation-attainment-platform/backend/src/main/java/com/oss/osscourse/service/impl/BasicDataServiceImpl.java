package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.dto.basic.*;
import com.oss.osscourse.entity.AcademicTerm;
import com.oss.osscourse.entity.College;
import com.oss.osscourse.mapper.AcademicTermMapper;
import com.oss.osscourse.mapper.CollegeMapper;
import com.oss.osscourse.service.BasicDataService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
public class BasicDataServiceImpl implements BasicDataService {

    private static final Set<String> BASIC_DATA_ROLES = Set.of("admin", "academic_affairs");

    private final CollegeMapper collegeMapper;
    private final AcademicTermMapper academicTermMapper;

    public BasicDataServiceImpl(CollegeMapper collegeMapper, AcademicTermMapper academicTermMapper) {
        this.collegeMapper = collegeMapper;
        this.academicTermMapper = academicTermMapper;
    }

    @Override
    public List<CollegeResponse> listColleges(CollegeQueryRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        CollegeQueryRequest query = request == null ? new CollegeQueryRequest() : request;

        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getCollegeCode())) {
            wrapper.like(College::getCollegeCode, query.getCollegeCode().trim());
        }
        if (StringUtils.hasText(query.getCollegeName())) {
            wrapper.like(College::getCollegeName, query.getCollegeName().trim());
        }
        wrapper.orderByAsc(College::getCollegeCode);

        return collegeMapper.selectList(wrapper).stream()
                .map(this::toCollegeResponse)
                .toList();
    }

    @Override
    public void addCollege(CollegeSaveRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        assertCollegeCodeUnique(request.getCollegeCode(), null);

        College college = new College();
        college.setCollegeCode(request.getCollegeCode().trim());
        college.setCollegeName(request.getCollegeName().trim());
        collegeMapper.insert(college);
    }

    @Override
    public void updateCollege(CollegeSaveRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        if (request.getCollegeId() == null) {
            throw new BusinessException(400, "学院ID不能为空");
        }

        College existing = collegeMapper.selectById(request.getCollegeId());
        if (existing == null) {
            throw new BusinessException(404, "学院不存在");
        }

        assertCollegeCodeUnique(request.getCollegeCode(), request.getCollegeId());

        existing.setCollegeCode(request.getCollegeCode().trim());
        existing.setCollegeName(request.getCollegeName().trim());
        collegeMapper.updateById(existing);
    }

    @Override
    public void deleteCollege(CollegeDeleteRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        College existing = collegeMapper.selectById(request.getCollegeId());
        if (existing == null) {
            throw new BusinessException(404, "学院不存在");
        }

        try {
            collegeMapper.deleteById(request.getCollegeId());
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(400, "该学院已被专业或其他数据引用，无法删除");
        }
    }

    @Override
    public List<AcademicTermResponse> listAcademicTerms(AcademicTermQueryRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        AcademicTermQueryRequest query = request == null ? new AcademicTermQueryRequest() : request;

        LambdaQueryWrapper<AcademicTerm> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getTermCode())) {
            wrapper.like(AcademicTerm::getTermCode, query.getTermCode().trim());
        }
        if (query.getAcademicYear() != null) {
            wrapper.eq(AcademicTerm::getAcademicYear, query.getAcademicYear());
        }
        if (query.getSemester() != null) {
            wrapper.eq(AcademicTerm::getSemester, query.getSemester());
        }
        wrapper.orderByDesc(AcademicTerm::getAcademicYear)
                .orderByDesc(AcademicTerm::getSemester);

        return academicTermMapper.selectList(wrapper).stream()
                .map(this::toAcademicTermResponse)
                .toList();
    }

    @Override
    public void addAcademicTerm(AcademicTermSaveRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        validateTermDates(request);
        assertTermCodeUnique(request.getTermCode(), null);

        AcademicTerm term = new AcademicTerm();
        term.setTermCode(request.getTermCode().trim());
        term.setAcademicYear(request.getAcademicYear());
        term.setSemester(request.getSemester());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        academicTermMapper.insert(term);
    }

    @Override
    public void updateAcademicTerm(AcademicTermSaveRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        if (request.getTermId() == null) {
            throw new BusinessException(400, "学期ID不能为空");
        }

        AcademicTerm existing = academicTermMapper.selectById(request.getTermId());
        if (existing == null) {
            throw new BusinessException(404, "学年学期不存在");
        }

        validateTermDates(request);
        assertTermCodeUnique(request.getTermCode(), request.getTermId());

        existing.setTermCode(request.getTermCode().trim());
        existing.setAcademicYear(request.getAcademicYear());
        existing.setSemester(request.getSemester());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        academicTermMapper.updateById(existing);
    }

    @Override
    public void deleteAcademicTerm(AcademicTermDeleteRequest request, List<String> roles) {
        assertBasicDataRole(roles);
        AcademicTerm existing = academicTermMapper.selectById(request.getTermId());
        if (existing == null) {
            throw new BusinessException(404, "学年学期不存在");
        }

        try {
            academicTermMapper.deleteById(request.getTermId());
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(400, "该学年学期已被教学班或其他数据引用，无法删除");
        }
    }

    private void assertBasicDataRole(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(BASIC_DATA_ROLES::contains)) {
            throw new BusinessException(403, "无权限操作基础数据");
        }
    }

    private void assertCollegeCodeUnique(String collegeCode, Long excludeId) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<College>()
                .eq(College::getCollegeCode, collegeCode.trim());
        if (excludeId != null) {
            wrapper.ne(College::getCollegeId, excludeId);
        }
        if (collegeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "学院编码已存在");
        }
    }

    private void assertTermCodeUnique(String termCode, Long excludeId) {
        LambdaQueryWrapper<AcademicTerm> wrapper = new LambdaQueryWrapper<AcademicTerm>()
                .eq(AcademicTerm::getTermCode, termCode.trim());
        if (excludeId != null) {
            wrapper.ne(AcademicTerm::getTermId, excludeId);
        }
        if (academicTermMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "学期编码已存在");
        }
    }

    private void validateTermDates(AcademicTermSaveRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
    }

    private CollegeResponse toCollegeResponse(College college) {
        return CollegeResponse.builder()
                .collegeId(college.getCollegeId())
                .collegeCode(college.getCollegeCode())
                .collegeName(college.getCollegeName())
                .createdAt(college.getCreatedAt())
                .updatedAt(college.getUpdatedAt())
                .build();
    }

    private AcademicTermResponse toAcademicTermResponse(AcademicTerm term) {
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
