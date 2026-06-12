package com.oss.osscourse.controller;

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
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/teacher/report")
@RequiredArgsConstructor
@Tag(name = "课程级评价报表", description = "课程级评价报表数据查询和导出接口")
public class CourseReportController {

    private final CourseReportService courseReportService;

    @PostMapping("/data")
    @Operation(summary = "查询课程级评价报表数据", description = "根据课程ID和年级查询课程级评价报表数据")
    public Result<CourseReportResponse> getCourseReport(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request) {
        CourseReportResponse response = courseReportService.getCourseReport(request);
        return Result.ok(response);
    }

    @PostMapping("/export/excel")
    @Operation(summary = "导出课程级评价报表Excel", description = "根据课程ID和年级导出Excel格式的课程级评价报表")
    public ResponseEntity<byte[]> exportExcel(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request) {
        byte[] excelBytes = courseReportService.exportCourseReportExcel(request);

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
    @Operation(summary = "导出课程级评价报表PDF", description = "根据课程ID和年级导出PDF格式的课程级评价报表")
    public ResponseEntity<byte[]> exportPdf(
            @Parameter(description = "报表查询请求", required = true)
            @Valid @RequestBody CourseReportRequest request) {
        byte[] pdfBytes = courseReportService.exportCourseReportPdf(request);

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
}
