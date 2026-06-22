package com.oss.osscourse.controller;

import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.report.CourseReportRequest;
import com.oss.osscourse.dto.report.CourseReportResponse;
import com.oss.osscourse.service.CourseReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/report")
@RequiredArgsConstructor
@Tag(name = "课程级评价报表", description = "课程级评价报表数据查询和导出接口")
public class CourseReportController {

    private static final List<String> COURSE_REPORT_ROLES = List.of("instructor", "program_director", "academic_affairs");

    private final CourseReportService courseReportService;

    @PostMapping("/data")
    @Operation(summary = "查询课程级评价报表数据", description = "根据课程ID和年级查询课程级评价报表数据")
    public Result<CourseReportResponse> getCourseReport(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        ensureCourseReportAccess(roles);
        return Result.ok(courseReportService.getCourseReport(request, userId, roles));
    }

    @PostMapping("/export/excel")
    @Operation(summary = "导出课程级评价报表 Excel", description = "根据课程ID和年级导出 Excel 格式的课程级评价报表")
    public ResponseEntity<byte[]> exportExcel(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        ensureCourseReportAccess(roles);
        byte[] excelBytes = courseReportService.exportCourseReportExcel(request, userId, roles);

        String fileName = "课程级评价报表_" + request.getCourseId() + "_" + request.getGradeYear() + ".xlsx";
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            encodedFileName = "course_report.xlsx";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(encodedFileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @PostMapping("/export/pdf")
    @Operation(summary = "导出课程级评价报表 PDF", description = "根据课程ID和年级导出 PDF 格式的课程级评价报表")
    public ResponseEntity<byte[]> exportPdf(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roles") List<String> roles) {
        ensureCourseReportAccess(roles);
        byte[] pdfBytes = courseReportService.exportCourseReportPdf(request, userId, roles);

        String fileName = "课程级评价报表_" + request.getCourseId() + "_" + request.getGradeYear() + ".pdf";
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            encodedFileName = "course_report.pdf";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(encodedFileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private void ensureCourseReportAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(COURSE_REPORT_ROLES::contains)) {
            throw new BusinessException(403, "当前账号无权查看课程级评价报表");
        }
    }
}
