package com.oss.osscourse.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程响应对象")
public class CourseResponse {

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程代码", example = "CS201")
    private String courseCode;

    @Schema(description = "课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "学分", example = "4.0")
    private Float credit;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "所属专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "状态：1=开课中，0=停开", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
