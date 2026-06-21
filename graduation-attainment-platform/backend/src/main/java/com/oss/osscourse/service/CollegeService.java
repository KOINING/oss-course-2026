package com.oss.osscourse.service;

import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.college.*;

import java.util.List;

public interface CollegeService {

    /**
     * 查询学院列表
     * @param request 查询条件
     * @return 学院列表
     */
    List<CollegeResponse> listColleges(CollegeQueryRequest request);

    PageResult<CollegeResponse> listCollegesByPage(CollegeQueryRequest request);

    /**
     * 根据ID查询学院
     * @param collegeId 学院ID
     * @return 学院详情
     */
    CollegeResponse getCollegeById(Long collegeId);

    /**
     * 新增学院
     * @param request 新增请求
     */
    void createCollege(CollegeCreateRequest request);

    /**
     * 更新学院
     * @param request 更新请求
     */
    void updateCollege(CollegeUpdateRequest request);

    /**
     * 删除学院
     * @param collegeId 学院ID
     */
    void deleteCollege(Long collegeId);
}
