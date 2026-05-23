package com.oss.osscourse.dto.requirement;

import lombok.Data;

@Data
public class GraduationRequirementQueryRequest {
    private String grCode;
    private Long majorId;
}
