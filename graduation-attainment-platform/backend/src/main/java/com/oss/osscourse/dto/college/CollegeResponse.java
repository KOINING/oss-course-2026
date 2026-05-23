package com.oss.osscourse.dto.college;

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
@Schema(description = "学院响应对象")
public class CollegeResponse {

    @Schema(description = "学院ID", example = "1")
    private Long collegeId;

    @Schema(description = "学院编码", example = "001")
    private String collegeCode;

    @Schema(description = "学院名称", example = "计算机科学与技术学院")
    private String collegeName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
