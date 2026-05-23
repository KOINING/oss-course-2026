package com.oss.osscourse.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "专业视图对象")
public class MajorVO {
    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "所属学院ID")
    private Long collegeId;

    @Schema(description = "所属学院名称")
    private String collegeName;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
