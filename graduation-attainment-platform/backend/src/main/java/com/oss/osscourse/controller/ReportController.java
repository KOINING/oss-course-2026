package com.oss.osscourse.controller;

import com.oss.osscourse.common.Result;
import com.oss.osscourse.dto.report.MajorReportRequest;
import com.oss.osscourse.dto.report.MajorReportResponse;
import com.oss.osscourse.service.MajorReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "报表数据服务", description = "课程级与专业级报表的数据装配接口，图表、表格、导出共用一个结果源")
public class ReportController {

    private static final List<String> MAJOR_REPORT_ROLES = List.of("program_director", "academic_affairs");

    private final MajorReportService majorReportService;

    @PostMapping("/majorReport")
    @Operation(
            summary = "专业级评价报告数据装配",
            description = "以专业+年级组织报告主体，读取 major_indicator_achievement 作为统一结果源，"
                    + "关联支撑课程与课程级达成度，产出图表、表格、导出可共用的报告数据。"
                    + "角色：专业负责人、教务管理员。"
    )
    public Result<MajorReportResponse> getMajorReport(
            @Valid @RequestBody MajorReportRequest request,
            @RequestAttribute("roles") List<String> roles) {
        ensureMajorReportAccess(roles);
        return Result.ok(majorReportService.assembleMajorReport(request));
    }

    private void ensureMajorReportAccess(List<String> roles) {
        if (roles == null || roles.stream().noneMatch(MAJOR_REPORT_ROLES::contains)) {
            throw new com.oss.osscourse.common.BusinessException(403, "当前账号无权查看专业级评价报告");
        }
    }
}
