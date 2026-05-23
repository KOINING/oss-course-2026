package com.oss.osscourse.dto.requirement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndicatorPointResponse {
    private Long ipId;
    private String ipCode;
    private String ipDescription;
    private Long grId;
    private String grCode;
    private String grDescription;
}
