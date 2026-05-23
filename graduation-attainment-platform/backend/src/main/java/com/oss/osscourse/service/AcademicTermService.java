package com.oss.osscourse.service;

import com.oss.osscourse.dto.academicterm.*;

import java.util.List;

public interface AcademicTermService {

    /**
     * 查询学年学期列表
     * @param request 查询条件
     * @return 学年学期列表
     */
    List<AcademicTermResponse> listAcademicTerms(AcademicTermQueryRequest request);

    /**
     * 根据ID查询学年学期
     * @param termId 学期ID
     * @return 学年学期详情
     */
    AcademicTermResponse getAcademicTermById(Long termId);

    /**
     * 新增学年学期
     * @param request 新增请求
     */
    void createAcademicTerm(AcademicTermCreateRequest request);

    /**
     * 更新学年学期
     * @param request 更新请求
     */
    void updateAcademicTerm(AcademicTermUpdateRequest request);

    /**
     * 删除学年学期
     * @param termId 学期ID
     */
    void deleteAcademicTerm(Long termId);
}
