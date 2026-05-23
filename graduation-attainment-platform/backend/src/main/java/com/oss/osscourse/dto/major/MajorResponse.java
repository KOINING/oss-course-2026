package com.oss.osscourse.dto.major;

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
@Schema(description = "专业响应对象")
public class MajorResponse {

    @Schema(description = "专业ID", example = "1")
    private Long majorId;

    @Schema(description = "专业代码", example = "080901")
    private String majorCode;

    @Schema(description = "专业名称", example = "计算机科学与技术")
    private String majorName;

    @Schema(description = "所属学院ID", example = "1")
    private Long collegeId;

    @Schema(description = "所属学院名称", example = "计算机科学与技术学院")
    private String collegeName;

    @Schema(description = "状态：1=招生中，0=停招", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
