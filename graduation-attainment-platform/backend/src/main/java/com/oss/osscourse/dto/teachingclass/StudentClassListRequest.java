package com.oss.osscourse.dto.teachingclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学生-教学班关联查询请求")
public class StudentClassListRequest {

    @Schema(description = "教学班ID，按教学班查询学生时传入", example = "1")
    private Long teachingClassId;

    @Schema(description = "学生ID，按学生查询教学班时传入", example = "1")
    private Long studentId;
}
