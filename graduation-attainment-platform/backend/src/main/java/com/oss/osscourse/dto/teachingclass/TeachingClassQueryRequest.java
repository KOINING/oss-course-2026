package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教学班查询请求")
public class TeachingClassQueryRequest {

    @Schema(description = "班级名称，模糊查询")
    private String className;

    @Schema(description = "所属课程ID")
    private Long courseId;

    @Schema(description = "所属学期ID")
    private Long termId;

    @Schema(description = "主讲教师ID")
    private Long teacherId;

    @Schema(description = "计算状态：unsubmitted/score_imported/calculating/locked")
    private String calcStatus;
}
