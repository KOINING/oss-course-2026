package com.oss.osscourse.dto.teachercontext;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教师端教学班查询请求")
public class TeacherTeachingClassQueryRequest {
    @Schema(description = "课程ID，精确筛选", example = "1")
    private Long courseId;

    @Schema(description = "学期ID，精确筛选", example = "1")
    private Long termId;

    @Schema(description = "培养方案年级，精确筛选", example = "2024")
    private Integer gradeYear;

    @Schema(description = "教学班编号，模糊筛选", example = "TC2024")
    private String classCode;

    @Schema(description = "计算状态，精确筛选", example = "unsubmitted")
    private String calcStatus;
}
