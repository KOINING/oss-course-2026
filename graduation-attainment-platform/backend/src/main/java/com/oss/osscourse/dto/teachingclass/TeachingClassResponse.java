package com.oss.osscourse.dto.teachingclass;

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
@Schema(description = "教学班响应对象")
public class TeachingClassResponse {

    @Schema(description = "教学班ID", example = "1")
    private Long classId;

    @Schema(description = "班级名称", example = "数据结构2024-2025-1班")
    private String className;

    @Schema(description = "所属课程ID", example = "1")
    private Long courseId;

    @Schema(description = "所属课程名称", example = "数据结构")
    private String courseName;

    @Schema(description = "所属课程编码", example = "CS201")
    private String courseCode;

    @Schema(description = "所属学期ID", example = "1")
    private Long termId;

    @Schema(description = "所属学期编码", example = "2024-2025-1")
    private String termCode;

    @Schema(description = "主讲教师ID", example = "1")
    private Long teacherId;

    @Schema(description = "主讲教师姓名", example = "张教授")
    private String teacherName;

    @Schema(description = "计算状态", example = "unsubmitted")
    private String calcStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
