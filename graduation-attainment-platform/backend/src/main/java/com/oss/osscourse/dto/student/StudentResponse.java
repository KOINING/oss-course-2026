package com.oss.osscourse.dto.student;

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
@Schema(description = "学生响应对象")
public class StudentResponse {

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "学号", example = "20220101001")
    private String studentNo;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "所属专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "所属专业编码", example = "080901")
    private String majorCode;

    @Schema(description = "入学年份", example = "2022")
    private Integer enrollmentYear;

    @Schema(description = "关联用户ID", example = "1")
    private Long userId;

    @Schema(description = "状态：1=在读，2=毕业，3=休学，0=退学", example = "1")
    private Integer status;

    @Schema(description = "状态文本", example = "在读")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
