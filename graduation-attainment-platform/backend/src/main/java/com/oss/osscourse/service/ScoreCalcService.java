package com.oss.osscourse.service;

import com.oss.osscourse.dto.achievement.*;
import com.oss.osscourse.dto.score.*;

public interface ScoreCalcService {

    /**
     * 生成成绩模板预览
     * @param classId 教学班ID
     * @return 模板预览数据
     */
    ScoreTemplatePreviewResponse previewTemplate(Long classId);

    /**
     * 下载成绩模板Excel
     * @param classId 教学班ID
     * @return Excel文件字节数组
     */
    byte[] downloadTemplate(Long classId);

    /**
     * 导入成绩预校验
     * @param request 导入请求
     * @return 预览结果
     */
    ScoreImportPreviewResponse importScorePreview(ScoreImportRequest request);

    /**
     * 保存成绩
     * @param request 保存请求
     */
    void saveScores(ScoreSaveRequest request);

    /**
     * 课程级达成度计算
     * @param request 计算请求
     * @return 计算结果
     */
    CourseCalcResponse calcCourseAchievement(CourseCalcRequest request);

    /**
     * 专业级达成度汇总
     * @param request 汇总请求
     * @return 汇总结果
     */
    MajorCalcResponse calcMajorAchievement(MajorCalcRequest request);

    /**
     * 查询课程计算状态汇总
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 状态汇总
     */
    CourseCalcStatusResponse getCourseCalcStatus(Long majorId, Long termId);
}
