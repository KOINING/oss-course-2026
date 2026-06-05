package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程响应对象")
public class CourseResponse {

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程编码", example = "CS201")
    private String courseCode;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "学分", example = "4.0")
    private Float credit;

    @Schema(description = "所属专业ID列表", example = "[1,2]")
    private List<Long> majorIds;

    @Schema(description = "所属专业名称列表", example = "[\"计算机科学与技术\",\"软件工程\"]")
    private List<String> majorNames;

    @Schema(description = "课程适用的专业-年级绑定关系")
    private List<CourseMajorGradeYearBindingResponse> majorGradeYearBindings;

    @Schema(description = "状态：1=启用，0=停用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
