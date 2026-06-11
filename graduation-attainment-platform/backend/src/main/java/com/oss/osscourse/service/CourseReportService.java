package com.oss.osscourse.service;

import com.oss.osscourse.dto.report.CourseReportRequest;
import com.oss.osscourse.dto.report.CourseReportResponse;

public interface CourseReportService {

    /**
     * 查询课程级评价报表数据
     * @param request 查询请求
     * @return 报表数据
     */
    CourseReportResponse getCourseReport(CourseReportRequest request);

    /**
     * 导出课程级评价报表为Excel
     * @param request 查询请求
     * @return Excel文件字节数组
     */
    byte[] exportCourseReportExcel(CourseReportRequest request);

    /**
     * 导出课程级评价报表为PDF
     * @param request 查询请求
     * @return PDF文件字节数组
     */
    byte[] exportCourseReportPdf(CourseReportRequest request);
}
