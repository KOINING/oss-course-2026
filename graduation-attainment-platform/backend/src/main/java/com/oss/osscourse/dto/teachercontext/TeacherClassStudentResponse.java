package com.oss.osscourse.dto.teachercontext;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教师端教学班学生名单项")
public class TeacherClassStudentResponse {
    @Schema(description = "学生-教学班关联ID", example = "1")
    private Long scId;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "学号", example = "20240101001")
    private String studentNo;

    @Schema(description = "姓名", example = "张三")
    private String studentName;

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "入学年份", example = "2024")
    private Integer enrollmentYear;

    @Schema(description = "学生状态", example = "1")
    private Integer status;
}
