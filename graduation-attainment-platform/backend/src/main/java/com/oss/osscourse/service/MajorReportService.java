package com.oss.osscourse.service;

import com.oss.osscourse.dto.report.MajorReportRequest;
import com.oss.osscourse.dto.report.MajorReportResponse;

public interface MajorReportService {

    /**
     * 装配专业级评价报告数据。
     * 以专业+年级组织报告主体，读取 major_indicator_achievement 作为结果源，
     * 同时关联支撑课程与课程级结果，使图表、表格、导出共用一个数据源。
     *
     * @param request 查询请求（majorId + gradeYear，可选 termId）
     * @return 专业级报告完整数据
     */
    MajorReportResponse assembleMajorReport(MajorReportRequest request);
}
