package com.oss.osscourse.dto.supportmatrix;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "课程-毕业要求指标点支撑关系响应")
public class CourseIndicatorSupportResponse {
    @Schema(description = "关系ID", example = "1")
    private Long cisId;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程编码", example = "CS101")
    private String courseCode;

    @Schema(description = "课程名称", example = "程序设计基础")
    private String courseName;

    @Schema(description = "课程状态：1=启用，0=停用", example = "1")
    private Integer courseStatus;

    @Schema(description = "指标点ID", example = "1")
    private Long ipId;

    @Schema(description = "指标点编号", example = "IP01-1")
    private String ipCode;

    @Schema(description = "指标点描述")
    private String ipDescription;

    @Schema(description = "指标点状态：1=启用，0=停用", example = "1")
    private Integer ipStatus;

    @Schema(description = "毕业要求ID", example = "1")
    private Long grId;

    @Schema(description = "毕业要求编号", example = "GR01")
    private String grCode;

    @Schema(description = "毕业要求描述")
    private String grDescription;

    @Schema(description = "所属专业ID", example = "1")
    private Long majorId;

    @Schema(description = "总支撑权重", example = "0.40")
    private Float totalWeight;
}
