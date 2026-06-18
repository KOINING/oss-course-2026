package com.oss.osscourse.service;

import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.teachingclass.*;

import java.util.List;

public interface TeachingClassService {

    /**
     * 查询教学班列表
     * @param request 查询条件
     * @return 教学班列表
     */
    List<TeachingClassResponse> listTeachingClasses(TeachingClassQueryRequest request);

    /**
     * 查询教学班下拉列表（供选择使用）
     * @return 教学班列表
     */
    List<TeachingClassResponse> listTeachingClassesForSelect();

    /**
     * 根据ID查询教学班详情
     * @param classId 教学班ID
     * @return 教学班详情
     */
    TeachingClassResponse getTeachingClassById(Long classId);

    /**
     * 新增或更新教学班
     * @param request 保存请求
     */
    void saveTeachingClass(TeachingClassSaveRequest request);

    /**
     * 更新教学班计算状态
     * @param request 状态更新请求
     */
    void updateTeachingClassStatus(TeachingClassStatusRequest request);

    /**
     * 分页查询教学班列表
     * @param request 查询条件（含分页参数）
     * @return 分页结果
     */
    PageResult<TeachingClassResponse> listTeachingClassesByPage(TeachingClassQueryRequest request);

    /**
     * 删除教学班
     * @param classId 教学班ID
     */
    void deleteTeachingClass(Long classId);
}
